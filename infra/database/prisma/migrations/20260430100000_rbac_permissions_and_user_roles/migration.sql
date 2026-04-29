-- Introduce normalized RBAC permissions and explicit user-role assignments.

CREATE TABLE IF NOT EXISTS "rbac_permission" (
  "id" BIGSERIAL NOT NULL,
  "permission_code" VARCHAR(120) NOT NULL,
  "description" VARCHAR(255),
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT "rbac_permission_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX IF NOT EXISTS "rbac_permission_permission_code_key"
ON "rbac_permission"("permission_code");

CREATE TABLE IF NOT EXISTS "rbac_role_permission" (
  "role_id" BIGINT NOT NULL,
  "permission_id" BIGINT NOT NULL,

  CONSTRAINT "rbac_role_permission_pkey" PRIMARY KEY ("role_id", "permission_id"),
  CONSTRAINT "rbac_role_permission_role_id_fkey"
    FOREIGN KEY ("role_id") REFERENCES "rbac_role"("id") ON DELETE CASCADE,
  CONSTRAINT "rbac_role_permission_permission_id_fkey"
    FOREIGN KEY ("permission_id") REFERENCES "rbac_permission"("id") ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS "idx_rbac_role_permission_permission_id"
ON "rbac_role_permission"("permission_id");

CREATE TABLE IF NOT EXISTS "user_role" (
  "user_id" BIGINT NOT NULL,
  "role_id" BIGINT NOT NULL,
  "assigned_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "assigned_by" VARCHAR(120),

  CONSTRAINT "user_role_pkey" PRIMARY KEY ("user_id", "role_id"),
  CONSTRAINT "user_role_user_id_fkey"
    FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
  CONSTRAINT "user_role_role_id_fkey"
    FOREIGN KEY ("role_id") REFERENCES "rbac_role"("id") ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS "idx_user_role_role_id"
ON "user_role"("role_id");

-- Backfill normalized permissions from legacy rbac_role.permissions_csv.
INSERT INTO "rbac_permission" ("permission_code")
SELECT DISTINCT TRIM(permission_code) AS permission_code
FROM "rbac_role" r,
LATERAL regexp_split_to_table(COALESCE(r."permissions_csv", ''), ',') AS permission_code
WHERE TRIM(permission_code) <> ''
ON CONFLICT ("permission_code") DO NOTHING;

INSERT INTO "rbac_role_permission" ("role_id", "permission_id")
SELECT DISTINCT r."id", p."id"
FROM "rbac_role" r
JOIN LATERAL regexp_split_to_table(COALESCE(r."permissions_csv", ''), ',') AS permission_code ON TRUE
JOIN "rbac_permission" p ON p."permission_code" = TRIM(permission_code)
WHERE TRIM(permission_code) <> ''
ON CONFLICT ("role_id", "permission_id") DO NOTHING;

-- Bootstrap explicit role assignments from existing access_level semantics.
INSERT INTO "user_role" ("user_id", "role_id", "assigned_by")
SELECT u."id", r."id", 'migration:access_level'
FROM "user" u
JOIN "rbac_role" r ON r."role_name" =
  CASE
    WHEN u."access_level" IN ('SUPERADMIN', 'ADMIN') THEN u."access_level"
    ELSE 'USER'
  END
ON CONFLICT ("user_id", "role_id") DO NOTHING;
