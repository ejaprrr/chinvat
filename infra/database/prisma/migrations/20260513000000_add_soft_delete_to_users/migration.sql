-- Add soft delete support to user table for GDPR/compliance compliance
-- Enterprise-grade: tracks deletion with audit trail via deleted_at timestamp

-- Add deleted_at column to user table
ALTER TABLE "user" ADD COLUMN "deleted_at" TIMESTAMPTZ;

-- Create index on deleted_at for efficient filtering of active users
CREATE INDEX "idx_user_deleted_at" ON "user"("deleted_at");

-- Composite index for finding users by email that are NOT deleted (active users)
-- Used in login queries: WHERE email = ? AND deleted_at IS NULL
CREATE INDEX "idx_user_email_active" ON "user"("email", "deleted_at");

-- Composite index for finding users by username that are NOT deleted
-- Used in registration queries: WHERE username = ? AND deleted_at IS NULL
CREATE INDEX "idx_user_username_active" ON "user"("username", "deleted_at");
