#!/bin/bash
# Generates a daily modernization progress report
# Usage: generate-daily-report.sh [output_file]

set -euo pipefail

DATE=$(date +%Y-%m-%d)
OUTPUT="${1:-output/docs/DAILY-REPORT-${DATE}.md}"

echo "Generating daily report for ${DATE}..."

cat > "$OUTPUT" << EOF
# Daily Modernization Report — ${DATE}

## Module Progress

### Source Inventory
EOF

# Count source files
CBL_COUNT=$(find source -name "*.cbl" 2>/dev/null | wc -l | tr -d ' ')
COB_COUNT=$(find source -name "*.cob" 2>/dev/null | wc -l | tr -d ' ')
CPY_COUNT=$(find source -name "*.cpy" 2>/dev/null | wc -l | tr -d ' ')
TOTAL_SOURCE=$((CBL_COUNT + COB_COUNT))

echo "- COBOL programs: ${TOTAL_SOURCE}" >> "$OUTPUT"
echo "- Copybooks: ${CPY_COUNT}" >> "$OUTPUT"

# Count analyzed modules
ANALYZED=$(find output/docs -name "RE-*.md" 2>/dev/null | wc -l | tr -d ' ')
echo "- Modules analyzed (RE reports): ${ANALYZED}" >> "$OUTPUT"

# Count translated modules
JAVA_FILES=$(find output/java -name "*.java" 2>/dev/null | wc -l | tr -d ' ')
echo "- Java files generated: ${JAVA_FILES}" >> "$OUTPUT"

# Count test files
TEST_FILES=$(find output/tests -name "*Test*.java" -o -name "*test*.java" 2>/dev/null | wc -l | tr -d ' ')
echo "- Test files: ${TEST_FILES}" >> "$OUTPUT"

# Count approved modules
APPROVED=$(find output/docs -name "REVIEW-*.md" 2>/dev/null | wc -l | tr -d ' ')
echo "- Modules approved (Witness reviewed): ${APPROVED}" >> "$OUTPUT"

cat >> "$OUTPUT" << 'EOF'

## Pipeline Status

| Stage | Count | Status |
|-------|-------|--------|
EOF

echo "| Discover (analyzed) | ${ANALYZED} | $([ "$ANALYZED" -gt 0 ] && echo 'Active' || echo 'Not started') |" >> "$OUTPUT"
echo "| Translate (Java files) | ${JAVA_FILES} | $([ "$JAVA_FILES" -gt 0 ] && echo 'Active' || echo 'Not started') |" >> "$OUTPUT"
echo "| Validate (approved) | ${APPROVED} | $([ "$APPROVED" -gt 0 ] && echo 'Active' || echo 'Not started') |" >> "$OUTPUT"

cat >> "$OUTPUT" << 'EOF'

## Action Items
- Review any modules stuck in translation for >48 hours
- Check token spend against daily budget
- Verify observability stack is healthy

---
*Generated automatically by monitoring-modernization skill*
EOF

echo "Report written to: ${OUTPUT}"
