#!/bin/bash
##############################################################################
# Redis ACL Security Setup Script
# 
# CRITICAL SECURITY: This script configures strict ACL rules for production
# Redis instances to prevent unauthorized access and data contamination.
#
# Usage:
#   ./redis-acl-setup.sh <redis-host> <redis-port> <redis-password> [tls-ca]
#
# Examples:
#   # Local development (no TLS)
#   ./redis-acl-setup.sh localhost 6379 "dev-password"
#
#   # Production with TLS
#   ./redis-acl-setup.sh redis.example.com 6379 "strong-password" /etc/redis/ca.crt
#
# ARCHITECTURE:
#   redis-global:   Global data (permissions, org info) - read-only for auth service
#   redis-auth:     Auth service data (eIDAS state, user cache) - read-write for auth service
#
# GENERATED USERS:
#   default:              Default admin user (CHANGE PASSWORD IMMEDIATELY)
#   eidas-app:            Auth service user (read-write on auth service keyspace)
#   monitoring:           Prometheus/monitoring (read-only on all keys)
#   global-readonly:      Other services (read-only on permissions:* and org:*)
#
##############################################################################

set -euo pipefail

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
REDIS_HOST="${1:-localhost}"
REDIS_PORT="${2:-6379}"
REDIS_PASSWORD="${3:-change-me}"
TLS_CA="${4:-}"

echo -e "${GREEN}=== Redis ACL Security Configuration ===${NC}"
echo "Host: $REDIS_HOST:$REDIS_PORT"
echo "TLS CA: ${TLS_CA:-DISABLED (insecure)}"
echo ""

# Build redis-cli command with TLS support if CA is provided
REDIS_CLI="redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD"
if [ -n "$TLS_CA" ]; then
    REDIS_CLI="$REDIS_CLI --tls --cacert $TLS_CA"
    echo -e "${YELLOW}TLS enabled for this connection${NC}"
fi

# Verify connection
echo -e "${YELLOW}Verifying Redis connection...${NC}"
if ! $REDIS_CLI ping > /dev/null 2>&1; then
    echo -e "${RED}ERROR: Cannot connect to Redis at $REDIS_HOST:$REDIS_PORT${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Connected successfully${NC}"
echo ""

##############################################################################
# STEP 1: Setup default user (admin)
##############################################################################
echo -e "${YELLOW}[1/5] Configuring default admin user...${NC}"
$REDIS_CLI ACL SETUSER default on >$REDIS_PASSWORD \
    +@all \
    ~* \
    -FLUSHDB -FLUSHALL -SHUTDOWN -MONITOR -CONFIG -CLIENT -DEBUG
echo -e "${GREEN}✓ Default admin user configured${NC}"
echo "  ⚠️  IMPORTANT: Change the default password immediately!"
echo ""

##############################################################################
# STEP 2: Setup eidas-app user (auth service)
##############################################################################
echo -e "${YELLOW}[2/5] Creating 'eidas-app' user (auth service)...${NC}"

# Generate strong password for eidas-app (or use existing)
EIDAS_APP_PASSWORD="${EIDAS_APP_PASSWORD:-$(openssl rand -base64 32)}"

$REDIS_CLI ACL SETUSER eidas-app on ">$EIDAS_APP_PASSWORD" \
    +@all \
    ~chinvat:eidas:* \
    ~chinvat:users:* \
    ~chinvat:trust:* \
    ~chinvat:cache:* \
    -FLUSHDB -FLUSHALL -SHUTDOWN -MONITOR -CONFIG -CLIENT -DEBUG -BGSAVE -LASTSAVE -SAVE

echo -e "${GREEN}✓ eidas-app user created${NC}"
echo "  Username: eidas-app"
echo "  Password: $EIDAS_APP_PASSWORD"
echo "  Keyspace: chinvat:eidas:*, chinvat:users:*, chinvat:trust:*, chinvat:cache:*"
echo "  ⚠️  Store this password securely in your secrets manager (Vault, AWS Secrets, etc.)"
echo ""

##############################################################################
# STEP 3: Setup monitoring user
##############################################################################
echo -e "${YELLOW}[3/5] Creating 'monitoring' user (Prometheus, etc.)...${NC}"

