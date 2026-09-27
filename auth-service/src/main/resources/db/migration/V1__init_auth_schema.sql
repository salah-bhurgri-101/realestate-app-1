-- ============================================================================
-- V1__init_auth_schema.sql
-- Initial schema for auth_service: users, refresh_tokens, sessions, otps,
-- audit_logs.
--
-- Creation order respects FK dependencies:
--   users -> refresh_tokens -> sessions -> otps -> audit_logs
--
-- Enum-backed columns (role, device_type, type, action) are stored as
-- VARCHAR to match Hibernate's @Enumerated(EnumType.STRING) mapping. No
-- CHECK constraints are added here since the enum classes weren't provided;
-- add them later if you want DB-level validation of the allowed values.
-- ============================================================================

-- ── users ───────────────────────────────────────────────────────────────────

CREATE TABLE users (
                       id             UUID PRIMARY KEY,
                       email          VARCHAR(150) NOT NULL,
                       phone          VARCHAR(20),
                       name           VARCHAR(100) NOT NULL,
                       password_hash  VARCHAR(255) NOT NULL,
                       is_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
                       is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
                       token_version  INTEGER      NOT NULL DEFAULT 0,
                       role           VARCHAR(30)  NOT NULL DEFAULT 'ROLE_USER',
                       created_at     TIMESTAMP    NOT NULL,
                       updated_at     TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_users_email ON users (email);
CREATE UNIQUE INDEX idx_users_phone ON users (phone);

-- ── refresh_tokens ──────────────────────────────────────────────────────────

CREATE TABLE refresh_tokens (
                                id           UUID PRIMARY KEY,
                                user_id      UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                                token_hash   VARCHAR(255) NOT NULL,
                                device_name  VARCHAR(100),
                                device_type  VARCHAR(20),
                                ip_address   VARCHAR(45),
                                user_agent   VARCHAR(512),
                                expires_at   TIMESTAMP    NOT NULL,
                                revoked_at   TIMESTAMP,
                                created_at   TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_refresh_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_token_user ON refresh_tokens (user_id);

-- ── sessions ────────────────────────────────────────────────────────────────
-- One-to-one with refresh_tokens: a session ends when its refresh token
-- is revoked/deleted, so ON DELETE CASCADE mirrors the entity's cascade.

CREATE TABLE sessions (
                          id                UUID PRIMARY KEY,
                          user_id           UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                          refresh_token_id  UUID         NOT NULL REFERENCES refresh_tokens (id) ON DELETE CASCADE,
                          device_name       VARCHAR(100),
                          device_type       VARCHAR(20),
                          ip_address        VARCHAR(45),
                          user_agent        VARCHAR(512),
                          last_active_at    TIMESTAMP    NOT NULL,
                          created_at        TIMESTAMP    NOT NULL
);

CREATE INDEX idx_session_user ON sessions (user_id);
CREATE UNIQUE INDEX idx_session_refresh_token ON sessions (refresh_token_id);

-- ── otps ────────────────────────────────────────────────────────────────────

CREATE TABLE otps (
                      id          UUID PRIMARY KEY,
                      user_id     UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                      code        VARCHAR(10) NOT NULL,
                      type        VARCHAR(30) NOT NULL,
                      attempts    INTEGER     NOT NULL DEFAULT 0,
                      used        BOOLEAN     NOT NULL DEFAULT FALSE,
                      expires_at  TIMESTAMP   NOT NULL,
                      created_at  TIMESTAMP   NOT NULL
);

CREATE INDEX idx_otp_user_type ON otps (user_id, type);

-- ── audit_logs ──────────────────────────────────────────────────────────────
-- user_id is nullable (e.g. failed login for an unknown email) and kept on
-- user deletion (SET NULL) so audit history isn't lost.

CREATE TABLE audit_logs (
                            id          UUID PRIMARY KEY,
                            user_id     UUID REFERENCES users (id) ON DELETE SET NULL,
                            action      VARCHAR(50) NOT NULL,
                            ip_address  VARCHAR(45),
                            user_agent  VARCHAR(512),
                            detail      TEXT,
                            created_at  TIMESTAMP   NOT NULL
);

CREATE INDEX idx_audit_user ON audit_logs (user_id);
CREATE INDEX idx_audit_action ON audit_logs (action);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at);