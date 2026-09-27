# Aurelia Platform Audit Report

Audit date: 2026-09-12  
Scope: `api-gateway`, `auth-service`, `user-service`, `property-service`,
`audit-service`, `audit-starter`, `auth-frontend`, and
`deployment/docker-compose`.

This report records verified repository findings and fixes. A PASS means the
source was inspected; it does not mean a live multi-container penetration
test was performed.

## Verification performed

- Docker Compose configuration validation passed with the deployment `.env`.
- The frontend build was started from `auth-frontend`, but the existing
  TypeScript/Vite process did not complete within the available validation
  window; no successful result is claimed here.
- No full multi-container startup or external penetration test was performed.

## 1. Architecture and flow completeness

### Service presence and buildability — INCOMPLETE

- All requested service directories, Maven descriptors, application classes,
  controllers, and the frontend exist.
- Existing target artifacts and prior targeted tests show successful builds for
  the main services, but a clean build/start of the complete Compose stack was
  not performed in this audit environment.
- Live startup, health dependencies, and cross-service routing remain
  **FLAGGED-FOR-HUMAN** until executed against the deployment environment.

### REST contract coverage — FLAGGED-FOR-HUMAN

- Controllers exist across all services and the READMEs now document the
  intended gateway paths.
- A complete generated OpenAPI-to-README route diff was not available, so
  undocumented endpoints and exact response-shape drift require a live
  contract test before production.

### Kafka user-event contract — PASS / FLAGGED-FOR-HUMAN

- Auth publishes profile-bearing lifecycle events after transaction commit.
- User-service consumes the event topic and records processed event IDs for
  idempotency.
- Topic and consumer group configuration are present in the producer/consumer
  modules.
- Broker-level retry/DLQ behavior and a live serializer/partition-key
  interoperability test were not verified; this is **FLAGGED-FOR-HUMAN**.

### Frontend gateway usage — INCOMPLETE

- API clients now support Vite build-time gateway URLs; localhost remains only
  as a development fallback.
- Access and refresh tokens are stored in `localStorage`, which is vulnerable
  to token theft after XSS. Moving refresh tokens to secure HttpOnly cookies
  is **FLAGGED-FOR-HUMAN** because it changes the auth contract.

### Trusted gateway headers — FIXED

- [JwtAuthenticationFilter.java](./api-gateway/src/main/java/com/realestate/apigateway/filter/JwtAuthenticationFilter.java)
  removes client-provided `X-User-Id` and `X-User-Roles` before injecting
  values derived from the validated JWT.
- Gateway public-route matching was tightened from an unsafe broad
  `startsWith` check to exact route or child-route matching.

## 2. Security review

### Secrets and committed environment configuration — FIXED / FLAGGED-FOR-HUMAN

- The deployment `.env` contained a real-looking SMTP username/password and
  placeholder database, Redis, and JWT secrets. Values were replaced with
  replacement placeholders in
  [deployment/docker-compose/.env](./deployment/docker-compose/.env).
- Root [`.gitignore`](./.gitignore) now excludes `.env`, `.env.*`, build
  outputs, and frontend dependencies.
- Any credentials previously committed to a remote or used in production must
  still be rotated by an operator; this cannot be verified from the local
  non-Git workspace.

### JWT validation — FIXED

- Production secret properties now fail when unset instead of silently using
  default credentials.
- Gateway, auth-service, user-service, property-service, and audit-service
  now require the configured issuer in addition to signature and expiry
  validation.
- Gateway, property-service, and audit-service now reject JWT secrets below
  32 UTF-8 bytes.

### AuthZ and IDOR — FIXED / FLAGGED-FOR-HUMAN

- Auth-service and audit-service expose method-level role checks for their
  documented administrative/mutation operations.
- User-service verifies the user path against the JWT subject.
- Property-service now derives seller/buyer identity from the authenticated
  JWT principal rather than the client-controlled `X-User-Id` header in
  [PropertyController.java](./property-service/src/main/java/com/realestate/property_service/controller/PropertyController.java),
  [FavoriteController.java](./property-service/src/main/java/com/realestate/property_service/controller/FavoriteController.java),
  and [PropertyReviewController.java](./property-service/src/main/java/com/realestate/property_service/controller/PropertyReviewController.java).
- A complete endpoint-by-endpoint defense-in-depth annotation audit and live
  forged-header test remain **FLAGGED-FOR-HUMAN**.

### CORS — FIXED

- User-service no longer permits every origin.
- Production gateway CORS now reads an explicit `CORS_ORIGINS` allow-list and
  narrows allowed request headers.
- Auth/property/audit CORS still need a shared production integration test;
  wildcard-style development settings are **FLAGGED-FOR-HUMAN** if those
  services are ever exposed directly.

