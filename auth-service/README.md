# Auth Service

Spring Boot authentication and identity service.

## Responsibilities

- Registration and email OTP verification
- Login, JWT access tokens, refresh-token rotation, logout, and logout-all
- Password reset and password change
- User sessions and audit records
- Admin listing, activation/deactivation, and role management
- Publishing user lifecycle events to Kafka

Auth-service owns identity and credentials. Profile fields are synchronized to
user-service through Kafka.

## Stack and port

- Java 21, Spring Boot
- PostgreSQL and Flyway
- Redis for session and token support
- Kafka producer for `auth.user.events`
- SMTP for OTP and password emails
- Default port: `8081`

## Run locally

```powershell
.\mvnw.cmd spring-boot:run
```

Build:

```powershell
.\mvnw.cmd package -DskipTests
```

## API

Base path: `/api/v1/auth`.

| Method | Path | Auth |
|---|---|---|
| POST | `/register` | Public |
| POST | `/otp/verify` | Public |
| POST | `/otp/resend` | Public |
| POST | `/login` | Public |
| POST | `/token/refresh` | Public |
| POST | `/password/forgot` | Public |
| POST | `/password/reset` | Public |
| GET | `/me` | User |
| POST | `/logout` | User |
| POST | `/logout/all` | User |
| PATCH | `/password/change` | User |

Admin endpoints:

```text
GET   /api/v1/auth/admin/users
GET   /api/v1/auth/admin/users?role=ROLE_USER
GET   /api/v1/auth/admin/users?role=ROLE_MODERATOR
PATCH /api/v1/auth/admin/users/{id}/status
PATCH /api/v1/auth/admin/users/{id}/role
```

The current `ROLE_MODERATOR` role is used by the application as seller access.

## Kafka synchronization

After a successful registration transaction, auth-service publishes
`USER_REGISTERED` to `auth.user.events` with the identity and profile fields.
User-service consumes the event and creates the matching profile. Events are
published after commit and processed idempotently downstream.

## Required configuration

- `JWT_SECRET`: shared with gateway, user-service, and property-service; at least 32 bytes
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- `CORS_ORIGINS`

Never use default secrets in production.
