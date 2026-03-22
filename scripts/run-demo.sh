#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "=== EvolutionAI Demo Runner ==="

# Run setup if source files are not present
if [ ! -d "${PROJECT_ROOT}/source/cobol" ] || [ -z "$(ls -A "${PROJECT_ROOT}/source/cobol" 2>/dev/null)" ]; then
    echo "Source files not found. Running setup first..."
    bash "${PROJECT_ROOT}/scripts/setup.sh"
    echo ""
fi

# Start services
echo "Starting services with docker compose..."
cd "${PROJECT_ROOT}"
docker compose up -d --build

# Wait for services to become healthy
echo ""
echo "Waiting for services to start..."

max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -sf http://localhost:8080/health > /dev/null 2>&1; then
        break
    fi
    attempt=$((attempt + 1))
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo "WARNING: metrics-exporter did not become healthy within 60 seconds."
    echo "Check logs with: docker compose logs metrics-exporter"
else
    echo "Metrics exporter is healthy."
fi

# Check other services
for svc_url in "http://localhost:9090/-/healthy" "http://localhost:3001/api/health"; do
    if curl -sf "${svc_url}" > /dev/null 2>&1; then
        echo "  $(echo ${svc_url} | cut -d/ -f3) is healthy."
    fi
done

echo ""
echo "=== All Services Running ==="
echo ""
echo "  Dashboard:        http://localhost:3000"
echo "  Langfuse:         http://localhost:3100"
echo "  Prometheus:       http://localhost:9090"
echo "  Grafana:          http://localhost:3001  (admin/admin)"
echo "  Metrics Exporter: http://localhost:8080/metrics"
echo ""
echo "To stop:  docker compose down"
echo "To logs:  docker compose logs -f"
