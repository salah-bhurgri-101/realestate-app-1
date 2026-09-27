# audit-starter

A drop-in WebClient-based client for reporting events to the
`audit-service` from any other microservice. Auto-configures itself
via Spring Boot's auto-configuration mechanism — add the dependency, set
one property, and you're done.

## Install

Build and install to your local/internal Maven repo:
```bash
mvn install
```

Then add to the consuming microservice's `pom.xml`:
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>audit-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configure

Add to the consuming service's `application.yml` (see
`docs/application-example.yml`):
```yaml
audit:
  client:
    transport: webclient   # or "kafka" — see below
    base-url: http://audit-service:8084
    enabled: true
    timeout: 2s
    max-retries: 2
    retry-backoff: 200ms
    fail-silently: true
```

That's it — `AuditClient` is now available for `@Autowired`/constructor
injection anywhere in the app. **Calling code never changes** regardless
of which transport you pick below.

## Choosing a transport

| | `webclient` (default) | `kafka` |
|---|---|---|
| Coupling | Synchronous HTTP call | True async — publish-and-forget |
| If audit-service is down | Retries, then drops (if `fail-silently: true`) | Events queue in the topic, processed once it's back |
| Ordering | N/A (each call independent) | Guaranteed per entity (same partition key) |
| Extra infra | None | Requires a Kafka cluster |
| Good for | Simple setups, low/medium volume | High volume, services that can't tolerate audit-service downtime affecting them at all |

### Using Kafka transport
1. Add `spring-kafka` to the consuming service's own `pom.xml` (it's an
   *optional* dependency in audit-starter, so it only activates if you
   bring it yourself):
   ```xml
   <dependency>
       <groupId>org.springframework.kafka</groupId>
       <artifactId>spring-kafka</artifactId>
   </dependency>
   ```
2. Set:
   ```yaml
   spring:
     kafka:
       bootstrap-servers: localhost:9092
   audit:
     client:
       transport: kafka
       kafka:
         topic: audit-events   # must match audit-service's audit.kafka.topic
   ```
3. Done. `AuditKafkaAutoConfiguration` activates automatically (it's
   gated on `spring-kafka` being present + `transport: kafka`), builds a
   dedicated `KafkaTemplate` (bean name `auditKafkaTemplate`) with its
   own JSON serializer config so it won't collide with any Kafka
   producers the host app already has, and registers that as the
   `AuditClient` implementation.

Events are keyed by `serviceName:entityType:entityId` so all events for
the same entity land on the same partition and are consumed in order.
The audit-service side of this (the `@KafkaListener`, dead-letter
topic, retry policy) is documented in its own README.

## Usage

### Fire-and-forget (recommended for most call sites)
```java
@RequiredArgsConstructor
@Service
public class OrderService {
    private final AuditClient auditClient;

    public void shipOrder(String orderId) {
        // ... business logic ...
        auditClient.log(AuditEvent.builder()
                .action(AuditAction.UPDATE)
                .entityType("Order")
                .entityId(orderId)
                .description("Order shipped")
                .build());
    }
}
```
`log()` returns immediately. The HTTP call to audit-service runs in
the background and never blocks or throws back into your business logic
(by default — see `fail-silently` below).

### Blocking
```java
auditClient.logBlocking(event); // waits for the call to complete or time out
```
Use only when you have a hard requirement that the audit record exists
before continuing (e.g. before returning a compliance-sensitive response).

### Reactive (WebFlux services)
```java
return auditClient.logReactive(event).then(Mono.just(result));
```

### Declarative — no manual logging code at all
```java
@Auditable(action = AuditAction.UPDATE, entityType = "Order", entityIdExpression = "#orderId")
public Order updateOrder(String orderId, OrderRequest req) {
    // business logic only — audit event fires automatically,
    // status is SUCCESS or FAILURE based on whether this method throws
}
```
`entityIdExpression` is SpEL evaluated against the method's parameter
names, so `#orderId` pulls from a parameter literally named `orderId`.

## Resilience behavior
- **Timeout**: configurable per-request (`audit.client.timeout`, default 2s).
- **Retry**: exponential backoff, only on network errors or 5xx responses
  — 4xx (bad request, validation errors) is never retried.
- **Fail-silently** (default `true`): if the audit-service is
  unreachable even after retries, the error is logged at WARN and
  swallowed. Your business transaction is never affected by an audit
  outage. Set to `false` only if audit completeness is itself a hard
  business/compliance requirement for the call site — in that case
  use `logBlocking()` and handle the exception explicitly.

## Disabling for tests
```yaml
audit:
  client:
    enabled: false
```
All `AuditClient` methods become no-ops — useful in unit/integration
tests where you don't want to stand up the audit-service.

## How auto-configuration works
This jar ships a
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
file listing two auto-configuration classes:

- **`AuditWebClientAutoConfiguration`** — active by default (or when
  `audit.client.transport=webclient`). Registers a `WebClient` bean
  named `auditWebClient` and the `AuditClient` bean.
- **`AuditKafkaAutoConfiguration`** — active only when `spring-kafka` is
  on the classpath AND `audit.client.transport=kafka`. Registers a
  dedicated `ProducerFactory`/`KafkaTemplate` (`auditKafkaTemplate`) and
  the `AuditClient` bean.

Both back off (`@ConditionalOnMissingBean`) if the consuming app already
defines its own bean of that type/name, so either can be overridden.
Only one `AuditClient` bean is ever active at a time since the two
configs are mutually exclusive on the `transport` property.

## Files
```
src/main/java/com/example/auditstarter/
  model/             AuditAction, AuditEvent
  config/            AuditClientProperties,
                      AuditWebClientAutoConfiguration,
                      AuditKafkaAutoConfiguration
  impl/              WebClientAuditClient, KafkaAuditClient
  annotation/        @AuditLog
  aspect/            AuditAspect
  examples/          sample usage (not required at runtime)
```

## Troubleshooting: "@AuditLog laga diya par kuch ho hi nahi raha"

If the annotation seems to do nothing, check these in order:

1. **Self-invocation.** Spring AOP proxies only intercept calls that come
   from *outside* the bean. If `methodA()` calls `this.methodB()` inside
   the same class, and `methodB()` is `@AuditLog`-annotated, the aspect
   **will not fire** — the call never goes through the proxy. Fix: call
   the annotated method from a different bean, or restructure into two
   beans.
2. **Method visibility.** The annotated method must be `public`. Private,
   protected, or package-private methods are invisible to the proxy.
3. **Class must be a Spring bean.** Annotating a method on a plain `new
   Foo()` object (not managed by the Spring container) does nothing —
   there's no proxy to intercept it.
4. **`audit.client.annotation-enabled` not accidentally set to `false`.**
   Defaults to `true`; check your `application.yml` if it's off.
5. **Check logs at DEBUG for `com.example.auditstarter`** —
   `AuditAspectAutoConfiguration`'s bean only activates once an
   `AuditClient` bean exists (depends on the transport auto-config
   running first). If that failed silently — e.g. missing
   `audit.client.base-url` — the aspect bean never gets created either.

If you're on an older build of this jar: the root cause used to be that
`AuditAspect` had no `@Component` and lived outside every consuming
service's component-scan base package, so the aspect bean was never
created at all. Registration is now explicit via
`AuditAspectAutoConfiguration` — no component-scan reliance, no silent
no-op.
