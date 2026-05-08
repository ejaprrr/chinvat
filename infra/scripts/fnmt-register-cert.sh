#!/bin/sh
set -eu

if [ "$#" -lt 3 ]; then
  echo "Usage: $0 <dev|prod> <user-email> <certificate-path>" >&2
  exit 1
fi

ENV_NAME="$1"
USER_EMAIL="$2"
CERT_PATH="$3"

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
INFRA_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
ENV_FILE="$INFRA_DIR/docker/.env.${ENV_NAME}.example"
COMPOSE_FILE="$INFRA_DIR/docker/compose.${ENV_NAME}.yml"

if [ ! -f "$ENV_FILE" ] || [ ! -f "$COMPOSE_FILE" ]; then
  echo "Unknown environment: $ENV_NAME" >&2
  exit 1
fi

if [ ! -f "$CERT_PATH" ]; then
  echo "Certificate not found: $CERT_PATH" >&2
  exit 1
fi

POSTGRES_DB=$(grep '^POSTGRES_DB=' "$ENV_FILE" | cut -d= -f2-)
POSTGRES_USER=$(grep '^POSTGRES_USER=' "$ENV_FILE" | cut -d= -f2-)

subject_dn=$(openssl x509 -in "$CERT_PATH" -noout -subject -nameopt RFC2253 | sed 's/^subject=//')
issuer_dn=$(openssl x509 -in "$CERT_PATH" -noout -issuer -nameopt RFC2253 | sed 's/^issuer=//')
serial_number=$(openssl x509 -in "$CERT_PATH" -noout -serial | sed 's/^serial=//' | tr '[:lower:]' '[:upper:]')
not_before=$(openssl x509 -in "$CERT_PATH" -noout -startdate | sed 's/^notBefore=//')
not_after=$(openssl x509 -in "$CERT_PATH" -noout -enddate | sed 's/^notAfter=//')
thumbprint_sha256=$(openssl x509 -in "$CERT_PATH" -outform der | openssl dgst -sha256 -binary | xxd -p -c 256 | tr '[:lower:]' '[:upper:]')

escape_sql() {
  printf "%s" "$1" | sed "s/'/''/g"
}

subject_dn_sql=$(escape_sql "$subject_dn")
issuer_dn_sql=$(escape_sql "$issuer_dn")
serial_number_sql=$(escape_sql "$serial_number")
thumbprint_sql=$(escape_sql "$thumbprint_sha256")
email_sql=$(escape_sql "$USER_EMAIL")

user_id=$(
  cd "$INFRA_DIR" &&
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
      psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tA \
      -c "SELECT id FROM \"user\" WHERE lower(email) = lower('$email_sql') LIMIT 1"
)

user_id=$(printf "%s" "$user_id" | tr -d '[:space:]')
if [ -z "$user_id" ]; then
  echo "User not found for email: $USER_EMAIL" >&2
  exit 1
fi

sql="
UPDATE \"user_certificate\"
SET \"revoked_at\" = NOW()
WHERE \"user_id\" = $user_id AND \"revoked_at\" IS NULL;

INSERT INTO \"user_certificate\" (
  \"user_id\", \"subject_dn\", \"issuer_dn\", \"serial_number\", \"thumbprint_sha256\", \"not_before\", \"not_after\", \"created_at\"
) VALUES (
  $user_id,
  '$subject_dn_sql',
  '$issuer_dn_sql',
  '$serial_number_sql',
  '$thumbprint_sql',
  '$not_before',
  '$not_after',
  NOW()
)
ON CONFLICT (\"thumbprint_sha256\") DO UPDATE SET
  \"user_id\" = EXCLUDED.\"user_id\",
  \"subject_dn\" = EXCLUDED.\"subject_dn\",
  \"issuer_dn\" = EXCLUDED.\"issuer_dn\",
  \"serial_number\" = EXCLUDED.\"serial_number\",
  \"not_before\" = EXCLUDED.\"not_before\",
  \"not_after\" = EXCLUDED.\"not_after\",
  \"revoked_at\" = NULL;
"

cd "$INFRA_DIR"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -c "$sql"

echo "Registered certificate thumbprint $thumbprint_sha256 for $USER_EMAIL (user_id=$user_id)"
echo "Certificate: $CERT_PATH"