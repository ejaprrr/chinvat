-- Add auth_session table for opaque bearer token store (access + refresh sessions).
-- References users_account (current JPA working table, not app_user from future Prisma model).

CREATE TABLE IF NOT EXISTS "auth_session" (
  "id"                 UUID         NOT NULL DEFAULT gen_random_uuid(),
  "user_id"            BIGINT       NOT NULL,
  "session_token_hash" VARCHAR(255) NOT NULL,
  "issued_at"          TIMESTAMPTZ  NOT NULL,
  "expires_at"         TIMESTAMPTZ  NOT NULL,
  "revoked_at"         TIMESTAMPTZ,
  "client_ip"          VARCHAR(64),
  "user_agent"         VARCHAR(512),

  CONSTRAINT "auth_session_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "auth_session_user_id_fkey"
    FOREIGN KEY ("user_id") REFERENCES "users_account"("id") ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS "auth_session_session_token_hash_key"
  ON "auth_session"("session_token_hash");

CREATE INDEX IF NOT EXISTS "idx_auth_session_user_id"
  ON "auth_session"("user_id");

CREATE INDEX IF NOT EXISTS "idx_auth_session_expires_at"
  ON "auth_session"("expires_at");
