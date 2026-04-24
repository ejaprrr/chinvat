# Database

Migrace jsou spravovány přes **Prisma Migrate**. Seed data jsou čistá SQL.

## Auth/Identity schema (Chinvat)

Aktuální model splňuje požadovanou strukturu a zároveň přidává bezpečnostní rozšíření:

- `app_user` (User Table)
  - `id`, `username`, `full_name`, `phone_number`, `email`
  - `user_type` (`INDIVIDUAL`, `LIBRARY`)
  - `access_level` (`SUPERADMIN`, `ADMIN`, `GOLD`, `PREMIUM`, `NORMAL`)
  - `address_line`, `postal_code`, `city`, `country`, `default_language`
- `user_password` (Password Table)
  - `user_id` (FK na `app_user.id`)
  - `password_hash` (+ metadata změny hesla)
- `password_recovery_token`
  - tokeny pro obnovu hesla (expirace + single-use přes `used_at`)
- `auth_session`
  - hash session tokenu, expirace/revokace
  - unikátní hash tokenu + omezení jedné aktivní nerevokované session na uživatele
- `user_certificate`
  - podpora loginu přes digitální certifikát (subject/issuer/fingerprint/platnost)

## Struktura

```
database/
  Dockerfile.migrate          # Node image s Prisma CLI – spouštěn v Docker Compose i CI/CD
  prisma/
    schema.prisma             # Zdrojová pravda schématu databáze
    package.json
    migrations/
      20260423000000_init_identity/migration.sql
  scripts/
    seed.sh                   # Generický seed runner (psql)
    reset.sh                  # DEV ONLY – DROP SCHEMA + CREATE SCHEMA
  seeds/
    dev/001_seed.sql          # Idempotentní (ON CONFLICT DO NOTHING + audit guard)
    prod/001_seed.sql         # Idempotentní přes PROD_SEED_COMPLETED audit event
```

## Make targety

| Target | Prostředí | Popis |
|--------|-----------|-------|
| `make db-migrate` | dev | Spustí `prisma migrate deploy` proti dev DB |
| `make db-seed` | dev | Nahraje seed data (bezpečné opakovat) |
| `make db-reset` | **DEV ONLY** | DROP schema → migrate → seed |

> ⚠️ `db-reset` je chráněn proměnnou `ALLOW_DB_RESET=true`.
> V produkci tato proměnná **nikdy** nesmí být nastavena.

## Jak přidat novou migraci

1. Uprav `prisma/schema.prisma`
2. Lokálně spusť:
   ```sh
   cd infra/database/prisma
   npx prisma migrate dev --name popis_zmeny
   ```
3. Commitni vygenerovaný soubor v `prisma/migrations/`
4. CI/CD pipeline spustí `prisma migrate deploy` automaticky při deployi na main

## Bezpečnostní garance pro produkci

- `compose.prod.yml` **neobsahuje** service `db-reset`
- `reset.sh` odmítne spustit bez `ALLOW_DB_RESET=true`
- CD pipeline spouští **pouze** `prisma migrate deploy` (nikdy reset)
- Prod seed kontroluje audit event `PROD_SEED_COMPLETED` – nespustí se dvakrát
