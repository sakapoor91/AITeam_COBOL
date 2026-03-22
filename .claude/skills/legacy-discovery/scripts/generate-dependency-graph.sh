#!/bin/bash
# Parses COPY and CALL statements to build a dependency graph
# Usage: generate-dependency-graph.sh [source_dir]

set -euo pipefail

SOURCE_DIR="${1:-source}"

if [ ! -d "$SOURCE_DIR" ]; then
    echo "ERROR: Source directory '${SOURCE_DIR}' not found"
    exit 1
fi

echo "=== Dependency Graph ==="
echo "Source directory: ${SOURCE_DIR}"
echo ""

echo "--- COPY Dependencies (module → copybook) ---"
for file in "$SOURCE_DIR"/*.cbl "$SOURCE_DIR"/*.cob 2>/dev/null; do
    [ -f "$file" ] || continue
    module=$(basename "$file" | sed 's/\.[^.]*$//')
    copies=$(grep -i "COPY " "$file" 2>/dev/null | sed 's/.*COPY *//i' | sed 's/[. ].*//' | sort -u)
    if [ -n "$copies" ]; then
        while IFS= read -r dep; do
            echo "  ${module} → ${dep}"
        done <<< "$copies"
    fi
done

echo ""
echo "--- CALL Dependencies (module → module) ---"
for file in "$SOURCE_DIR"/*.cbl "$SOURCE_DIR"/*.cob 2>/dev/null; do
    [ -f "$file" ] || continue
    module=$(basename "$file" | sed 's/\.[^.]*$//')
    calls=$(grep -i "CALL " "$file" 2>/dev/null | sed "s/.*CALL *['\"]//i" | sed "s/['\"].*//" | sort -u)
    if [ -n "$calls" ]; then
        while IFS= read -r dep; do
            echo "  ${module} → ${dep}"
        done <<< "$calls"
    fi
done

echo ""
echo "--- CICS LINK/XCTL Dependencies ---"
for file in "$SOURCE_DIR"/*.cbl "$SOURCE_DIR"/*.cob 2>/dev/null; do
    [ -f "$file" ] || continue
    module=$(basename "$file" | sed 's/\.[^.]*$//')
    links=$(grep -i "EXEC CICS \(LINK\|XCTL\)" "$file" 2>/dev/null | sed "s/.*PROGRAM *( *['\"]//i" | sed "s/['\"].*//" | sort -u)
    if [ -n "$links" ]; then
        while IFS= read -r dep; do
            echo "  ${module} → ${dep}"
        done <<< "$links"
    fi
done

echo ""
echo "Dependency graph complete."
