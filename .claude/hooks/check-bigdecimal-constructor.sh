#!/bin/bash
# Pre-commit hook: Catch new BigDecimal(double) anti-pattern
# The double constructor still carries IEEE 754 floating-point error

set -euo pipefail

ERRORS=0

# Get staged Java files
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM -- '*.java' 2>/dev/null || true)

if [ -z "$STAGED_FILES" ]; then
    exit 0
fi

for file in $STAGED_FILES; do
    [ -f "$file" ] || continue

    # Check for new BigDecimal(number) — not new BigDecimal("number")
    matches=$(grep -n 'new BigDecimal\s*(\s*[0-9]' "$file" 2>/dev/null || true)

    if [ -n "$matches" ]; then
        echo ""
        echo "ERROR: new BigDecimal(double) anti-pattern detected"
        echo "  File: ${file}"
        echo "$matches" | while IFS= read -r line; do
            echo "  ${line}"
        done
        echo ""
        echo "WHY: new BigDecimal(0.1) creates a BigDecimal with value"
        echo "  0.1000000000000000055511151231257827021181583404541015625"
        echo "  because the double 0.1 is not exactly representable in IEEE 754."
        echo ""
        echo "FIX: Use the String constructor:"
        echo "  new BigDecimal(\"0.1\")  // Exact: 0.1"
        echo ""
        echo "  Or use BigDecimal.valueOf() for runtime values:"
        echo "  BigDecimal.valueOf(longValue)"
        ERRORS=$((ERRORS + 1))
    fi

    # Also check for BigDecimal.valueOf(double) in financial contexts
    bd_valueof=$(grep -n 'BigDecimal\.valueOf\s*(\s*[0-9]*\.[0-9]' "$file" 2>/dev/null || true)
    if [ -n "$bd_valueof" ]; then
        echo ""
        echo "WARNING: BigDecimal.valueOf(double literal) found"
        echo "  File: ${file}"
        echo "$bd_valueof" | while IFS= read -r line; do
            echo "  ${line}"
        done
        echo ""
        echo "  Consider using new BigDecimal(\"value\") for exact representation."
    fi
done

if [ "$ERRORS" -gt 0 ]; then
    echo ""
    echo "Commit blocked: ${ERRORS} BigDecimal constructor violation(s) found."
    exit 1
fi

exit 0
