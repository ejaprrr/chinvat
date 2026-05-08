# Audit certifikátové a eIDAS implementace

Datum: 2026-05-08

## Executive Summary

Aktuální implementace v projektu poskytuje základní certifikátový login přes mTLS gateway a lookup certifikátu podle SHA-256 fingerprintu v databázi. Tento stav je použitelný jako omezený FNMT proof-of-concept pro řízené prostředí, ale nesplňuje enterprise požadavky na evropský trust framework, plnohodnotný eIDAS login, lifecycle správu digitálních identit ani důsledné oddělení odpovědností podle clean architecture.

Současné řešení řeší pouze tento scénář:

- klient předloží X.509 certifikát na NGINX gateway
- gateway certifikát ověří proti lokálně nahranému CA bundle
- backend si z předaného certifikátu spočítá fingerprint
- backend dohledá aktivní vazbu certifikátu na interního uživatele
- backend vydá standardní access a refresh token

To znamená, že systém dnes neimplementuje plný evropský trust model, ale pouze lokální certificate binding. Chybí zejména trust service layer, EU Trusted Lists, revocation validation, eIDAS identity federation, kvalifikované atributy certifikátů, auditní model identity evidence a jednotný provider model.

## Zjištěný stav v projektu

Implementace dnes stojí na těchto stavebních blocích:

- endpoint pro login certifikátem v backend/modules/auth/src/main/java/eu/alboranplus/chinvat/auth/api/controller/AuthController.java
- resolver certifikátu z proxy hlaviček v backend/modules/auth/src/main/java/eu/alboranplus/chinvat/auth/api/security/MtlsClientCertificateResolver.java
- aplikační use case pro přihlášení podle fingerprintu v backend/modules/auth/src/main/java/eu/alboranplus/chinvat/auth/application/usecase/CertificateLoginUseCase.java
- persistence model user_certificate v infra/database/prisma/schema.prisma
- NGINX mTLS gateway konfigurace v infra/docker/nginx/templates/gateway.conf.template
- helper skripty pro dev/prod registraci certifikátů v infra/scripts/fnmt-dev-setup.sh a infra/scripts/fnmt-register-cert.sh

### Co už funguje

- dedikovaný FNMT login endpoint přes gateway
- ověření, že požadavek přišel přes důvěryhodnou proxy pomocí sdíleného secretu
- výpočet SHA-256 fingerprintu z X.509 certifikátu
- lookup aktivního certifikátu v databázi
- revokace certifikátu na aplikační úrovni přes revoked_at
- oddělení auth a users modulu přes facade/port vrstvu

### Co dnes systém ve skutečnosti neumí

- plnohodnotný eIDAS cross-border login občana nebo organizace
- federaci identit přes eIDAS node nebo trusted broker
- validaci evropských trusted service providers přes LOTL/TSL
- OCSP nebo CRL revocation validaci v aplikační vrstvě
- rozlišování QWAC, QSealC, QES, nepodporovaných certifikátů a běžných národních certifikátů
- parsing QCStatements, certificate policies a assurance atributů
- správu providerů a trust konfigurace jako doménového modelu
- enrollment workflow pro navázání certifikátu na uživatele s auditní stopou
- více typů identit: person, organization, service, signing credential
- jednoznačné oddělení transportní validace, trust validace a identity bindingu

## Hlavní mezery a rizika

### 1. Architektonická mezera

Provider-specific certifikátový login je dnes součástí auth modulu a pojmenovaný jako FNMT. To je příliš nízká abstrakce. Auth modul má být zodpovědný za vydání tokenů a session management, ne za trust model certifikátů nebo evropskou identitní interoperabilitu.

Riziko:

- další národní nebo evropské provider flow se bude přidávat do auth modulu a zhorší jeho soudržnost
- endpointy a use cases budou provider-specific místo capability-based
- clean architecture bude postupně erodovat do orchestrace v controlleru a infrastruktuře

### 2. Trust mezera

Backend dnes nevydává trust rozhodnutí nad certifikátem. Spoléhá na gateway a na lokální registraci fingerprintu. To nestačí pro enterprise trust framework.

Riziko:

- bez TSL/LOTL nevíte, zda issuer patří mezi důvěryhodné evropské QTSP
- bez OCSP/CRL nemáte online revocation kontrolu mimo lokální revoked_at
- bez policy a QC parsing nevíte, zda jde o kvalifikovaný nebo nepodporovaný certifikát

