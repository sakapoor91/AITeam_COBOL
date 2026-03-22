#!/bin/bash
# Validates MCP server configuration in settings.json
# Usage: validate-mcp-config.sh

set -euo pipefail

SETTINGS_FILE=".claude/settings.json"
ERRORS=0

echo "Validating MCP configuration: ${SETTINGS_FILE}"
echo ""

if [ ! -f "$SETTINGS_FILE" ]; then
    echo "ERROR: Settings file not found at ${SETTINGS_FILE}"
    echo "FIX: Create .claude/settings.json with mcpServers configuration"
    exit 1
fi

# Check valid JSON
if ! jq . "$SETTINGS_FILE" > /dev/null 2>&1; then
    echo "ERROR: ${SETTINGS_FILE} is not valid JSON"
    echo "FIX: Check for syntax errors (missing commas, brackets, quotes)"
    exit 1
fi
echo "  OK: Valid JSON"

# Check mcpServers key exists
if ! jq -e '.mcpServers' "$SETTINGS_FILE" > /dev/null 2>&1; then
    echo "ERROR: No 'mcpServers' key found"
    ERRORS=$((ERRORS + 1))
else
    echo "  OK: mcpServers section found"

    # Check each server has required fields
    for server in $(jq -r '.mcpServers | keys[]' "$SETTINGS_FILE" 2>/dev/null); do
        echo ""
        echo "  Server: ${server}"

        if ! jq -e ".mcpServers.\"${server}\".command" "$SETTINGS_FILE" > /dev/null 2>&1; then
            echo "    ERROR: Missing 'command' field"
            ERRORS=$((ERRORS + 1))
        else
            echo "    OK: command configured"
        fi

        if ! jq -e ".mcpServers.\"${server}\".args" "$SETTINGS_FILE" > /dev/null 2>&1; then
            echo "    WARNING: No 'args' field"
        else
            echo "    OK: args configured"
        fi

        # Check for hardcoded credentials (security check)
        if jq -r ".mcpServers.\"${server}\" | tostring" "$SETTINGS_FILE" | grep -qE '(sk-|pk-|ghp_|password|secret)'; then
            echo "    ERROR: Possible hardcoded credential detected"
            echo "    FIX: Use environment variable references: \${VARIABLE_NAME}"
            ERRORS=$((ERRORS + 1))
        else
            echo "    OK: No hardcoded credentials"
        fi
    done
fi

echo ""
if [ "$ERRORS" -gt 0 ]; then
    echo "FAIL: ${ERRORS} issues found"
    exit 1
else
    echo "PASS: MCP configuration is valid"
    exit 0
fi
