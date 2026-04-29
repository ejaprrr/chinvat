# Infrastructure

## Layout

- `docker/` - container images, Compose stacks, NGINX gateway, environment templates
- `database/` - migrations, seed scripts, database jobs
- `qodana/` - Qodana configuration

## Quick Start

```bash
cd /Users/jiriposavad/Documents/FullStack/chinvat-malaga-2026
make infra-dev-up
```

Compose defaults come from:

- `infra/docker/.env.dev.example`
- `infra/docker/.env.prod.example`

## Stack Order

1. PostgreSQL starts
2. Migration job runs and exits
3. Seed job runs and exits
4. Backend starts
5. NGINX starts

## TLS and mTLS

NGINX exposes:

- HTTP on `8080` (redirect-only to HTTPS)
- HTTPS on `8443` (primary entrypoint)

Use HTTPS for local API calls:

```bash
curl -k https://localhost:8443/api/v1/health
```

The HTTPS gateway is prepared for mTLS with:

- server certificate directory: `infra/docker/nginx/certs/<env>/tls`
- client CA directory: `infra/docker/nginx/certs/<env>/mtls`

If certificates are missing, the container generates local self-signed material for startup.