### 3. Identitní mezera

Certifikát je dnes pouze vazba na uživatele podle fingerprintu. Chybí oddělení identity evidence od login session.

Riziko:

- chybí audit, jak byl certifikát zaregistrován a kdo registraci schválil
- chybí link mezi externí identitou a interním účtem
- chybí assurance level a identity source metadata

### 4. Provozní mezera

Správa CA bundle a certifikátů je dnes převážně souborová a skriptovaná. To je pro začátek v pořádku, ale není to enterprise-grade operating model.

Riziko:

- nekontrolovaná změna trust store
- nedostatečný audit rotace a revokace
- absence centrálního provider registry a konfigurační politiky

## Co je potřeba doplnit

### A. Cílové schopnosti systému

Systém by měl podporovat tři oddělené capability vrstvy:

1. Certificate Authentication
Použití klientského certifikátu jako autentizačního faktoru pro řízené scénáře.

2. eIDAS Identity Federation
Cross-border přihlášení přes eIDAS identity flow, typicky přes eIDAS node nebo enterprise broker.

3. Trust Validation
Vyhodnocení evropských certifikátů, podpisů a trust evidence nezávisle na login flow.

### B. Nové moduly

Doporučená cílová struktura:

- backend/modules/auth
  zůstává pouze pro token issuance, refresh, logout, session management
- backend/modules/eidas
  federované identity, callback flow, external identity mapping, assurance level
- backend/modules/trust
  X.509 validace, TSL/LOTL, QC parsing, OCSP/CRL, signature validation, provider registry
- backend/modules/users
  interní uživatel a vazba na credential bindingy a identity linky

### C. Nové doménové koncepty

Je potřeba zavést minimálně tyto entity nebo agregační modely:

- TrustProvider
- TrustAnchor
- TrustedService
- CertificateCredential
- ExternalIdentity
- IdentityBinding
- TrustValidationResult
- CertificateEnrollment
- CredentialRevocationEvent
- IdentityAuditEvent

## Jak tam dostat certifikáty

Existují dva různé scénáře a nesmí se míchat.

### Scénář 1. Trust store certifikáty providerů a CA

To jsou certifikáty a trust anchors, podle kterých systém rozhoduje, zda issuer patří mezi důvěryhodné autority.

Doporučený způsob:

- nepoužívat pouze ručně nahraný soubor ca.crt jako jediný zdroj pravdy
- zavést trust repository s verzovanou evidencí providerů a anchorů
- pravidelně synchronizovat EU LOTL/TSL a z nich generovat provozní trust store
- do gateway nasazovat pouze exportovanou a schválenou trust bundle verzi

Pro dev a fallback režim:

- ponechat file-based CA bundle v infra/docker/nginx/certs/<env>/mtls
- ale v dokumentaci i architektuře to označit jako operational artifact, ne jako zdroj pravdy

### Scénář 2. Uživatelské nebo servisní klientské certifikáty

To jsou konkrétní certifikáty navázané na interní účet nebo službu.

Doporučený způsob:

- nezakládat pouze fingerprint z ručního skriptu
- zavést enrollment endpoint nebo admin workflow
- ukládat plnou evidenci bindingu: subject, issuer, serial, thumbprint, policy OIDs, usage, provider, assurance, registration source, approvedBy, approvedAt, revokedAt
- umožnit více aktivních credentialů pro jednoho uživatele podle politiky

Doporučené workflow registrace:

1. administrátor nebo onboarding proces nahraje PEM/DER certifikát
2. trust modul provede syntaktickou a trust validaci
3. systém klasifikuje credential type a provider
4. systém vytvoří návrh bindingu
5. autorizovaný actor binding schválí
6. credential se stane aktivním pro login nebo signing use-case

## Doporučené endpointy

### Certificate authentication

- POST /api/v1/auth/certificates/login
- POST /api/v1/auth/certificates/challenge
- POST /api/v1/auth/certificates/bind
- POST /api/v1/auth/certificates/unbind
- GET /api/v1/auth/certificates/me

### eIDAS federation

- GET /api/v1/auth/eidas/login
- POST /api/v1/auth/eidas/callback
- POST /api/v1/auth/eidas/link
- POST /api/v1/auth/eidas/unlink
- GET /api/v1/auth/eidas/providers

