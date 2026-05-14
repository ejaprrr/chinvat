# CHINVAT — Technical Documentation

> Java 21 · Spring Boot 4 · React 19 · PostgreSQL 16 · Redis 7  
> Developed during internship placement · Málaga 2026

---

## Project Background

Chinvat was developed as part of a 4-week internship assignment. The goal was to build a **reusable identity verification module** for use in Spanish-market applications — one that integrates the national certificate infrastructure (FNMT) and the Spanish state identity platform (Cl@ve / eIDAS), and that can be dropped into future projects without rebuilding the security layer from scratch.

**Key decisions made during the first week:**
- Java + Spring Boot selected as primary technology after comparing implementation approaches against project requirements
- Modular Maven architecture chosen so each layer is independently testable and replaceable
- Post-quantum cryptographic algorithm compatibility built into the architecture from day one — avoids fundamental rewrites when quantum-safe standards become mandatory
- Dual Redis instance design (permissions vs. auth data) decided early to enforce security separation at the infrastructure level, not just at the code level
- All infrastructure (Docker, DB migrations, CI/CD, seeds) set up in week 1 so the entire team could work from a stable base immediately

**Development timeline:**

| Week | Focus |
|---|---|
| 20–24 Apr | Architecture analysis, technology selection, modular project structure, Docker + DB infrastructure, containerisation |
| 27–29 Apr | User module (CRUD + validation), Swagger API docs, login/registration/password reset, FNMT + eIDAS analysis |
| 30 Apr – 8 May | eIDAS Cl@ve 2.0 integration, mTLS certificates, RBAC, audit log, dual Redis architecture |
| 9–14 May | Finalisation, documentation, test coverage, production readiness |

---

## What Is Chinvat

Chinvat is an **authentication and identity service**. It can run in two modes:

- **Standalone service** — other services call its REST API to validate tokens and check permissions
- **Modular backend** — embed the Maven modules (`auth`, `rbac`, `trust`, `eidas`) directly into your Spring Boot app

It handles: password login, mTLS certificate login, eIDAS federation (Cl@ve 2.0), JWT sessions, RBAC, X.509 certificate validation, and user profile management.

---

## Architecture

```
Browser
  │ HTTPS
  ▼
NGINX (infra/docker/nginx/)
  ├── /api/*  → Spring Boot :8080
  └── /*      → React frontend

Spring Boot (7 Maven modules)
  ├── common   — shared Redis, JPA, audit, pagination
  ├── users    — user accounts and credentials
  ├── rbac     — roles and permissions
  ├── auth     — login, JWT, sessions, password reset
  ├── trust    — X.509 / TSL certificate validation
  ├── eidas    — eIDAS federation gateway (Cl@ve 2.0)
  └── app      — entry point, security config, wires everything

PostgreSQL 16   — all persistent data
redis-global    — permissions cache (readable by other services)
redis-auth      — app cache + eIDAS state (private to this service)
```

---

## Connecting Other Services

### Option 1 — Read permissions from redis-global (recommended)

The RBAC permissions for every logged-in user are cached in `redis-global` under:

```
KEY  chinvat:permissions:{userId}
TYPE string (JSON)
TTL  15 minutes
```

Example value:
```json
["user:read", "cert:upload", "cert:validate", "user:read:self"]
```

Your service connects to `redis-global` with the **`global-readonly`** ACL user (created by `infra/scripts/redis-acl-setup.sh`). This user has read-only access to `chinvat:permissions:*` and `chinvat:org:*` and nothing else.

```bash
# What the ACL script creates for you:
# global-readonly: GET/MGET only on chinvat:permissions:* and chinvat:org:*
./infra/scripts/redis-acl-setup.sh <host> <port> <admin-password>
```

Integration in your service (any language):

```python
# Python example
import redis, json

r = redis.Redis(host="redis-global", port=6379,
                username="global-readonly", password="...")

def has_permission(user_id: str, permission: str) -> bool:
    raw = r.get(f"chinvat:permissions:{user_id}")
    if not raw:
        return False  # cache miss → deny or call Chinvat API
    return permission in json.loads(raw)
```

```typescript
// Node.js example
import { createClient } from 'redis'

const redis = createClient({ url: 'redis://global-readonly:pass@redis-global:6379' })

async function hasPermission(userId: string, permission: string): Promise<boolean> {
  const raw = await redis.get(`chinvat:permissions:${userId}`)
  if (!raw) return false
  return JSON.parse(raw).includes(permission)
}
```

> **Cache miss** means the user either isn't logged in or the cache expired. On miss you can either deny the request or call `GET /api/v1/auth/me` with the user's Bearer token to get current info.

