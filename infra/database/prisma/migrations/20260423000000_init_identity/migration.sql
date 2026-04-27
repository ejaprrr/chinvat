-- CreateExtension
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- CreateEnum
CREATE TYPE "user_type" AS ENUM ('INDIVIDUAL', 'LIBRARY');

-- CreateEnum
CREATE TYPE "access_level" AS ENUM ('SUPERADMIN', 'ADMIN', 'GOLD', 'PREMIUM', 'NORMAL');

-- CreateTable
CREATE TABLE "user" (
    "id" BIGSERIAL NOT NULL,
    "username" VARCHAR(100) NOT NULL,
    "full_name" VARCHAR(255) NOT NULL,
    "phone_number" VARCHAR(40),
    "email" VARCHAR(255) NOT NULL,
    "user_type" "user_type" NOT NULL,
    "access_level" "access_level" NOT NULL,
    "address_line" VARCHAR(255),
    "postal_code" VARCHAR(40),
    "city" VARCHAR(120),
    "country" VARCHAR(120),
    "default_language" VARCHAR(12) NOT NULL,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "user_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "user_password" (
    "user_id" BIGINT NOT NULL,
    "password_hash" VARCHAR(255) NOT NULL,
    "password_algorithm" VARCHAR(50) NOT NULL,
    "password_changed_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "recovery_required" BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT "user_password_pkey" PRIMARY KEY ("user_id")
);

-- CreateTable
CREATE TABLE "password_recovery_token" (
    "id" BIGSERIAL NOT NULL,
    "user_id" BIGINT NOT NULL,
    "token_hash" VARCHAR(255) NOT NULL,
    "expires_at" TIMESTAMPTZ NOT NULL,
    "used_at" TIMESTAMPTZ,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "password_recovery_token_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "user_certificate" (
    "id" BIGSERIAL NOT NULL,
    "user_id" BIGINT NOT NULL,
    "subject_dn" VARCHAR(512) NOT NULL,
    "issuer_dn" VARCHAR(512) NOT NULL,
    "serial_number" VARCHAR(128) NOT NULL,
    "thumbprint_sha256" VARCHAR(64) NOT NULL,
    "not_before" TIMESTAMPTZ NOT NULL,
    "not_after" TIMESTAMPTZ NOT NULL,
    "revoked_at" TIMESTAMPTZ,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "user_certificate_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "auth_session" (
    "id" UUID NOT NULL DEFAULT gen_random_uuid(),
    "user_id" BIGINT NOT NULL,
    "session_token_hash" VARCHAR(255) NOT NULL,
    "issued_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "expires_at" TIMESTAMPTZ NOT NULL,
    "revoked_at" TIMESTAMPTZ,
    "client_ip" VARCHAR(64),
    "user_agent" VARCHAR(512),

    CONSTRAINT "auth_session_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "auth_audit_event" (
    "id" BIGSERIAL NOT NULL,
    "event_type" VARCHAR(120) NOT NULL,
    "user_id" BIGINT,
    "details" JSONB NOT NULL DEFAULT '{}'::jsonb,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "auth_audit_event_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "user_username_key" ON "user"("username");

-- CreateIndex
CREATE UNIQUE INDEX "user_email_key" ON "user"("email");

-- CreateIndex
CREATE UNIQUE INDEX "password_recovery_token_token_hash_key" ON "password_recovery_token"("token_hash");

-- CreateIndex
CREATE INDEX "idx_password_recovery_token_user_id" ON "password_recovery_token"("user_id");

-- CreateIndex
CREATE UNIQUE INDEX "user_certificate_thumbprint_sha256_key" ON "user_certificate"("thumbprint_sha256");

-- CreateIndex
CREATE INDEX "idx_user_certificate_user_id" ON "user_certificate"("user_id");

-- CreateIndex
CREATE INDEX "idx_user_certificate_thumbprint" ON "user_certificate"("thumbprint_sha256");

-- CreateIndex
CREATE UNIQUE INDEX "auth_session_session_token_hash_key" ON "auth_session"("session_token_hash");

-- CreateIndex
CREATE INDEX "idx_auth_session_user_id" ON "auth_session"("user_id");

-- CreateIndex
CREATE INDEX "idx_auth_session_expires_at" ON "auth_session"("expires_at");

-- Keep at most one non-revoked session per user (single-use session behavior).
CREATE UNIQUE INDEX "uq_auth_session_single_active_user"
ON "auth_session"("user_id")
WHERE "revoked_at" IS NULL;

-- CreateIndex
CREATE INDEX "idx_auth_audit_event_event_type" ON "auth_audit_event"("event_type");

-- AddForeignKey
ALTER TABLE "user_password" ADD CONSTRAINT "user_password_user_id_fkey"
FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "password_recovery_token" ADD CONSTRAINT "password_recovery_token_user_id_fkey"
FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "user_certificate" ADD CONSTRAINT "user_certificate_user_id_fkey"
FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auth_session" ADD CONSTRAINT "auth_session_user_id_fkey"
FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auth_audit_event" ADD CONSTRAINT "auth_audit_event_user_id_fkey"
FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE SET NULL ON UPDATE CASCADE;

