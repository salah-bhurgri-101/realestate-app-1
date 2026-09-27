# API Gateway

Reactive Spring Cloud Gateway used as the single public API entry point.

## Responsibilities

- Validates JWT signatures, expiration, issuer, and claims.
- Rejects requests without a valid bearer token except for configured public routes.
- Removes client-supplied identity headers and adds trusted `X-User-Id` and `X-User-Roles`.
- Routes requests to auth, user, property, and audit services.
- Applies Redis-backed request rate limiting.
- Adds request correlation IDs and circuit-breaker fallbacks.
- Handles CORS for the frontend.

## Local run

The gateway listens on `http://localhost:8080`.

```powershell
.\mvnw.cmd spring-boot:run
```

Required environment variables include `JWT_SECRET`, `REDIS_HOST`, `REDIS_PORT`, and `REDIS_PASSWORD`. Service URLs can be configured with `AUTH_SERVICE_URI`, `USER_SERVICE_URI`, `PROPERTY_SERVICE_URI`, and `AUDIT_SERVICE_URI`.

## Routes

| Gateway path | Target |
|---|---|
| `/api/v1/auth/**` | auth-service |
| `/api/v1/users/**` | user-service |
| `/api/properties/**` | property-service |
| `/api/favorites/**` | property-service |
| `/api/reviews/**` | property-service |
| `/api/v1/audit-logs/**` | audit-service |

Public property routes include `/api/properties/search` and `/api/properties/featured`. Authentication routes such as registration, login, OTP, password reset, and token refresh are public. All other routes require a bearer token.

## Configuration

Profile-specific routing is in `src/main/resources/application-*.yml`. The Compose deployment uses internal DNS names such as `http://auth-service:8081`; local development uses localhost URLs.

Do not expose backend services directly in production. Only the gateway should be internet-facing.
