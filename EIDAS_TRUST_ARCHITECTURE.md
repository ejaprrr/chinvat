# eIDAS, FNMT, Certifikaty a Trust: Vysvetleni Od Nuly

Tento dokument je psany pro cloveka, ktery to vidi poprve. Cilem je pochopit:

1. co je FNMT certifikat
2. co je eIDAS
3. co je broker
4. co je callback a proc existuje
5. proc existuje modul `trust` a k cemu je DSS knihovna
6. co se uklada do Redis a co do databaze
7. co uz je v implementaci hotove a co jeste chybi

## 1) Velmi strucna intuice

Mas dve hlavni veci, ktere spolu souvisi, ale nejsou stejne:

- federovana identita (eIDAS login flow)
- kryptograficka duvera v certifikaty (trust a validace)

Proto jsou v repozitari dva moduly:

- `eidas`: stara se o federacni login tok (redirect, callback, state, broker)
- `trust`: stara se o duveryhodnost certifikatu a trust listu (DSS, LOTL, TSL)

## 2) Co je FNMT certifikat

FNMT je spanelska certifikacni autorita. V praxi to znamena, ze uzivatel muze mit klientsky certifikat, kterym prokazuje identitu.

Takovy certifikat je technicky X.509 certifikat (standardni format certifikatu), ktery obsahuje napriklad:

- kdo certifikat vydal (issuer)
- komu patri (subject)
- serial number
- platnost od-do
- kryptograficky verejny klic

Pri prihlaseni certifikatem aplikace typicky:

1. obdrzi certifikat od gateway/reverse proxy
2. vytvori otisk certifikatu (thumbprint, typicky SHA-256)
3. najde interni vazbu "tento certifikat patri tomuto uzivateli"
4. pusti uzivatele dal

To je certifikat-first login bez federace.

## 3) Co je eIDAS

eIDAS je evropsky ramec pro elektronickou identitu a duveryhodne sluzby. Prakticky:

- uzivatel muze prijit s identitou z jineho statu/sluzby
- tvoje aplikace ji prijme pres federacni tok
- dostanes identity claims (napr. identifikator, jmeno, uroven zaruky)

eIDAS flow neni to same co lokalni mTLS certifikat flow. Je to federace identity.

## 4) Co je broker a proc ho mate

Broker je prostrednik mezi tvoji aplikaci a federacnim svetem (eIDAS uzly/IDP).

Proc je dobry:

- aplikace nemusi implementovat vsechny protokolarni detaily sama
- broker sjednocuje rozhrani pro ruzne providery
- aplikace vola jedno konzistentni API

V kodu je broker adapter implementovany jako HTTP klient:

- trida `HttpEidasBrokerAdapter`
- nacita seznam provideru
- vyzada login URL
- vymeni auth code za identity data

## 5) Co je callback a proc existuje

Callback je navratovy krok po redirect loginu.

Logika je stejna jako u OAuth2/OIDC:

1. uzivatel klikne "prihlasit"
2. aplikace ho presmeruje pryc (na broker/IDP)
3. uzivatel se overi mimo tvoji aplikaci
4. externi system vrati uzivatele zpatky (callback)

Proc je callback nutny:

- overeni identity probiha mimo tvuj backend
- backend potrebuje bezpecne dodelat transakci po navratu
- callback nese data (napr. authorization code), ktera backend vymeni za claims

## 6) Co je `state` a proc je to dulezite

`state` je jednorazovy token transakce loginu.

Pouziva se na ochranu proti:

- CSRF
- zneuziti callbacku z jine transakce
- replay starych callbacku

V tomto projektu:

1. pri login startu se vygeneruje nahodne `state`
2. ulozi se do Redis s expiraci (TTL)
3. pri callbacku se `state` "consume" (cteni + smazani)
4. kdyz chybi/nesedi/expiruje, callback se odmitne

To je kriticky bezpecnostni krok.

## 7) Kde se co deje v kodu (krok po kroku)

### 7.1 Start loginu

Endpoint:

- `POST /api/v1/auth/eidas/login`

Co se stane:

1. overi se, ze provider existuje a je enabled
2. spocte se expirace podle `stateTtl`
3. vygeneruje se UUID state
4. ulozi se do Redis (`state`, `providerCode`, `expiresAt`)
5. broker vrati authorization URL

Vysledek pro frontend:

- URL, kam ma uzivatele presmerovat
- state identifikator transakce

### 7.2 Callback po navratu

Endpoint:

- `POST /api/v1/auth/eidas/callback`

Co se stane:

1. nacte a spotrebuje se `state` z Redis
2. overi se, ze provider callbacku odpovida puvodnimu provideru
3. overi se expirace state
4. zavola se broker exchange auth code -> claims
5. hleda se, jestli external identita uz ma interni `userId`
6. ulozi se/aktualizuje `external_identity` zaznam
7. vrati se stav:
	- `ACTIVE`, pokud je uz linked user
	- `PENDING_PROFILE`, pokud linked user neni
8. vraci se `profileCompletionRequired` boolean

Tj. zadne admin schvalovani. Uzivatel jde rovnou do dokonceni profilu.

## 8) Proc Redis vs proc databaze

### Redis

Vhodny pro kratkodoba data transakce:

- login state
- expirace
- jednorazova konzumace

Charakteristika:

- rychle
- TTL
- neni to dlouhodoby auditni zdroj

### Databaze

Vhodna pro dlouhodoba a auditovatelna data:

- `external_identity`
- vazby certifikatu na uzivatele
- historie a forenzni stopa

Pravidlo:

- docasny transakcni kontext -> Redis
- dlouhodoba identita a historie -> DB

## 9) K cemu je DSS knihovna a trust modul

Tohle je cast, kterou casto lide pletou s eIDAS brokerem.

Neni to stejne.

- broker = federacni komunikace login flow
- DSS = kryptograficka a trust validace certifikatu

### 9.1 Co DSS realne dela

DSS (Digital Signature Services) knihovna umi:

1. nacist X.509 certifikat
2. ziskat metadata (subject, issuer, serial, validity)
3. spocitat fingerprint
4. overit, jestli certifikat pochazi z duveryhodneho zdroje

V projektu to dela `DssCertificateValidationAdapter`.

### 9.2 LOTL a TSL

- LOTL = List Of Trusted Lists (evropsky "seznam seznamu")
- TSL = Trusted Service List (narodni/autoritativni seznam duveryhodnych sluzeb)

`DssTrustedProviderSyncAdapter` synchronizuje trust data z LOTL/TSL.

Proc je to dulezite:

- duvera v certifikat nesmi byt hardcoded
- trust zdroje se meni v case
- musis umet refresh a auditovatelnost

## 10) Jak spolu FNMT, eIDAS a Trust souvisi

### FNMT certifikat login

- certifikat-first
- rychly local flow
- trust validace certifikatu je zasadni

### eIDAS federace

- redirect/callback flow pres broker
- identity claims prichazeji zvenci
- musis hlidat state a callback integritu

### Spolecny jmenovatel

- bezpecnost identity
- audit
- jasna pravidla vazby identity -> interni user

Proto modulove oddeleni (`eidas` a `trust`) dava smysl.

## 11) Aktualni rozhodnuti v tomto repu

Plati:

1. quick login je primarni vstup
2. admin approve/reject flow se nepouziva
3. bez navazaneho uzivatele je stav `PENDING_PROFILE`
4. frontend ma uzivatele dovest do profile completion

Tj. onboarding je rychly, ale stale kontrolovany business pravidly pri aktivaci profilu.

## 12) Co je hotove a co chybi

### Hotove

- callback flow bez admin schvalovani
- status `PENDING_PROFILE`
- response flag `profileCompletionRequired`
- per-environment broker konfigurace
- Redis state adapter + E2E test

### Chybi

- endpointy pro spravu certifikatu v profilu (list/add/remove/primary)
- endpoint pro dokonceni profilu po callbacku
- validacni pravidla pred aktivaci uctu
- dalsi audit eventy pro profile completion a certifikatovou spravu
- navazne integracni testy profilu a certifikatove spravy

## 13) Nejdulezitejsi takeaways pro prvni porozumeni

1. Broker neni trust engine. Broker je federacni transport a normalizace identity dat.
2. DSS neni login redirect vrstva. DSS je knihovna pro certifikaty a duveryhodnost.
3. Callback je normalni a nutny navrat po externim overeni.
4. Redis drzi jen kratky, jednorazovy kontext transakce.
5. Databaze drzi dlouhodobou identitu a audit.
6. `PENDING_PROFILE` znamena: identita prisla, ale profil jeste neni kompletni pro plnou aktivaci.