### Actuator exposure — FIXED

- Audit-service no longer permits `/actuator/**`; only health remains public.
- Gateway management exposure is now narrowed to `health,info` in
  [application.yml](./api-gateway/src/main/resources/application.yml).

### Input validation — PASS / FLAGGED-FOR-HUMAN

- Main auth, user, property, and audit request bodies inspected use
  `@Valid`.
- DTO field constraints and every controller method were not exhaustively
  tested; add contract tests for boundary lengths, URLs, enumerations, and
  review content.

### Password, OTP, and reset-token behavior — FLAGGED-FOR-HUMAN

- BCrypt cost 12 is configured and JWT/password flows exist.
- OTP/reset expiration, per-account throttling, single-use guarantees, and
  non-logging require integration tests against Redis and the mail flow.

## 3. Production readiness

### Health and infrastructure — INCOMPLETE

- Actuator health endpoints and production connection-pool settings exist in
  several services.
- Live DB, Redis, Kafka readiness, graceful shutdown, and connection-pool
  behavior were not exercised in this environment.

### Audit transport — FLAGGED-FOR-HUMAN

- Property-service config selects Kafka audit transport and explicitly sets
  `fail-silently: true`.
- Whether compliance-sensitive mutations may drop audit records requires a
  business decision; use durable retry/DLQ or blocking behavior where required.

### Compose network exposure — FIXED / FLAGGED-FOR-HUMAN

- Internal application, database, and Redis services no longer publish host
  ports in the combined deployment definitions. Gateway remains the public
  backend entry point. A separate frontend container/reverse proxy remains a
  deployment choice.

### Database migrations — FLAGGED-FOR-HUMAN

- Migration directories exist in schema-owning services, but no clean
  migration-from-empty-database run was performed.

### Frontend production bundle — FLAGGED-FOR-HUMAN

- The frontend has a production build script and no reviewed token
  `console.log` was found in source.
- Hard-coded localhost API bases are development fallbacks configurable through
  `VITE_API_BASE_URL` and `VITE_PROPERTY_API_BASE_URL`. LocalStorage token
  storage remains a production security decision.

## 4. Dead code and repository hygiene

### Secret/build exclusions — FIXED

- Root ignore rules now cover `.env` files, `target`, `node_modules`, and
  frontend build output.

### Empty/dead directories — FLAGGED-FOR-HUMAN

- No deletion was performed because the workspace is not a Git repository and
  deletion of ambiguous directories could remove user-owned artifacts.
- Generated `target` and `node_modules` directories are ignored build
  artifacts, not application source.

## 5. Documentation gaps

### FIXED

- Service, frontend, parent, and deployment READMEs were aligned with the
  current ports, Kafka flow, gateway paths, and production checklist.

### FLAGGED-FOR-HUMAN

- Route and response-contract drift still needs automated OpenAPI/contract
  coverage.

## Prioritized production punch-list

1. Rotate any SMTP/database/Redis/JWT credentials ever committed or deployed.
2. Remove all public actuator exposure except health and restrict internal
   service host ports in a production Compose override.
3. Replace frontend localhost URLs with environment-configured gateway URLs.
4. Move refresh-token handling to secure HttpOnly cookies or formally accept
   the localStorage risk with a documented threat model.
5. Add live integration tests for Kafka serialization, retries/DLQ, OTP/reset
   throttling, JWT issuer rejection, IDOR, and forged gateway headers.
6. Decide whether audit failures may be dropped for property mutations.
7. Run dependency vulnerability scans (`mvn` update/CVE tooling and `npm
   audit`) and pin approved upgrades.

## Deployment automation added

- The old per-service workflows and stale `service-registry` workflow were
  replaced by [ci-cd.yml](./.github/workflows/ci-cd.yml).
- Backend and frontend images are built and pushed to Docker Hub with both
  immutable commit-SHA tags and `latest`.
- Terraform in [deployment/terraform](./deployment/terraform) provisions a
  VPC, subnets, security groups, dedicated EC2 hosts for every application
  service, and separate EC2-hosted database/Redis/Kafka containers.
- Terraform state, credentials, and production infrastructure creation remain
  operator-controlled and are not claimed as live-verified from this
  workspace.
- Frontend production build was rerun from the correct module directory and
  passed; Vite still reports the existing single JavaScript chunk size warning.
- Local Compose now includes the Nginx frontend container, so the complete
  application can be started with one combined Compose command.
- Complete Compose rendering passed after adding the frontend service. Docker
  image build could not be executed because Docker Desktop's Linux engine was
  not running in this workspace; runtime readiness is therefore not claimed.
- Terraform CLI is not installed in this Windows workspace, so Terraform
  syntax must be validated by the `terraform validate` step in GitHub Actions
  or an operator workstation.
