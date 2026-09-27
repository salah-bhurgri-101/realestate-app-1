-- ============================================================================
-- V1__init_user_schema.sql
-- Initial schema for user_service: user_profiles, addresses, user_settings,
-- user_follows, user_blocks, processed_events.
--
-- ARCHITECTURE NOTE — this service owns a SEPARATE database from
-- auth_service and does NOT keep a local copy of the `users` table.
--
--   1. Every table below stores a bare `user_id UUID` column with NO
--      foreign key constraint — there is nothing locally to reference,
--      since identity is owned entirely by auth_service.
--   2. `user_id` MUST equal the `id` (UUID) of the corresponding row in
--      auth_service.users. Referential integrity across services is
--      enforced at the application layer, not the DB layer.
--   3. Enriched data (email, name, role, active status) is NOT stored
--      here. Callers needing that must query auth_service directly
--      (sync API call, or a batch/bulk lookup endpoint for list views).
--   4. `processed_events` still exists to keep event consumption from
--      auth_service idempotent for whatever events THIS service does
--      react to (e.g. UserDeleted -> cascade-delete profile/settings/
--      follows/blocks for that user_id, since there's no DB-level
--      ON DELETE CASCADE possible across services). Insert the event_id
--      here in the same transaction as the resulting deletes/updates.
--
-- Enum-backed columns are stored as VARCHAR (Hibernate
-- @Enumerated(EnumType.STRING) convention), matching auth_service's style.
-- ============================================================================

-- ── user_profiles ───────────────────────────────────────────────────────────
-- One-to-one extended profile data owned by this service.

CREATE TABLE user_profiles (
                               id            UUID         PRIMARY KEY,
                               user_id       UUID         NOT NULL,
                               display_name  VARCHAR(100),
                               bio           TEXT,
                               avatar_url    VARCHAR(512),
                               cover_url     VARCHAR(512),
                               date_of_birth DATE,
                               gender        VARCHAR(20),
                               phone         VARCHAR(20),
                               created_at    TIMESTAMP    NOT NULL,
                               updated_at    TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_profile_user ON user_profiles (user_id);

-- ── addresses ───────────────────────────────────────────────────────────────
-- One-to-many: a user can have multiple addresses (home/work/other).

CREATE TABLE addresses (
                           id           UUID         PRIMARY KEY,
                           user_id      UUID         NOT NULL,
                           label        VARCHAR(20)  NOT NULL DEFAULT 'HOME', -- HOME, WORK, OTHER
                           line1        VARCHAR(200) NOT NULL,
                           line2        VARCHAR(200),
                           city         VARCHAR(100) NOT NULL,
                           state        VARCHAR(100),
                           postal_code  VARCHAR(20),
                           country      VARCHAR(2)   NOT NULL, -- ISO 3166-1 alpha-2
                           is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
                           created_at   TIMESTAMP    NOT NULL,
                           updated_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_address_user ON addresses (user_id);

-- ── user_settings ───────────────────────────────────────────────────────────
-- One-to-one preferences/settings owned by this service.

CREATE TABLE user_settings (
                               id                   UUID        PRIMARY KEY,
                               user_id              UUID        NOT NULL,
                               language             VARCHAR(10) NOT NULL DEFAULT 'en',
                               timezone             VARCHAR(50) NOT NULL DEFAULT 'UTC',
                               theme                VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
                               profile_visibility   VARCHAR(20) NOT NULL DEFAULT 'PUBLIC', -- PUBLIC, FRIENDS_ONLY, PRIVATE
                               email_notifications  BOOLEAN     NOT NULL DEFAULT TRUE,
                               push_notifications   BOOLEAN     NOT NULL DEFAULT TRUE,
                               sms_notifications    BOOLEAN     NOT NULL DEFAULT FALSE,
                               created_at           TIMESTAMP   NOT NULL,
                               updated_at           TIMESTAMP   NOT NULL
);

CREATE UNIQUE INDEX idx_settings_user ON user_settings (user_id);

-- ── user_follows ────────────────────────────────────────────────────────────
-- Self-referential many-to-many relationship (follower -> followee).
-- No FK on either side since `users` doesn't live in this DB.

CREATE TABLE user_follows (
                              id           UUID      PRIMARY KEY,
                              follower_id  UUID      NOT NULL,
                              followee_id  UUID      NOT NULL,
                              created_at   TIMESTAMP NOT NULL,
                              CONSTRAINT chk_follow_not_self CHECK (follower_id <> followee_id)
);

CREATE UNIQUE INDEX idx_follow_pair ON user_follows (follower_id, followee_id);
CREATE INDEX idx_follow_followee ON user_follows (followee_id); -- fast "who follows me" lookups

-- ── user_blocks ─────────────────────────────────────────────────────────────

CREATE TABLE user_blocks (
                             id          UUID      PRIMARY KEY,
                             blocker_id  UUID      NOT NULL,
                             blocked_id  UUID      NOT NULL,
                             created_at  TIMESTAMP NOT NULL,
                             CONSTRAINT chk_block_not_self CHECK (blocker_id <> blocked_id)
);

CREATE UNIQUE INDEX idx_block_pair ON user_blocks (blocker_id, blocked_id);

-- ── processed_events ────────────────────────────────────────────────────────
-- Idempotency ledger for inbound events consumed from auth_service (e.g.
-- UserDeleted, UserDeactivated). Insert here in the SAME transaction as the
-- resulting state change; check for existing event_id before applying.
-- This is the mechanism that keeps this service's data eventually
-- consistent with auth_service despite there being no DB-level FK/cascade.

CREATE TABLE processed_events (
                                  id           UUID         PRIMARY KEY,
                                  event_id     VARCHAR(100) NOT NULL, -- id assigned by the publisher (e.g. Kafka message key / UUID in payload)
                                  event_type   VARCHAR(50)  NOT NULL, -- e.g. USER_DEACTIVATED, USER_DELETED
                                  source       VARCHAR(50)  NOT NULL DEFAULT 'auth_service',
                                  processed_at TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_processed_event_id ON processed_events (event_id);
CREATE INDEX idx_processed_event_type ON processed_events (event_type);
