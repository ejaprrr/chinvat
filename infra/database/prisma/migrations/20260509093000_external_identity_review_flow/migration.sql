-- eIDAS external identity review/compliance extension
-- Adds columns needed for explicit internal approval flow and richer compliance context.

ALTER TABLE "external_identity"
  ADD COLUMN IF NOT EXISTS "identity_reference" VARCHAR(255),
  ADD COLUMN IF NOT EXISTS "nationality" VARCHAR(80),
  ADD COLUMN IF NOT EXISTS "raw_claims_json" TEXT,
  ADD COLUMN IF NOT EXISTS "reviewed_by" VARCHAR(120),
  ADD COLUMN IF NOT EXISTS "reviewed_at" TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS "review_reason" TEXT;

CREATE INDEX IF NOT EXISTS "idx_external_identity_current_status"
  ON "external_identity"("current_status");

CREATE INDEX IF NOT EXISTS "idx_external_identity_reviewed_at"
  ON "external_identity"("reviewed_at");
