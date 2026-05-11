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

-- Trust provider registry - used by certificate and external identity bindings
CREATE TABLE "trust_provider" (
  "id" BIGSERIAL NOT NULL,
  "provider_code" VARCHAR(80) NOT NULL UNIQUE,
  "display_name" VARCHAR(160) NOT NULL,
  "provider_type" VARCHAR(80) NOT NULL,
  "source_type" VARCHAR(80) NOT NULL,
  "country_code" VARCHAR(2),
  "lotl_url" VARCHAR(1024),
  "tsl_url" VARCHAR(1024),
  "trust_list_key" VARCHAR(255),
  "active" BOOLEAN NOT NULL DEFAULT TRUE,
  "last_synchronized_at" TIMESTAMPTZ,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT "trust_provider_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "idx_trust_provider_code" ON "trust_provider"("provider_code");
CREATE INDEX "idx_trust_provider_provider_type" ON "trust_provider"("provider_type");
CREATE INDEX "idx_trust_provider_active" ON "trust_provider"("active");

-- Enterprise credential binding for X.509 login and signing use-cases
CREATE TABLE "certificate_credential" (
  "id" BIGSERIAL NOT NULL,
  "user_id" BIGINT NOT NULL,
  "provider_id" BIGINT,
  "provider_code" VARCHAR(80),
  "credential_type" VARCHAR(80) NOT NULL,
  "trust_status" VARCHAR(80) NOT NULL,
  "revocation_status" VARCHAR(80) NOT NULL,
  "assurance_level" VARCHAR(80),
  "registration_source" VARCHAR(80) NOT NULL,
  "external_subject_id" VARCHAR(255),
  "linked_identity_source" VARCHAR(120),
  "certificate_pem" TEXT NOT NULL,
  "subject_dn" VARCHAR(1024) NOT NULL,
  "issuer_dn" VARCHAR(1024) NOT NULL,
  "serial_number" VARCHAR(128) NOT NULL,
  "thumbprint_sha256" VARCHAR(64) NOT NULL UNIQUE,
  "policy_oids" TEXT,
  "qc_statement_flags" TEXT,
  "key_usage_flags" TEXT,
  "extended_key_usage_flags" TEXT,
  "not_before" TIMESTAMPTZ NOT NULL,
  "not_after" TIMESTAMPTZ NOT NULL,
  "trust_checked_at" TIMESTAMPTZ,
  "revocation_checked_at" TIMESTAMPTZ,
  "approved_by" VARCHAR(120),
  "approved_at" TIMESTAMPTZ,
  "revoked_by" VARCHAR(120),
  "revoked_at" TIMESTAMPTZ,
  "is_primary" BOOLEAN NOT NULL DEFAULT FALSE,
  "last_successful_auth_at" TIMESTAMPTZ,
  "last_failed_auth_at" TIMESTAMPTZ,
  "failure_count" INTEGER NOT NULL DEFAULT 0,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT "certificate_credential_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "certificate_credential_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
  CONSTRAINT "certificate_credential_provider_id_fkey" FOREIGN KEY ("provider_id") REFERENCES "trust_provider"("id") ON DELETE SET NULL
);

CREATE INDEX "idx_certificate_credential_user_id" ON "certificate_credential"("user_id");
CREATE INDEX "idx_certificate_credential_provider_id" ON "certificate_credential"("provider_id");
CREATE INDEX "idx_certificate_credential_provider_code" ON "certificate_credential"("provider_code");
CREATE INDEX "idx_certificate_credential_trust_status" ON "certificate_credential"("trust_status");
CREATE INDEX "idx_certificate_credential_revocation_status" ON "certificate_credential"("revocation_status");
CREATE INDEX "idx_certificate_credential_thumbprint" ON "certificate_credential"("thumbprint_sha256");

-- Enrollment and approval workflow for certificate onboarding
CREATE TABLE "certificate_enrollment" (
  "id" BIGSERIAL NOT NULL,
  "user_id" BIGINT,
  "certificate_credential_id" BIGINT,
  "provider_code" VARCHAR(80),
  "requested_by" VARCHAR(120) NOT NULL,
  "status" VARCHAR(80) NOT NULL,
  "registration_source" VARCHAR(80) NOT NULL,
  "request_payload" JSON NOT NULL DEFAULT '{}',
  "review_notes" TEXT,
  "approved_by" VARCHAR(120),
  "approved_at" TIMESTAMPTZ,
  "rejected_by" VARCHAR(120),
  "rejected_at" TIMESTAMPTZ,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT "certificate_enrollment_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "certificate_enrollment_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE SET NULL,
  CONSTRAINT "certificate_enrollment_certificate_credential_id_fkey" FOREIGN KEY ("certificate_credential_id") REFERENCES "certificate_credential"("id") ON DELETE SET NULL
);

CREATE INDEX "idx_certificate_enrollment_user_id" ON "certificate_enrollment"("user_id");
CREATE INDEX "idx_certificate_enrollment_credential_id" ON "certificate_enrollment"("certificate_credential_id");
CREATE INDEX "idx_certificate_enrollment_status" ON "certificate_enrollment"("status");

-- Linked external identity from eIDAS or another trust broker
CREATE TABLE "external_identity" (
  "id" BIGSERIAL NOT NULL,
  "user_id" BIGINT,
  "provider_id" BIGINT,
  "provider_code" VARCHAR(80) NOT NULL,
  "identity_source" VARCHAR(80) NOT NULL,
  "external_subject_id" VARCHAR(255) NOT NULL,
  "assurance_level" VARCHAR(80),
  "person_identifier" VARCHAR(255),
  "legal_person_identifier" VARCHAR(255),
  "identity_reference" VARCHAR(255),
  "nationality" VARCHAR(80),
  "first_name" VARCHAR(160),
  "family_name" VARCHAR(160),
  "date_of_birth" VARCHAR(40),
  "raw_claims_json" TEXT,
  "current_status" VARCHAR(80) NOT NULL,
  "reviewed_by" VARCHAR(120),
  "reviewed_at" TIMESTAMPTZ,
  "review_reason" TEXT,
  "linked_at" TIMESTAMPTZ,
  "unlinked_at" TIMESTAMPTZ,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT "external_identity_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "external_identity_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE SET NULL,
  CONSTRAINT "external_identity_provider_id_fkey" FOREIGN KEY ("provider_id") REFERENCES "trust_provider"("id") ON DELETE SET NULL,
  CONSTRAINT "uq_external_identity_provider_subject" UNIQUE ("provider_code", "external_subject_id")
);

CREATE INDEX "idx_external_identity_user_id" ON "external_identity"("user_id");
CREATE INDEX "idx_external_identity_provider_id" ON "external_identity"("provider_id");
CREATE INDEX "idx_external_identity_current_status" ON "external_identity"("current_status");
CREATE INDEX "idx_external_identity_reviewed_at" ON "external_identity"("reviewed_at");

-- Immutable high-level audit stream for trust and identity lifecycle events
CREATE TABLE "identity_audit_event" (
  "id" BIGSERIAL NOT NULL,
  "event_type" VARCHAR(120) NOT NULL,
  "actor" VARCHAR(120),
  "user_id" BIGINT,
  "certificate_credential_id" BIGINT,
  "external_identity_id" BIGINT,
  "details" JSON NOT NULL DEFAULT '{}',
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT "identity_audit_event_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "identity_audit_event_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE SET NULL,
  CONSTRAINT "identity_audit_event_certificate_credential_id_fkey" FOREIGN KEY ("certificate_credential_id") REFERENCES "certificate_credential"("id") ON DELETE SET NULL,
  CONSTRAINT "identity_audit_event_external_identity_id_fkey" FOREIGN KEY ("external_identity_id") REFERENCES "external_identity"("id") ON DELETE SET NULL
);

CREATE INDEX "idx_identity_audit_event_event_type" ON "identity_audit_event"("event_type");
CREATE INDEX "idx_identity_audit_event_user_id" ON "identity_audit_event"("user_id");
CREATE INDEX "idx_identity_audit_event_credential_id" ON "identity_audit_event"("certificate_credential_id");
CREATE INDEX "idx_identity_audit_event_external_identity_id" ON "identity_audit_event"("external_identity_id");

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
