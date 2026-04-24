#!/bin/sh
set -eu

SEED_FILE=${1:?seed file is required}

until pg_isready -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB"; do
  sleep 2
done

PGPASSWORD="$POSTGRES_PASSWORD" \
  psql \
  -v ON_ERROR_STOP=1 \
  -h "$POSTGRES_HOST" \
  -p "$POSTGRES_PORT" \
  -U "$POSTGRES_USER" \
  -d "$POSTGRES_DB" \
  -f "$SEED_FILE"

