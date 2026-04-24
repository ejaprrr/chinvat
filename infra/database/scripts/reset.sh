#!/bin/sh
# reset.sh – DROP + recreate schema, then run migrations.
# !! DEV ONLY – never call this in production !!
set -eu

if [ "${ALLOW_DB_RESET:-false}" != "true" ]; then
  echo "ERROR: DB reset is disabled. Set ALLOW_DB_RESET=true only in dev environments." >&2
  exit 1
fi

until pg_isready -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB"; do
  sleep 2
done

echo "==> Resetting database '${POSTGRES_DB}' on ${POSTGRES_HOST}..."

PGPASSWORD="$POSTGRES_PASSWORD" psql \
  -v ON_ERROR_STOP=1 \
  -h "$POSTGRES_HOST" \
  -p "$POSTGRES_PORT" \
  -U "$POSTGRES_USER" \
  -d "$POSTGRES_DB" \
  -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

echo "==> Schema dropped and recreated. Migrations will run next."

