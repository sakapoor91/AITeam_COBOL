#!/bin/bash
# Pre-commit hook: Block float/double in monetary contexts
# Scans staged Java files for forbidden floating-point usage in financial code

set -euo pipefail

ERRORS=0

# Get staged Java files
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM -- '*.java' 2>/dev/null || true)

if [ -z "$STAGED_FILES" ]; then
    exit 0
fi

for file in $STAGED_FILES; do
    [ -f "$file" ] || continue

    # Check for float/double in monetary contexts
    matches=$(grep -n -i '\b\(float\|double\)\b' "$file" 2>/dev/null \
        | grep -iv '//\|/\*\|\*\|import\|@Override\|toString\|parse\|latitude\|longitude\|weight\|height\|percentage' \
        | grep -i 'amount\|balance\|price\|total\|rate\|fee\|charge\|interest\|payment\|credit\|debit\|cost\|currency\|money\|salary\|wage' \
        || true)

    if [ -n "$matches" ]; then
        echo ""
        echo "ERROR: float/double detected in monetary context"
        echo "  File: ${file}"
        echo "$matches" | while IFS= read -r line; do
            echo "  ${line}"
        done
        echo ""
        echo "WHY: IEEE 754 floating-point arithmetic produces rounding errors."
        echo "  In banking, 0.1 + 0.2 = 0.30000000000000004, not 0.3."
        echo "  This error compounds across millions of transactions."
        echo "  COBOL's packed decimal (COMP-3) is inherently exact."
        echo ""
        echo "FIX: Use BigDecimal instead:"
        echo "  BigDecimal amount = new BigDecimal(\"100.50\");"
        echo "  BigDecimal result = amount.add(other);"
        ERRORS=$((ERRORS + 1))
    fi
done

# Also check SQL files
SQL_FILES=$(git diff --cached --name-only --diff-filter=ACM -- '*.sql' 2>/dev/null || true)
for file in $SQL_FILES; do
    [ -f "$file" ] || continue
    if grep -n -i 'FLOAT\|DOUBLE PRECISION\|REAL' "$file" 2>/dev/null | grep -iv '--' | head -5 | grep -q .; then
        echo ""
        echo "ERROR: FLOAT/DOUBLE PRECISION found in SQL"
        echo "  File: ${file}"
        echo ""
        echo "FIX: Use NUMERIC or DECIMAL for monetary columns in PostgreSQL."
        ERRORS=$((ERRORS + 1))
    fi
done

if [ "$ERRORS" -gt 0 ]; then
    echo ""
    echo "Commit blocked: ${ERRORS} monetary float/double violation(s) found."
    exit 1
fi

exit 0
