#!/bin/bash

###############################################################################
# Chinvat Backend API Testing Script
# Tests all Profile endpoints with various permission scenarios
# 
# Usage:
#   chmod +x test-endpoints.sh
#   ./test-endpoints.sh
#
# Requirements:
#   - Application running on http://localhost:8080
#   - jq installed for JSON processing
#   - curl installed
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
API_BASE="http://localhost:8080/api/v1"
PROFILE_BASE="${API_BASE}/profile"

# Test data
TEST_CERT_PEM="-----BEGIN CERTIFICATE-----\nMIIBkTCB+wIJAKHHCgVtbH4vMA0GCSqGSIb3DQEBBQUAMBMxETAPBgNVBAMMCHNl\nY3NlcnZlcjAeFw0yNDA1MDExMDEyNDRaFw0yNTA1MDExMDEyNDRaMBMxETAPBgNV\nBAMMCHNlY3NlcnZlcjBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCTMiJ8cjkUU0Zy\nLkRKm0kI5KEyYKkLvG7fR8j8KZJh8mVU5V9u3+gU3vR/pKYKvFfF8V7qD4p4jWMt\nJ9QjAgMBAAEwDQYJKoZIhvcNAQEFBQADQQBTUDkA5QP8T4p3kTm5+XF8/4pB\n-----END CERTIFICATE-----"

# Counters
PASSED=0
FAILED=0

###############################################################################
# Helper Functions
###############################################################################

print_header() {
  echo -e "\n${BLUE}==================== $1 ====================${NC}\n"
}

print_test() {
  echo -e "${YELLOW}→ Testing: $1${NC}"
}

print_success() {
  echo -e "${GREEN}✓ SUCCESS: $1${NC}"
  ((PASSED++))
}

print_failure() {
  echo -e "${RED}✗ FAILURE: $1${NC}"
  ((FAILED++))
}

check_response_code() {
  local actual=$1
  local expected=$2
  local test_name=$3
  
  if [ "$actual" -eq "$expected" ]; then
    print_success "$test_name (HTTP $actual)"
  else
    print_failure "$test_name (expected HTTP $expected, got $actual)"
  fi
}

check_response_contains() {
  local response=$1
  local pattern=$2
  local test_name=$3
  
  if echo "$response" | grep -q "$pattern"; then
    print_success "$test_name"
  else
    print_failure "$test_name"
    echo "  Response: $response"
  fi
}

###############################################################################
# Mock Token Generation (Simulated - Replace with real token from auth flow)
###############################################################################

generate_test_tokens() {
  print_header "Simulating Test Tokens"
  
  # In real scenarios, you would authenticate and get these tokens
  # For now, we'll use placeholder tokens that should be set via environment
  
  if [ -z "$VALID_TOKEN" ]; then
    echo -e "${YELLOW}⚠ Warning: VALID_TOKEN not set. Set it via environment:${NC}"
    echo "  export VALID_TOKEN='<jwt-token-from-auth-endpoint>'"
    echo ""
    VALID_TOKEN="invalid-test-token-placeholder"
  fi
  
  if [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${YELLOW}⚠ Warning: ADMIN_TOKEN not set. Set it via environment.${NC}"
    ADMIN_TOKEN="invalid-admin-token-placeholder"
  fi
  
  if [ -z "$NO_PROFILE_WRITE_TOKEN" ]; then
    echo -e "${YELLOW}⚠ Warning: NO_PROFILE_WRITE_TOKEN not set (token with PROFILE:READ only).${NC}"
    NO_PROFILE_WRITE_TOKEN="invalid-no-write-token-placeholder"
  fi
}

###############################################################################
# Test: Complete eIDAS Profile (Public Endpoint - No Auth)
###############################################################################

test_eidas_complete_public() {
  print_header "Test 1: Complete eIDAS Profile (Public - No Auth Required)"
  print_test "POST /profile/eidas/complete - public endpoint"
  
  RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${PROFILE_BASE}/eidas/complete" \
    -H "Content-Type: application/json" \
    -d '{
      "providerCode": "EIDAS_EU",
      "externalSubjectId": "subject-test-'$(date +%s)'",
      "username": "maria.silva.test",
      "fullName": "Maria Silva Test",
      "phoneNumber": "+34 600 000 111",
      "email": "maria.test@example.com",
      "password": "SecurePass123!@#",
      "addressLine": "Rua Principal 1",
      "postalCode": "3000-001",
      "city": "Covilha",
      "country": "PT",
      "defaultLanguage": "pt",
      "certificatePem": "'${TEST_CERT_PEM}'",
      "assuranceLevel": "high",
      "certificateProviderCode": "FNMT",
      "identityReference": "ref-test-'$(date +%s)'",
      "nationality": "PT"
    }')
  
  HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
  BODY=$(echo "$RESPONSE" | head -n -1)
  
  # Expected: 200 OK (if identity exists) or 404 (if not mocked)
  if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 404 ]; then
    print_success "eIDAS profile endpoint accessible (HTTP $HTTP_CODE)"
  else
    print_failure "eIDAS profile endpoint (HTTP $HTTP_CODE)"
    echo "  Response: $BODY"
  fi
}

