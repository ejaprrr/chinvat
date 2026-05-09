# Chinvat Backend - To-Do a stav implementace

Datum: 10. 5. 2026
Branch: backend

## 1. Dokoncene body

### 1.1 Profile API endpointy a permission enforcement
- [x] POST /api/v1/profile/eidas/complete
  - Public endpoint (bez autentizace)
  - Ucel: dokonceni uzivatelskeho profilu po eIDAS callbacku
- [x] GET /api/v1/profile/certificates
  - Vyžaduje permission PROFILE:READ
  - Ucel: vypsani certifikatu uzivatele
- [x] POST /api/v1/profile/certificates
  - Vyžaduje permission PROFILE:WRITE
  - Ucel: pridani certifikatu
- [x] DELETE /api/v1/profile/certificates/{id}
  - Vyžaduje permission PROFILE:WRITE
  - Ucel: revokace certifikatu
- [x] POST /api/v1/profile/certificates/{id}/primary
  - Vyžaduje permission PROFILE:WRITE
  - Ucel: nastaveni primarniho certifikatu

### 1.2 RBAC a security vrstva
- [x] JWT Bearer token autentizace
- [x] Role-based access control (RBAC)
- [x] 3 built-in role: USER, ADMIN, SUPERADMIN
- [x] 5 granularnich permission:
  - PROFILE:READ
  - PROFILE:WRITE
  - USERS:MANAGE
  - RBAC:MANAGE
  - AUTH:MANAGE
- [x] Permission resolution pres TokenPrincipal + databazove mapovani
- [x] @PreAuthorize enforcement na protected endpointy
- [x] Exception handling (400/403/404) pro authorization/validation scenare

### 1.3 Local runtime fix (NAMED_ENUM blocker)
- [x] Opraven local profil tak, aby stabilne startoval
- [x] Zmena v application-local.properties:
  - spring.jpa.hibernate.ddl-auto: create-drop -> none
- [x] Duvod:
  - H2 neumi PostgreSQL NAMED_ENUM (SqlTypes 6001)
  - Pri ddl-auto=create-drop padalo generovani DDL

### 1.4 Testy
- [x] ProfileControllerIT: 4/4 prochazi
  - completeEidasProfile_createsUserBindsCertificateAndLinksIdentity
  - certificateEndpoints_requireAuthAndSupportLifecycle
  - setPrimaryCertificate_whenCredentialMissing_returns404
  - setPrimaryCertificate_whenCredentialNotActive_returns400
- [x] Trust module testy: 6/6 prochazi
  - BindCertificateCredentialUseCaseTest (2/2)
  - RevokeCertificateCredentialUseCaseTest (2/2)
  - SetPrimaryCertificateCredentialUseCaseTest (2/2)
- [x] Celkem overeno: 10/10 testu passing

### 1.5 Dokumentace a skripty
- [x] ENDPOINTS_AND_PERMISSIONS.md
  - Kompletní seznam endpointu
  - Permission matrix
  - curl priklady
- [x] ENTERPRISE_READINESS_CHECKLIST.md
  - Production readiness checklist
  - Priority akce
- [x] test-endpoints.sh
  - Automatizovane testovani endpointu

## 2. Permission matrix

| Role       | PROFILE:READ | PROFILE:WRITE | USERS:MANAGE | RBAC:MANAGE | AUTH:MANAGE |
|------------|--------------|---------------|--------------|-------------|-------------|
| USER       | ano          | ne            | ne           | ne          | ne          |
| ADMIN      | ano          | ano           | ano          | ne          | ne          |
| SUPERADMIN | ano          | ano           | ano          | ano         | ano         |

## 3. Stav enterprise readiness

Aktualni hodnoceni: funkcne hotovo, pripraveno pro staging, pred production jsou nutne hardening kroky.

Skore (orientacne):
- Security: 70/100
- Testing: 40/100
- Documentation: 95/100
- Deployment: 60/100
- Performance: 50/100
- Monitoring: 30/100

Doporuceni: staging ano, production az po splneni high-priority bodu niz.

## 4. High priority pred production

- [ ] CSRF protection
  - Riziko: cross-site request forgery
  - Akce: zapnout CSRF ochranu v Spring Security
- [ ] Rate limiting
  - Riziko: brute-force a DoS/DDoS
  - Akce: pridat rate-limit vrstvu (filter/gateway)
- [ ] Encrypted storage citlivych dat
  - Riziko: uniky tokenu/citlivych hodnot pri kompromitaci DB
  - Akce: field-level encryption
- [ ] CORS hardening
  - Riziko: prilis volne origin policy
  - Akce: omezit CORS na schvalene domény
- [ ] Zvysit test coverage na 80%+
  - Aktualne cca 10-15%
- [ ] Secrets management
  - Akce: Vault/AWS Secrets Manager nebo ekvivalent
- [ ] Monitoring a observability
  - Akce: centralizovane logy + metriky + alerting

## 5. Medium priority

- [ ] CI/CD pipeline (build, test, deploy gates)
- [ ] Load/performance testy
- [ ] Hardening deployment procesu

## 6. Low priority

- [ ] Dalsi performance optimalizace
- [ ] Rozsirene audit/reporting dashboardy

## 7. Quick start (lokalni overeni)

1) Spusteni aplikace (local):

```bash
SERVER_PORT=8080 SPRING_PROFILES_ACTIVE=local ./backend/mvnw -f backend/app/pom.xml spring-boot:run -DskipTests
```

2) Spusteni endpoint test skriptu:

```bash
./test-endpoints.sh
```

3) Manual eIDAS complete (public endpoint):

```bash
curl -X POST http://localhost:8080/api/v1/profile/eidas/complete \
  -H "Content-Type: application/json" \
  -d '{"providerCode":"EIDAS_EU"}'
```

4) Ziskani tokenu (podle auth endpointu):

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass"}'
```

5) Test autentizovaneho endpointu:

```bash
curl -X GET http://localhost:8080/api/v1/profile/certificates \
  -H "Authorization: Bearer <token>"
```

## 8. Poznamky

- Local profil je stabilizovany proti NAMED_ENUM/H2 konfliktu.
- Permission enforcement na Profile API je aktivni a testovany.
- Pro produkci je nutne dodelat bezpecnostni hardening (sekce 4).
