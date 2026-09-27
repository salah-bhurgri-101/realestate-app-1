# Aurelia Real Estate Platform

Production-oriented real-estate marketplace built as a Spring Boot microservice system with a React frontend.

## Architecture

| Component | Port | Responsibility |
|---|---:|---|
| `api-gateway` | 8080 | Public API entry point, JWT validation, routing, rate limiting, CORS |
| `auth-service` | 8081 | Registration, OTP verification, login, JWT/refresh tokens, sessions, admin user management |
| `user-service` | 8083 | Profiles, settings, addresses, follows, blocks |
| `property-service` | 8082 | Property listings, images, amenities, search, favorites, reviews |
| `audit-service` | 8084 | Central audit log ingestion and administration |
| `auth-frontend` | 5173 | React/Vite buyer, seller, admin, and public web application |

Infrastructure is provided by PostgreSQL, MySQL, Redis, Kafka, and Zookeeper in [deployment/docker-compose](./deployment/docker-compose).

## Main request flow

```text
Browser
  -> API Gateway :8080
      -> Auth Service :8081
      -> User Service :8083
      -> Property Service :8082
      -> Audit Service :8084
```

Auth identity data remains in auth-service. After registration or account changes, auth-service publishes `auth.user.events` to Kafka. User-service consumes the event and creates or updates the matching `user_profiles` row using the same UUID.

## Requirements

- Java 21
- Maven Wrapper (`mvnw.cmd` on Windows)
- Node.js 20+ and npm
- Docker Desktop with Compose
- At least 6 GB available memory for the complete Compose stack

## Quick start with Docker Compose

1. Copy the deployment environment file and replace every placeholder secret:

   ```powershell
   cd deployment\docker-compose
   Copy-Item .env .env.local
   ```

   Use `.env` directly only for local development. Never commit real passwords, SMTP credentials, or JWT secrets.

2. Start infrastructure and applications:

   ```powershell
   docker compose --env-file .env -f infra.yml -f app.yml up -d --build
   ```

3. Check service health:

   ```powershell
   docker compose --env-file .env -f infra.yml -f app.yml ps
   curl http://localhost:8080/actuator/health
   ```

4. Start the frontend separately:

   ```powershell
   cd ..\..\auth-frontend
   npm.cmd install
   npm.cmd run dev
   ```

   Open `http://localhost:5173`.

Stop the stack with:

```powershell
cd deployment\docker-compose
docker compose --env-file .env -f infra.yml -f app.yml down
```

Add `-v` only when intentionally deleting local database volumes.

## Local development without application containers

Start infrastructure first:

```powershell
cd deployment\docker-compose
docker compose --env-file .env -f infra.yml up -d
```

Then run each Spring Boot module from its directory:

```powershell
cd auth-service
.\mvnw.cmd spring-boot:run
```

Repeat for `user-service`, `property-service`, `audit-service`, and `api-gateway`. For local processes, use `localhost` service/database hostnames and set the required environment variables for the selected Spring profile.

Run the frontend:

```powershell
cd auth-frontend
npm.cmd install
npm.cmd run dev
```

## API entry points

All browser requests should use the gateway:

```text
http://localhost:8080/api/v1
```

Important routes:

- `/api/v1/auth/**` - authentication and admin user management
- `/api/v1/users/**` - user-service profile and relationship APIs
- `/api/properties/**` - property-service listings and search
- `/api/favorites/**` - property favorites
- `/api/reviews/**` - property reviews
- `/api/v1/audit-logs/**` - audit logs

Public examples:

```text
GET  /api/properties/search
GET  /api/properties/featured
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/otp/verify
```

Protected requests require `Authorization: Bearer <access-token>`. The gateway derives trusted `X-User-Id` and `X-User-Roles` headers from the JWT; clients must not attempt to set those headers.

## Build and validation

Build all Maven services:

```powershell
.\mvnw.cmd package -DskipTests
```

Run user-service tests:

```powershell
.\mvnw.cmd -pl user-service test
```

Build the frontend:

```powershell
cd auth-frontend
npm.cmd run build
```

Validate Compose:

```powershell
cd deployment\docker-compose
docker compose --env-file .env -f infra.yml -f app.yml config --quiet
```

## Security and production checklist

- Replace all `change_me_*` values.
- Use a random JWT secret of at least 32 UTF-8 bytes; use one identical value in auth-service, gateway, property-service, and user-service.
- Provide SMTP credentials through environment variables; use a provider app password.
- Keep PostgreSQL, MySQL, Redis, Kafka, and internal service ports private.
- Expose only the gateway and frontend publicly.
- Configure `CORS_ORIGINS` for the deployed frontend origin.
- Use TLS at the gateway or reverse proxy.
- Back up all database volumes.
- Monitor `/actuator/health`, application logs, Kafka consumer lag, and database connections.
- For high-value production deployments, replace shared HS256 secrets with asymmetric signing and public-key verification.

## Documentation map

- [Auth Service](./auth-service/README.md)
- [User Service](./user-service/README.md)
- [Property Service](./property-service/README.md)
- [Audit Service](./audit-service/README.md)
- [Audit Starter](./audit-starter/README.md)
- [API Gateway](./api-gateway/README.md)
- [Frontend](./auth-frontend/README.md)
- [Docker deployment](./deployment/docker-compose/README.md)
- [AWS EC2/Terraform deployment](./deployment/terraform/README.md)

## CI/CD

[`.github/workflows/ci-cd.yml`](./.github/workflows/ci-cd.yml) runs Java and
frontend validation on pull requests. On pushes to `main`, it builds all five
backend images and the frontend image, pushes immutable commit-SHA tags and
`latest` to Docker Hub, validates Terraform, and can pull/restart those images
on the dedicated EC2 hosts in the protected `production` environment.
