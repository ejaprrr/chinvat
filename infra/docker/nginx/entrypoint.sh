#!/bin/sh
set -eu

TLS_DIR=$(dirname "${TLS_CERT_PATH:-/etc/nginx/tls/server.crt}")
MTLS_DIR=$(dirname "${MTLS_CA_PATH:-/etc/nginx/mtls/ca.crt}")
TLS_CERT_PATH=${TLS_CERT_PATH:-/etc/nginx/tls/server.crt}
TLS_KEY_PATH=${TLS_KEY_PATH:-/etc/nginx/tls/server.key}
MTLS_CA_PATH=${MTLS_CA_PATH:-/etc/nginx/mtls/ca.crt}
UPSTREAM_HOST=${UPSTREAM_HOST:-app}
UPSTREAM_PORT=${UPSTREAM_PORT:-8080}
MTLS_VERIFY_MODE=${MTLS_VERIFY_MODE:-optional}
HTTPS_REDIRECT_PORT=${HTTPS_REDIRECT_PORT:-8443}

mkdir -p "$TLS_DIR" "$MTLS_DIR"

if [ ! -f "$TLS_CERT_PATH" ] || [ ! -f "$TLS_KEY_PATH" ]; then
  openssl req -x509 -nodes -newkey rsa:2048 \
    -keyout "$TLS_KEY_PATH" \
    -out "$TLS_CERT_PATH" \
    -days 365 \
    -subj "/CN=localhost"
fi

if [ ! -f "$MTLS_CA_PATH" ]; then
  openssl req -x509 -nodes -newkey rsa:2048 \
    -keyout "$MTLS_DIR/ca.key" \
    -out "$MTLS_CA_PATH" \
    -days 365 \
    -subj "/CN=chinvat-local-ca"
fi

envsubst '${UPSTREAM_HOST} ${UPSTREAM_PORT} ${MTLS_VERIFY_MODE} ${TLS_CERT_PATH} ${TLS_KEY_PATH} ${MTLS_CA_PATH} ${HTTPS_REDIRECT_PORT}' \
  < /etc/nginx/templates/gateway.conf.template \
  > /etc/nginx/conf.d/default.conf

exec "$@"

