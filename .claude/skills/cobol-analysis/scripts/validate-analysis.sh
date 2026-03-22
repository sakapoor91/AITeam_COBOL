#!/bin/bash
# Validates that an RE report has all required sections
# Usage: validate-analysis.sh <MODULE_NAME>

set -euo pipefail

MODULE="${1:?Usage: validate-analysis.sh <MODULE_NAME>}"
RE_REPORT="output/docs/RE-${MODULE}.md"

ERRORS=0

if [ ! -f "$RE_REPORT" ]; then
    echo "ERROR: RE report not found at ${RE_REPORT}"
    echo ""
    echo "WHY: Every COBOL module must have a complete Reverse-Engineering report"
    echo "     before any translation can begin. This prevents costly rework from"
    echo "     translating code without fully understanding it."
    echo ""
    echo "FIX: Run the cobol-analysis skill on source/${MODULE}.cbl first."
    exit 1
fi

echo "Validating RE report: ${RE_REPORT}"
echo ""

# Check for required sections
REQUIRED_SECTIONS=(
    "Business Rules"
    "Data Structures"
    "Dependencies"
    "Error Handling"
    "Complexity"
)

for section in "${REQUIRED_SECTIONS[@]}"; do
    if ! grep -qi "$section" "$RE_REPORT"; then
        echo "MISSING: Section '${section}' not found in RE report"
        ERRORS=$((ERRORS + 1))
    else
        echo "  OK: ${section}"
    fi
done

# Check for PIC clause documentation
if ! grep -q "PIC" "$RE_REPORT"; then
    echo "MISSING: No PIC clause documentation found"
    ERRORS=$((ERRORS + 1))
else
    echo "  OK: PIC clauses documented"
fi

# Check for PERFORM paragraph documentation
if ! grep -qi "PERFORM\|paragraph\|procedure" "$RE_REPORT"; then
    echo "WARNING: No PERFORM paragraph documentation found"
fi

echo ""
if [ "$ERRORS" -gt 0 ]; then
    echo "FAIL: ${ERRORS} required sections missing from RE report"
    exit 1
else
    echo "PASS: RE report for ${MODULE} is complete"
    exit 0
fi
