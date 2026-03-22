#!/bin/bash
# Pre-translate hook: Verify RE report exists before translation
# The #1 rule: never translate without understanding

set -euo pipefail

MODULE="${1:?Usage: check-re-report-exists.sh <MODULE_NAME>}"
RE_REPORT="output/docs/RE-${MODULE}.md"

if [ ! -f "$RE_REPORT" ]; then
    echo ""
    echo "ERROR: Reverse-Engineering report not found"
    echo "  Expected: ${RE_REPORT}"
    echo ""
    echo "WHY: The single most common cause of modernization failure is"
    echo "  translating code without fully understanding it. COBOL programs"
    echo "  contain decades of accumulated business logic that is not"
    echo "  self-documenting. Skipping analysis saves hours upfront and"
    echo "  costs weeks in rework."
    echo ""
    echo "FIX: Run the analysis step first:"
    echo "  1. Use the /assess command: /assess ${MODULE}"
    echo "  2. Use the cobol-analysis skill to generate a full RE report"
    echo "  3. Have a human architect review the RE report"
    echo "  4. Then proceed with translation"
    echo ""
    echo "  The RE report must document:"
    echo "    - All business rules"
    echo "    - Data structures with PIC clause mappings"
    echo "    - External dependencies"
    echo "    - Error handling paths"
    exit 1
fi

echo "OK: RE report found for ${MODULE}"
exit 0