---

### Option 2 — Validate JWT tokens via REST API

If your service receives a Bearer token from the frontend:

```
GET /api/v1/auth/me
Authorization: Bearer <token>

→ 200 { id, username, fullName, email, roles, permissions }
→ 401 if token is invalid or session revoked
```

This is the simplest integration — just forward the token and check the response.

---

### Option 3 — Embed as Maven modules

Add the modules as dependencies to your Spring Boot app's `pom.xml`. The `common` module provides shared Redis and JPA config. Pull in `rbac` for permission management, `auth` for JWT validation, etc.

This mode is for cases where you want Chinvat's functionality in your own service without a separate HTTP hop.

---

## What's in infra/

Everything needed to run the full stack is in `infra/`. You don't need to configure anything manually.

```
infra/
├── docker/
│   ├── compose.dev.yml          — full dev stack (DB, Redis ×2, backend, frontend, nginx)
│   ├── compose.prod.yml         — production stack (hardened, no reset service, ACL enabled)
│   ├── .env.dev.example         — copy to /.env for dev
│   ├── .env.prod.example        — template for production secrets
│   └── nginx/
│       ├── Dockerfile           — NGINX image build
│       ├── templates/gateway.conf.template   — proxy config (TLS, mTLS, API routing)
│       ├── entrypoint.sh        — envsubst at startup (no rebuild needed for config changes)
│       └── certs/
│           ├── dev/             — self-signed TLS + mTLS CA certs for local dev (committed)
│           └── prod/            — .gitkeep only (you supply real certs)
├── database/
│   ├── prisma/schema.prisma     — single source of truth for the DB schema
│   ├── prisma/migrations/       — all SQL migrations in order
│   ├── seeds/dev/001_seed.sql   — dev seed (roles, permissions, test users)
│   ├── seeds/prod/001_seed.sql  — prod seed (minimal, idempotent)
│   └── scripts/
│       ├── reset.sh             — drops public schema (DEV ONLY, gated by ALLOW_DB_RESET=true)
│       └── seed.sh              — runs a SQL file via psql
├── redis/
│   ├── README.md                — Redis config guide
│   └── SECURITY.md              — production hardening checklist
├── scripts/
│   ├── redis-acl-setup.sh       — creates ACL users (eidas-app, global-readonly, monitoring)
│   ├── fnmt-dev-setup.sh        — generates dev mTLS client certificate for FNMT testing
│   └── fnmt-register-cert.sh    — registers a cert against the API
└── qodana/
    └── qodana.yaml              — JetBrains static analysis config
```

### Quick start

```bash
cp infra/docker/.env.dev.example .env
make dev-up
# Frontend: http://localhost:3000
# Backend:  http://localhost:8080
# Swagger:  http://localhost:8080/swagger-ui.html
```

---

## Dual Redis — Why Two Instances

| | redis-global :6379 | redis-auth :6380 |
|---|---|---|
| **Stores** | Permission cache | App cache + eIDAS state |
| **Access** | Read-only for app services | Read-write, Chinvat only |
| **ACL user for other services** | `global-readonly` | none (private) |
| **Key pattern** | `chinvat:permissions:{userId}` | `chinvat:eidas:state:{token}` (DB1) |
| **TTL** | 15 min | 10 min (eIDAS), 5–30 min (cache) |

The separation means: a compromised credential for `redis-global` cannot write eIDAS state tokens. A service reading permissions from `redis-global` cannot touch session data in `redis-auth`.

In dev, both Redis instances run without passwords and `APP_CACHE_REDIS_DEDICATED=false` (so the app just uses one connection). In production, flip `APP_CACHE_REDIS_DEDICATED=true` and `EIDAS_STATE_REDIS_DEDICATED=true` and provide passwords.

---

## eIDAS Flow (Cl@ve 2.0)

```
1. POST /api/v1/auth/eidas/login  { providerCode: "CLAVE_ES" }
   → backend generates state token → saves to redis-auth DB1 (TTL 10min)
   ← { authUrl: "https://clave2.gob.es/...", state: "..." }

2. Frontend redirects user to authUrl
   User authenticates with DNI/NIE on Cl@ve portal

3. Cl@ve redirects back: GET /eidas/callback?code=...&state=...
   Frontend POSTs: /api/v1/auth/eidas/callback  { providerCode, code, state }
   → backend validates state (consumed from Redis, single-use)
   → exchanges code for OIDC token
   → saves ExternalIdentity to DB
   ← { externalSubjectId, firstName, lastName, email, certificate, action }

4. POST /api/v1/profile/eidas/complete  { externalSubjectId, username, password, certificatePem, ... }
   → creates user account + links eIDAS identity + validates and binds certificate
   ← { userId, status: "LINKED", linkedAt, completedAt }
```

