---
name: cobol-analysis
description: Analyze COBOL source files — extract structure, business rules, data layouts, and dependencies
agent: analyst
stage: discover
---

# COBOL Analysis Skill

## When to Use
Invoke when analyzing any `.cbl`, `.cob`, or `.cpy` file from the CardDemo source.

## Prerequisites
- COBOL source file exists in `source/` directory
- Module is cataloged in the legacy-discovery inventory

## Steps

1. **Identify the module**: Read the IDENTIFICATION DIVISION for PROGRAM-ID, AUTHOR, DATE-WRITTEN
2. **Map the environment**: Parse ENVIRONMENT DIVISION for FILE-CONTROL (VSAM → repository mappings)
3. **Extract data structures**: Parse DATA DIVISION
   - FILE SECTION (FD): Record layouts → Java DTOs
   - WORKING-STORAGE: Local variables → Java fields
   - LINKAGE SECTION: Parameters → method signatures
   - COPY statements: Shared structures → shared DTOs
   - Use PIC clause mapping from `references/pic-clause-mapping.md`
4. **Analyze business logic**: Parse PROCEDURE DIVISION
   - Map every PERFORM paragraph to a business rule description
   - Map CICS commands using `references/cics-command-mapping.md`
   - Document EVALUATE/WHEN, IF/ELSE logic paths
   - Identify CALL targets and parameter mappings
5. **Score complexity**: Rate as Simple/Moderate/Complex based on LOC, CICS usage, inter-program calls
6. **Generate RE Report**: Write to `output/docs/RE-{MODULE}.md`
7. **Validate**: Run `scripts/validate-analysis.sh {MODULE}` to verify completeness

## Output Checklist
- [ ] Every PERFORM paragraph with business rule description
- [ ] Every data field with PIC clause → Java type mapping
- [ ] Every COPY reference with the copybook content
- [ ] Every CICS command with REST endpoint equivalent
- [ ] Every CALL with target program and parameter mapping
- [ ] Complexity rating and migration recommendation

## References
- `references/pic-clause-mapping.md` — PIC clause → Java type mapping table
- `references/cics-command-mapping.md` — CICS → REST mapping table
- `references/carddemo-modules.md` — CardDemo module inventory
