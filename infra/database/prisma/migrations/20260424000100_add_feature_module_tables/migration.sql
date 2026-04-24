-- Align DB schema with current feature modules (users/rbac) used by backend app.

CREATE TABLE IF NOT EXISTS "users_account" (
  "id" BIGSERIAL NOT NULL,
  "email" VARCHAR(255) NOT NULL,
  "display_name" VARCHAR(120) NOT NULL,
  "password_hash" VARCHAR(255) NOT NULL,
  "roles_csv" VARCHAR(255) NOT NULL,
  "is_active" BOOLEAN NOT NULL,
  "created_at" TIMESTAMPTZ NOT NULL,

  CONSTRAINT "users_account_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX IF NOT EXISTS "users_account_email_key"
ON "users_account"("email");

CREATE TABLE IF NOT EXISTS "rbac_role" (
  "id" BIGSERIAL NOT NULL,
  "role_name" VARCHAR(80) NOT NULL,
  "permissions_csv" VARCHAR(2000) NOT NULL,

  CONSTRAINT "rbac_role_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX IF NOT EXISTS "rbac_role_role_name_key"
ON "rbac_role"("role_name");
