# CHINVAT — Enterprise Technical Documentation

> **Version:** 1.0.0-SNAPSHOT  
> **Platform:** Java 21 · Spring Boot 4 · React 19 · PostgreSQL 16 · Redis 7  
> **Classification:** Internal Engineering Reference

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [High-Level Architecture](#2-high-level-architecture)
3. [Backend Module Breakdown](#3-backend-module-breakdown)
   - 3.1 [Common](#31-common)
   - 3.2 [Users](#32-users)
   - 3.3 [RBAC](#33-rbac)
   - 3.4 [Auth](#34-auth)
   - 3.5 [Trust](#35-trust)
   - 3.6 [eIDAS](#36-eidas)
   - 3.7 [App (Entry Point)](#37-app-entry-point)
4. [API Reference](#4-api-reference)
5. [eIDAS Integration — Deep Dive](#5-eidas-integration--deep-dive)
   - 5.1 [Protocol Flow](#51-protocol-flow)
   - 5.2 [State Management](#52-state-management)
   - 5.3 [Cl@ve 2.0 (Spain)](#53-clave-20-spain)
   - 5.4 [Provider Registry](#54-provider-registry)
   - 5.5 [Profile Completion](#55-profile-completion)
   - 5.6 [What Still Needs to Be Done](#56-what-still-needs-to-be-done)
6. [Dual Redis Architecture](#6-dual-redis-architecture)
   - 6.1 [Why Two Instances](#61-why-two-instances)
   - 6.2 [redis-global](#62-redis-global)
   - 6.3 [redis-auth](#63-redis-auth)
   - 6.4 [Database Index Layout](#64-database-index-layout)
   - 6.5 [Java Configuration Classes](#65-java-configuration-classes)
   - 6.6 [Production Hardening](#66-production-hardening)
7. [Authentication Flows](#7-authentication-flows)
   - 7.1 [Password Login](#71-password-login)
   - 7.2 [mTLS Certificate Login](#72-mtls-certificate-login)
   - 7.3 [eIDAS Login](#73-eidas-login)
   - 7.4 [Token Refresh](#74-token-refresh)
8. [Certificate & Trust Infrastructure](#8-certificate--trust-infrastructure)
9. [Role-Based Access Control (RBAC)](#9-role-based-access-control-rbac)
10. [Security Architecture](#10-security-architecture)
11. [Database Schema](#11-database-schema)
12. [Frontend Architecture](#12-frontend-architecture)
13. [Infrastructure & Docker](#13-infrastructure--docker)
14. [CI/CD Pipelines](#14-cicd-pipelines)
15. [Developer Guide](#15-developer-guide)
16. [Configuration Reference](#16-configuration-reference)
17. [Roadmap & Outstanding Work](#17-roadmap--outstanding-work)

---

## 1. Project Overview

**Chinvat** is a production-ready, enterprise-grade authentication and identity management platform. It provides a unified gateway for user login across multiple authentication mechanisms — password, mTLS client certificates, and cross-border eIDAS federation (Cl@ve 2.0 OIDC) — combined with a flexible RBAC authorisation layer, X.509 certificate lifecycle management, and audit-grade session tracking.

The name *Chinvat* refers to the bridge in Zoroastrian cosmology that every soul must cross — only those who authenticate correctly may pass.

### Core Capabilities

| Capability | Description |
|---|---|
| Password authentication | Bcrypt/scrypt hashed passwords, single-use reset tokens |
| mTLS certificate login | Client certificate extracted from `X-Client-Certificate` header (NGINX proxy) |
| eIDAS federation | OIDC-based cross-border login via Cl@ve 2.0 broker (ES, DE, FR, IT, PT) |
| Session management | Database-backed JWT sessions with refresh and per-device revocation |
| RBAC | Roles, permissions, and their assignments cached in Redis |
| X.509 trust validation | sd-dss library with TSL (Trusted Service List) validation |
| Profile management | Self-service certificate upload and profile completion after eIDAS |
| Audit trail | Immutable audit events for every sensitive operation |
| Rate limiting | Redis-backed per-IP/user throttling on all public endpoints |
| Internationalisation | React frontend with i18next, multiple language support |

---

## 2. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT TIER                                    │
│                                                                             │
│   Browser / Native App                                                      │
│      React 19 + TypeScript + Vite + Tailwind CSS                           │
│      React Router 7  ·  Axios  ·  i18next                                  │
└───────────────────────┬────────────────────────────────────────────────────┘
                        │ HTTPS (TLS 1.3)
                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               NGINX PROXY                                   │
│                                                                             │
│   Ports 8080 (HTTP) / 8443 (HTTPS)                                         │
│   TLS termination · mTLS forwarding (X-Client-Certificate header)          │
│   Static frontend serving in production                                     │
└───────────────────────┬────────────────────────────────────────────────────┘
                        │ HTTP (internal Docker network)
                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SPRING BOOT APPLICATION                            │
│                         Java 21  ·  Spring Boot 4.0.5                      │
│                                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────────┐ │
│  │  common  │  │  users   │  │   rbac   │  │   auth   │  │    trust    │ │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └─────────────┘ │
│                                                                             │
│  ┌──────────┐  ┌────────────────────────────────────────────────────────┐  │
│  │  eidas   │  │  app  (SecurityConfig · CacheConfig · GlobalHandlers)  │  │
│  └──────────┘  └────────────────────────────────────────────────────────┘  │
└───┬───────────────────────────────────────────┬───────────────────────────┘
    │                                           │
    ▼                                           ▼
┌───────────────┐                   ┌──────────────────────┐
│  PostgreSQL   │                   │  redis-global :6379   │
│  16           │                   │  Permissions cache    │
│  Prisma ORM   │                   │  (read-only for app)  │
└───────────────┘                   └──────────────────────┘
                                    ┌──────────────────────┐
                                    │  redis-auth   :6380   │
                                    │  DB0: App cache       │
                                    │  DB1: eIDAS state     │
                                    └──────────────────────┘
                                                │
                              ┌─────────────────┘
                              ▼
                    ┌──────────────────────┐
                    │  External eIDAS      │
                    │  Broker (Cl@ve 2.0)  │
                    │  OIDC / SAML         │
                    └──────────────────────┘
```

### Technology Stack Summary

| Layer | Technology | Version |
|---|---|---|
| Backend language | Java | 21 |
| Backend framework | Spring Boot | 4.0.5 |
| Build tool | Apache Maven | 3.9+ |
| Frontend language | TypeScript | 6.0.2 |
| Frontend framework | React | 19.2.5 |
| Frontend bundler | Vite | 8.0.12 |
| CSS framework | Tailwind CSS | 4.2.4 |
| HTTP client | Axios | latest |
| Router | React Router | 7.14.2 |
| Relational DB | PostgreSQL | 16 |
| DB migration | Prisma Migrate | latest |
| Cache/state | Redis | 7 |
| Proxy | NGINX | latest |
| Containerisation | Docker / Compose | latest |
| Cryptography | Bouncy Castle, sd-dss | latest |
| Code style | Google Java Format (Spotless) | — |
| CI/CD | GitHub Actions | — |

---

## 3. Backend Module Breakdown

The backend is a **Maven multi-module project** with seven modules arranged in a strict dependency hierarchy. Modules at a lower level never depend on modules at a higher level.

```
backend/
├── pom.xml          ← parent BOM
├── common/          ← shared infrastructure (no business logic)
├── modules/
│   ├── users/       ← user account model
│   ├── rbac/        ← roles and permissions
│   ├── auth/        ← authentication and session management
│   ├── trust/       ← X.509 / TSL certificate validation
│   └── eidas/       ← eIDAS federation gateway
└── app/             ← Spring Boot entry point, wires all modules
```

Dependency graph (A → B means A depends on B):

```
app → eidas → common
app → auth  → users → common
app → trust → common
app → rbac  → common
```

---

### 3.1 Common

**Path:** `backend/common`

The common module provides the shared infrastructure contracts that all other modules build on. It contains no business logic and no domain entities.

| Class | Role |
|---|---|
| `RedisConfiguration` | Creates the primary `RedisTemplate` and `StringRedisTemplate` beans wired to `redis-global`. Serialises values as JSON (Jackson). |
| `DatabaseConfiguration` | JPA `EntityManagerFactory`, `TransactionManager`, Hibernate dialect configuration. |
| `AuditFacadeService` | Saves `AuditEvent` records for every sensitive operation. Called by upper modules. |
| `RedisPermissionCacheFacade` | Abstraction over direct Redis calls for permission reads. Returns `Optional` and handles cache misses cleanly. |
| `PageResponse<T>` | Generic paginated response DTO used across all list endpoints. |
| `PaginationRequest` | Uniform page/size/sort request parsing. |
| `ApiErrorResponse` | Standardised error envelope `{ code, message, details, timestamp }`. |
| `ApiErrorFactory` | Produces `ApiErrorResponse` from exceptions or explicit codes. |

---

### 3.2 Users

**Path:** `backend/modules/users`

Owns the canonical representation of a user account and all credential types.

| Entity | Table | Description |
|---|---|---|
| `UserAccountJpaEntity` | `user` | Core account: username, full_name, email, phone, address, language, access_level, user_type |
| `UserPasswordJpaEntity` | `user_password` | Bcrypt/scrypt hash of the user's password |
| `UserCertificateJpaEntity` | `user_certificate` | Raw PEM of client certificates permitted for mTLS login |

| Service/Facade | Description |
|---|---|
| `UsersFacadeService` | Exposes `createUser`, `findById`, `findByUsername`, `findByCertificate`, `updateProfile` operations to upper modules. Upper modules must never touch JPA repositories directly. |
| `UsersRepository` | Spring Data JPA repository — private to this module. |

DTOs used in inter-module calls: `CreateUserRequest`, `UserResponse`, `UpdateUserRequest`.

---

### 3.3 RBAC

**Path:** `backend/modules/rbac`

Provides fine-grained role-based authorisation with Redis-backed permission caching.

| Entity | Table | Description |
|---|---|---|
| `RoleJpaEntity` | `role` | Named roles (e.g. `ADMIN`, `OPERATOR`, `USER`) |
| `RbacPermissionJpaEntity` | `permission` | Atomic permissions (e.g. `user:read`, `cert:upload`) |
| `UserRoleJpaEntity` | `user_role` | Many-to-many: users → roles |
| `RbacRolePermissionJpaEntity` | `role_permission` | Many-to-many: roles → permissions |

| Class | Description |
|---|---|
| `RbacFacadeService` | Resolves all effective permissions for a given user (role union). Caches the result in `redis-global` DB0. Cache key: `permissions:{userId}`. TTL: 15 minutes. |
| `BuiltinRolePermissions` | Defines the default set of permissions assigned to each built-in role at seed time. |
| `RbacController` | Exposes CRUD REST endpoints for managing roles, permissions, and assignments. Admin-only. |

Permission caching flow:

```
Request → BearerTokenAuthFilter → JWT parsed → userId extracted
         → RbacFacadeService.getPermissions(userId)
           → Redis GET permissions:{userId}
             HIT  → deserialise and return
             MISS → PostgreSQL query → serialise → Redis SET (TTL 15min) → return
```

---

### 3.4 Auth

**Path:** `backend/modules/auth`

The largest module. Handles every aspect of authentication lifecycle — login, token issuance, refresh, logout, password reset, and session tracking.

| Entity | Table | Description |
|---|---|---|
| `AuthSessionJpaEntity` | `auth_session` | Active sessions: hash of access token, refresh token hash, user ID, expiry, revocation flag, client IP, user agent |
| `AuthPasswordResetJpaEntity` | `auth_password_reset` | Single-use password reset tokens with expiry |

**Use cases (command objects):**

| Use Case | Trigger | Description |
|---|---|---|
| `LoginUseCase` | POST `/auth/login` | Validates credentials, creates session, issues JWT pair |
| `CertificateLoginUseCase` | POST `/auth/certificates/login` | Extracts and validates X.509 from header, creates session |
| `RefreshCommand` | POST `/auth/refresh` | Validates refresh token, rotates JWT pair |
| `LogoutUseCase` | POST `/auth/logout` | Marks session as revoked |
| `RegisterCommand` | POST `/auth/register` | Creates user account + password |
| `RequestPasswordResetCommand` | POST `/auth/password-reset/request` | Issues expiring reset token, triggers email |
| `ConfirmPasswordResetCommand` | POST `/auth/password-reset/confirm` | Validates token (single-use), sets new password |
| `ChangePasswordCommand` | POST `/auth/change-password` | Authenticated password change |

**Infrastructure classes:**

| Class | Description |
|---|---|
| `BearerTokenAuthFilter` | Servlet filter that extracts `Authorization: Bearer <token>`, validates JWT signature and expiry, checks session not revoked, sets `SecurityContext`. |
| `MtlsClientCertificateResolver` | Parses the `X-Client-Certificate` header (URL-encoded PEM forwarded by NGINX) into a `X509Certificate`. |
| `AuthController` | Maps HTTP requests to use cases. Returns `AuthResponse` with `accessToken`, `refreshToken`, `expiresIn`, and embedded user info. |
| `AuthMeController` | Returns current user info from `SecurityContext`. Requires authentication. |
| `AuthSessionsController` | Lists all active sessions for the authenticated user. |

Response models: `AuthResponse`, `AuthMeResponse`, `AuthSessionResponse`.

---

### 3.5 Trust

**Path:** `backend/modules/trust`

Handles X.509 certificate lifecycle: storage, chain validation, TSL (EU Trusted Service List) synchronisation, and credential management.

| Entity | Table | Description |
|---|---|---|
| `CertificateCredentialJpaEntity` | `certificate_credential` | Stored X.509 certificates with subject, issuer, serial, validity dates |

| Class | Description |
|---|---|
| `TrustFacadeService` | Primary entry point. Validates a PEM-encoded certificate against the TSL, checks revocation (CRL/OCSP), and returns a `ValidationReport`. |
| `TslSyncService` | Background job that periodically downloads and caches the EU TSL (Trusted Service List) from the LOTL. Prevents cold-start validation failures. |
| `CertificateCredentialRepository` | JPA repository for stored credentials. Private to this module. |

**Library:** sd-dss (Digital Signature Service) — EU Commission reference implementation for X.509, CAdES, XAdES, and TSL handling. Combined with Bouncy Castle for low-level cryptography.

---

### 3.6 eIDAS

**Path:** `backend/modules/eidas`

Implements the eIDAS cross-border identity federation gateway. Supports OIDC-based flows (Cl@ve 2.0) with a pluggable broker adapter pattern.

See [Section 5](#5-eidas-integration--deep-dive) for the full deep dive.

---

### 3.7 App (Entry Point)

**Path:** `backend/app`

The `app` module is the sole Spring Boot application. It wires all other modules together and provides cross-cutting infrastructure.

| Class | Description |
|---|---|
| `ChinvatApplication` | `@SpringBootApplication` entry point. |
| `SecurityConfig` | Configures the Spring Security filter chain (see [Section 10](#10-security-architecture)). |
| `CacheConfig` | Spring `@EnableCaching` with named caches: `users` (5 min TTL), `trust` (30 min TTL), `eidasProviders` (30 min TTL). |
| `CacheRedisConfiguration` | Conditionally wires app-level caching to `redis-auth` when `APP_CACHE_REDIS_DEDICATED=true`. Falls back to `redis-global` connection for development. |
| `GlobalApiExceptionHandler` | `@RestControllerAdvice` — maps all application exceptions to standardised `ApiErrorResponse` payloads. Never leaks stack traces. |
| `RateLimitingFilter` | Redis-backed sliding window rate limiter. Applied to login, password-reset, and eIDAS endpoints. |
| `ProfileController` | Handles post-eIDAS profile completion and certificate self-management. |

---

## 4. API Reference

All endpoints are served under the base path `/api/v1`. The API is JSON-only (`Content-Type: application/json`). JWT authentication uses `Authorization: Bearer <token>`.

### Authentication (`/api/v1/auth`)

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/login` | Public | Password-based login |
| `POST` | `/certificates/login` | Public | mTLS certificate login |
| `POST` | `/fnmt/login` | Public | FNMT proxy login |
| `POST` | `/refresh` | Public | Rotate JWT pair using refresh token |
| `POST` | `/logout` | Public | Revoke current session |
| `POST` | `/register` | Public | Create new user account |
| `POST` | `/password-reset/request` | Public | Request password reset email |
| `POST` | `/password-reset/confirm` | Public | Confirm reset with token |
| `POST` | `/change-password` | **JWT** | Change password (authenticated) |
| `GET` | `/me` | **JWT** | Current user info |
| `GET` | `/sessions` | **JWT** | List active sessions |

### eIDAS (`/api/v1/auth/eidas`)

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/login` | Public | Initiate eIDAS login — returns `authUrl` + `state` |
| `POST` | `/callback` | Public | Handle broker callback — returns identity + `externalSubjectId` |
| `GET` | `/providers` | Public | List all configured eIDAS providers |
| `GET` | `/providers/paged` | Public | Paginated provider list |

### Profile (`/api/v1/profile`)

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/eidas/complete` | Public* | Complete profile after eIDAS callback |
| `GET` | `/certificates` | **JWT** | List own certificates |
| `POST` | `/certificates` | **JWT** | Upload and validate new certificate |
| `DELETE` | `/certificates/{id}` | **JWT** | Delete own certificate |

> *Public because the user does not have a session yet at this point in the eIDAS flow.

### RBAC (`/api/v1/rbac`)

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/permissions` | **JWT + Admin** | Create permission |
| `GET` | `/permissions` | **JWT + Admin** | List all permissions |
| `GET` | `/permissions/{id}` | **JWT + Admin** | Get single permission |
| `PUT` | `/permissions/{id}` | **JWT + Admin** | Update permission |
| `DELETE` | `/permissions/{id}` | **JWT + Admin** | Delete permission |
| `GET` | `/roles/{roleId}/permissions` | **JWT + Admin** | List permissions for role |
| `POST` | `/roles/{roleId}/permissions/{permissionId}` | **JWT + Admin** | Assign permission to role |
| `DELETE` | `/roles/{roleId}/permissions/{permissionId}` | **JWT + Admin** | Unassign permission from role |
| `GET` | `/users/{userId}/roles` | **JWT + Admin** | List roles for user |
| `POST` | `/users/{userId}/roles/{roleId}` | **JWT + Admin** | Assign role to user |
| `DELETE` | `/users/{userId}/roles/{roleId}` | **JWT + Admin** | Unassign role from user |

### Health (`/api/v1/health`)

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/health` | Public | Liveness probe — returns `{ status: "UP" }` |

---

## 5. eIDAS Integration — Deep Dive

### 5.1 Protocol Flow

The eIDAS flow spans three systems: the Chinvat backend, an external eIDAS broker (Cl@ve 2.0), and the user's browser. The complete interaction is:

```
User (Browser)               Chinvat Backend              Cl@ve 2.0 Broker
     │                              │                             │
     │── POST /eidas/login ────────►│                             │
     │   { providerCode: "CLAVE_ES"}│                             │
     │                              │── POST /broker/login ──────►│
     │                              │   { provider, redirectUri } │
     │                              │◄── { authUrl, sessionId } ──│
     │                              │                             │
     │   State token generated      │                             │
     │   Stored in redis-auth DB1   │                             │
     │   TTL: 10 minutes            │                             │
     │                              │                             │
     │◄── { authUrl, state } ───────│                             │
     │                              │                             │
     │── GET {authUrl} ────────────────────────────────────────►  │
     │   (browser redirects to Cl@ve)                             │
     │                          User authenticates with DNI/NIE   │
     │◄──────────────────────────────────── 302 /callback?code=...│
     │                              │                             │
     │── POST /eidas/callback ─────►│                             │
     │   { providerCode, code,      │                             │
     │     state }                  │── POST /broker/callback ───►│
     │                              │   { code }                  │
     │                              │◄── { identityClaims } ──────│
     │                              │                             │
     │                   State validated (redis-auth DB1)          │
     │                   ExternalIdentity persisted (PostgreSQL)   │
     │                              │                             │
     │◄── { externalSubjectId,      │                             │
     │      firstName, lastName,    │                             │
     │      email, certificate,     │                             │
     │      action }                │                             │
     │                              │                             │
     │── POST /profile/eidas/complete►                            │
     │   { externalSubjectId,       │                             │
     │     username, password,      │                             │
     │     certificatePem, ... }    │                             │
     │                              │                             │
     │                   Account created / linked                  │
     │                   Certificate bound                         │
     │                   ExternalIdentity status → LINKED         │
     │                              │                             │
     │◄── { userId, status,         │                             │
     │      linkedAt, completedAt } │                             │
```

### 5.2 State Management

The eIDAS OAuth2/OIDC `state` parameter prevents CSRF attacks during the redirect flow. Its lifecycle:

| Step | Action |
|---|---|
| Login initiation | 128-bit random `state` token generated |
| Storage | Written to **redis-auth DB1** with key `chinvat:eidas:state:{state}` and TTL **10 minutes** |
| Callback arrival | Backend reads and **atomically deletes** the key (single-use enforcement) |
| Validation failure | 400 / 401 returned; no identity claims exposed |

**Configuration class:** `EidasStateRedisConfiguration` — connects to `redis-auth` specifically on database index `1`, isolated from the application cache on index `0`.

**Adapter:** `RedisEidasStateAdapter` — the only class that reads/writes to DB1. All other components use the `EidasStatePort` interface, keeping the storage strategy swappable (see `InMemoryEidasStateAdapter` for local unit tests).

### 5.3 Cl@ve 2.0 (Spain)

[Cl@ve 2.0](https://clave.gob.es) is the Spanish government's unified identity platform. Chinvat integrates with it via **OpenID Connect**.

**Activation:** Add `clave` to `SPRING_PROFILES_ACTIVE`. The profile loads `application-clave.properties`.

**Configuration:**

```properties
# application-clave.properties (partial)
clave.oidc.issuer=https://clave2.gob.es/oidc
clave.oidc.token-endpoint=https://clave2.gob.es/oidc/token
clave.oidc.jwks-uri=https://clave2.gob.es/oidc/jwks.json
clave.oidc.client-id=chinvat-prod
clave.oidc.client-secret=${CLAVE_CLIENT_SECRET}
clave.oidc.redirect-uri=https://your-domain.com/api/v1/auth/eidas/callback
clave.supported-countries=ES,DE,FR,IT,PT
```

**JWT Validation:** `ClaveConfiguration` configures a Spring Security `JwtDecoder` that validates:
- Token signature (via JWKS)
- `iss` (issuer) claim
- `aud` (audience) claim
- Token expiry

**Adapter:** `ClaveBrokerAdapter` — implements `EidasBrokerPort` using Spring's OAuth2 client infrastructure for the token exchange.

### 5.4 Provider Registry

Providers are loaded from configuration at startup by `ConfigurationEidasProviderRegistryAdapter`. The registry is exposed via:
- `GET /api/v1/auth/eidas/providers` — full list
- `GET /api/v1/auth/eidas/providers/paged` — paginated

Provider response (`EidasProviderResponse`):
```json
{
  "code": "CLAVE_ES",
  "name": "España — Cl@ve 2.0",
  "country": "ES",
  "assuranceLevel": "HIGH",
  "enabled": true
}
```

### 5.5 Profile Completion

After the callback returns an `externalSubjectId`, the frontend guides the user through a profile completion form. The `POST /profile/eidas/complete` endpoint (`CompleteEidasProfileRequest`) accepts:

| Field | Description |
|---|---|
| `providerCode` | e.g. `CLAVE_ES` |
| `externalSubjectId` | Opaque identifier from broker |
| `username` | Desired username |
| `password` | Initial password |
| `firstName`, `lastName` | Pre-filled from eIDAS claims, editable |
| `email` | Pre-filled from claims |
| `phone` | Optional |
| `address` | Optional |
| `language` | UI language preference |
| `certificatePem` | X.509 certificate from eIDAS provider (PEM) |

The `ProfileService` then:
1. Creates the `UserAccount`
2. Hashes and saves the password
3. Validates the certificate via `TrustFacadeService`
4. Saves `CertificateCredential` and links it to the user
5. Links `ExternalIdentity` to the user (status → `LINKED`)
6. Activates the account (access_level based on eIDAS assurance level)

Response (`CompleteEidasProfileResponse`):
```json
{
  "userId": "uuid",
  "providerCode": "CLAVE_ES",
  "externalSubjectId": "...",
  "currentStatus": "LINKED",
  "linkedAt": "2026-01-15T10:00:00Z",
  "completedAt": "2026-01-15T10:00:05Z"
}
```

### 5.6 What Still Needs to Be Done

The following items are identified as outstanding work in the eIDAS integration:

| Priority | Item | Details |
|---|---|---|
| **HIGH** | Production Cl@ve credentials | Obtain `client-id` and `client-secret` from MPTFP (Spain). Set `CLAVE_CLIENT_SECRET` in prod secrets. |
| **HIGH** | Redirect URI registration | Register `https://{prod-domain}/api/v1/auth/eidas/callback` with Cl@ve 2.0 portal. |
| **HIGH** | SAML broker adapter | `HttpEidasBrokerAdapter` is the generic HTTP adapter; a full SAML 2.0 adapter (e.g. via OpenSAML) is needed for non-OIDC providers. |
| **MEDIUM** | Existing user linking | `action: LINK_EXISTING_USER` path in the callback response is defined but the frontend flow for linking to an already-registered account needs implementing. |
| **MEDIUM** | eIDAS assurance level enforcement | Map `LOW / SUBSTANTIAL / HIGH` assurance levels to `access_level` values in `CompleteEidasProfileUseCase`. Currently stored but not enforced. |
| **MEDIUM** | Attribute mapping per country | Different EU countries expose different claim names (e.g. `given_name` vs `firstName`). Mapping table per provider needs completing. |
| **LOW** | Mock broker hardening | `MockEidasBrokerAdapter` currently returns fixed test identities. Consider making it configurable per test scenario. |
| **LOW** | eIDAS provider management UI | Frontend admin page for enabling/disabling providers at runtime without redeploy. |
| **LOW** | Germany / France / Italy providers | Cl@ve technically federates DE/FR/IT/PT but end-to-end testing with each country's national ID has not been performed. |

---

## 6. Dual Redis Architecture

### 6.1 Why Two Instances

Chinvat operates **two separate Redis instances** as a deliberate security and operational architecture decision, not as a redundancy measure.

The core principle is **separation of concerns and blast-radius reduction**:

- **redis-global** holds permissions cache data shared across the platform. In the future, multiple services may read from it. It is configured **read-only** from the application's perspective in production (ACL `nocommands +get +mget +hget`).
- **redis-auth** holds data that is security-sensitive and write-intensive: the application object cache and, critically, the **eIDAS OIDC state tokens**. It is accessible only by the auth service using a dedicated `eidas-app` ACL user.

This separation means:
- A compromised `redis-global` credential cannot write eIDAS state tokens, preventing state injection attacks.
- A compromised `redis-auth` credential does not give read access to the permissions catalogue.
- In a future multi-service architecture, redis-global can be promoted to a shared read replica without touching redis-auth.

### 6.2 redis-global

| Property | Value |
|---|---|
| Port | 6379 |
| Purpose | Permissions cache (`RbacFacadeService`) |
| Access | Read-only for app in production |
| Database | 0 |
| Key namespace | `permissions:{userId}` |
| TTL | 15 minutes |
| Java config | `RedisConfiguration` (common module) |
| Env var | `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` |

### 6.3 redis-auth

| Property | Value |
|---|---|
| Port | 6380 |
| Purpose | App-level object cache + eIDAS state tokens |
| Access | Read-write (eidas-app ACL user only in production) |
| Databases | 0 (app cache), 1 (eIDAS state) |
| Key namespaces | DB0: `cache:users:*`, `cache:trust:*`, `cache:eidasProviders:*` |
| | DB1: `chinvat:eidas:state:{token}` |
| TTL | App cache: 5–30 min (per cache name) · eIDAS state: 10 min |
| Java config | `CacheRedisConfiguration`, `EidasStateRedisConfiguration` |
| Env var | `APP_CACHE_REDIS_*`, `EIDAS_STATE_REDIS_*` |

### 6.4 Database Index Layout

```
redis-global :6379
└── DB 0
    ├── permissions:{userId}      TTL 15m   (serialised permission set)
    └── org:{orgId}               TTL 30m   (organisation cache — future)

redis-auth :6380
├── DB 0  (Spring Cache)
│   ├── cache:users:{userId}      TTL 5m    (UserResponse DTO)
│   ├── cache:trust:{thumbprint}  TTL 30m   (TrustValidationResult)
│   └── cache:eidasProviders      TTL 30m   (provider list)
└── DB 1  (eIDAS state — single-use)
    └── chinvat:eidas:state:{token}  TTL 10m
```

### 6.5 Java Configuration Classes

| Class | Module | Description |
|---|---|---|
| `RedisConfiguration` | `common` | Primary `RedisConnectionFactory` pointing to `redis-global`. All modules use this unless overridden. |
| `CacheRedisConfiguration` | `app` | When `APP_CACHE_REDIS_DEDICATED=true`, creates a **secondary** `RedisConnectionFactory` for `CacheManager`. When false, the primary connection is reused (dev mode). |
| `EidasStateRedisConfiguration` | `eidas` | Always creates a **dedicated** `RedisConnectionFactory` for DB1 of `redis-auth`. In development (`EIDAS_STATE_REDIS_DEDICATED=false`) it reuses the primary connection but selects DB1. |

### 6.6 Production Hardening

The full security hardening guide is at [`/infra/redis/SECURITY.md`](../infra/redis/SECURITY.md). Key points:

1. **Firewall:** Block external access to `:6379` and `:6380`. Only the Docker internal network should reach Redis.
2. **ACL users:** Default user is disabled. `eidas-app` user has explicit command allowlist.
3. **`requirepass`:** Set via environment variables in `compose.prod.yml`.
4. **Key prefix restrictions:** ACL `resetkeys ~chinvat:eidas:state:*` limits eidas-app to its own namespace.
5. **TLS:** Optional (`rediss://`) — recommended for multi-host deployments.
6. **Setup script:** `/infra/scripts/redis-acl-setup.sh` automates ACL user creation.
7. **Monitoring:** Prometheus Redis exporter + slowlog + ACL denial counters.

---

## 7. Authentication Flows

### 7.1 Password Login

```
POST /api/v1/auth/login
{
  "username": "jdoe",
  "password": "s3cr3t",
  "clientIp": "192.168.1.1",
  "userAgent": "Mozilla/5.0 ..."
}
```

Internal steps:

1. `AuthController.login` delegates to `LoginUseCase`
2. `UsersFacadeService.findByUsername` — load account
3. `PasswordEncoder.matches` — verify bcrypt/scrypt hash
4. `RbacFacadeService.getPermissions` — load roles (cache-first)
5. JWT signed with application secret, embedded claims: `sub`, `roles`, `exp`
6. `AuthSessionJpaEntity` persisted with hashed access token
7. `AuditFacadeService.logLogin` — write audit event

Response:
```json
{
  "accessToken":  "eyJ...",
  "refreshToken": "eyJ...",
  "expiresIn":    3600,
  "user": {
    "id":       "uuid",
    "username": "jdoe",
    "fullName": "John Doe",
    "email":    "jdoe@example.com",
    "roles":    ["USER"]
  }
}
```

### 7.2 mTLS Certificate Login

```
POST /api/v1/auth/certificates/login
X-Client-Certificate: -----BEGIN%20CERTIFICATE-----%0AMIIBkTC...
```

1. NGINX terminates TLS and forwards the client certificate as a URL-encoded PEM header.
2. `MtlsClientCertificateResolver` URL-decodes and parses the header into a `X509Certificate`.
3. `CertificateLoginUseCase` calls `TrustFacadeService.validate` — chain + revocation check.
4. `UsersFacadeService.findByCertificate` — resolve user by certificate thumbprint.
5. If valid and user active: session created, JWT pair issued.

### 7.3 eIDAS Login

See [Section 5.1](#51-protocol-flow) for the full sequence diagram.

### 7.4 Token Refresh

```
POST /api/v1/auth/refresh
{ "refreshToken": "eyJ..." }
```

1. `RefreshCommand` validates the refresh token signature and expiry.
2. Looks up `AuthSessionJpaEntity` by refresh token hash — checks not revoked.
3. Issues a new JWT pair (both access and refresh tokens rotated).
4. Old session record updated with new hashes and expiry.

---

## 8. Certificate & Trust Infrastructure

### X.509 Validation Pipeline

```
PEM input
  │
  ├── PEM parse (Bouncy Castle PEMParser)
  │
  ├── Chain building (sd-dss CertificateVerifier)
  │   ├── AIA fetch (intermediate CAs from URL in cert)
  │   └── Trust anchor lookup (EU TSL cache)
  │
  ├── Revocation (sd-dss)
  │   ├── OCSP (preferred, via AIA)
  │   └── CRL fallback
  │
  └── ValidationReport
      ├── VALID     → proceed
      ├── EXPIRED   → 422 Unprocessable
      ├── REVOKED   → 403 Forbidden
      └── UNTRUSTED → 403 Forbidden
```

### TSL Synchronisation

`TslSyncService` runs on startup and periodically (configurable interval) to:
1. Download the **EU List of Trusted Lists (LOTL)** from `https://ec.europa.eu/tools/lotl/eu-lotl.xml`.
2. Parse and cache all national TSLs.
3. Store validated trust anchors in memory for fast certificate chain building.

This prevents cold-start failures on the first certificate validation after deployment.

### Certificate Self-Management (Profile)

Authenticated users can manage their own certificates:

| Operation | Description |
|---|---|
| `GET /profile/certificates` | Lists all certificates associated with the user's account |
| `POST /profile/certificates` | Validates and stores a new certificate (runs full TSL validation) |
| `DELETE /profile/certificates/{id}` | Removes a certificate (user can no longer use it for mTLS) |

After adding or removing a certificate, relevant caches are evicted to ensure the next authentication attempt uses fresh data.

---

## 9. Role-Based Access Control (RBAC)

### Data Model

```
User ──── UserRole ──── Role ──── RolePermission ──── Permission
```

- A **user** can have multiple **roles**.
- A **role** aggregates multiple **permissions**.
- **Permissions** are atomic strings, e.g. `user:read`, `cert:upload`, `admin:rbac`.
- Effective permissions = union of all permissions across all of the user's roles.

### Built-in Roles

| Role | Key Permissions |
|---|---|
| `ADMIN` | Full access — `admin:rbac`, all `user:*`, all `cert:*` |
| `OPERATOR` | `user:read`, `cert:read`, `cert:upload`, `cert:validate` |
| `USER` | `user:read:self`, `cert:read:self`, `cert:upload:self` |

Built-in roles are created at database seed time by `BuiltinRolePermissions`.

### Permission Caching

```
Login / Token validation
  │
  └── RbacFacadeService.getPermissions(userId)
        │
        ├── Redis GET  permissions:{userId}
        │   HIT  → return Set<String>
        │   MISS → PostgreSQL join query
        │         → SET permissions:{userId} EX 900
        │         → return Set<String>
        │
        └── Injected into SecurityContext as GrantedAuthority list
```

Cache is invalidated whenever a role or permission assignment changes.

---

## 10. Security Architecture

### Spring Security Filter Chain

Filters execute in this order on every request:

```
1. RateLimitingFilter
   └── Checks Redis sliding window for IP + endpoint key
   └── Returns 429 if limit exceeded

2. BearerTokenAuthFilter
   └── Extracts Authorization: Bearer header
   └── Validates JWT (signature, expiry)
   └── Checks AuthSession not revoked (DB)
   └── Loads permissions from Redis
   └── Sets SecurityContext

3. Spring Security FilterChain
   └── CORS filter
   └── ExceptionTranslationFilter
   └── AuthorizationFilter (checks SecurityContext)
```

### Public vs. Protected Endpoints

**Public (no JWT required):**
- `/api/v1/health`
- `/api/v1/auth/login`
- `/api/v1/auth/certificates/login`
- `/api/v1/auth/fnmt/login`
- `/api/v1/auth/refresh`
- `/api/v1/auth/logout`
- `/api/v1/auth/register`
- `/api/v1/auth/password-reset/**`
- `/api/v1/auth/eidas/**`
- `/api/v1/profile/eidas/complete`
- `/swagger-ui.html`, `/v3/api-docs/**` (dev only)

**Protected (JWT required):**
- `/api/v1/auth/me`
- `/api/v1/auth/sessions`
- `/api/v1/profile/**` (except eidas/complete)
- `/api/v1/rbac/**` (additionally requires Admin role)

### Transport Security

- All production traffic is TLS 1.3 (NGINX terminates).
- mTLS: NGINX forwards client certificate via `X-Client-Certificate` header.
- HSTS headers set by NGINX.
- CORS: origins configured via `CORS_ALLOWED_ORIGINS` (comma-separated). Defaults to localhost in dev.
- CSRF disabled (stateless API — no session cookies).

### Secrets Management

| Secret | Storage |
|---|---|
| JWT signing key | `JWT_SECRET` env var |
| Database password | `POSTGRES_PASSWORD` env var |
| Redis passwords | `REDIS_GLOBAL_PASSWORD`, `REDIS_AUTH_PASSWORD` env vars |
| Cl@ve client secret | `CLAVE_CLIENT_SECRET` env var |
| FNMT shared secret | `FNMT_PROXY_SHARED_SECRET` env var |

In production, these should be injected via a secrets manager (HashiCorp Vault, AWS Secrets Manager, Kubernetes Secrets) — **never** committed to `.env` files in the repository.

---

## 11. Database Schema

The schema is managed by **Prisma Migrate**. Source of truth: `/infra/database/prisma/schema.prisma`.

### Core Tables

| Table | Primary Key | Description |
|---|---|---|
| `user` | `id` (UUID) | User account — username, full_name, email, phone, address, language, access_level, user_type |
| `user_password` | `id` (UUID) | Password hash (bcrypt/scrypt), FK → `user` |
| `user_certificate` | `id` (UUID) | mTLS certificate PEM, FK → `user` |
| `auth_session` | `id` (UUID) | JWT session — access hash, refresh hash, expiry, revoked, client_ip, user_agent |
| `auth_password_reset` | `id` (UUID) | Single-use reset token, expiry, FK → `user` |
| `role` | `id` (UUID) | Named role |
| `permission` | `id` (UUID) | Atomic permission string |
| `user_role` | composite | `user_id` + `role_id` |
| `role_permission` | composite | `role_id` + `permission_id` |
| `external_identity` | `id` (UUID) | Pending/linked eIDAS identity — provider, subject_id, state, status, linked_at |
| `certificate_credential` | `id` (UUID) | Full X.509 credential — subject, issuer, serial, not_before, not_after, pem |
| `audit_event` | `id` (UUID) | Immutable audit log — event_type, actor_id, target_id, metadata, timestamp |

### Migration Strategy

| Environment | Command | Notes |
|---|---|---|
| Development | `make db-reset` | Drops schema, applies all migrations, runs seed |
| Development (incremental) | `make db-migrate` | Apply new migrations only |
| Production | `prisma migrate deploy` | Never drops data, only forward migrations |
| Production seed | `make db-seed` | Idempotent — guarded by audit event check |

---

## 12. Frontend Architecture

### Directory Structure

```
frontend/src/
├── features/
│   ├── auth/          ← Login, register, password reset, logout
│   ├── eidas/         ← Provider list, initiate login
│   ├── profile/       ← Profile completion, certificate management
│   ├── admin/         ← RBAC management UI
│   ├── trust/         ← Certificate validation UI
│   └── health/        ← Status page
├── shared/
│   ├── api/           ← Axios instance, interceptors (token inject + refresh)
│   ├── auth/          ← AuthContext, useAuth hook, token storage
│   ├── hooks/         ← useForm, usePagination, useBreakpoint
│   ├── types/         ← TypeScript interfaces mirroring backend DTOs
│   ├── ui/            ← Reusable components
│   │   ├── FormField.tsx
│   │   ├── TextInput.tsx
│   │   ├── PasswordField.tsx
│   │   ├── PhoneNumberField.tsx
│   │   ├── FormPage.tsx
│   │   ├── FlowForm.tsx
│   │   └── ProgressStepper.tsx
│   └── i18n/          ← i18next initialisation
├── pages/             ← Route-level page components
├── layouts/           ← Authenticated layout, guest layout
├── router/            ← Route definitions, auth guards
└── locales/           ← Translation files (en, es, de, fr, it, pt)
```

### Key Pages

| Page | Route | Description |
|---|---|---|
| `LoginPage` | `/login` | Username/password form + links to eIDAS and certificate login |
| `RegisterPage` | `/register` | Account creation form |
| `EidasCallbackPage` | `/eidas/callback` | Reads `?code=&state=` from URL, calls `/callback`, redirects to profile completion |
| `ProfilePage` | `/profile` | View/edit profile, manage certificates |
| `AdminPage` | `/admin` | RBAC management (roles, permissions, assignments) |
| `ResetPasswordPage` | `/reset-password` | Request + confirm password reset |

### Authentication State

The frontend stores tokens in `localStorage` (access + refresh). `AuthContext` exposes:
- `user` — current user info or `null`
- `login(credentials)` — calls API, stores tokens, sets user
- `logout()` — calls API, clears tokens, redirects
- `refreshToken()` — called automatically by Axios interceptor on 401

The Axios instance injects `Authorization: Bearer {accessToken}` on every request. On a 401 response, the interceptor attempts one silent token refresh before redirecting to `/login`.

### Internationalisation

- **Library:** i18next + react-i18next
- **Languages supported:** English (default), Spanish, German, French, Italian, Portuguese
- **Language detection:** Browser preference → saved preference → fallback to `en`
- **Translation files:** `/frontend/src/locales/{lang}/translation.json`
- **Component:** `LanguageSwitcher` in the header — writes selection to `localStorage`

### Build Output

Production build produces a static site at `frontend/dist/`. NGINX serves it from there and proxies `/api/` to the backend.

---

## 13. Infrastructure & Docker

### Docker Compose Environments

| File | Purpose |
|---|---|
| `infra/docker/compose.dev.yml` | Local development stack |
| `infra/docker/compose.prod.yml` | Production-grade stack |

### Development Stack Services

| Service | Image | Ports | Description |
|---|---|---|---|
| `postgres` | postgres:16 | 5432 | Primary database |
| `redis-global` | redis:7 | 6379 | Permissions cache |
| `redis-auth` | redis:7 | 6380 | App cache + eIDAS state |
| `db-reset` | node (prisma) | — | DEV ONLY: drops schema |
| `db-migrate` | node (prisma) | — | Applies Prisma migrations |
| `db-seed` | node (prisma) | — | Loads initial data |
| `backend` | chinvat-backend | 8080, 5005 | Spring Boot app + remote debug |
| `frontend` | chinvat-frontend-dev | 3000 | Vite dev server (HMR) |
| `nginx` | nginx | 8080, 8443 | Reverse proxy |

### Production Stack Differences

| Aspect | Dev | Prod |
|---|---|---|
| Schema reset service | Present | **Absent** |
| Seed mode | Full reset | Idempotent only |
| Backend debug port | 5005 open | **Closed** |
| Redis authentication | None | `requirepass` + ACL |
| Swagger UI | Enabled | **Disabled** |
| SQL logging | Enabled | **Disabled** |
| Spring profile | `dev` | `prod` |

### Backend Dockerfile (Multi-stage)

```dockerfile
# Stage 1 — Maven build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2 — Development runtime (with source, debug)
FROM eclipse-temurin:21-jdk AS dev
COPY --from=builder /app/app/target/*.jar /app.jar
EXPOSE 8080 5005
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,...", "-jar", "/app.jar"]

# Stage 3 — Production runtime (minimal)
FROM eclipse-temurin:21-jre AS prod
COPY --from=builder /app/app/target/*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Frontend Dockerfile

```dockerfile
# Stage 1 — Node build
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json .
RUN npm ci
COPY . .
RUN npm run build

# Stage 2 — NGINX serve
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

Runtime configuration (API base URL) is injected at container start via an `env.sh` script that writes `window.__ENV__` into the HTML — avoiding the need to rebuild the image for environment changes.

---

## 14. CI/CD Pipelines

### GitHub Actions Workflows

| Workflow | Trigger | Steps |
|---|---|---|
| `backend-ci.yml` | Push / PR on `backend/` | Spotless format check → Maven test → Docker build |
| `ci.yml` | PR to `main` / `develop` | Backend CI + Prisma schema validation + Docker prod build |
| `cd.yml` | Push to `main` | Docker build + push to registry + deploy |
| `qodana_code_quality.yml` | Scheduled / PR | JetBrains Qodana static analysis |

### CI Caching

- Maven dependencies: GitHub Actions cache keyed on `pom.xml` hash
- npm dependencies: GitHub Actions cache keyed on `package-lock.json` hash

### Code Quality Gates

1. **Google Java Format** — enforced by Spotless Maven plugin. CI fails if any file is not formatted. Run locally: `make backend-fmt`.
2. **ArchUnit** — architecture tests verify that modules do not violate the dependency hierarchy (e.g. `common` cannot depend on `auth`).
3. **Qodana** — JetBrains static analysis for bug patterns, security issues, and code smells.

---

## 15. Developer Guide

### Prerequisites

| Tool | Version | Purpose |
|---|---|---|
| Docker Desktop | Latest | Running the dev stack |
| GNU Make | Any | Makefile targets |
| Java JDK | 21+ | Local backend build / IDE support |
| Node.js | 22+ | Local frontend build / IDE support |
| IntelliJ IDEA | 2024+ | Recommended IDE |

### First-Time Setup

```bash
# 1. Clone the repository
git clone https://github.com/ejaprrr/chinvat
cd chinvat

# 2. Copy the example environment file
cp infra/docker/.env.dev.example .env

# 3. Start the full dev stack
make dev-up

# Services available:
#   Frontend:  http://localhost:3000
#   Backend:   http://localhost:8080
#   Swagger:   http://localhost:8080/swagger-ui.html
#   Postgres:  localhost:5432 (user: chinvat / chinvat)
#   Redis:     localhost:6379 (global), localhost:6380 (auth)
```

### Makefile Reference

| Target | Description |
|---|---|
| `make dev-up` | Start full Docker dev stack |
| `make dev-down` | Stop and remove containers |
| `make backend-build` | `mvnw clean package` (skip tests) |
| `make backend-test` | `mvnw test` (all modules) |
| `make backend-fmt` | Apply Google Java Format |
| `make backend-fmt-check` | Verify formatting (CI mode) |
| `make backend-run-local` | Run backend with `local` profile (no Docker) |
| `make backend-run-dev` | Run backend with `dev` profile |
| `make db-migrate` | Apply pending Prisma migrations |
| `make db-seed` | Load seed data |
| `make db-reset` | **DEV ONLY** — drop schema, migrate, seed |
| `make redis-flush` | **DEV ONLY** — flush all Redis data |
| `make infra-config-dev` | Validate dev Compose file |

### Running the Backend Locally (Without Docker)

```bash
# Start only infrastructure
docker compose -f infra/docker/compose.dev.yml up postgres redis-global redis-auth db-migrate db-seed -d

# Run backend
make backend-run-local
# or
./backend/mvnw spring-boot:run -pl app -Dspring-boot.run.profiles=local
```

### Remote Debug (Dev Stack)

The dev backend exposes JDWP on port `5005`. In IntelliJ IDEA:

1. **Run → Edit Configurations → Add Remote JVM Debug**
2. Host: `localhost`, Port: `5005`
3. Connect after `make dev-up` completes.

### Code Style

The project uses **Google Java Format** enforced by Spotless. Configure your IDE:

- **IntelliJ IDEA:** Install the _google-java-format_ plugin. Enable "Reformat code on save."
- **Pre-commit:** Run `make backend-fmt` before every commit (or add to a git pre-commit hook).

---

## 16. Configuration Reference

### Backend Application Properties

| Property | Default | Description |
|---|---|---|
| `server.port` | `8080` | HTTP port |
| `spring.datasource.url` | — | JDBC URL (set via `DB_URL` env var) |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Never `create-drop` in production |
| `redis.host` / `redis.port` | `localhost` / `6379` | redis-global connection |
| `app.cache.redis.dedicated` | `false` | Use separate redis-auth for app cache |
| `eidas.state.redis.dedicated` | `false` | Use separate redis-auth DB1 for state |
| `eidas.broker.url` | — | External broker base URL |
| `cors.allowed-origins` | `http://localhost:3000` | Comma-separated CORS origins |
| `jwt.secret` | — | HS256 signing key (32+ chars) |
| `jwt.expiry-seconds` | `3600` | Access token lifetime |
| `jwt.refresh-expiry-seconds` | `86400` | Refresh token lifetime |
| `rate-limit.login.max` | `10` | Max login attempts per minute per IP |
| `rate-limit.eidas.max` | `5` | Max eIDAS initiations per minute per IP |
| `fnmt.proxy.shared-secret` | — | FNMT proxy HMAC secret |

### Environment Variables

| Variable | Module | Description |
|---|---|---|
| `POSTGRES_DB` | infra | Database name |
| `POSTGRES_USER` | infra | Database user |
| `POSTGRES_PASSWORD` | infra | Database password |
| `REDIS_HOST` | app | redis-global hostname |
| `REDIS_PORT` | app | redis-global port |
| `REDIS_PASSWORD` | app | redis-global password (prod) |
| `APP_CACHE_REDIS_DEDICATED` | app | `true` = use redis-auth for app cache |
| `APP_CACHE_REDIS_HOST` | app | redis-auth hostname |
| `APP_CACHE_REDIS_PORT` | app | redis-auth port |
| `APP_CACHE_REDIS_DATABASE` | app | redis-auth DB index for cache (0) |
| `APP_CACHE_REDIS_USERNAME` | app | redis-auth ACL username (prod) |
| `APP_CACHE_REDIS_PASSWORD` | app | redis-auth ACL password (prod) |
| `EIDAS_STATE_REDIS_DEDICATED` | eidas | `true` = use redis-auth DB1 for state |
| `EIDAS_STATE_REDIS_HOST` | eidas | redis-auth hostname |
| `EIDAS_STATE_REDIS_PORT` | eidas | redis-auth port |
| `EIDAS_STATE_REDIS_DATABASE` | eidas | redis-auth DB index for state (1) |
| `EIDAS_STATE_REDIS_USERNAME` | eidas | redis-auth ACL username (prod) |
| `EIDAS_STATE_REDIS_PASSWORD` | eidas | redis-auth ACL password (prod) |
| `JWT_SECRET` | auth | JWT signing secret |
| `CLAVE_CLIENT_SECRET` | eidas | Cl@ve 2.0 OIDC client secret |
| `FNMT_PROXY_SHARED_SECRET` | auth | FNMT proxy HMAC key |
| `CORS_ALLOWED_ORIGINS` | app | Comma-separated allowed origins |
| `SPRING_PROFILES_ACTIVE` | app | e.g. `prod,clave` |
| `VITE_API_BASE_URL` | frontend | Backend API base URL |

---

## 17. Roadmap & Outstanding Work

### Security & Production Readiness

| Priority | Item | Module |
|---|---|---|
| **CRITICAL** | Rotate all default secrets before first production deploy | infra |
| **CRITICAL** | Enable `APP_CACHE_REDIS_DEDICATED=true` and `EIDAS_STATE_REDIS_DEDICATED=true` in production | app / eidas |
| **CRITICAL** | Apply Redis ACL setup (`/infra/scripts/redis-acl-setup.sh`) before go-live | infra |
| **HIGH** | Register Cl@ve 2.0 redirect URI with MPTFP Spain | eidas |
| **HIGH** | Obtain production Cl@ve 2.0 `client-id` and `client-secret` | eidas |
| **HIGH** | Enable Swagger UI only in dev/staging profiles (prod should return 404) | app |
| **HIGH** | Configure HSTS headers in NGINX production config | infra |

### eIDAS Completion

| Priority | Item |
|---|---|
| **HIGH** | Implement SAML 2.0 broker adapter (OpenSAML) for non-OIDC providers |
| **HIGH** | End-to-end test with German, French, Italian, and Portuguese national eIDs |
| **MEDIUM** | Implement "link to existing account" flow (currently only "create new user") |
| **MEDIUM** | Enforce eIDAS assurance level (`LOW` / `SUBSTANTIAL` / `HIGH`) → `access_level` mapping |
| **MEDIUM** | Per-country attribute claim name mapping table |
| **LOW** | Admin UI for runtime provider enable/disable without redeploy |
| **LOW** | Configurable mock broker scenarios for automated testing |

### Backend Features

| Priority | Item |
|---|---|
| **HIGH** | Email delivery integration for password reset (currently no email sender configured) |
| **HIGH** | Scheduled job to clean up expired `auth_session` and `auth_password_reset` records |
| **MEDIUM** | Implement `ArchUnit` tests for all module dependency rules |
| **MEDIUM** | OpenAPI schema refinement — add response examples and error schemas |
| **MEDIUM** | Actuator health endpoints with Redis and DB indicators |
| **LOW** | GraphQL or gRPC transport option for high-throughput clients |

### Frontend Features

| Priority | Item |
|---|---|
| **HIGH** | Full i18n coverage — some UI strings are not yet extracted to translation files |
| **HIGH** | Accessibility audit — WCAG 2.1 AA compliance pass |
| **MEDIUM** | Progressive Web App (PWA) manifest and service worker |
| **MEDIUM** | Admin RBAC UI — full role/permission management frontend |
| **LOW** | Dark mode support |
| **LOW** | Native mobile app (React Native) wrapper |

### Infrastructure

| Priority | Item |
|---|---|
| **HIGH** | Kubernetes Helm chart (currently Docker Compose only) |
| **HIGH** | Secrets management integration (Vault / AWS Secrets Manager) |
| **MEDIUM** | Prometheus + Grafana monitoring stack in `compose.prod.yml` |
| **MEDIUM** | Automated backup for PostgreSQL |
| **LOW** | Redis Sentinel or Cluster for HA in production |
| **LOW** | CDN for frontend static assets |

---

*Documentation maintained by the Chinvat engineering team. For corrections or additions, open a PR against `docs/README.md` on the `main` branch.*
