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

You can copy `.env.example` into `.env` inside the `frontend/` folder to configure the app before running it.

## License

See [LICENSE](LICENSE).
