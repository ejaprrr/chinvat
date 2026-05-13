#!/bin/sh
set -e

# Get the backend upstream URL from environment variable, default to localhost
BACKEND_UPSTREAM_URL="${BACKEND_UPSTREAM_URL:-http://localhost:8080}"

echo "Starting frontend..."
echo "BACKEND_UPSTREAM_URL: $BACKEND_UPSTREAM_URL"

# Process nginx template with environment variables
envsubst '$BACKEND_UPSTREAM_URL' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

# Execute the command
exec "$@"
