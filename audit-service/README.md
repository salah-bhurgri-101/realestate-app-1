# Audit Log Service

A standalone Spring Boot microservice that centralizes audit logging for
other microservices in your system. Any service can report an event by:

1. Calling the REST API directly, or
2. Using the `@Auditable` annotation + `AuditAspect` if you copy the
   `annotation`/`aspect` package into a shared client library, or
3. Embedding this module directly if you prefer audit logging to live
   in-process rather than as a separate network call.

## Stack
- Java 17, Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring AOP, Validation, Actuator
- H2 (dev) / PostgreSQL (prod, via `prod` profile)
- springdoc-openapi (Swagger UI at `/swagger-ui.html`)

## Project layout
```
src/main/java/com/example/auditservice/
  entity/        AuditLog, AuditAction
  repository/    AuditLogRepository (search/filter queries)
  dto/           AuditLogRequest, AuditLogResponse
  service/       AuditLogService + impl (sync + async writes)
  controller/    AuditLogController (REST API)
  annotation/    @Auditable
  aspect/        AuditAspect (auto-logs annotated methods)
  config/        AsyncConfig (dedicated thread pool for audit writes)
  exception/     AuditLogNotFoundException, GlobalExceptionHandler
```

## Running it
```bash
mvn spring-boot:run
# Swagger UI: http://localhost:8084/swagger-ui.html
# H2 console: http://localhost:8084/h2-console
```

For production, run with `--spring.profiles.active=prod` and set
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`.

## Security model

`audit-service` validates JWTs issued by the Auth service **independently**
— no OIDC discovery call, no shared session state. Each request's token
is verified locally against either an HMAC shared secret or the Auth
service's RSA public key.

| Endpoint | Required role |
|---|---|
| `GET /api/v1/audit-logs`, `GET /api/v1/audit-logs/{id}` | `ADMIN` |
| `POST /api/v1/audit-logs`, `POST /api/v1/audit-logs/async` | `ADMIN` or `SERVICE` |
| `/actuator/health`, `/swagger-ui/**` | public |
| everything else | any authenticated token |

Enforced twice — once in `SecurityConfig`'s filter chain, once via
`@PreAuthorize` on each controller method — so a future change to one
doesn't silently widen access.

### Configuring verification
```yaml
audit:
  security:
    jwt:
      algorithm: HS256                 # or RS256
      secret: ${AUTH_JWT_SECRET}       # HS256/384/512 — must match the Auth service's signing secret
      # public-key-location: classpath:keys/auth-public-key.pem   # RS256 instead of `secret`
      role-claim: role                 # claim name; accepts "ADMIN", "ADMIN,AGENT", or ["ADMIN","AGENT"]
      expected-issuer: ""              # set to enforce the `iss` claim
```
`AUTH_JWT_SECRET` has no default in the `prod` profile — the app fails
fast on startup rather than ever running with a guessable secret.

### Service-to-service auth
The REST ingestion endpoints (`POST /api/v1/audit-logs*`) require a
`SERVICE`-roled token, not an end-user `ADMIN` token. Issue a long-lived
service-account token per microservice from the Auth service and
configure it on that service's `audit-starter` dependency via
`audit.client.service-token` (see audit-starter's README). This only
applies to the WEBCLIENT transport — the Kafka transport bypasses HTTP
entirely, so its access control is a broker-level concern (Kafka ACLs /
network policy), not covered by this JWT layer.

### Testing it
`src/test/java/.../security/JwtAuthorizationIntegrationTest.java` hand-signs
HS256 tokens with the configured dev secret and exercises all four rules
above (no token → 401, wrong role → 403, `ADMIN` → 200, `SERVICE` → 201)
against a real embedded server. Run it to see the policy enforced, or use
it as a template for hand-testing against a real Auth-service-issued
token locally.

## REST API

### Record an event (blocking)
```http
POST /api/v1/audit-logs
Content-Type: application/json
Authorization: Bearer <SERVICE-or-ADMIN-roled JWT>

{
  "serviceName": "order-service",
  "action": "UPDATE",
  "entityType": "Order",
  "entityId": "ORD-1001",
  "username": "jane.doe",
  "ipAddress": "10.0.0.5",
  "description": "Order status changed to SHIPPED",
  "metadata": { "previousStatus": "PACKED", "newStatus": "SHIPPED" },
  "status": "SUCCESS",
  "traceId": "a1b2c3"
}
```

### Record an event (non-blocking — recommended from hot paths)
```http
POST /api/v1/audit-logs/async
Authorization: Bearer <SERVICE-or-ADMIN-roled JWT>
```
Returns `202 Accepted` immediately; persistence happens on a dedicated
thread pool so it never adds latency to the caller's request.

### Fetch one record
```http
GET /api/v1/audit-logs/{id}
Authorization: Bearer <ADMIN-roled JWT>
```

### Search / filter (paginated)
```http
GET /api/v1/audit-logs?serviceName=order-service&action=UPDATE&from=2026-06-01T00:00:00Z&page=0&size=20
Authorization: Bearer <ADMIN-roled JWT>
```

## Calling it from another microservice
Simplest approach — a thin `RestTemplate`/`WebClient` call from a shared
filter or interceptor:

```java
AuditLogRequest event = new AuditLogRequest();
event.setServiceName("payment-service");
event.setAction(AuditAction.CREATE);
event.setEntityType("Payment");
event.setEntityId(paymentId);
event.setUsername(currentUser);
event.setDescription("Payment captured");

webClient.post()
    .uri("http://audit-service/api/v1/audit-logs/async")
    .bodyValue(event)
    .retrieve()
    .toBodilessEntity()
    .subscribe(); // fire-and-forget
```

## Declarative logging with `@Auditable`
If you copy the `annotation` and `aspect` packages into the calling
service (or a shared internal library), you can skip manual logging
calls entirely:

```java
@Auditable(action = AuditAction.UPDATE, entityType = "Order", entityIdExpression = "#orderId")
public Order updateOrder(String orderId, OrderRequest req) {
    // business logic only — audit entry is written automatically,
    // including SUCCESS/FAILURE status based on whether this throws.
}
```
In that scenario, point `AuditLogService` at a `WebClient`/`RestTemplate`
implementation instead of the local repository, so the annotated service
calls this audit microservice over the network rather than writing to its
own database.

## Design notes
- `entityId` is stored as `String` so the schema stays entity-agnostic —
  no foreign keys into other services' databases.
- `metadata` is stored as JSON text for flexibility; switch the column to
  a native `jsonb` type if you're on Postgres and want to query inside it.
- Async writes swallow exceptions (logged, not thrown) so a logging
  failure can never break the business flow that triggered it.
- Add Spring Security and replace the placeholder `X-User-Name` header
  read in `AuditAspect.resolveUsername()` with the authenticated
  principal if you need stronger identity guarantees.
- Consider adding Kafka/RabbitMQ instead of REST if you want true
  decoupling and back-pressure handling at very high audit volumes — the
  `AuditLogService` interface stays the same either way.
