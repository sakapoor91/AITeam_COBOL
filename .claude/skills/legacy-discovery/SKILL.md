---
name: legacy-discovery
description: Inventory COBOL codebase, generate dependency graphs, and score complexity
agent: analyst
stage: discover
---

# Legacy Discovery Skill

## When to Use
First step in the Five-Stage Loop. Use to catalog all COBOL modules before analysis begins.

## Steps

1. **Scan inventory**: Run `scripts/scan-cobol-inventory.sh` to catalog all `.cbl`, `.cob`, `.cpy` files in `source/`
2. **Count and classify**: For each module, determine:
   - Line count (LOC)
   - Type: Online (CICS) or Batch
   - Prefix pattern: CO* = Online, CB* = Batch
3. **Map dependencies**: Run `scripts/generate-dependency-graph.sh` to parse:
   - `COPY` statements → shared copybook dependencies
   - `CALL` statements → inter-program dependencies
   - `EXEC CICS LINK/XCTL` → CICS program transfers
4. **Score complexity**: Apply rubric from `references/complexity-scoring.md`:
   - Simple: <200 LOC, no CICS, no inter-program calls
   - Moderate: 200-500 LOC, or uses CICS, or has 1-2 dependencies
   - Complex: >500 LOC, CICS + inter-program calls, >2 dependencies
5. **Generate inventory report**: Write to `output/docs/INVENTORY.md` with:
   - Module list with LOC, type, complexity, dependencies
   - Dependency graph (text-based)
   - Recommended migration order (leaf dependencies first)
6. **Identify migration waves**: Group modules into waves based on dependencies

## Output
- `output/docs/INVENTORY.md` — Full inventory with complexity scores
- Dependency graph showing module relationships
- Recommended migration order

## References
- `references/complexity-scoring.md` — Scoring rubric and thresholds
