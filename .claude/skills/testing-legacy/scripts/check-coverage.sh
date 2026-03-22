#!/bin/bash
# Verifies JaCoCo coverage meets thresholds
# Usage: check-coverage.sh [java_dir]

set -euo pipefail

JAVA_DIR="${1:-output/java}"
LINE_MIN=90
BRANCH_MIN=80

echo "Checking test coverage thresholds..."
echo "  Required: ${LINE_MIN}% line, ${BRANCH_MIN}% branch"
echo ""

JACOCO_REPORT="${JAVA_DIR}/target/site/jacoco/jacoco.csv"

if [ ! -f "$JACOCO_REPORT" ]; then
    echo "WARNING: JaCoCo report not found at ${JACOCO_REPORT}"
    echo "  Run './mvnw verify' in ${JAVA_DIR} to generate coverage report."
    echo ""
    echo "  Alternatively, check for XML report..."

    JACOCO_XML="${JAVA_DIR}/target/site/jacoco/jacoco.xml"
    if [ ! -f "$JACOCO_XML" ]; then
        echo "  No JaCoCo reports found. Run tests first."
        exit 1
    fi
fi

# If CSV exists, parse it for a quick check
if [ -f "$JACOCO_REPORT" ]; then
    echo "Parsing JaCoCo CSV report..."

    # Sum up covered and missed for lines and branches
    TOTAL_LINE_MISSED=0
    TOTAL_LINE_COVERED=0
    TOTAL_BRANCH_MISSED=0
    TOTAL_BRANCH_COVERED=0

    while IFS=, read -r group package class instr_missed instr_covered branch_missed branch_covered line_missed line_covered complexity_missed complexity_covered method_missed method_covered; do
        # Skip header
        [[ "$group" == "GROUP" ]] && continue
        TOTAL_LINE_MISSED=$((TOTAL_LINE_MISSED + line_missed))
        TOTAL_LINE_COVERED=$((TOTAL_LINE_COVERED + line_covered))
        TOTAL_BRANCH_MISSED=$((TOTAL_BRANCH_MISSED + branch_missed))
        TOTAL_BRANCH_COVERED=$((TOTAL_BRANCH_COVERED + branch_covered))
    done < "$JACOCO_REPORT"

    TOTAL_LINES=$((TOTAL_LINE_MISSED + TOTAL_LINE_COVERED))
    TOTAL_BRANCHES=$((TOTAL_BRANCH_MISSED + TOTAL_BRANCH_COVERED))

    if [ "$TOTAL_LINES" -gt 0 ]; then
        LINE_PCT=$((TOTAL_LINE_COVERED * 100 / TOTAL_LINES))
    else
        LINE_PCT=0
    fi

    if [ "$TOTAL_BRANCHES" -gt 0 ]; then
        BRANCH_PCT=$((TOTAL_BRANCH_COVERED * 100 / TOTAL_BRANCHES))
    else
        BRANCH_PCT=0
    fi

    echo "  Line coverage:   ${LINE_PCT}% (${TOTAL_LINE_COVERED}/${TOTAL_LINES})"
    echo "  Branch coverage: ${BRANCH_PCT}% (${TOTAL_BRANCH_COVERED}/${TOTAL_BRANCHES})"
    echo ""

    ERRORS=0
    if [ "$LINE_PCT" -lt "$LINE_MIN" ]; then
        echo "FAIL: Line coverage ${LINE_PCT}% is below minimum ${LINE_MIN}%"
        ERRORS=$((ERRORS + 1))
    fi
    if [ "$BRANCH_PCT" -lt "$BRANCH_MIN" ]; then
        echo "FAIL: Branch coverage ${BRANCH_PCT}% is below minimum ${BRANCH_MIN}%"
        ERRORS=$((ERRORS + 1))
    fi

    if [ "$ERRORS" -gt 0 ]; then
        echo ""
        echo "FIX: Add more tests to improve coverage. Focus on untested branches."
        exit 1
    else
        echo "PASS: Coverage thresholds met"
        exit 0
    fi
fi
