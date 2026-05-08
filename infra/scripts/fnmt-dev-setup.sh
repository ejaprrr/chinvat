#!/bin/sh
set -eu

if [ "$#" -lt 1 ]; then
  echo "Usage: $0 <user-email> [client-name]" >&2
  exit 1
fi

USER_EMAIL="$1"
CLIENT_NAME="${2:-fnmt-dev-client}"

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
INFRA_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
MTLS_DIR="$INFRA_DIR/docker/nginx/certs/dev/mtls"
CA_CERT="$MTLS_DIR/ca.crt"
CA_KEY="$MTLS_DIR/ca.key"
CLIENT_KEY="$MTLS_DIR/${CLIENT_NAME}.key"
CLIENT_CSR="$MTLS_DIR/${CLIENT_NAME}.csr"
CLIENT_CERT="$MTLS_DIR/${CLIENT_NAME}.crt"
SERIAL_FILE="$MTLS_DIR/ca.srl"

mkdir -p "$MTLS_DIR"

if [ ! -f "$CA_CERT" ] || [ ! -f "$CA_KEY" ]; then
  openssl req -x509 -nodes -newkey rsa:2048 \
    -keyout "$CA_KEY" \
    -out "$CA_CERT" \
    -days 365 \
    -subj "/CN=chinvat-local-ca" \
    -addext "basicConstraints=critical,CA:TRUE" \
    -addext "keyUsage=critical,keyCertSign,cRLSign"
fi

CLIENT_EXT="$MTLS_DIR/${CLIENT_NAME}.ext"
cat > "$CLIENT_EXT" <<'EOF'
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature,keyEncipherment
extendedKeyUsage=clientAuth
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl req -new -nodes -newkey rsa:2048 \
  -keyout "$CLIENT_KEY" \
  -out "$CLIENT_CSR" \
  -subj "/CN=${CLIENT_NAME}/emailAddress=${USER_EMAIL}/O=Chinvat Dev/OU=FNMT Dev"

openssl x509 -req \
  -in "$CLIENT_CSR" \
  -CA "$CA_CERT" \
  -CAkey "$CA_KEY" \
  -CAcreateserial \
  -CAserial "$SERIAL_FILE" \
  -out "$CLIENT_CERT" \
  -days 365 \
  -sha256 \
  -extfile "$CLIENT_EXT"

"$SCRIPT_DIR/fnmt-register-cert.sh" dev "$USER_EMAIL" "$CLIENT_CERT"

echo
echo "Dev FNMT certificate ready:"
echo "  cert: $CLIENT_CERT"
echo "  key:  $CLIENT_KEY"
echo
echo "Test with:"
echo "  curl --cert $CLIENT_CERT --key $CLIENT_KEY --cacert $CA_CERT https://localhost:8443/api/v1/auth/fnmt/login -X POST"