###############################################################################
# Test: List Certificates (Requires PROFILE:READ)
###############################################################################

test_list_certificates_unauthorized() {
  print_header "Test 2: List Certificates - No Token (Should Fail)"
  print_test "GET /profile/certificates - without token"
  
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${PROFILE_BASE}/certificates")
  check_response_code "$HTTP_CODE" 401 "List certificates without token returns 401"
}

test_list_certificates_with_token() {
  print_header "Test 3: List Certificates - With Valid Token"
  print_test "GET /profile/certificates - with PROFILE:READ permission"
  
  RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${PROFILE_BASE}/certificates" \
    -H "Authorization: Bearer ${VALID_TOKEN}" \
    -H "Content-Type: application/json")
  
  HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
  BODY=$(echo "$RESPONSE" | head -n -1)
  
  # Expected: 200 OK (with empty list or populated list) or 403 (if token invalid)
  if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "List certificates with valid token (HTTP 200)"
    echo "  Response: $BODY" | head -c 200
    echo "..."
    
    # Try to extract first certificate ID if list not empty
    FIRST_CERT_ID=$(echo "$BODY" | jq -r '.[0].id // empty' 2>/dev/null)
    if [ -n "$FIRST_CERT_ID" ]; then
      export TEST_CERT_ID="$FIRST_CERT_ID"
      echo "  Found certificate ID: $TEST_CERT_ID"
    fi
  elif [ "$HTTP_CODE" -eq 403 ]; then
    print_failure "List certificates - token might lack PROFILE:READ (HTTP 403)"
  else
    print_failure "List certificates (HTTP $HTTP_CODE)"
    echo "  Response: $BODY"
  fi
}

###############################################################################
# Test: Add Certificate (Requires PROFILE:WRITE)
###############################################################################

test_add_certificate_unauthorized() {
  print_header "Test 4: Add Certificate - No Token (Should Fail)"
  print_test "POST /profile/certificates - without token"
  
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${PROFILE_BASE}/certificates" \
    -H "Content-Type: application/json" \
    -d '{"certificatePem":"test","providerCode":"FNMT","assuranceLevel":"high"}')
  check_response_code "$HTTP_CODE" 401 "Add certificate without token returns 401"
}

