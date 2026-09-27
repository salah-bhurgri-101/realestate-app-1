# User Service

Spring Boot service for profile and user-community data. Auth identity remains
owned by auth-service. This service stores profiles, settings, addresses,
follows, blocks, and event-processing state keyed by the auth user UUID.

## Stack and port

- Java 21, Spring Boot
- PostgreSQL and Flyway
- Redis cache
- Kafka consumer
- Default port: `8083`

## Run locally

```powershell
.\mvnw.cmd spring-boot:run
```

Run tests:

```powershell
.\mvnw.cmd test
```

## API

All routes use `/api/v1/users/{userId}`. Gateway and service security ensure
that the path user ID belongs to the authenticated user.

| Method | Path | Description |
|---|---|---|
| GET | `/profile` | Get profile |
| PUT | `/profile` | Create/update profile |
| DELETE | `/profile` | Delete profile |
| GET | `/addresses` | List addresses |
| POST | `/addresses` | Add address |
| PUT | `/addresses/{addressId}` | Update address |
| DELETE | `/addresses/{addressId}` | Delete address |
| GET | `/settings` | Get settings |
| PATCH | `/settings` | Partially update settings |
| POST | `/follows/{targetUserId}` | Follow |
| DELETE | `/follows/{targetUserId}` | Unfollow |
| GET | `/follows/following` | Following page |
| GET | `/follows/followers` | Followers page |
| GET | `/follows/counts` | Follower/following counts |
| POST | `/blocks/{targetUserId}` | Block |
| DELETE | `/blocks/{targetUserId}` | Unblock |
| GET | `/blocks/{targetUserId}/status` | Block status |
| GET | `/blocks` | Blocked-user page |

## Kafka synchronization

Topic: `auth.user.events`.

Events include `USER_REGISTERED`, `USER_UPDATED`, `USER_DEACTIVATED`,
`USER_REACTIVATED`, and `USER_DELETED`. Registration and profile-bearing
updates contain display name, bio, avatar URL, cover URL, date of birth,
gender, and phone. The listener creates or updates `user_profiles` for
registration/update events and removes user-owned data for deletion events.

Every event is recorded in `processed_events` so redelivery does not apply it
twice.

## Configuration

- `SERVER_PORT` (default `8083`)
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`

Use the same JWT secret as auth-service and gateway. Keep this service private
in production and expose it through the gateway.
