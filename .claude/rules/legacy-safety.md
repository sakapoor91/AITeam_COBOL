# Legacy Safety Rules

These rules prevent the most expensive modernization mistakes.

## Rules

1. **Never translate a COBOL module without a completed RE report.** The RE report at `output/docs/RE-{MODULE}.md` must exist and contain: business rules, data structures, dependencies, and error handling paths. Translation without understanding causes weeks of rework.

2. **Never skip Witness agent review before merging.** All translated code must pass the Witness agent's quality gate (equivalence tests, compliance checks, code review) before the Refinery agent merges it. No exceptions for "simple" modules.

3. **Never modify COBOL source files.** Files in `source/` are read-only reference material. They represent the ground truth for behavioral equivalence testing. Any modification invalidates all test comparisons.

4. **Always preserve the original program's behavior exactly.** The Java translation must produce identical outputs for identical inputs. Intentional behavioral changes (better error messages, stricter validation) must be explicitly documented and approved by a human architect.

5. **Never allow an agent to self-approve security-sensitive changes.** Authentication, authorization, encryption, and PII handling changes require human review regardless of test pass rates.

## Rationale
A safety-first approach adds modest overhead per module but prevents catastrophic rework across the entire migration. One silently incorrect financial calculation can cost more than the entire modernization budget.
