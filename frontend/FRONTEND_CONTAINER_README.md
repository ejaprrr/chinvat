# Chinvat Frontend - Containerized Module

This is a containerized frontend module that can run independently with any compatible backend API.

## Quick Start

### Using Docker Compose (Recommended)

```bash
# Run frontend pointing to your backend
API_BASE_URL=http://your-backend:8080 docker compose up -d

# Frontend will be available at http://localhost:3000
```

### Using Docker Directly

```bash
# Build the image
docker build -t chinvat-frontend:latest .

# Run the container pointing to your backend
docker run -d \
  -e API_BASE_URL=http://your-backend:8080 \
  -p 3000:80 \
  chinvat-frontend:latest
```

### Using Podman

```bash
# Build with Podman
podman build -t chinvat-frontend:latest .

# Run with Podman
podman run -d \
  -e API_BASE_URL=http://your-backend:8080 \
  -p 3000:80 \
  chinvat-frontend:latest
```

## Configuration

### Environment Variables

| Variable        | Default                 | Description                                                                     |
| --------------- | ----------------------- | ------------------------------------------------------------------------------- |
| `API_BASE_URL`  | `http://localhost:8080` | Backend API base URL (e.g., `http://backend:8080` or `https://api.example.com`) |
| `FRONTEND_PORT` | `3000`                  | Port to expose frontend (in docker-compose)                                     |

### Custom Port

```bash
# Using compose
FRONTEND_PORT=8000 docker compose up -d

# Using docker run
docker run -d \
  -e API_BASE_URL=http://your-backend:8080 \
  -p 8000:80 \
  chinvat-frontend:latest
```

## How It Works

1. **Build Phase**: Multi-stage Dockerfile builds the React/TypeScript frontend with Vite
2. **Runtime Injection**: Environment variable `API_BASE_URL` is injected into the runtime config
3. **Nginx Serving**: Production-grade Nginx server with:
   - Gzip compression enabled
   - Proper cache headers (versioned assets cached 1 year, index.html not cached)
   - SPA routing (all routes serve index.html)
   - API proxy to backend
   - Security headers (X-Frame-Options, X-Content-Type-Options, etc.)

## Integration Examples

### With Your Own Backend

```bash
# Backend runs on different server/container
API_BASE_URL=https://api.mycompany.com docker compose up -d
```

### In Kubernetes

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: chinvat-frontend
spec:
  containers:
    - name: frontend
      image: chinvat-frontend:latest
      env:
        - name: API_BASE_URL
          value: 'http://chinvat-backend:8080'
      ports:
        - containerPort: 80
      livenessProbe:
        httpGet:
          path: /health
          port: 80
        initialDelaySeconds: 5
        periodSeconds: 30
```

### Docker Compose with Backend

```yaml
version: '3.9'
services:
  backend:
    image: your-backend:latest
    ports:
      - '8080:8080'

  frontend:
    build:
      context: .
      dockerfile: Dockerfile
    environment:
      API_BASE_URL: http://backend:8080
    ports:
      - '3000:80'
    depends_on:
      - backend
```

## Features

✅ Production-grade containerization
✅ Multi-stage build for minimal image size
✅ Runtime configuration (no rebuild needed for different backends)
✅ Nginx reverse proxy with compression
✅ SPA routing support
✅ Security headers
✅ Health check endpoint (`/health`)
✅ API proxy to backend
✅ Proper cache busting for versioned assets

## Building for Production

```bash
# Build without compose
docker build -t chinvat-frontend:1.0.0 .

# Tag for registry
docker tag chinvat-frontend:1.0.0 registry.example.com/chinvat-frontend:1.0.0

# Push to registry
docker push registry.example.com/chinvat-frontend:1.0.0
```

## Troubleshooting

### Frontend can't reach backend

Check that `API_BASE_URL` is correct and accessible from the container:

```bash
# View running container logs
docker compose logs frontend

# Test backend connectivity
docker exec -it <container-id> wget -q -O- http://your-backend:8080/health
```

### CORS errors from backend

The backend must have CORS configured to allow the frontend origin. Common frontend URLs:

- `http://localhost:3000` (default frontend port)
- `http://localhost` (when accessed via reverse proxy)
- `https://yourdomain.com` (production)

### Images not loading

Check that versioned assets are being served correctly with cache headers:

```bash
curl -I http://localhost:3000/assets/main.abc123.js
```

Should return: `Cache-Control: public, immutable` and `expires` far in future.

## Development

To modify the frontend:

```bash
# Install dependencies
npm install

# Run dev server
npm run dev

# Build for production
npm run build

# Lint
npm lint

# Format
npm run format
```

## Image Size

- Base nginx:alpine: ~45 MB
- Built frontend: ~500 KB - 2 MB
- Total: ~45-47 MB

## License

See LICENSE file in root directory.
