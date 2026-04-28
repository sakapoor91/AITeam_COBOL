#!/bin/bash
# Pre-commit hook: Verify every staged BIZ-*.md has all required sections.
# Blocks commits that introduce incomplete documentation.

set -euo pipefail

ERRORS=0
STAGED=$(git diff --cached --name-only --diff-filter=ACM -- 'business-docs/*/BIZ-*.md' 2>/dev/null || true)

[ -z "$STAGED" ] && exit 0

check_section() {
    local file="$1"
    local pattern="$2"
    local label="$3"
    if ! grep -q "$pattern" "$file" 2>/dev/null; then
        echo "  MISSING: ${label}"
        return 1
    fi
    return 0
}

for file in $STAGED; do
    [ -f "$file" ] || continue
    prog=$(basename "$(dirname "$file")")
    file_errors=0

    echo ""
    echo "Checking: ${file}"

    check_section "$file" "Application :" "Header block" || file_errors=$((file_errors+1))
    check_section "$file" "## 1\. Purpose\|^# .*Purpose" "Section 1 — Purpose" || file_errors=$((file_errors+1))
    check_section "$file" "### 2\.1\|## 2\. Program Flow" "Section 2 — Program Flow (2.1)" || file_errors=$((file_errors+1))
    check_section "$file" "### 2\.2" "Section 2.2 subsection" || file_errors=$((file_errors+1))
    check_section "$file" "### 2\.3" "Section 2.3 subsection" || file_errors=$((file_errors+1))
    check_section "$file" "## 3\. Error\|## Error Handling" "Section 3 — Error Handling" || file_errors=$((file_errors+1))
    check_section "$file" "## 4\. Migration\|## Migration Notes" "Section 4 — Migration Notes" || file_errors=$((file_errors+1))
    check_section "$file" "## Appendix A\|Appendix A —" "Appendix A — Files" || file_errors=$((file_errors+1))
    check_section "$file" "## Appendix B\|Appendix B —" "Appendix B — Copybooks" || file_errors=$((file_errors+1))
    check_section "$file" "## Appendix C\|Appendix C —" "Appendix C — Literals" || file_errors=$((file_errors+1))
    check_section "$file" "## Appendix D\|Appendix D —" "Appendix D — Working Fields" || file_errors=$((file_errors+1))
    check_section "$file" "flowchart TD\|flowchart LR" "Appendix E — Mermaid diagram" || file_errors=$((file_errors+1))

    # Check for forbidden raw COBOL blocks
    if grep -q '```cobol\|```COBOL' "$file" 2>/dev/null; then
        echo "  FORBIDDEN: raw COBOL code block found (```cobol)"
        echo "  Replace with plain English descriptions."
        file_errors=$((file_errors+1))
    fi

    if [ "$file_errors" -eq 0 ]; then
        echo "  OK: all sections present"
    else
        echo "  FAIL: ${file_errors} issue(s) in ${prog}"
        ERRORS=$((ERRORS + file_errors))
    fi
done

if [ "$ERRORS" -gt 0 ]; then
    echo ""
    echo "Commit blocked: ${ERRORS} documentation issue(s) found."
    echo "Fix the missing sections, then re-commit."
    echo "See business-docs/DOCUMENTATION-STANDARD.md for requirements."
    exit 1
fi

exit 0