**Enable Cl@ve:** add `clave` to `SPRING_PROFILES_ACTIVE`. Configure `application-clave.properties` with issuer, JWKS URI, client-id, client-secret, redirect URI.

---

## API — Key Endpoints

**Public:**
```
POST /api/v1/auth/login                    password login
POST /api/v1/auth/certificates/login       mTLS login
POST /api/v1/auth/refresh                  rotate JWT pair
POST /api/v1/auth/register                 create account
POST /api/v1/auth/eidas/login              start eIDAS
POST /api/v1/auth/eidas/callback           handle eIDAS callback
POST /api/v1/profile/eidas/complete        complete profile post-eIDAS
```

**Authenticated (Bearer token):**
```
GET  /api/v1/auth/me                       current user + permissions
GET  /api/v1/auth/sessions                 active sessions
POST /api/v1/auth/logout                   revoke session
GET  /api/v1/profile/certificates          list own certificates
POST /api/v1/profile/certificates          upload + validate certificate
GET  /api/v1/rbac/permissions              list permissions (admin)
```

---

## NGINX and mTLS

NGINX handles TLS termination and forwards mTLS client certificates as the `X-Client-Certificate` header (URL-encoded PEM). The backend reads this header to authenticate certificate logins — the backend itself never terminates TLS.

For the FNMT proxy flow (`/api/v1/auth/fnmt/login`), NGINX additionally enforces `ssl_verify_client SUCCESS` before forwarding, and injects `X-Internal-Proxy-Auth` so the backend knows the certificate was already verified by NGINX.

Dev certificates (self-signed CA + server cert) are committed in `infra/docker/nginx/certs/dev/`. For production, place real certs in `infra/docker/nginx/certs/prod/` (the `.gitkeep` files are placeholders).

---

## Outstanding Work

### Must do before production

| Item | Where |
|---|---|
| Run `redis-acl-setup.sh`, set `APP_CACHE_REDIS_DEDICATED=true` and `EIDAS_STATE_REDIS_DEDICATED=true` | `infra/scripts/` |
| Register Chinvat redirect URI with Cl@ve 2.0 portal (MPTFP Spain) | eIDAS module |
| Supply real TLS + mTLS CA certs in `infra/docker/nginx/certs/prod/` | nginx |
| Set `CLAVE_CLIENT_SECRET` in production secrets | eIDAS module |
| Wire up email delivery for password reset (no SMTP configured yet) | auth module |

### eIDAS — not yet done

| Item | Notes |
|---|---|
| "Link to existing account" flow | `action: LINK_EXISTING_USER` is returned but the frontend flow is not implemented |
| Assurance level → access_level mapping | LOW / SUBSTANTIAL / HIGH stored but not enforced |
| End-to-end test with DE, FR, IT, PT national eIDs | Cl@ve federates them but untested |
| SAML 2.0 broker adapter | `HttpEidasBrokerAdapter` is generic HTTP; OpenSAML adapter needed for non-OIDC providers |

---

## Developer Quickstart

```bash
# Prerequisites: Docker, Make, Java 21 (for local backend dev)

cp infra/docker/.env.dev.example .env
make dev-up          # starts postgres, redis ×2, backend, frontend, nginx

make backend-fmt     # apply Google Java Format (required before commit)
make backend-test    # run all tests
make db-reset        # DEV ONLY: drop schema + migrate + seed
make redis-flush     # DEV ONLY: clear Redis

# Backend debug port: 5005 (connect with IntelliJ Remote JVM Debug)
```

### Key env vars

```bash
# Minimal for dev (already in .env.dev.example)
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:postgresql://postgres:5432/chinvat
REDIS_HOST=redis-global
APP_CACHE_REDIS_DEDICATED=false     # use redis-global for cache too in dev
EIDAS_STATE_REDIS_DEDICATED=false

# Production additions
APP_CACHE_REDIS_DEDICATED=true
APP_CACHE_REDIS_HOST=redis-auth
APP_CACHE_REDIS_DATABASE=0
EIDAS_STATE_REDIS_DEDICATED=true
EIDAS_STATE_REDIS_HOST=redis-auth
EIDAS_STATE_REDIS_DATABASE=1
SPRING_PROFILES_ACTIVE=prod,clave
CLAVE_CLIENT_SECRET=<from secrets manager>
```