MONITORING_PASSWORD="${MONITORING_PASSWORD:-$(openssl rand -base64 32)}"

$REDIS_CLI ACL SETUSER monitoring on ">$MONITORING_PASSWORD" \
    +INFO +CLIENT +CONFIG|GET +COMMAND +KEYS +SCAN +DBSIZE \
    ~* \
    -@all \
    -FLUSHDB -FLUSHALL -SHUTDOWN -MONITOR -CONFIG|SET

echo -e "${GREEN}✓ monitoring user created${NC}"
echo "  Username: monitoring"
echo "  Password: $MONITORING_PASSWORD"
echo "  Permissions: Read-only for INFO, monitoring commands"
echo ""

##############################################################################
# STEP 4: Setup global-readonly user (other services)
##############################################################################
echo -e "${YELLOW}[4/5] Creating 'global-readonly' user (other services)...${NC}"

GLOBAL_READONLY_PASSWORD="${GLOBAL_READONLY_PASSWORD:-$(openssl rand -base64 32)}"

$REDIS_CLI ACL SETUSER global-readonly on ">$GLOBAL_READONLY_PASSWORD" \
    +GET +MGET +KEYS +SCAN +DBSIZE +INFO|KEYSPACE \
    ~chinvat:permissions:* \
    ~chinvat:org:* \
    -@all

echo -e "${GREEN}✓ global-readonly user created${NC}"
echo "  Username: global-readonly"
echo "  Password: $GLOBAL_READONLY_PASSWORD"
echo "  Keyspace: chinvat:permissions:*, chinvat:org:* (READ-ONLY)"
echo ""

##############################################################################
# STEP 5: Display current ACL configuration
##############################################################################
echo -e "${YELLOW}[5/5] Verifying ACL configuration...${NC}"
echo -e "${GREEN}Current ACL Users:${NC}"
$REDIS_CLI ACL LIST | while IFS= read -r line; do
    # Parse and display user info
    if [[ $line == "user "* ]]; then
        username=$(echo "$line" | awk '{print $2}')
        permissions=$(echo "$line" | sed 's/.*patterns/patterns/')
        echo "  • $username"
    fi
done
echo ""

##############################################################################
# Security Recommendations
##############################################################################
echo -e "${YELLOW}=== SECURITY CHECKLIST FOR PRODUCTION ===${NC}"
echo ""
echo -e "${RED}CRITICAL - Must be done before production deployment:${NC}"
echo "  ☐ 1. Enable TLS (--tls-port 6380, --tls-cert-file, --tls-key-file)"
echo "  ☐ 2. Bind Redis to private network ONLY (--bind 10.0.0.0/8)"
echo "  ☐ 3. DO NOT expose Redis to 0.0.0.0 or internet"
echo "  ☐ 4. Store passwords in secrets manager (Vault, AWS Secrets, etc.)"
echo "  ☐ 5. Enable Redis persistence with AOF (--appendonly yes)"
echo "  ☐ 6. Configure firewall rules to block telnet access"
echo "  ☐ 7. Set up monitoring/alerting for unauthorized access attempts"
echo "  ☐ 8. Regularly rotate ACL passwords (recommended: quarterly)"
echo "  ☐ 9. Test ACL rules before deploying to production"
echo "  ☐ 10. Document ACL configuration in your runbooks"
echo ""

echo -e "${GREEN}=== Configuration Summary ===${NC}"
echo "redis-global password: $REDIS_PASSWORD"
echo "redis-auth password: $REDIS_PASSWORD (same container, different instance)"
echo ""
echo "Set these environment variables in your deployment:"
echo "  REDIS_GLOBAL_PASSWORD=$REDIS_PASSWORD"
echo "  REDIS_AUTH_PASSWORD=$REDIS_PASSWORD"
echo "  EIDAS_APP_PASSWORD=$EIDAS_APP_PASSWORD"
echo "  MONITORING_PASSWORD=$MONITORING_PASSWORD"
echo "  GLOBAL_READONLY_PASSWORD=$GLOBAL_READONLY_PASSWORD"
echo ""

echo -e "${GREEN}✓ ACL configuration complete!${NC}"
echo "⚠️  Next step: Update your Kubernetes secrets or Docker secrets with the passwords above"
