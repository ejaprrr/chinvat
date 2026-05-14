# Build stage
FROM node:22-slim AS builder

WORKDIR /app

# Copy package files only
COPY package.json ./

# Clean install (no existing node_modules or package-lock to avoid platform issues)
RUN npm install

# Copy source
COPY . .

# Build frontend
RUN npm run build

# Runtime stage - use alpine for small size
FROM nginx:alpine

# Install bash and gettext for better compatibility
RUN apk add --no-cache bash gettext

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf.template /etc/nginx/templates/default.conf.template

# Copy built frontend from builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy entrypoint script that injects runtime config
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/health || exit 1

EXPOSE 80

ENTRYPOINT ["/entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
