#!/bin/sh
set -eu

TLS_DIR=$(dirname "${TLS_CERT_PATH:-/etc/nginx/tls/server.crt}")
MTLS_DIR=$(dirname "${MTLS_CA_PATH:-/etc/nginx/mtls/ca.crt}")
TLS_CERT_PATH=${TLS_CERT_PATH:-/etc/nginx/tls/server.crt}
TLS_KEY_PATH=${TLS_KEY_PATH:-/etc/nginx/tls/server.key}
MTLS_CA_PATH=${MTLS_CA_PATH:-/etc/nginx/mtls/ca.crt}
UPSTREAM_HOST=${UPSTREAM_HOST:-app}
UPSTREAM_PORT=${UPSTREAM_PORT:-8080}
FRONTEND_UPSTREAM_HOST=${FRONTEND_UPSTREAM_HOST:-frontend}
FRONTEND_UPSTREAM_PORT=${FRONTEND_UPSTREAM_PORT:-80}
MTLS_VERIFY_MODE=${MTLS_VERIFY_MODE:-optional}
MTLS_PROXY_SHARED_SECRET=${MTLS_PROXY_SHARED_SECRET:-dev-fnmt-proxy-secret}
HTTPS_REDIRECT_PORT=${HTTPS_REDIRECT_PORT:-8443}

mkdir -p "$TLS_DIR" "$MTLS_DIR"

# Ensure CA exists first
if [ ! -f "$MTLS_CA_PATH" ]; then
  openssl req -x509 -nodes -newkey rsa:2048 \
    -keyout "$MTLS_DIR/ca.key" \
    -out "$MTLS_CA_PATH" \
    -days 365 \
    -subj "/CN=chinvat-local-ca" \
    -addext "basicConstraints=critical,CA:TRUE" \
    -addext "keyUsage=critical,keyCertSign,cRLSign"
fi

# Generate server cert signed by CA
if [ ! -f "$TLS_CERT_PATH" ] || [ ! -f "$TLS_KEY_PATH" ]; then
  # Create server CSR
  openssl req -new -nodes -newkey rsa:2048 \
    -keyout "$TLS_KEY_PATH" \
    -out "$MTLS_DIR/server.csr" \
    -subj "/CN=localhost"
  
  # Create server cert extensions file
  cat > "$MTLS_DIR/server.ext" <<'EOF'
subjectAltName=DNS:localhost
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
EOF
  
  # Sign server CSR with CA
  openssl x509 -req \
    -in "$MTLS_DIR/server.csr" \
    -CA "$MTLS_CA_PATH" \
    -CAkey "$MTLS_DIR/ca.key" \
    -CAcreateserial \
    -out "$TLS_CERT_PATH" \
    -days 365 \
    -sha256 \
    -extfile "$MTLS_DIR/server.ext"
fi

envsubst '${UPSTREAM_HOST} ${UPSTREAM_PORT} ${FRONTEND_UPSTREAM_HOST} ${FRONTEND_UPSTREAM_PORT} ${MTLS_VERIFY_MODE} ${MTLS_PROXY_SHARED_SECRET} ${TLS_CERT_PATH} ${TLS_KEY_PATH} ${MTLS_CA_PATH} ${HTTPS_REDIRECT_PORT}' \
  < /etc/nginx/templates/gateway.conf.template \
  > /etc/nginx/conf.d/default.conf

exec "$@"

