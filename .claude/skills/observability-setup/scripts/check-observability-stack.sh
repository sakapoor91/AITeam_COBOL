#!/bin/bash
# Verifies all observability services are running
# Usage: check-observability-stack.sh

set -euo pipefail

ERRORS=0

echo "Checking observability stack health..."
echo ""

# Check Langfuse
if curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 2>/dev/null | grep -q "200\|302"; then
    echo "  OK: Langfuse is running at http://localhost:3000"
else
    echo "  FAIL: Langfuse not reachable at http://localhost:3000"
    ERRORS=$((ERRORS + 1))
fi

# Check Prometheus
if curl -s -o /dev/null -w "%{http_code}" http://localhost:9090/-/healthy 2>/dev/null | grep -q "200"; then
    echo "  OK: Prometheus is running at http://localhost:9090"
else
    echo "  FAIL: Prometheus not reachable at http://localhost:9090"
    ERRORS=$((ERRORS + 1))
fi

# Check Grafana
if curl -s -o /dev/null -w "%{http_code}" http://localhost:3001/api/health 2>/dev/null | grep -q "200"; then
    echo "  OK: Grafana is running at http://localhost:3001"
else
    echo "  FAIL: Grafana not reachable at http://localhost:3001"
    ERRORS=$((ERRORS + 1))
fi

# Check metrics exporter
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/metrics 2>/dev/null | grep -q "200"; then
    echo "  OK: Metrics exporter is running at http://localhost:8080"
else
    echo "  FAIL: Metrics exporter not reachable at http://localhost:8080"
    ERRORS=$((ERRORS + 1))
fi

echo ""
if [ "$ERRORS" -gt 0 ]; then
    echo "FAIL: ${ERRORS} services not running"
    echo ""
    echo "FIX: Run 'cd metrics/ && docker compose -f docker-compose.observability.yml up -d'"
    exit 1
else
    echo "PASS: All observability services are healthy"
    exit 0
fi
