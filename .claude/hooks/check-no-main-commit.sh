#!/bin/bash
# Pre-commit hook: Prevent direct commits to main branch
# All work must go through feature branches for agent isolation

set -euo pipefail

BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")

if [ "$BRANCH" = "main" ] || [ "$BRANCH" = "master" ]; then
    echo ""
    echo "ERROR: Direct commit to '${BRANCH}' branch is not allowed"
    echo ""
    echo "WHY: This project uses agent isolation via Git worktrees."
    echo "  Each agent works on its own feature branch to prevent"
    echo "  interference between parallel translation tasks."
    echo "  Only the Refinery agent merges approved work to main."
    echo ""
    echo "FIX: Create a feature branch first:"
    echo "  git checkout -b agent/<role>/<module-name>"
    echo ""
    echo "  Branch naming convention:"
    echo "    agent/analyst/COSGN00C    — analysis work"
    echo "    agent/polecat/COACTUPC    — translation work"
    echo "    agent/witness/COACTUPC    — review work"
    echo "    feature/<description>     — manual feature work"
    exit 1
fi

exit 0
