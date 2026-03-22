---
name: code-transforming
description: COBOL-to-Java translation protocol with validation loops
agent: polecat
stage: translate
---

# Code Transforming Skill

## When to Use
Stage 3 of the Five-Stage Loop. Use after RE report is complete and design decisions are approved.

## Prerequisites
- RE report exists at `output/docs/RE-{MODULE}.md`
- Architecture decisions documented (API shape, DB schema approved by human)
- PIC clause mappings available in `cobol-analysis` skill references

## Steps

1. **Verify prerequisites**: Confirm RE report exists — run `.claude/hooks/check-re-report-exists.sh {MODULE}`
2. **Load source**: Read COBOL source from `source/{MODULE}.cbl`
3. **Translate IDENTIFICATION DIVISION**:
   - PROGRAM-ID → Java class name
   - Add Javadoc with `@see` referencing source COBOL program
4. **Translate DATA DIVISION**:
   - FILE SECTION (FD) → JPA entity classes using Java records
   - WORKING-STORAGE → private fields
   - LINKAGE SECTION → method parameters
   - COPY references → shared DTO imports
   - Apply PIC mappings from `references/translation-protocol.md`
5. **Translate PROCEDURE DIVISION**:
   - PERFORM paragraphs → Java methods
   - EVALUATE/WHEN → switch expressions
   - EXEC CICS → REST endpoints / repository calls
   - CALL → service method invocations
6. **Apply financial rules**: See `references/java-conventions.md`
   - All monetary values → `BigDecimal` with String constructor
   - Division → explicit `RoundingMode.HALF_EVEN`
   - Comparison → `compareTo()`, never `equals()`
7. **Validate**: Run `scripts/validate-translation.sh {MODULE}`
8. **Generate tests**: Create JUnit test stubs for all public methods
9. **Output**: Write to `output/java/` preserving package structure

## Forbidden Patterns
- `float` or `double` for any monetary amount
- `new BigDecimal(0.1)` — use `new BigDecimal("0.1")`
- `var` in public API signatures
- Missing Javadoc on public classes/methods
- Spring WebFlux (use servlet model only)

## References
- `references/translation-protocol.md` — Division-by-division translation rules
- `references/java-conventions.md` — Java coding conventions for this project
