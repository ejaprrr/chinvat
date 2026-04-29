-- Align PostgreSQL schema with current backend JPA entities for auth/users/rbac.

-- auth_session must distinguish ACCESS and REFRESH tokens.
ALTER TABLE "auth_session"
  ADD COLUMN IF NOT EXISTS "session_token_kind" VARCHAR(20);

UPDATE "auth_session"
SET "session_token_kind" = 'ACCESS'
WHERE "session_token_kind" IS NULL;

ALTER TABLE "auth_session"
  ALTER COLUMN "session_token_kind" SET NOT NULL;

-- Old unique index allowed only one active session per user, which breaks ACCESS+REFRESH pair.
DROP INDEX IF EXISTS "uq_auth_session_single_active_user";

CREATE UNIQUE INDEX IF NOT EXISTS "uq_auth_session_active_user_kind"
ON "auth_session" ("user_id", "session_token_kind")
WHERE "revoked_at" IS NULL;

-- Password reset token store used by auth module.
CREATE TABLE IF NOT EXISTS "auth_password_reset" (
  "id" UUID NOT NULL DEFAULT gen_random_uuid(),
  "user_id" BIGINT NOT NULL,
  "reset_token_hash" VARCHAR(255) NOT NULL,
  "issued_at" TIMESTAMPTZ NOT NULL,
  "expires_at" TIMESTAMPTZ NOT NULL,
  "consumed_at" TIMESTAMPTZ,
  "client_ip" VARCHAR(64),
  "user_agent" VARCHAR(512),

  CONSTRAINT "auth_password_reset_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX IF NOT EXISTS "auth_password_reset_reset_token_hash_key"
ON "auth_password_reset" ("reset_token_hash");

CREATE INDEX IF NOT EXISTS "idx_auth_password_reset_user_id"
ON "auth_password_reset" ("user_id");

CREATE INDEX IF NOT EXISTS "idx_auth_password_reset_expires_at"
ON "auth_password_reset" ("expires_at");

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'auth_password_reset_user_id_fkey'
  ) THEN
    ALTER TABLE "auth_password_reset"
      ADD CONSTRAINT "auth_password_reset_user_id_fkey"
      FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE ON UPDATE CASCADE;
  END IF;
END;
$$;

-- Legacy table from earlier model, replaced by auth_password_reset.
DROP TABLE IF EXISTS "password_recovery_token";

-- Legacy table from feature module bootstrap, not used by backend entities.
DROP TABLE IF EXISTS "users_account";
