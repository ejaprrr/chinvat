# Redis Security Guidelines

## Architecture Overview

### Two Redis Instances

```
┌──────────────────────────────────────────────────────────────┐
│                    PRODUCTION DEPLOYMENT                      │
└──────────────────────────────────────────────────────────────┘

┌─────────────────────┐                ┌──────────────────────┐
│   redis-global      │                │    redis-auth        │
├─────────────────────┤                ├──────────────────────┤
│ Keyspace:           │                │ Keyspace:            │
│ • permissions:*     │                │ • eidas:state:*      │
│ • org:*             │                │ • users:*            │
│ • (shared data)     │                │ • trust:*            │
│                     │                │ • cache:*            │
│ Access: READ-ONLY   │                │ (auth service only)  │
│ (for auth service)  │                │                      │
│                     │                │ Access: READ-WRITE   │
│ Users:              │                │ (eidas-app user)     │
│ • default (admin)   │                │                      │
│ • global-readonly   │                │ Users:               │
│ • monitoring        │                │ • default (admin)    │
└─────────────────────┘                │ • eidas-app          │
        ↑                              │ • monitoring         │
   Other services                      └──────────────────────┘
   (read permissions)                           ↑
                                      Auth Service
                                      (read-write)
```

## Security Model: ACL vs. Database Index vs. Key Prefix

### Problem We're Solving

Without isolation, a compromised service could:
- Read OAuth tokens (eIDAS state)
- Modify user cache data
- Delete all data (FLUSHDB)
- Elevate privileges

### Solution: Layered Defense

```
┌─────────────────────────────────────────┐
│  Layer 1: Network (firewall rules)      │  ← Only private network access
│          bind 10.0.0.0/8, NO 0.0.0.0    │
├─────────────────────────────────────────┤
│  Layer 2: Transport (TLS)               │  ← Encrypt in transit
│          redis.conf: tls-port 6380      │
├─────────────────────────────────────────┤
│  Layer 3: Authentication (requirepass)  │  ← Strong password enforcement
│          redis.conf: requirepass        │
├─────────────────────────────────────────┤
│  Layer 4: Authorization (ACL)           │  ← Per-user, per-command, per-key
│          ACL SETUSER eidas-app ...      │  ← Most important layer
├─────────────────────────────────────────┤
│  Layer 5: Namespacing (DB index)        │  ← Logical separation
│          SELECT 0 (app cache)           │  ← Database 0-15
│          SELECT 1 (eIDAS state)         │  ← Even if ACL breached
├─────────────────────────────────────────┤
│  Layer 6: Key Prefix (monitoring)       │  ← Helps identify misuse
│          chinvat:eidas:*, chinvat:*     │  ← Easy to audit
└─────────────────────────────────────────┘
```

### Key Differences

| Feature | Database Index | Key Prefix | ACL |
|---------|---|---|---|
| **Enforcement** | Hard (Redis enforces) | Soft (just naming) | Hard (Redis enforces) |
| **Performance** | No overhead | Minimal (string match) | Minimal |
| **Compliance** | No – access to all keys if you know DB# | No – access if you guess prefix | Yes – denied at Redis level |
| **For Production** | ✅ Use with ACL | ⚠️ Helpful but not enough | ✅ Required |
| **Best Practice** | ACL + DB index | Always | Always |

**Bottom Line**: ACL + DB index + key prefix = defense in depth

---

## Production Configuration Checklist

### Redis Server Configuration

Create `/etc/redis/redis-global.conf`:

```bash
# Network security - CRITICAL
bind 10.0.0.0/8 127.0.0.1          # NO 0.0.0.0, NO exposed IPs
protected-mode yes                  # Refuse anonymous connections
port 6379
timeout 0

# Authentication
requirepass "strong-random-password-64-chars-min"
user default >strong-password +@all ~* -FLUSHDB -FLUSHALL -SHUTDOWN

# Persistence (depends on your RPO/RTO)
save 900 1                          # Save if 1 key changed in 15 min
save 300 10                         # Save if 10 keys changed in 5 min
appendonly yes                      # AOF for durability
appendfilename "appendonly-global.aof"

# Security hardening
always-show-logo no
logfile /var/log/redis/redis-global.log
loglevel notice
databases 16                        # Use only DB 0 for global data

# Performance tuning
maxmemory 2gb                       # Adjust based on your needs
maxmemory-policy allkeys-lru        # Evict least recently used if full
lazyfree-lazy-eviction yes          # Non-blocking eviction

# Monitoring
latency-monitor-threshold 100       # Warn if commands take > 100ms
slowlog-log-slower-than 10000       # Log commands > 10ms
slowlog-max-len 128

# TLS (production only)
tls-port 6380
tls-cert-file /etc/redis/certs/redis-global.crt
tls-key-file /etc/redis/certs/redis-global.key
tls-ca-cert-file /etc/redis/certs/ca.crt
tls-protocols "TLSv1.2 TLSv1.3"
tls-ciphers TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256:TLS_AES_128_GCM_SHA256
tls-ciphersuites "HIGH:!aNULL:!MD5"
```

