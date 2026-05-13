# Chinvat Frontend Module

A self-contained, accessible, multilingual authentication UI module built with React, TypeScript, and Vite. Designed to be integrated into any project that needs user login, registration, and profile management.

## Features

- **User Authentication**
  - Login (username/password)
  - Registration
  - Password reset & change flows
  - Profile management (view/edit user info)

- **Accessibility (WCAG 2.1 AA)**
  - Full ARIA support
  - Keyboard navigation
  - Screen reader friendly
  - High contrast support

- **Internationalization (i18n)**
  - 5 languages: English, Spanish, Catalan, Basque, Galician
  - Easy to add more languages

- **Responsive Design**
  - Works on desktop, tablet, mobile
  - Touch-friendly
  - Progressive enhancement

- **Open Source**
  - MIT License
  - Zero external dependencies for auth logic

## Quick Start

### Development

```bash
cd frontend
npm install
npm run dev
```

The app runs on `http://localhost:5173` by default.

### Build

```bash
npm run build
```

Outputs optimized static files to `dist/` folder.

## Integration into Other Projects

### Option 1: Docker Build (Recommended)

Use the Dockerfile to build static assets in a clean, reproducible environment:

```bash
# From project root
docker build -t chinvat-frontend:build frontend/
docker create --name temp chinvat-frontend:build
docker cp temp:/dist ./frontend-dist
docker rm temp
```

Then serve `frontend-dist/` from your web server or CDN.

### Option 2: Local Build

```bash
cd frontend
npm install
npm run build
# Copy dist/ to your project's static assets folder
```

### Option 3: Volume Mount (Docker Compose)

For development, mount the built assets in your compose:

```yaml
services:
  web:
    image: nginx:alpine
    volumes:
      - ./frontend-dist:/usr/share/nginx/html:ro
    ports:
      - "3000:80"
```

## Configuration

The frontend reads configuration from a runtime-injected `config.js` file served at `/config.js`. This allows changing API endpoints without rebuilding.

### Environment Variables (Build Time)

Set during `npm run build`:

- `VITE_API_BASE_URL` - Base URL for backend API (e.g., `https://api.example.com`)
- `VITE_API_PREFIX` - API prefix (default: `/api/v1`)

Example:

```bash
VITE_API_BASE_URL=https://api.example.com npm run build
```

### Runtime Configuration (Recommended)

Serve a `config.js` file from your web server:

```javascript
// public/config.js or generated dynamically
window.__CHINVAT_CONFIG__ = {
  apiBaseUrl: 'https://api.example.com',
  apiPrefix: '/api/v1'
};
```

If not provided, the app defaults to development settings.

## Nginx Configuration

The repo includes an `nginx.conf` example for standalone serving. Use this as reference if you're setting up your own nginx:

```bash
# Reference the provided nginx.conf when configuring your server
# See nginx.conf for SPA routing, caching, and asset handling
```

Key features:
- SPA routing: all unknown routes serve `index.html`
- Cache busting: `assets/` cached for 365 days (with content-hash filenames)
- Config endpoint: `/config.js` always revalidated
- Security: hidden files denied

## API Contract

The frontend expects the backend to provide these endpoints:

### Authentication
- `POST /api/v1/auth/login` - Login with credentials
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/logout` - Logout
- `POST /api/v1/auth/password-reset` - Request password reset
- `POST /api/v1/auth/password-reset/confirm` - Complete reset with token

### Profile
- `GET /api/v1/auth/me` - Get current user profile
- `PUT /api/v1/profile` - Update profile
- `GET /api/v1/profile/certificates` - List user certificates
- `POST /api/v1/profile/certificates` - Add certificate
- `DELETE /api/v1/profile/certificates/{id}` - Revoke certificate

### eIDAS
- `POST /api/v1/auth/eidas/request` - Initiate eIDAS login
- `POST /api/v1/profile/eidas/complete` - Complete eIDAS profile

Token format: Bearer JWT in `Authorization` header.

## Development

### Build Commands

```bash
npm run dev          # Start dev server
npm run build        # Production build
npm run lint         # Check code quality
npm run format       # Format code with Prettier
npm run preview      # Preview production build
```

### Project Structure

```
src/
├── components/     # UI components (auth, forms, etc)
├── pages/          # Page components
├── hooks/          # Custom React hooks
├── lib/            # Utilities (API, validation, i18n, etc)
├── types/          # TypeScript types
├── contexts/       # React contexts
├── locales/        # i18n translations
├── layouts/        # Layout components
├── config/         # App configuration
└── router/         # React Router setup
```

### Adding Languages

Edit `src/locales/` to add new languages:

1. Create `src/locales/{lang}.json`
2. Copy structure from existing language file
3. Translate all strings
4. Register in `src/lib/i18n/`

## Browser Support

- Chrome/Edge 90+
- Firefox 88+
- Safari 14+
- Mobile browsers (iOS Safari, Chrome Mobile)

## Performance

- Code splitting: Routes are lazy-loaded
- Asset hashing: Cache-busting with content-based filenames
- Tree-shaking: Unused code removed at build time
- Gzip compression: Enabled at server level

Typical bundle:
- Main bundle: ~50-70 KB (gzipped)
- Total size: ~100-150 KB (all assets gzipped)

## Accessibility

All components follow WCAG 2.1 Level AA standards:

- Semantic HTML
- ARIA labels and roles
- Keyboard navigation (Tab, Enter, Escape)
- Focus management
- Color contrast (WCAG AA)
- Screen reader tested

## License

MIT - See [LICENSE](../LICENSE)

## Support

For issues, feature requests, or integration questions, open an issue in the repository.