test_add_certificate_insufficient_permissions() {
  print_header "Test 5: Add Certificate - Token Without PROFILE:WRITE"
  print_test "POST /profile/certificates - without PROFILE:WRITE permission"
  
  RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${PROFILE_BASE}/certificates" \
    -H "Authorization: Bearer ${NO_PROFILE_WRITE_TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{
      "certificatePem": "'${TEST_CERT_PEM}'",
      "providerCode": "FNMT",
      "assuranceLevel": "high"
    }')
  
  HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
  BODY=$(echo "$RESPONSE" | head -n -1)
  
  # Expected: 403 Forbidden (permission denied)
  if [ "$HTTP_CODE" -eq 403 ]; then
    print_success "Add certificate without PROFILE:WRITE returns 403"
  elif [ "$HTTP_CODE" -eq 400 ]; then
    print_failure "Invalid certificate format (HTTP 400) - check test data"
  else
    print_failure "Add certificate permission check (expected 403, got $HTTP_CODE)"
  fi
}

test_add_certificate_with_permissions() {
  print_header "Test 6: Add Certificate - With PROFILE:WRITE Permission"
  print_test "POST /profile/certificates - with PROFILE:WRITE permission"
  
  RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${PROFILE_BASE}/certificates" \
    -H "Authorization: Bearer ${VALID_TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{
      "certificatePem": "'${TEST_CERT_PEM}'",
      "providerCode": "FNMT",
      "assuranceLevel": "high"
    }')
  
  HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
  BODY=$(echo "$RESPONSE" | head -n -1)
  
  if [ "$HTTP_CODE" -eq 201 ]; then
    print_success "Add certificate with PROFILE:WRITE (HTTP 201)"
    ADDED_CERT_ID=$(echo "$BODY" | jq -r '.id // empty' 2>/dev/null)
    if [ -n "$ADDED_CERT_ID" ]; then
      export TEST_CERT_ID="$ADDED_CERT_ID"
      echo "  Created certificate ID: $TEST_CERT_ID"
    fi
  elif [ "$HTTP_CODE" -eq 403 ]; then
    print_failure "Token lacks PROFILE:WRITE (HTTP 403)"
  else
    print_failure "Add certificate (HTTP $HTTP_CODE)"
    echo "  Response: $BODY"
  fi
}

###############################################################################
# Test: Set Primary Certificate (Requires PROFILE:WRITE)
###############################################################################

test_set_primary_certificate_no_cert_id() {
  print_header "Test 7: Set Primary Certificate - Missing Certificate ID"
  print_test "POST /profile/certificates/{id}/primary - with invalid ID"
  
  RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${PROFILE_BASE}/certificates/99999/primary" \
    -H "Authorization: Bearer ${VALID_TOKEN}")
  
  HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
  BODY=$(echo "$RESPONSE" | head -n -1)
  
  if [ "$HTTP_CODE" -eq 404 ]; then
    print_success "Set primary with non-existent cert returns 404"
  else
    print_failure "Set primary with invalid ID (expected 404, got $HTTP_CODE)"
  fi
}

test_set_primary_certificate_with_valid_id() {
  print_header "Test 8: Set Primary Certificate - With Valid ID"
  print_test "POST /profile/certificates/{id}/primary - with PROFILE:WRITE"
  
  if [ -z "$TEST_CERT_ID" ]; then
    echo -e "${YELLOW}⚠ Skipping - no certificate ID available from previous tests${NC}"
    return
  fi
  
  RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${PROFILE_BASE}/certificates/${TEST_CERT_ID}/primary" \
    -H "Authorization: Bearer ${VALID_TOKEN}")
  
  HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
  BODY=$(echo "$RESPONSE" | head -n -1)
  
  if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Set primary certificate (HTTP 200)"
  elif [ "$HTTP_CODE" -eq 400 ]; then
    print_failure "Certificate not in ACTIVE state (HTTP 400)"
  elif [ "$HTTP_CODE" -eq 403 ]; then
    print_failure "Missing PROFILE:WRITE permission (HTTP 403)"
  else
    print_failure "Set primary certificate (HTTP $HTTP_CODE)"
  fi
}

###############################################################################
# Test: Delete/Revoke Certificate (Requires PROFILE:WRITE)
###############################################################################

test_revoke_certificate_no_token() {
  print_header "Test 9: Revoke Certificate - No Token"
  print_test "DELETE /profile/certificates/{id} - without token"
  
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "${PROFILE_BASE}/certificates/1")
  check_response_code "$HTTP_CODE" 401 "Revoke certificate without token returns 401"
}

test_revoke_certificate_with_valid_id() {
  print_header "Test 10: Revoke Certificate - With Valid ID"
  print_test "DELETE /profile/certificates/{id} - with PROFILE:WRITE"
  
  if [ -z "$TEST_CERT_ID" ]; then
    echo -e "${YELLOW}⚠ Skipping - no certificate ID available${NC}"
    return
  fi
  
  RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "${PROFILE_BASE}/certificates/${TEST_CERT_ID}?reason=USER_REQUEST" \
    -H "Authorization: Bearer ${VALID_TOKEN}")
  
  HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
  
  if [ "$HTTP_CODE" -eq 204 ]; then
    print_success "Revoke certificate (HTTP 204)"
  elif [ "$HTTP_CODE" -eq 403 ]; then
    print_failure "Missing PROFILE:WRITE permission (HTTP 403)"
  elif [ "$HTTP_CODE" -eq 404 ]; then
    print_failure "Certificate not found (HTTP 404)"
  else
    print_failure "Revoke certificate (HTTP $HTTP_CODE)"
  fi
}

###############################################################################
# Main Test Runner
###############################################################################

main() {
  echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║   Chinvat Backend API - Endpoint Test Suite            ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
  
  # Check if API is running
  echo -e "\n${YELLOW}Checking API availability...${NC}"
  if ! curl -s -o /dev/null -w "%{http_code}" "${API_BASE}/rbac/permissions" | grep -q "200\|401\|403"; then
    echo -e "${RED}✗ API not running on ${API_BASE}${NC}"
    echo "  Start the application with: SERVER_PORT=8080 ./backend/mvnw spring-boot:run"
    exit 1
  fi
  echo -e "${GREEN}✓ API is running${NC}"
  
  # Generate test tokens
  generate_test_tokens
  
  # Run all tests
  test_eidas_complete_public
  test_list_certificates_unauthorized
  test_list_certificates_with_token
  test_add_certificate_unauthorized
  test_add_certificate_insufficient_permissions
  test_add_certificate_with_permissions
  test_set_primary_certificate_no_cert_id
  test_set_primary_certificate_with_valid_id
  test_revoke_certificate_no_token
  test_revoke_certificate_with_valid_id
  
  # Summary
  TOTAL=$((PASSED + FAILED))
  echo -e "\n${BLUE}════════════════════════════════════════════════════════${NC}"
  echo -e "${BLUE}Test Results:${NC}"
  echo -e "  ${GREEN}Passed: $PASSED${NC}"
  echo -e "  ${RED}Failed: $FAILED${NC}"
  echo -e "  Total: $TOTAL"
  echo -e "${BLUE}════════════════════════════════════════════════════════${NC}\n"
  
  # Exit with failure if any tests failed
  [ "$FAILED" -eq 0 ] && exit 0 || exit 1
}

# Run the tests
main "$@"
