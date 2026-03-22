# Testing Requirements

These rules enforce the quality bar for translated code.

## Rules

1. **Minimum 90% line coverage on all translated Java code.** Measured by JaCoCo. No module is considered "translated" until this threshold is met.

2. **Minimum 80% branch coverage on all translated Java code.** Every conditional path in the COBOL original must have a corresponding test. Untested branches are potential behavioral divergences.

3. **Every translated module must have behavioral equivalence tests.** These tests provide identical inputs to both the COBOL and Java implementations and compare outputs field by field.

4. **Equivalence tests must compare: output values, side effects, and error behavior.** Output values include amounts, dates, status codes. Side effects include database writes and audit entries. Error behavior includes which inputs produce errors and what codes are returned.

5. **Test data must include edge cases from the COBOL system.** Boundary values, maximum PIC sizes, negative amounts, date boundaries (leap years, month-end), and empty/null inputs.

6. **Never mark a module as "translated" without passing equivalence tests.** The target pass rate is ≥99.7%. The remaining 0.3% must be individually reviewed and justified as intentional improvements.

## Rationale
Behavioral equivalence is the only meaningful success metric for legacy modernization. A beautifully translated module that produces different outputs is a failed translation.
