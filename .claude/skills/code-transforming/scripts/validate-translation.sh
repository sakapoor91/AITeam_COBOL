#!/bin/bash
# Validates translated Java for forbidden patterns
# Usage: validate-translation.sh <MODULE_NAME>

set -euo pipefail

MODULE="${1:?Usage: validate-translation.sh <MODULE_NAME>}"
JAVA_DIR="output/java"
ERRORS=0

echo "Validating translation for module: ${MODULE}"
echo ""

# Check RE report exists
if [ ! -f "output/docs/RE-${MODULE}.md" ]; then
    echo "ERROR: RE report not found at output/docs/RE-${MODULE}.md"
    echo "  Translation must not proceed without reverse-engineering documentation."
    ERRORS=$((ERRORS + 1))
fi

# Find Java files for this module
JAVA_FILES=$(find "$JAVA_DIR" -name "*.java" 2>/dev/null | head -50)

if [ -z "$JAVA_FILES" ]; then
    echo "WARNING: No Java files found in ${JAVA_DIR}"
    echo "  This may be expected if translation hasn't started yet."
    exit 0
fi

# Check for float/double in monetary contexts
echo "Checking for forbidden float/double usage..."
while IFS= read -r file; do
    matches=$(grep -n -i 'float\|double' "$file" 2>/dev/null | grep -iv '//\|javadoc\|\*\|import' | grep -i 'amount\|balance\|price\|total\|rate\|fee\|charge\|interest\|payment\|credit\|debit\|cost\|currency' || true)
    if [ -n "$matches" ]; then
        echo "  ERROR in ${file}:"
        echo "  ${matches}"
        echo ""
        echo "  WHY: IEEE 754 floating-point produces rounding errors in financial calculations."
        echo "  FIX: Use BigDecimal instead."
        ERRORS=$((ERRORS + 1))
    fi
done <<< "$JAVA_FILES"

# Check for new BigDecimal(double)
echo "Checking for BigDecimal(double) anti-pattern..."
while IFS= read -r file; do
    matches=$(grep -n 'new BigDecimal([0-9]' "$file" 2>/dev/null || true)
    if [ -n "$matches" ]; then
        echo "  ERROR in ${file}:"
        echo "  ${matches}"
        echo ""
        echo "  WHY: new BigDecimal(0.1) still carries floating-point error."
        echo "  FIX: Use new BigDecimal(\"0.1\") with String constructor."
        ERRORS=$((ERRORS + 1))
    fi
done <<< "$JAVA_FILES"

# Check for var in public signatures
echo "Checking for var in public API..."
while IFS= read -r file; do
    matches=$(grep -n 'public.*\bvar\b' "$file" 2>/dev/null || true)
    if [ -n "$matches" ]; then
        echo "  ERROR in ${file}:"
        echo "  ${matches}"
        echo ""
        echo "  FIX: Use explicit types in public API signatures."
        ERRORS=$((ERRORS + 1))
    fi
done <<< "$JAVA_FILES"

# Check for Javadoc on public classes
echo "Checking for Javadoc..."
while IFS= read -r file; do
    if grep -q 'public class\|public record\|public interface' "$file" 2>/dev/null; then
        if ! grep -q '/\*\*' "$file" 2>/dev/null; then
            echo "  WARNING: No Javadoc found in ${file}"
        fi
    fi
done <<< "$JAVA_FILES"

echo ""
if [ "$ERRORS" -gt 0 ]; then
    echo "FAIL: ${ERRORS} issues found in translation"
    exit 1
else
    echo "PASS: Translation validation complete for ${MODULE}"
    exit 0
fi
