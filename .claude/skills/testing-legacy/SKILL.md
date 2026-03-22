---
name: testing-legacy
description: Behavioral equivalence testing and coverage enforcement for translated modules
agent: witness
stage: validate
---

# Testing Legacy Skill

## When to Use
Stage 4 of the Five-Stage Loop. Use after translation to validate behavioral equivalence.

## Prerequisites
- Translated Java code exists in `output/java/`
- RE report with documented I/O pairs exists in `output/docs/`
- JUnit 5 and JaCoCo configured in Maven POM

## Steps

1. **Collect COBOL I/O pairs**: Extract input/output examples from the RE report
2. **Generate equivalence tests**: For each I/O pair, create a JUnit test that:
   - Provides the same input to the Java code
   - Compares output field-by-field with expected COBOL output
   - Tests error conditions and edge cases
3. **Run test suite**: Execute `scripts/run-equivalence-tests.sh`
4. **Check coverage**: Run `scripts/check-coverage.sh` to verify thresholds:
   - Line coverage ≥ 90%
   - Branch coverage ≥ 80%
5. **Generate report**: Document pass rate, failures, and coverage in `output/docs/TEST-{MODULE}.md`
6. **Review failures**: Any equivalence failure must be individually justified

## What to Compare
- Output values (amounts, dates, status codes, messages)
- Side effects (database writes, file updates, audit log entries)
- Error behavior (which inputs produce errors, what error codes are returned)

## What NOT to Compare
- Performance characteristics
- Internal data structures
- Log formatting

## Acceptance Criteria
- ≥99.7% equivalence pass rate (remaining 0.3% must be individually justified)
- 90% line coverage, 80% branch coverage
- All financial calculations produce identical results to COBOL

## References
- `references/equivalence-testing-guide.md` — How to structure equivalence tests
