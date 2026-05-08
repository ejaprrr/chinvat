-- Chinvat Database - Consolidated Initial Schema
-- All tables with normalized RBAC permissions and N:M relationships

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create enum types
CREATE TYPE "user_type" AS ENUM ('INDIVIDUAL', 'LIBRARY');
CREATE TYPE "access_level" AS ENUM ('SUPERADMIN', 'ADMIN', 'GOLD', 'PREMIUM', 'NORMAL');
CREATE TYPE "auth_session_token_kind" AS ENUM ('ACCESS', 'REFRESH');

-- User table - base user entity
CREATE TABLE "user" (
  "id" BIGSERIAL NOT NULL,
  "username" VARCHAR(100) NOT NULL UNIQUE,
  "full_name" VARCHAR(255) NOT NULL,
  "phone_number" VARCHAR(40),
  "email" VARCHAR(255) NOT NULL UNIQUE,
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

CREATE UNIQUE INDEX "idx_user_username" ON "user"("username");
CREATE UNIQUE INDEX "idx_user_email" ON "user"("email");

-- User password table - password storage
CREATE TABLE "user_password" (
  "user_id" BIGINT NOT NULL,
  "password_hash" VARCHAR(255) NOT NULL,
  "password_algorithm" VARCHAR(50) NOT NULL,
  "password_changed_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "recovery_required" BOOLEAN NOT NULL DEFAULT FALSE,

  CONSTRAINT "user_password_pkey" PRIMARY KEY ("user_id"),
  CONSTRAINT "user_password_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

-- User certificate table - X.509 certificate-based login
CREATE TABLE "user_certificate" (
  "id" BIGSERIAL NOT NULL,
  "user_id" BIGINT NOT NULL,
  "subject_dn" VARCHAR(512) NOT NULL,
  "issuer_dn" VARCHAR(512) NOT NULL,
  "serial_number" VARCHAR(128) NOT NULL,
  "thumbprint_sha256" VARCHAR(64) NOT NULL UNIQUE,
  "not_before" TIMESTAMPTZ NOT NULL,
  "not_after" TIMESTAMPTZ NOT NULL,
  "revoked_at" TIMESTAMPTZ,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT "user_certificate_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "user_certificate_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_user_certificate_user_id" ON "user_certificate"("user_id");
CREATE INDEX "idx_user_certificate_thumbprint" ON "user_certificate"("thumbprint_sha256");

-- Auth password reset table - password reset tokens
CREATE TABLE "auth_password_reset" (
  "id" UUID NOT NULL DEFAULT gen_random_uuid(),
  "user_id" BIGINT NOT NULL,
  "reset_token_hash" VARCHAR(255) NOT NULL,
  "issued_at" TIMESTAMPTZ NOT NULL,
  "expires_at" TIMESTAMPTZ NOT NULL,
  "consumed_at" TIMESTAMPTZ,
  "client_ip" VARCHAR(64),
  "user_agent" VARCHAR(512),

  CONSTRAINT "auth_password_reset_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "auth_password_reset_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_auth_password_reset_user_id" ON "auth_password_reset"("user_id");
CREATE INDEX "idx_auth_password_reset_user_hash" ON "auth_password_reset"("user_id", "reset_token_hash");
CREATE INDEX "idx_auth_password_reset_expires_at" ON "auth_password_reset"("expires_at");

-- Auth session table - session management
CREATE TABLE "auth_session" (
  "id" UUID NOT NULL DEFAULT gen_random_uuid(),
  "user_id" BIGINT NOT NULL,
  "session_token_hash" VARCHAR(255) NOT NULL UNIQUE,
  "session_token_kind" "auth_session_token_kind" NOT NULL,
  "issued_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "expires_at" TIMESTAMPTZ NOT NULL,
  "revoked_at" TIMESTAMPTZ,
  "client_ip" VARCHAR(64),
  "user_agent" VARCHAR(512),

  CONSTRAINT "auth_session_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "auth_session_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_auth_session_user_id" ON "auth_session"("user_id");
CREATE INDEX "idx_auth_session_expires_at" ON "auth_session"("expires_at");

-- Auth audit event table - audit logging
CREATE TABLE "auth_audit_event" (
  "id" BIGSERIAL NOT NULL,
  "event_type" VARCHAR(120) NOT NULL,
  "user_id" BIGINT,
  "details" JSON NOT NULL DEFAULT '{}',
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT "auth_audit_event_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "auth_audit_event_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE SET NULL
);

CREATE INDEX "idx_auth_audit_event_event_type" ON "auth_audit_event"("event_type");

-- RBAC role table - role definitions (no legacy permissions_csv)
CREATE TABLE "rbac_role" (
  "id" BIGSERIAL NOT NULL,
  "role_name" VARCHAR(80) NOT NULL UNIQUE,

  CONSTRAINT "rbac_role_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "idx_rbac_role_name" ON "rbac_role"("role_name");

-- RBAC permission table - permission definitions
CREATE TABLE "rbac_permission" (
  "id" BIGSERIAL NOT NULL,
  "permission_code" VARCHAR(120) NOT NULL UNIQUE,
  "description" VARCHAR(255),
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT "rbac_permission_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "idx_rbac_permission_code" ON "rbac_permission"("permission_code");

-- RBAC role permission table - N:M relationship between roles and permissions
CREATE TABLE "rbac_role_permission" (
  "role_id" BIGINT NOT NULL,
  "permission_id" BIGINT NOT NULL,

  CONSTRAINT "rbac_role_permission_pkey" PRIMARY KEY ("role_id", "permission_id"),
  CONSTRAINT "rbac_role_permission_role_id_fkey" FOREIGN KEY ("role_id") REFERENCES "rbac_role"("id") ON DELETE CASCADE,
  CONSTRAINT "rbac_role_permission_permission_id_fkey" FOREIGN KEY ("permission_id") REFERENCES "rbac_permission"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_rbac_role_permission_permission_id" ON "rbac_role_permission"("permission_id");

-- User role table - user-to-role assignment
CREATE TABLE "user_role" (
  "user_id" BIGINT NOT NULL,
  "role_id" BIGINT NOT NULL,
  "assigned_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "assigned_by" VARCHAR(120),

  CONSTRAINT "user_role_pkey" PRIMARY KEY ("user_id", "role_id"),
  CONSTRAINT "user_role_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
  CONSTRAINT "user_role_role_id_fkey" FOREIGN KEY ("role_id") REFERENCES "rbac_role"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_user_role_role_id" ON "user_role"("role_id");
