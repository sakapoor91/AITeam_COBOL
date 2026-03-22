Run a full validation suite on the translated module: $ARGUMENTS

Perform these checks in order and report pass/fail for each:

### 1. RE Report Check
- Verify `output/docs/RE-{MODULE}.md` exists
- Check it contains required sections: Business Rules, Data Structures, Dependencies, Error Handling, Complexity

### 2. Forbidden Pattern Scan
- Search translated Java files for `float` or `double` used in monetary/financial contexts
- Search for `Float` or `Double` wrapper types in financial DTOs
- Check SQL files for `FLOAT` or `DOUBLE PRECISION` column types

### 3. BigDecimal Anti-Pattern Check
- Search for `new BigDecimal(` followed by a numeric literal (not a String)
- Search for `BigDecimal.valueOf(double)` in financial calculations
- Verify `RoundingMode` is specified on all `divide()` calls

### 4. Code Quality Check
- Verify all public classes have Javadoc with `@see` COBOL source reference
- Check for `var` in public API method signatures
- Verify record types are used for DTOs

### 5. Test Coverage Check
- If JaCoCo reports exist, verify: ≥90% line coverage, ≥80% branch coverage
- If no reports exist, note as "NOT RUN"

### 6. Equivalence Test Check
- If equivalence tests exist, report pass/fail count and rate
- If no tests exist, note as "NOT RUN"

Output a validation report:

```
## Validation Report: {MODULE}

| Check | Status | Details |
|-------|--------|---------|
| RE Report | PASS/FAIL | {details} |
| Float/Double | PASS/FAIL | {files and lines if failed} |
| BigDecimal Constructor | PASS/FAIL | {files and lines if failed} |
| Code Quality | PASS/FAIL | {issues found} |
| Coverage (line) | PASS/FAIL/NOT RUN | {percentage}% |
| Coverage (branch) | PASS/FAIL/NOT RUN | {percentage}% |
| Equivalence Tests | PASS/FAIL/NOT RUN | {pass rate}% |

### Overall: PASS / FAIL
{summary and recommended actions}
```
