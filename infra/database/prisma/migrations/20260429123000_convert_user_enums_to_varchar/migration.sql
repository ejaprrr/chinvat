-- Convert enum-backed user columns to VARCHAR so JPA EnumType.STRING binds work consistently.
ALTER TABLE "user"
  ALTER COLUMN "user_type" TYPE VARCHAR(20) USING "user_type"::text,
  ALTER COLUMN "access_level" TYPE VARCHAR(20) USING "access_level"::text;

DROP TYPE IF EXISTS "user_type";
DROP TYPE IF EXISTS "access_level";
