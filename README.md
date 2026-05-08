# Chinvat

## Požadavky

- [Docker](https://docs.docker.com/get-docker/) + Docker Compose
- [GNU Make](https://www.gnu.org/software/make/)
- Java 21+ (pouze pro lokální spuštění backendu bez Dockeru)

## Rychlý start – dev

### 1. Připrav prostředí

Zkopíruj vzorový soubor do rootu projektu:

```bash
cp infra/docker/.env.dev.example .env
```

Uprav hodnoty v `.env` dle potřeby (výchozí hodnoty fungují pro lokální vývoj).

### 2. Spusť dev-stack

```bash
make dev-up
```

Tím se spustí celý dev stack (databáze, backend, nginx) přes Docker Compose.

### 3. Zastav dev-stack

```bash
make dev-down
```