### Trust services

- POST /api/v1/trust/certificates/validate
- POST /api/v1/trust/signatures/validate
- POST /api/v1/trust/enrollments
- POST /api/v1/trust/enrollments/{id}/approve
- POST /api/v1/trust/enrollments/{id}/reject
- POST /api/v1/trust/tsl/sync
- GET /api/v1/trust/providers

### Admin and audit

- GET /api/v1/admin/credentials
- POST /api/v1/admin/credentials/{id}/revoke
- GET /api/v1/admin/identity-audit-events

## Datový model, který dnes chybí

Současná tabulka user_certificate nestačí pro enterprise provoz. Doplnit minimálně:

- certificate_pem nebo normalized_cert_der_hash reference
- issuer_country
- provider_code
- provider_type
- credential_type
- trust_status
- revocation_status
- revocation_checked_at
- trust_checked_at
- policy_oids
- qc_statement_flags
- key_usage_flags
- extended_key_usage_flags
- registration_source
- approved_by
- approved_at
- last_successful_auth_at
- last_failed_auth_at
- failure_count
- assurance_level
- external_subject_id
- linked_identity_source

Doporučené nové tabulky:

- trust_provider
- trust_anchor
- trust_service
- certificate_credential
- external_identity
- identity_binding
- certificate_enrollment
- trust_validation_event
- identity_audit_event

## Clean Architecture doporučení

### Co přesunout z current state

Zodpovědnosti je potřeba přeuspořádat takto:

- API vrstva pouze přijímá request a vrací response
- application vrstva orchestrace use case a doménová rozhodnutí
- domain vrstva pravidla trust klasifikace, identity binding policy, enrollment policy
- infrastructure vrstva X.509 parsing adaptery, OCSP/CRL klienti, TSL downloader, gateway integration, persistence

### Co nemá zůstat v auth API vrstvě

Současný MtlsClientCertificateResolver je příliš blízko controlleru. Správnější je tento rozpad:

- api přijme request context
- application zavolá port pro získání certificate evidence z trusted gateway nebo requestu
- trust application service provede validaci a klasifikaci
- auth application service na základě výsledku vydá token

### Doporučené porty

- ClientCertificateEvidencePort
- TrustValidationPort
- TrustProviderRegistryPort
- TslSynchronizationPort
- RevocationStatusPort
- ExternalIdentityProviderPort
- CertificateEnrollmentPort
- IdentityBindingPort
- IdentityAuditPort

## Enterprise implementační strategie

### Varianta A. Hardening současného FNMT řešení

Vhodné pouze jako krátkodobý krok.

Co udělat:

- přejmenovat FNMT login na generic certificate login
- doplnit audit a enrollment flow
- zavést provider metadata
- doplnit testy revokace a validace evidence

Výhoda:

- rychlá stabilizace

Nevýhoda:

- stále bez plného eIDAS a EU trust coverage

### Varianta B. Trust platform bez eIDAS federation

Vhodné, pokud je priorita pracovat s evropskými certifikáty a podpisy, ale ne řešit zatím cross-border login občanů.

Co udělat:

- založit trust modul
- zavést TSL/LOTL, QC statements, OCSP/CRL
- provider registry a enrollment workflow

Výhoda:

- pokryje trust a certifikátové scénáře profesionálně

Nevýhoda:

- stále bez plnohodnotného eIDAS federation login flow

### Varianta C. Hybridní enterprise model

Doporučená cílová varianta.

Co udělat:

- harden current certificate auth
- přidat trust modul
- přidat eIDAS module jako separátní federation capability

Výhoda:

- čisté oddělení odpovědností
- škálovatelnost na další evropské providery
- správná enterprise architektura

Nevýhoda:

- vyšší počáteční investice

## Doporučená roadmapa implementace

### Fáze 1. Stabilizace stávajícího certificate login flow

Cíl:
Z current FNMT proof-of-concept udělat generický a auditovatelný certificate auth flow.

Úkoly:

- přejmenovat endpoint /api/v1/auth/fnmt/login na /api/v1/auth/certificates/login
- zachovat případně backward compatibility alias na omezenou dobu
- zavést CertificateCredential model a provider_code
- rozšířit persistence model o metadata a audit
- přidat admin endpointy pro bind, revoke, list
- doplnit integration testy a security testy

Deliverables:

- generic certificate auth
- audit trail
- lepší datový model credentialů

### Fáze 2. Trust module

Cíl:
Zavést samostatnou trust vrstvu pro evropské certifikáty.

Úkoly:

- vytvořit backend/modules/trust
- přidat X.509 parsing adapter
- přidat trust decision engine
- přidat OCSP/CRL klienty
- přidat TSL/LOTL synchronizaci
- přidat provider registry
- přidat validate certificate a validate signature endpointy

Technické doporučení:

- použít standardní knihovnu typu EU DSS, neimplementovat PKI validaci ručně

Deliverables:

- validace evropských trust providerů
- provozní trust store lifecycle
- signature validation foundation

### Fáze 3. Enrollment a identity binding

Cíl:
Zavést profesionální správu vazeb mezi certifikáty a interními účty.

Úkoly:

- enrollment request model
- approval workflow
- manual i API onboarding
- assurance level evidence
- multi-credential support per user
- audit a revocation events

Deliverables:

- správa credentialů pod kontrolou politiky
- bezpečný onboarding a revokace

### Fáze 4. eIDAS federation

Cíl:
Zavést cross-border identitní flow oddělené od certificate auth.

Úkoly:

- vytvořit backend/modules/eidas
- zvolit integraci přes eIDAS node nebo enterprise broker
- implementovat login redirect, callback, state/nonce protection
- mapovat external identity na internal account
- evidovat identity source a assurance attributes

Deliverables:

- eIDAS login flow
- oddělená federation capability

### Fáze 5. Compliance a operations

Cíl:
Dokončit enterprise provozní model.

Úkoly:

- immutable audit trail
- operational dashboards a alerts
- rotace trust store bundle
- incident response playbook
- conformance test matrix
- security review a penetration tests

Deliverables:

- production-ready operating model

## Testovací strategie

Je potřeba rozlišit čtyři vrstvy testů:

1. Unit tests
Trust klasifikace, enrollment policy, identity binding rules.

2. Integration tests
Persistence, gateway headers, trust adaptery, OCSP/CRL, TSL sync.

3. End-to-end tests
Certificate login, revokovaný certifikát, netrusted issuer, expired certifikát, unbound certifikát, eIDAS callback flow.

4. Conformance tests
Provider matrix, policy OIDs, QC statements, negative trust cases.

Minimální matice scénářů:

- valid active bound certificate
- expired certificate
- revoked certificate
- unsupported issuer
- trusted issuer but unsupported policy
- certificate with missing required key usage
- duplicate binding attempt
- certificate bound to disabled user
- eIDAS login with valid identity assertion
- eIDAS login with invalid signature or wrong audience

## Praktické doporučení pro tento projekt

Pro tento konkrétní codebase doporučuji tento postup:

1. Nepřidávat další provider-specific endpointy do auth controlleru.
2. Zafixovat current FNMT flow jako přechodný compatibility endpoint.
3. Navrhnout a založit trust modul dříve, než se bude rozšiřovat počet certifikátových providerů.
4. Vytáhnout certifikátovou evidenci z jednoduchého user_certificate modelu do plnohodnotného credential modelu.
5. eIDAS neimplementovat jako další variantu certificate login endpointu, ale jako samostatný federation flow.
6. Všechny evropské provider-specific detaily držet v trust/eidas modulech, ne v auth.

## Priority

### Bezprostředně

- generic certificate auth endpoint
- rozšířený credential model
- audit trail
- admin lifecycle operace

### Krátkodobě

- trust modul
- provider registry
- OCSP/CRL
- TSL/LOTL sync

### Střednědobě

- eIDAS federation modul
- identity linking
- advanced compliance a operational hardening

## Závěr

Projekt má dobrý základ pro certifikátový login v omezeném scénáři, ale zatím neobsahuje plnohodnotnou implementaci evropského trust a eIDAS modelu. Pokud má být řešení enterprise-grade, je nutné oddělit certificate auth, trust validation a eIDAS federation do samostatných capability vrstev a modulů. Teprve tím vznikne architektura, která bude škálovat na evropské certifikáty, více providerů i auditní a compliance požadavky.

Nejdůležitější praktické rozhodnutí je toto: FNMT login nepovažovat za finální architekturu, ale za přechodný kanál, který je potřeba zobecnit, obalit trust vrstvou a doplnit o plnohodnotný identity a credential lifecycle.