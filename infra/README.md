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

### FNMT / mTLS login architecture

The production and development gateway now support certificate login on:

- `POST /api/v1/auth/fnmt/login`

Security model:

1. NGINX terminates TLS and validates the presented client certificate against the CA bundle mounted in `infra/docker/nginx/certs/<env>/mtls/ca.crt`.
2. Only the dedicated FNMT login route receives forwarded certificate headers.
3. NGINX also forwards a shared secret header (`X-Internal-Proxy-Auth`) so the backend trusts certificate headers only from the gateway.
4. The backend parses the forwarded PEM certificate, computes its SHA-256 fingerprint, and looks it up in `user_certificate`.
5. If the certificate is active, not revoked, within validity, and bound to an active user, the backend issues the standard access and refresh tokens.

What is stored in the database:

- `user_certificate.thumbprint_sha256` is the authoritative binding for login.
- subject, issuer, serial, and validity window are stored for traceability and operational checks.
- PostgreSQL remains the source of truth; Redis is only permission cache and is not used for certificate identity.

### Development testing

Start the stack:

```bash
cd /Users/jiriposavad/Documents/FullStack/chinvat-malaga-2026/infra
make dev-up
```

Generate and register a dev client certificate for an existing seeded user:

```bash
./scripts/fnmt-dev-setup.sh superadmin@chinvat.dev
```

The script:

1. creates a client certificate signed by the dev CA in `infra/docker/nginx/certs/dev/mtls`
2. computes its SHA-256 fingerprint
3. revokes any previously active certificate rows for that user
4. inserts or updates the `user_certificate` row in PostgreSQL

Call the FNMT login endpoint:

```bash
curl \
	--cert infra/docker/nginx/certs/dev/mtls/fnmt-dev-client.crt \
	--key infra/docker/nginx/certs/dev/mtls/fnmt-dev-client.key \
	--cacert infra/docker/nginx/certs/dev/mtls/ca.crt \
	-X POST \
	https://localhost:8443/api/v1/auth/fnmt/login
```

Expected result:

- `200 OK` with the same token payload as the standard login endpoint.

Negative verification checks:

1. call the endpoint without `--cert/--key`: expect `401`
2. call the endpoint with an unregistered cert: expect `401`
3. revoke the row in `user_certificate` and retry: expect `401`
4. call standard endpoints with spoofed `X-SSL-Client-*` headers directly: they are stripped by NGINX and the backend rejects the FNMT flow without the shared secret

### Production setup

For production:

1. mount the real server certificate and key in `infra/docker/nginx/certs/prod/tls`
2. mount the FNMT client CA bundle in `infra/docker/nginx/certs/prod/mtls/ca.crt`
3. set a strong random value for `FNMT_PROXY_SHARED_SECRET` in `infra/docker/.env.prod.example` or the real prod env file
4. register each approved client certificate in `user_certificate` using:

```bash
./scripts/fnmt-register-cert.sh prod user@example.com /absolute/path/to/client-cert.crt
```

Operational rules:

1. keep backend reachable only on the internal Docker network; expose only NGINX
2. rotate `FNMT_PROXY_SHARED_SECRET` with the same discipline as any internal service credential
3. treat CA bundle changes as security changes and deploy them through change control
4. revoke compromised certificates by setting `revoked_at` in `user_certificate`

## eIDAS State Redis Hardening

The eIDAS callback state is short-lived, security-sensitive data and should be isolated from
general caches (for example permission cache).

Current setup in this repository:

1. Dedicated Redis service `redis-eidas` in both `compose.dev.yml` and `compose.prod.yml`
2. Separate app connection settings via `EIDAS_STATE_REDIS_*`
3. Dedicated key prefix (`chinvat:eidas:state:`) and one-key delete operations only

Recommended production hardening:

1. Enable Redis ACL and create a dedicated user for eIDAS state (`EIDAS_STATE_REDIS_USERNAME` / `EIDAS_STATE_REDIS_PASSWORD`)
2. Restrict ACL permissions to the eIDAS prefix only (`~chinvat:eidas:state:*`) and required commands only (`+get +set +del +expire +ping`)
3. Keep `redis-eidas` private (no published ports), reachable only from backend network segment
4. Use independent credentials from the general Redis cache instance
5. Add monitoring/alerts for unexpected keyspace patterns and high delete/set anomaly rates

Example ACL profile for eIDAS state user:

```text
user chinvat-eidas-state on >strong-password ~chinvat:eidas:state:* +get +set +del +expire +ping -@all
```

The application intentionally avoids wildcard deletes in the eIDAS state adapter.

