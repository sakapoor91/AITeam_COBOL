#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CARDDEMO_REPO="https://github.com/aws-samples/aws-mainframe-modernization-carddemo.git"
CARDDEMO_DIR="${PROJECT_ROOT}/.carddemo-upstream"
SOURCE_DIR="${PROJECT_ROOT}/source/cobol"

echo "=== EvolutionAI Setup ==="

# Clone AWS CardDemo if not already present
if [ ! -d "${CARDDEMO_DIR}" ]; then
    echo "[1/4] Cloning AWS CardDemo repository..."
    git clone --depth 1 "${CARDDEMO_REPO}" "${CARDDEMO_DIR}"
else
    echo "[1/4] AWS CardDemo already cloned, skipping."
fi

# Extract COBOL source files
echo "[2/4] Extracting COBOL sources (.cbl, .cpy)..."
mkdir -p "${SOURCE_DIR}"

find "${CARDDEMO_DIR}" -type f \( -iname "*.cbl" -o -iname "*.cpy" \) -exec cp {} "${SOURCE_DIR}/" \;

cbl_count=$(find "${SOURCE_DIR}" -iname "*.cbl" | wc -l | tr -d ' ')
cpy_count=$(find "${SOURCE_DIR}" -iname "*.cpy" | wc -l | tr -d ' ')
echo "  Found ${cbl_count} .cbl files and ${cpy_count} .cpy files."

# Create output directory structure
echo "[3/4] Creating output directory structure..."
mkdir -p "${PROJECT_ROOT}/output/java"
mkdir -p "${PROJECT_ROOT}/output/tests"
mkdir -p "${PROJECT_ROOT}/output/docs"
mkdir -p "${PROJECT_ROOT}/specs"
mkdir -p "${PROJECT_ROOT}/compliance"

# Create STATUS.md
echo "[4/4] Generating STATUS.md..."
cat > "${PROJECT_ROOT}/STATUS.md" <<EOF
# EvolutionAI Project Status

## Source Inventory
- COBOL programs (.cbl): ${cbl_count}
- Copybooks (.cpy): ${cpy_count}
- Source location: source/cobol/

## Pipeline Status
| Stage       | Status  |
|-------------|---------|
| Discover    | Ready   |
| Design      | Pending |
| Translate   | Pending |
| Validate    | Pending |
| Deploy      | Pending |

## Service URLs (after docker-compose up)
- Dashboard:        http://localhost:3000
- Langfuse:         http://localhost:3100
- Prometheus:       http://localhost:9090
- Grafana:          http://localhost:3001
- Metrics Exporter: http://localhost:8080/metrics

Generated: $(date -u +"%Y-%m-%dT%H:%M:%SZ")
EOF

echo ""
echo "=== Setup Complete ==="
echo ""
echo "Service URLs (start with: docker compose up -d)"
echo "  Dashboard:        http://localhost:3000"
echo "  Langfuse:         http://localhost:3100"
echo "  Prometheus:       http://localhost:9090"
echo "  Grafana:          http://localhost:3001  (admin/admin)"
echo "  Metrics Exporter: http://localhost:8080/metrics"
