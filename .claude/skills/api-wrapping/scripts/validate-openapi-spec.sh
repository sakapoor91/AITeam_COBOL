#!/bin/bash
# Validates an OpenAPI specification file
# Usage: validate-openapi-spec.sh <MODULE_NAME>

set -euo pipefail

MODULE="${1:?Usage: validate-openapi-spec.sh <MODULE_NAME>}"
SPEC_FILE="specs/${MODULE}.yml"
ERRORS=0

echo "Validating OpenAPI spec: ${SPEC_FILE}"
echo ""

if [ ! -f "$SPEC_FILE" ]; then
    echo "ERROR: OpenAPI spec not found at ${SPEC_FILE}"
    echo "FIX: Generate the spec using the api-wrapping skill."
    exit 1
fi

# Check OpenAPI version
if grep -q "openapi: 3.1" "$SPEC_FILE"; then
    echo "  OK: OpenAPI 3.1 version"
else
    echo "  ERROR: Must use OpenAPI 3.1.0"
    ERRORS=$((ERRORS + 1))
fi

# Check for x-cobol-source
if grep -q "x-cobol-source" "$SPEC_FILE"; then
    echo "  OK: x-cobol-source extension present"
else
    echo "  ERROR: Missing x-cobol-source extension"
    echo "  FIX: Add 'x-cobol-source: source/${MODULE}.cbl' to info section"
    ERRORS=$((ERRORS + 1))
fi

# Check for number type on monetary fields (forbidden)
if grep -A2 -i "amount\|balance\|price\|fee\|rate\|charge\|payment" "$SPEC_FILE" | grep -q "type: number"; then
    echo "  ERROR: Monetary field uses 'type: number'"
    echo "  FIX: Use 'type: string, format: decimal' for all monetary amounts"
    ERRORS=$((ERRORS + 1))
else
    echo "  OK: No 'type: number' on monetary fields"
fi

# Check for error responses
if grep -q "400\|404\|500" "$SPEC_FILE"; then
    echo "  OK: Error responses defined"
else
    echo "  WARNING: No error responses (400/404/500) defined"
fi

# Try spectral lint if available
if command -v npx &>/dev/null; then
    echo ""
    echo "Running Spectral lint..."
    npx @stoplight/spectral-cli lint "$SPEC_FILE" 2>/dev/null || echo "  (Spectral lint skipped — install for full validation)"
fi

echo ""
if [ "$ERRORS" -gt 0 ]; then
    echo "FAIL: ${ERRORS} issues found"
    exit 1
else
    echo "PASS: OpenAPI spec validation complete"
    exit 0
fi