Similar for `redis-auth.conf` (same settings, different log file names).

### ACL Configuration

After running `redis-acl-setup.sh`, verify with:

```bash
# List all ACL rules
redis-cli -h redis-global -a <password> ACL LIST

# Test eidas-app user permissions
redis-cli -h redis-auth -u redis://eidas-app:<password>@redis-auth:6379/0 PING

# Try to access forbidden keyspace (should fail)
redis-cli -h redis-auth -u redis://eidas-app:<password>@redis-auth:6379/0 SET permissions:test value
# Result: ACL: This user has no permissions to run the 'set' command or its subcommands
```

### Firewall Rules

```bash
# Block telnet access (critical - user's specific risk)
sudo ufw delete allow 6379/tcp
sudo ufw delete allow 6380/tcp

# Allow only from application servers
sudo ufw allow from 10.0.1.0/24 to any port 6379
sudo ufw allow from 10.0.1.0/24 to any port 6380

# No 0.0.0.0:6379 - EVER
# If you need external access: use SSH tunnel, NOT exposed Redis

# Verify
sudo ufw status
```

### Docker Security (for Kubernetes deployments)

```yaml
# kubernetes/redis-global-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-global
spec:
  replicas: 1
  template:
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        command:
        - redis-server
        - /etc/redis/redis-global.conf
        - --requirepass
        - $(REDIS_PASSWORD)
        - --tls-port
        - "6380"
        - --tls-cert-file
        - /etc/redis/certs/server.crt
        - --tls-key-file
        - /etc/redis/certs/server.key
        ports:
        - name: redis
          containerPort: 6379
        - name: redis-tls
          containerPort: 6380
        env:
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: redis-global-secret
              key: password
        volumeMounts:
        - name: redis-config
          mountPath: /etc/redis
        - name: redis-certs
          mountPath: /etc/redis/certs
          readOnly: true
        securityContext:
          readOnlyRootFilesystem: true
          runAsNonRoot: true
          runAsUser: 999
      volumes:
      - name: redis-config
        configMap:
          name: redis-global-config
      - name: redis-certs
        secret:
          secretName: redis-tls-certs
```

---

## Monitoring & Alerting

### Critical Metrics

```prometheus
# Prometheus queries to alert on:

# 1. Failed authentication attempts
redis_keyspace_misses_total > 100 in 5m

# 2. Unauthorized ACL denials
redis_denied_commands_total > 10 in 5m

# 3. Memory near limit
redis_memory_used_bytes / redis_memory_max_bytes > 0.9

# 4. Slowlog indicating DoS
redis_slowlog_length > 50

# 5. Replication lag (if using sentinel)
redis_replication_offset_lag > 1000
```

### Audit Logging

```bash
# Enable Redis ACL logging
redis-cli CONFIG SET acllog-max-len 512

# Monitor real-time activity
redis-cli MONITOR

# Review slow commands
redis-cli SLOWLOG GET 100
```

---

## Password Management

### Rotation Policy

1. **Initial Setup** (first deployment):
   ```bash
   ./infra/scripts/redis-acl-setup.sh redis-auth.prod 6379 $(openssl rand -base64 32)
   ```

2. **Quarterly Rotation**:
   ```bash
   # Generate new passwords
   NEW_EIDAS_PASSWORD=$(openssl rand -base64 32)
   
   # Update in secrets manager (Vault, AWS Secrets, etc.)
   vault kv put secret/redis/eidas-app password=$NEW_EIDAS_PASSWORD
   
   # Update Redis
   redis-cli -h redis-auth -a $OLD_PASSWORD ACL SETUSER eidas-app ">$NEW_EIDAS_PASSWORD"
   
   # Restart backend pods to pick up new password
   kubectl rollout restart deployment/backend
   ```

