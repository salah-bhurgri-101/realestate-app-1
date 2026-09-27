# Docker Compose Deployment

This directory contains the local and single-host deployment definition for
the complete Aurelia platform, including the frontend container.

## Files

- `infra.yml` starts PostgreSQL, MySQL, Redis, and single-node Kafka in KRaft
  mode (no Zookeeper container).
- `app.yml` starts auth-service, user-service, property-service, audit-service,
  api-gateway, auth-frontend, and their dependencies.
- `.env` supplies local configuration. Keep real credentials out of source
  control and rotate all sample values before deployment.

## Start

From this directory:

```powershell
docker compose --env-file .env -f infra.yml -f app.yml up -d --build
```

Check status and logs:

```powershell
docker compose --env-file .env -f infra.yml -f app.yml ps
docker compose --env-file .env -f infra.yml -f app.yml logs -f api-gateway
```

Validate without starting:

```powershell
docker compose --env-file .env -f infra.yml -f app.yml config --quiet
```

Stop containers:

```powershell
docker compose --env-file .env -f infra.yml -f app.yml down
```

Open the application at `http://localhost:5173`. The API gateway is available
at `http://localhost:8080`.

Use `down -v` only when intentionally deleting local database and Kafka
volumes.

## Required production changes

- Replace every default password and token in `.env`.
- Generate a JWT secret of at least 32 bytes and use the exact same value in
  every JWT-validating service.
- Restrict database, Redis, Kafka, and internal service ports to the private
  network.
- Publish only the gateway and frontend through a TLS reverse proxy.
- Set `CORS_ORIGINS` to the real frontend origin.
- Configure persistent volumes, backups, health checks, log retention, and
  restart policies appropriate for the host.
- Prefer a managed Kafka/database service for production scale.
