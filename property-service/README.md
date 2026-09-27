# Property Service

Spring Boot service for real-estate inventory and marketplace operations.

## Responsibilities

- Create, update, delete, and view properties
- Property images and amenities
- Public search and featured properties
- Admin property status moderation
- Favorites and property reviews
- View tracking and audit events

## Stack and port

- Java 21, Spring Boot
- MySQL and Flyway
- Redis caching
- Shared JWT validation
- Audit Starter integration
- Default port: `8082`

## Run locally

```powershell
.\mvnw.cmd spring-boot:run
```

Build:

```powershell
.\mvnw.cmd package -DskipTests
```

## API

| Method | Path | Description |
|---|---|---|
| POST | `/api/properties` | Create listing |
| GET | `/api/properties/{id}` | View listing |
| PUT | `/api/properties/{id}` | Update owned listing |
| DELETE | `/api/properties/{id}` | Delete owned listing |
| GET | `/api/properties/search` | Public paginated search |
| GET | `/api/properties/featured` | Public approved featured listings |
| GET | `/api/properties/my` | Seller listings |
| PATCH | `/api/properties/{id}/status` | Admin moderation |
| POST | `/api/properties/{id}/images` | Add images |
| DELETE | `/api/properties/{id}/images/{imageId}` | Delete image |
| POST | `/api/{id}/favorite` | Toggle favorite |
| GET | `/api/properties/{id}/reviews` | List reviews |
| POST | `/api/properties/{id}/reviews` | Add review |

The frontend reaches this service through the gateway. Public featured results
are active, approved, and marked as featured.

## Configuration

- `PROPERTY_SERVER_PORT`
- `PROPERTY_DB_HOST`, `PROPERTY_DB_PORT`, `PROPERTY_DB_NAME`
- `PROPERTY_DB_USER`, `PROPERTY_DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `JWT_SECRET`
- `AUDIT_BASE_URL`

Keep the database and service port private in production.
