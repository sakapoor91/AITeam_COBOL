Verify that a BIZ-*.md meets the documentation depth standard: $ARGUMENTS

## Steps

1. Read `output/business-docs/$ARGUMENTS/BIZ-$ARGUMENTS.md`.
   If it does not exist, report MISSING and stop.

2. Check for the presence of all required sections:

   | Section | Expected heading |
   |---------|-----------------|
   | Header block | `Application :` line at top |
   | Purpose | `## 1. Purpose` |
   | Program Flow | `## 2. Program Flow` with `### 2.1` and `### 2.2` and `### 2.3` |
   | Error Handling | `## 3. Error Handling` |
   | Migration Notes | `## 4. Migration Notes` |
   | Appendix A | `## Appendix A` |
   | Appendix B | `## Appendix B` |
   | Appendix C | `## Appendix C` |
   | Appendix D | `## Appendix D` |
   | Appendix E | `## Appendix E` with `flowchart TD` |

3. Quality spot-checks:
   - Does Appendix B contain at least one table with `PIC` in the header? (copybook resolved inline)
   - Does Section 4 contain at least one numbered item with a line number reference?
   - Does the Mermaid diagram have `classDef` colour definitions?
   - Are there any ` ```cobol ` or ` ```COBOL ` code blocks? (these are forbidden — flag them)
   - Are there any raw COBOL keywords (MOVE / WRITE / IF / PERFORM) inside a code block? (forbidden)

4. Report a table of PASS / FAIL / MISSING for every check, plus a summary count.
   If any check fails, describe specifically what is missing or wrong.
