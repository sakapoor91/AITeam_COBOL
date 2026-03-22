#!/bin/bash
# Validates that generated Java uses correct Fineract API patterns
# Usage: validate-fineract-mapping.sh <JAVA_DIR>

set -euo pipefail

JAVA_DIR="${1:-output/java}"
ERRORS=0

echo "Validating Fineract integration patterns in: ${JAVA_DIR}"
echo ""

# Check for @RestClient usage
if find "$JAVA_DIR" -name "*.java" -exec grep -l "FineractClient\|fineract" {} \; 2>/dev/null | head -1 | grep -q .; then
    echo "  OK: Fineract client references found"
else
    echo "  INFO: No Fineract client references found (may not be needed yet)"
fi

# Check for forbidden float/double in Fineract DTOs
if find "$JAVA_DIR" -name "*Dto.java" -exec grep -n "float\|double" {} \; 2>/dev/null | grep -v "//"; then
    echo ""
    echo "ERROR: float/double found in DTO files"
    echo "  All monetary amounts in Fineract DTOs must use BigDecimal"
    ERRORS=$((ERRORS + 1))
else
    echo "  OK: No float/double in DTO files"
fi

# Check for proper BigDecimal construction
if find "$JAVA_DIR" -name "*.java" -exec grep -n 'new BigDecimal([0-9]' {} \; 2>/dev/null | head -5 | grep -q .; then
    echo ""
    echo "ERROR: new BigDecimal(number) found — use new BigDecimal(\"number\") instead"
    echo "  The double constructor carries IEEE 754 floating-point error"
    ERRORS=$((ERRORS + 1))
else
    echo "  OK: BigDecimal construction uses String literals"
fi

# Check for Fineract tenant header
if find "$JAVA_DIR" -name "*.java" -exec grep -l "TenantId\|Fineract-Platform" {} \; 2>/dev/null | head -1 | grep -q .; then
    echo "  OK: Fineract tenant header configured"
else
    echo "  WARNING: No Fineract-Platform-TenantId header found"
fi

echo ""
if [ "$ERRORS" -gt 0 ]; then
    echo "FAIL: ${ERRORS} issues found"
    exit 1
else
    echo "PASS: Fineract integration patterns valid"
    exit 0
fi