3. **Emergency Rotation** (suspected breach):
   ```bash
   # IMMEDIATE: Revoke compromised user
   redis-cli -h redis-auth -a $ADMIN_PASSWORD ACL DELUSER eidas-app
   
   # Investigate logs
   redis-cli SLOWLOG GET 100 | grep -i suspicious
   
   # Recreate with strong new password
   ./infra/scripts/redis-acl-setup.sh redis-auth.prod 6379 $ADMIN_PASSWORD
   ```

---

## Common Issues & Troubleshooting

### Problem: "DENIED Redis is in cluster mode"
**Cause**: Cluster mode enabled without intending it  
**Fix**: Ensure `cluster-enabled no` in redis.conf

### Problem: "Error: MISCONF Redis is configured to save RDB snapshots..."
**Cause**: AOF or RDB misconfigured  
**Fix**:
```bash
redis-cli CONFIG SET stop-writes-on-bgsave-error no
redis-cli BGSAVE
```

### Problem: "ACL: This user has no permissions to run 'ping' command"
**Cause**: User ACL too restrictive  
**Fix**: Add `+PING` to user's command list:
```bash
redis-cli ACL SETUSER eidas-app +PING
```

### Problem: "Connection refused on 0.0.0.0:6379"
**Cause**: Telnet port exposed (YOUR EXACT RISK)  
**Fix**:
```bash
# Immediately remove from all network interfaces
redis-cli -p 6379 SHUTDOWN
# Edit redis.conf: bind 127.0.0.1
redis-server /etc/redis/redis-global.conf
```

### Problem: "NOAUTH Authentication required"
**Cause**: Trying to connect without password  
**Fix**:
```bash
# Correct way
redis-cli -h redis-auth -a $PASSWORD

# Or use URL format
redis-cli -u redis://eidas-app:password@redis-auth:6379/0
```

---

## Testing Your Setup

### Pre-Production Verification

```bash
#!/bin/bash
set -e

echo "1. Testing redis-global connectivity..."
redis-cli -h redis-global -a $REDIS_GLOBAL_PASSWORD ping

echo "2. Testing redis-auth connectivity..."
redis-cli -h redis-auth -a $REDIS_AUTH_PASSWORD ping

echo "3. Testing eidas-app ACL permissions..."
# Should work
redis-cli -h redis-auth -u redis://eidas-app:$EIDAS_APP_PASSWORD@redis-auth:6379/0 \
  SET chinvat:cache:test:value test-data
echo "  ✓ Can write to chinvat:cache:*"

# Should work
redis-cli -h redis-auth -u redis://eidas-app:$EIDAS_APP_PASSWORD@redis-auth:6379/0 \
  GET chinvat:cache:test:value
echo "  ✓ Can read from chinvat:cache:*"

# Should fail
echo "4. Testing ACL denials (expected to fail)..."
redis-cli -h redis-auth -u redis://eidas-app:$EIDAS_APP_PASSWORD@redis-auth:6379/0 \
  SET permissions:test value || echo "  ✓ Correctly denied access to permissions:*"

# Should fail
redis-cli -h redis-auth -u redis://eidas-app:$EIDAS_APP_PASSWORD@redis-auth:6379/0 \
  FLUSHDB || echo "  ✓ Correctly denied FLUSHDB command"

echo "5. All tests passed! ✓"
```

---

## References

- [Redis ACL Documentation](https://redis.io/topics/acl)
- [Redis TLS Support](https://redis.io/topics/encryption)
- [Redis Security Best Practices](https://redis.io/topics/security)
- [OWASP: Key Management](https://cheatsheetseries.owasp.org/cheatsheets/Key_Management_Cheat_Sheet.html)

---

## Support & Escalation

If you suspect a Redis security incident:

1. **Immediate**: 
   - Isolate Redis from network (`bind 127.0.0.1` only)
   - Stop accepting connections

2. **Investigation**:
   - Review slowlog: `SLOWLOG GET 100`
   - Check ACL denials: `ACL LOG`
   - Look for unexpected keys: `KEYS *`

3. **Recovery**:
   - Restore from backup (AOF or RDB)
   - Rotate all ACL passwords
   - Audit all services that had access

4. **Post-Incident**:
   - Document what happened
   - Update security procedures
   - Consider automated password rotation
