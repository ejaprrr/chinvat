# Redis Security Setup Instructions

## Overview

This directory contains security configuration and scripts for the two-Redis architecture:
- **redis-global**: Permissions and org data (read-only for auth service)
- **redis-auth**: App cache and eIDAS state (read-write for auth service)

## Files

- `SECURITY.md` - Comprehensive security guidelines and hardening steps
- `redis-acl-setup.sh` - Automated ACL configuration script (production)

## Quick Start

### Local Development

```bash
cd /Users/jiriposavad/Documents/FullStack/chinvat-malaga-2026

# Start local Redis instances with docker-compose
docker-compose -f infra/docker/compose.dev.yml up -d redis-global redis-auth postgres

# Verify both Redis instances are healthy
docker-compose -f infra/docker/compose.dev.yml ps
```

Both instances run without ACL in dev (on localhost only).

### Production Deployment

#### Prerequisites

1. **Two Redis 7+ instances running** (cloud provider or on-premise):
   - `redis-global`: Permissions/org data
   - `redis-auth`: App cache + eIDAS state
   
2. **TLS certificates** (optional but recommended):
   - `/etc/redis/certs/server.crt`
   - `/etc/redis/certs/server.key`

#### Step 1: Configure ACL on redis-global

```bash
# SSH to redis-global host
ssh ops@redis-global.internal

# Run ACL setup
/path/to/infra/scripts/redis-acl-setup.sh redis-global 6379 "your-strong-password"

# Output will show:
# - Admin password
# - ACL users created (default, monitoring, global-readonly)
# - Environment variables to set
```

#### Step 2: Configure ACL on redis-auth

```bash
# SSH to redis-auth host
ssh ops@redis-auth.internal

# Run ACL setup
/path/to/infra/scripts/redis-acl-setup.sh redis-auth 6379 "your-strong-password"

# Output will show:
# - Admin password
# - eidas-app password (for auth service)
# - monitoring password
# - Environment variables to set
```

#### Step 3: Harden Redis Server Configuration

```bash
# On both redis-global and redis-auth hosts:

# 1. Copy hardened config
sudo cp /path/to/infra/redis/redis-*.conf /etc/redis/

# 2. Update redis.conf with TLS paths
sudo nano /etc/redis/redis-global.conf
sudo nano /etc/redis/redis-auth.conf

# 3. Restart Redis to apply new config
sudo systemctl restart redis

# 4. Verify ACLs are active
redis-cli -h redis-global ping  # Will fail without password
redis-cli -h redis-global -a "your-password" ping  # OK
```

#### Step 4: Configure Firewall

```bash
# Block telnet/6379 from all but internal subnet
# (CRITICAL - prevents your telnet incident from happening)

sudo ufw reset
sudo ufw default deny incoming
sudo ufw default allow outgoing

# Allow SSH (for management)
sudo ufw allow 22/tcp

# Allow only from app servers (internal network 10.0.1.0/24)
sudo ufw allow from 10.0.1.0/24 to any port 6379
sudo ufw allow from 10.0.1.0/24 to any port 6380  # TLS

sudo ufw enable
sudo ufw status
```

#### Step 5: Set Environment Variables

In your deployment (Kubernetes secrets, Docker Compose env, etc.):

```bash
# From redis-global ACL setup output:
REDIS_GLOBAL_PASSWORD=<output-from-step-1>

# From redis-auth ACL setup output:
REDIS_AUTH_PASSWORD=<output-from-step-2>
EIDAS_APP_PASSWORD=<eidas-app-password-from-step-2>
MONITORING_PASSWORD=<monitoring-password-from-step-2>
```

#### Step 6: Deploy Backend Application

The backend is already configured to use:
- `redis-global` for permissions (read-only)
- `redis-auth` for app cache and eIDAS state (read-write with eidas-app credentials)

Environment variables in `docker-compose.prod.yml` will automatically use the new instances:

```bash
# Start production deployment
docker-compose -f infra/docker/compose.prod.yml up -d
```

#### Step 7: Verify Setup

```bash
# From backend container, verify connectivity
docker exec chinvat-backend redis-cli -h redis-global -a $REDIS_GLOBAL_PASSWORD ping
docker exec chinvat-backend redis-cli -h redis-auth -a $REDIS_AUTH_PASSWORD ping

# Monitor real-time activity
redis-cli -h redis-auth -a $REDIS_AUTH_PASSWORD MONITOR

# Check ACL logs for denied access attempts
redis-cli -h redis-auth -a $REDIS_AUTH_PASSWORD ACL LOG 10
```

