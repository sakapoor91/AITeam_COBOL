#!/bin/bash
# Scans source/ directory and outputs COBOL module inventory
# Usage: scan-cobol-inventory.sh [source_dir]

set -euo pipefail

SOURCE_DIR="${1:-source}"

if [ ! -d "$SOURCE_DIR" ]; then
    echo "ERROR: Source directory '${SOURCE_DIR}' not found"
    echo "FIX: Ensure COBOL source files are in the source/ directory"
    exit 1
fi

echo "=== COBOL Module Inventory ==="
echo "Source directory: ${SOURCE_DIR}"
echo ""

# Count files by type
CBL_COUNT=$(find "$SOURCE_DIR" -name "*.cbl" 2>/dev/null | wc -l | tr -d ' ')
COB_COUNT=$(find "$SOURCE_DIR" -name "*.cob" 2>/dev/null | wc -l | tr -d ' ')
CPY_COUNT=$(find "$SOURCE_DIR" -name "*.cpy" 2>/dev/null | wc -l | tr -d ' ')

echo "Programs (.cbl): ${CBL_COUNT}"
echo "Programs (.cob): ${COB_COUNT}"
echo "Copybooks (.cpy): ${CPY_COUNT}"
echo "Total: $(( CBL_COUNT + COB_COUNT + CPY_COUNT ))"
echo ""

# List each module with LOC
echo "=== Module Details ==="
printf "%-20s %-10s %-10s %-10s\n" "Module" "Type" "LOC" "Category"
printf "%-20s %-10s %-10s %-10s\n" "------" "----" "---" "--------"

for file in "$SOURCE_DIR"/*.cbl "$SOURCE_DIR"/*.cob 2>/dev/null; do
    [ -f "$file" ] || continue
    name=$(basename "$file" | sed 's/\.[^.]*$//')
    ext="${file##*.}"
    loc=$(wc -l < "$file" | tr -d ' ')

    # Categorize by prefix
    if [[ "$name" == CO* ]]; then
        category="Online"
    elif [[ "$name" == CB* ]]; then
        category="Batch"
    else
        category="Unknown"
    fi

    printf "%-20s %-10s %-10s %-10s\n" "$name" "$ext" "$loc" "$category"
done

echo ""
echo "=== Copybooks ==="
for file in "$SOURCE_DIR"/*.cpy 2>/dev/null; do
    [ -f "$file" ] || continue
    name=$(basename "$file" | sed 's/\.[^.]*$//')
    loc=$(wc -l < "$file" | tr -d ' ')
    printf "%-20s %-10s\n" "$name" "${loc} lines"
done

echo ""
echo "Inventory scan complete."
