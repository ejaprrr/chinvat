DROP INDEX IF EXISTS "auth_password_reset_reset_token_hash_key";

CREATE INDEX IF NOT EXISTS "idx_auth_password_reset_user_hash"
ON "auth_password_reset" ("user_id", "reset_token_hash");