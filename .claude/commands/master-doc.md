Regenerate the master reference document for the entire CardDemo codebase.

Output: `output/MASTER-CARDDEMO.md` + `output/MASTER-CARDDEMO.docx`

## What the master document contains

1. **Executive Summary** — CardDemo system purpose, scope, infrastructure, validation status
2. **System Architecture** — CICS, Batch, VSAM, MQ, IMS, DB2, BMS; COMMAREA structure; XCTL navigation tree; shared file access matrix
3. **Business Domains** — All 11 domains with programs, primary files, and key copybooks
4. **End-to-End Business Flows** — 8 complete flows with file and copybook names at each step
5. **Key Data Entities** — Record-layout copybook, writer programs, reader programs for each entity
6. **Copybook Catalog** — All copybooks grouped by type (BMS_MAP, DATA_RECORD, WORKING_STORAGE, COMMAREA, PCB)
7. **Complete Program Reference** — All programs: Type, Domain, Lines, Files, Copybooks, Calls, Validation verdict
8. **Validation Summary** — Phase 1 + Phase 2 results, the one FAIL (COBIL00C), cross-cutting CONDITIONAL themes
9. **Migration Risk Register** — Numbered risks with affected programs, source, and Java recommendation
10. **How to Use This Repository** — Guide for Java developers starting migration

## Steps

1. Read `output/business-docs/BUSINESS-OVERVIEW.md` as the domain/flow base.

2. Read a representative sample of BIZ-*.md files (aim for 8-12 covering different domains):
   - `output/business-docs/CBACT01C/BIZ-CBACT01C.md` (canonical batch example)
   - `output/business-docs/COSGN00C/BIZ-COSGN00C.md` (CICS sign-on)
   - `output/business-docs/COACTUPC/BIZ-COACTUPC.md` (largest CICS program)
   - `output/business-docs/COBIL00C/BIZ-COBIL00C.md` (the FAIL — needs special callout)
   - `output/business-docs/CBTRN02C/BIZ-CBTRN02C.md` (core batch transaction)
   - `output/business-docs/COPAUA0C/BIZ-COPAUA0C.md` (MQ authorization)
   - `output/business-docs/CBSTM03A/BIZ-CBSTM03A.md` (statement generation)
   - Others as needed for completeness

3. Read the FAIL and key CONDITIONAL validation reports:
   - `output/validation/COBIL00C/COBIL00C-validation.md` (read in full — the only FAIL)
   - `output/validation/COADM01C/COADM01C-validation.md`
   - `output/validation/COUSR01C/COUSR01C-validation.md`
   - `output/validation/COTRN02C/COTRN02C-validation.md`

4. Read copybooks from `source/cobol/` as needed for the Copybook Catalog section.

5. Write `output/MASTER-CARDDEMO.md` with all 10 sections above.

6. Convert to DOCX:
   ```bash
   python output/business-docs/tools/_md_to_docx.py \
     output/MASTER-CARDDEMO.md \
     output/MASTER-CARDDEMO.docx
   ```

7. Report: line count of .md and whether DOCX was created.

## When to run

- After adding a new program (run `/document PROGNAME` and `/validate-doc PROGNAME` first)
- After fixing a FAIL or CONDITIONAL finding in any BIZ-*.md
- After re-running validation on any program
- On any significant update to the codebase understanding

## Writing rules

- Plain English throughout — no raw COBOL statements
- Field and paragraph names in `backticks`
- Tables for structured data
- Target length: 600–1000 lines
- Every risk in the Migration Risk Register must cite the validation report or BIZ-*.md where it was found
