#!/bin/bash

# Smoke test script for YSC LMS
# Usage: ./smoke-test.sh <base_url>

set -e

BASE_URL="${1:-http://localhost:8080}"
FAILURES=0

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "🔍 Running smoke tests against: ${BASE_URL}"
echo "================================================"

# Function to test endpoint
test_endpoint() {
    local endpoint=$1
    local expected_status=$2
    local description=$3
    
    echo -n "Testing ${description}... "
    
    response=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}${endpoint}")
    
    if [ "$response" == "$expected_status" ]; then
        echo -e "${GREEN}✓${NC} (${response})"
    else
        echo -e "${RED}✗${NC} (Expected: ${expected_status}, Got: ${response})"
        ((FAILURES++))
    fi
}

# Health checks
test_endpoint "/health/simple" "200" "Basic health check"
test_endpoint "/health/readiness" "200" "Readiness probe"
test_endpoint "/health/liveness" "200" "Liveness probe"

# API endpoints (public)
test_endpoint "/api/auth/login" "405" "Auth endpoint (expecting 405 for GET)"
test_endpoint "/api/products" "200" "Products listing"

# Static resources
test_endpoint "/" "200" "Frontend index"
test_endpoint "/favicon.ico" "200" "Favicon"

# Error handling
test_endpoint "/api/nonexistent" "404" "404 error handling"

# Actuator endpoints (should be protected)
test_endpoint "/actuator/health" "200" "Actuator health"
test_endpoint "/actuator/prometheus" "401" "Metrics endpoint (protected)"

echo "================================================"

if [ $FAILURES -eq 0 ]; then
    echo -e "${GREEN}✅ All smoke tests passed!${NC}"
    exit 0
else
    echo -e "${RED}❌ ${FAILURES} smoke test(s) failed!${NC}"
    exit 1
fi