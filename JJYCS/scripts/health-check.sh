#!/bin/bash

# Health check script for YSC LMS production
# Usage: ./health-check.sh <base_url>

set -e

BASE_URL="${1:-http://localhost:8080}"
MAX_RETRIES=5
RETRY_DELAY=5

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "🏥 Running health checks against: ${BASE_URL}"
echo "================================================"

# Function to check endpoint with retries
check_health() {
    local endpoint=$1
    local description=$2
    local retries=0
    
    echo -n "Checking ${description}... "
    
    while [ $retries -lt $MAX_RETRIES ]; do
        response=$(curl -s "${BASE_URL}${endpoint}")
        status=$(echo "$response" | jq -r '.status' 2>/dev/null || echo "UNKNOWN")
        
        if [ "$status" == "UP" ] || [ "$status" == "READY" ] || [ "$status" == "ALIVE" ]; then
            echo -e "${GREEN}✓${NC} ${status}"
            return 0
        fi
        
        ((retries++))
        if [ $retries -lt $MAX_RETRIES ]; then
            echo -n "."
            sleep $RETRY_DELAY
        fi
    done
    
    echo -e "${RED}✗${NC} Failed after ${MAX_RETRIES} attempts"
    return 1
}

# Check critical services
FAILURES=0

check_health "/health/simple" "Basic health" || ((FAILURES++))
check_health "/health/readiness" "Application readiness" || ((FAILURES++))
check_health "/health/liveness" "Application liveness" || ((FAILURES++))
check_health "/health" "Detailed health status" || ((FAILURES++))

# Check database connectivity
echo -n "Checking database connection... "
db_status=$(curl -s "${BASE_URL}/health" | jq -r '.database.status' 2>/dev/null || echo "UNKNOWN")
if [ "$db_status" == "UP" ]; then
    echo -e "${GREEN}✓${NC} Connected"
else
    echo -e "${RED}✗${NC} Database issue: ${db_status}"
    ((FAILURES++))
fi

# Check external APIs
echo -n "Checking external APIs... "
api_status=$(curl -s "${BASE_URL}/health" | jq -r '.externalApis.overallHealthy' 2>/dev/null || echo "false")
if [ "$api_status" == "true" ]; then
    echo -e "${GREEN}✓${NC} All APIs healthy"
else
    echo -e "${YELLOW}⚠${NC} Some APIs may be unavailable"
fi

# Check system metrics
echo -n "Checking system resources... "
metrics=$(curl -s "${BASE_URL}/health" | jq -r '.metrics' 2>/dev/null)
if [ ! -z "$metrics" ] && [ "$metrics" != "null" ]; then
    used_memory=$(echo "$metrics" | jq -r '.usedMemory' 2>/dev/null || echo "0")
    max_memory=$(echo "$metrics" | jq -r '.maxMemory' 2>/dev/null || echo "1")
    memory_percent=$((used_memory * 100 / max_memory))
    
    if [ $memory_percent -lt 80 ]; then
        echo -e "${GREEN}✓${NC} Memory usage: ${memory_percent}%"
    else
        echo -e "${YELLOW}⚠${NC} High memory usage: ${memory_percent}%"
    fi
else
    echo -e "${YELLOW}⚠${NC} Unable to retrieve metrics"
fi

echo "================================================"

# Final status
if [ $FAILURES -eq 0 ]; then
    echo -e "${GREEN}✅ All health checks passed!${NC}"
    echo "Application is healthy and ready to serve traffic."
    exit 0
else
    echo -e "${RED}❌ ${FAILURES} health check(s) failed!${NC}"
    echo "Application may not be fully operational."
    exit 1
fi