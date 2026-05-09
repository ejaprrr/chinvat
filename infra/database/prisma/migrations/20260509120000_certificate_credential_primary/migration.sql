ALTER TABLE "certificate_credential"
  ADD COLUMN IF NOT EXISTS "is_primary" BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS "idx_certificate_credential_is_primary"
  ON "certificate_credential"("user_id", "is_primary");