## Testing ACL Rules

See `SECURITY.md` → "Testing Your Setup" for comprehensive verification script.

Quick test:
```bash
# Should work: eidas-app can write to app cache
redis-cli -h redis-auth -u redis://eidas-app:$EIDAS_APP_PASSWORD@redis-auth:6379/0 \
  SET chinvat:cache:test value

# Should work: eidas-app can read from app cache
redis-cli -h redis-auth -u redis://eidas-app:$EIDAS_APP_PASSWORD@redis-auth:6379/0 \
  GET chinvat:cache:test

# Should FAIL: eidas-app cannot access permissions namespace
redis-cli -h redis-auth -u redis://eidas-app:$EIDAS_APP_PASSWORD@redis-auth:6379/0 \
  SET chinvat:permissions:test value
# Expected: ACL: This user has no permissions to run the 'set' command or its subcommands

# Should FAIL: eidas-app cannot flush database
redis-cli -h redis-auth -u redis://eidas-app:$EIDAS_APP_PASSWORD@redis-auth:6379/0 \
  FLUSHDB
# Expected: ACL: This user has no permissions to run the 'flushdb' command
```

## Troubleshooting

### Connection refused on port 6379
- Ensure firewall rules allow your app server's IP
- Check Redis is bound to private network only (`bind 10.0.0.0/8`)
- Verify no `bind 0.0.0.0` in redis.conf

### "NOAUTH Authentication required"
- Set `REDIS_GLOBAL_PASSWORD` and `REDIS_AUTH_PASSWORD` env vars
- Or use: `redis-cli -h host -a password`

### "ACL: permission denied"
- You're using wrong user or wrong keyspace
- Verify with: `redis-cli -h redis-auth -a $ADMIN_PASSWORD ACL WHOAMI`
- Check rules: `redis-cli -h redis-auth -a $ADMIN_PASSWORD ACL GETUSER eidas-app`

### TLS connection errors
- Ensure certificates are in `/etc/redis/certs/`
- Verify TLS port (6380) is open in firewall
- Test: `redis-cli -h redis-auth --tls --cacert /path/to/ca.crt -a $PASSWORD ping`

## Incident Response

If you suspect unauthorized Redis access (like the telnet incident):

1. **Immediate Containment**:
   ```bash
   # Block all external access
   redis-cli CONFIG SET bind 127.0.0.1
   redis-cli CONFIG REWRITE  # Persist to disk
   ```

2. **Investigation**:
   ```bash
   # Check slow commands for suspicious activity
   redis-cli SLOWLOG GET 100 | grep -i "FLUSHDB\|DEL\|KEYS"
   
   # Check ACL denials
   redis-cli ACL LOG 100
   ```

3. **Recovery**:
   ```bash
   # Restore from backup
   redis-cli --pipe < /path/to/backup.rdb
   
   # Rotate all passwords
   ./infra/scripts/redis-acl-setup.sh redis-global 6379 "new-strong-password"
   ```

## Monitoring

Integrate with Prometheus/Grafana:

```yaml
# prometheus.yml
scrape_configs:
  - job_name: redis-global
    static_configs:
      - targets: ['redis-global:6379']
    metrics_path: /metrics
    params:
      redis_addr: ['redis-global:6379']
      redis_password: ['${REDIS_GLOBAL_PASSWORD}']

  - job_name: redis-auth
    static_configs:
      - targets: ['redis-auth:6379']
    metrics_path: /metrics
    params:
      redis_addr: ['redis-auth:6379']
      redis_password: ['${REDIS_AUTH_PASSWORD}']
```

Alert on:
- `redis_denied_commands_total` > 10 (unauthorized access attempts)
- `redis_memory_used_bytes` / `redis_memory_max_bytes` > 0.9 (near limit)
- No ping responses (service down)

## Reference

- Complete security documentation: `./SECURITY.md`
- ACL setup script: `../scripts/redis-acl-setup.sh`
- Docker Compose configs:
  - Dev: `../docker/compose.dev.yml`
  - Prod: `../docker/compose.prod.yml`
- Backend config:
  - Dev: `../../backend/app/src/main/resources/application.properties`
  - Prod: `../../backend/app/src/main/resources/application-prod.properties`
