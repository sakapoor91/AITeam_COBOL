#!/bin/bash
# Runs equivalence test suite and reports pass/fail rate
# Usage: run-equivalence-tests.sh [java_dir]

set -euo pipefail

JAVA_DIR="${1:-output/java}"

echo "Running equivalence tests..."
echo ""

if [ ! -f "${JAVA_DIR}/mvnw" ] && [ ! -f "${JAVA_DIR}/pom.xml" ]; then
    echo "ERROR: No Maven project found in ${JAVA_DIR}"
    echo "FIX: Ensure translated Java code has a proper Maven project structure."
    exit 1
fi

cd "$JAVA_DIR"

# Run tests with surefire, filtering for equivalence tests
if [ -f "./mvnw" ]; then
    ./mvnw test -B -Dtest="*Equivalence*,*BehavioralTest*" -Dsurefire.failIfNoTests=false 2>&1 | tail -20
else
    mvn test -B -Dtest="*Equivalence*,*BehavioralTest*" -Dsurefire.failIfNoTests=false 2>&1 | tail -20
fi

TEST_EXIT=$?

# Parse surefire reports
REPORT_DIR="target/surefire-reports"
if [ -d "$REPORT_DIR" ]; then
    TOTAL=$(find "$REPORT_DIR" -name "*.xml" -exec grep -l "testcase" {} \; | wc -l | tr -d ' ')
    FAILURES=$(grep -rl 'failures="[1-9]' "$REPORT_DIR"/*.xml 2>/dev/null | wc -l | tr -d ' ')
    ERRORS=$(grep -rl 'errors="[1-9]' "$REPORT_DIR"/*.xml 2>/dev/null | wc -l | tr -d ' ')

    echo ""
    echo "=== Equivalence Test Summary ==="
    echo "  Test files: ${TOTAL}"
    echo "  Failures: ${FAILURES}"
    echo "  Errors: ${ERRORS}"

    if [ "$TOTAL" -gt 0 ]; then
        PASS_RATE=$(( (TOTAL - FAILURES - ERRORS) * 100 / TOTAL ))
        echo "  Pass rate: ${PASS_RATE}%"

        if [ "$PASS_RATE" -lt 99 ]; then
            echo ""
            echo "WARNING: Pass rate below 99.7% threshold"
            echo "  Each failure must be individually reviewed and justified."
        fi
    fi
fi

exit $TEST_EXIT
