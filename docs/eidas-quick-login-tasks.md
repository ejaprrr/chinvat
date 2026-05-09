# eIDAS Quick Login Tasks

## Rozhodnuti
- [x] Pouzit quick login jako primarni vstup (certifikat/eIDAS first).
- [x] Zrusit admin approve/reject flow pro external identity link.
- [x] Pouzit stav `PENDING_PROFILE` misto `PENDING_LINK`.

## Backend API
- [x] Odstranit endpointy `/api/v1/admin/eidas/identities/*`.
- [x] Upravit callback response: `profileCompletionRequired` misto `linkRequired`.
- [x] Udrzet `currentStatus` (`ACTIVE` nebo `PENDING_PROFILE`).

## Profil a certifikaty
- [ ] Pridat endpoint `GET /api/v1/profile/certificates`.
- [ ] Pridat endpoint `POST /api/v1/profile/certificates` (bind noveho certifikatu).
- [ ] Pridat endpoint `DELETE /api/v1/profile/certificates/{id}` (revoke/odpojeni).
- [ ] Pridat endpoint `PATCH /api/v1/profile/certificates/{id}/primary`.
- [ ] Pridat endpoint pro dokonceni profilu po eIDAS (`PATCH /api/v1/profile/completion`).

## Bezpecnost
- [ ] Vynutit policy, ze quick login je mozny jen pro certifikaty ve stavu ACTIVE.
- [ ] Pridat audit eventy pro profile completion a certifikatovou spravu.
- [ ] Dopsat business validace povinnych poli pred aktivaci uctu.

## Data model a migrace
- [ ] Rozhodnout, zda review sloupce v `external_identity` zustanou pro audit nebo se odstrani.
- [ ] Pokud se sloupce odstrani, pridat navazujici Prisma/SQL migraci.

## Testy
- [x] Smazat admin flow testy.
- [x] Upravit eIDAS testy na `PENDING_PROFILE/profileCompletionRequired`.
- [ ] Dopsat integracni test dokonceni profilu po callbacku.
- [ ] Dopsat testy profile certificate management endpointu.
