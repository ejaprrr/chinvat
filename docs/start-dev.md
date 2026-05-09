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

=======

# CHINVAT

## Overview

CHINVAT is an open-source, cross-platform, accessible, and multilingual authentication and profile management module. It is designed for seamless integration with backend APIs and supports modern accessibility and internationalization standards.

## Features

- User login (username/password, digital certificate planned)
- Password reset and change flows
- Profile management (view/edit user info)
- Accessibility (ARIA, keyboard navigation, color contrast)
- Multilingual (i18n)
- Responsive design (web/mobile)
- Open source (MIT License)

## Accessibility & i18n

- All forms and components use ARIA attributes and keyboard navigation.
- Text is fully internationalized using i18n (see `src/locales/`).

## Configuration

The frontend reads environment variables through Vite.

- `VITE_API_BASE_URL` — base URL for the backend API (for example `http://192.168.181.152`).
- `VITE_RESET_PASSWORD_URL` — optional client-side password reset redirect URL.

You can copy `.env.example` into `.env` inside the `frontend/` folder to configure the app before running it.

## License

See [LICENSE](LICENSE).
