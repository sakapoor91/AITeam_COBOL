# CardDemo COBOL Documentation Pipeline

## What This Project Does

Reads AWS CardDemo COBOL source files and produces deep, field-accurate business documentation for every program — plain English documents that a Java developer can trust instead of re-reading COBOL.

**Input**: `source/cobol/` — 44 COBOL programs + 62 copybooks  
**Output**:
- `output/business-docs/` — one subfolder per program, each containing `.md`, `.docx`, and `-flow.png`
- `output/validation/` — one subfolder per program with Phase 1 + Phase 2 validation reports
- `output/MASTER-CARDDEMO.md` — single master reference document for the entire codebase

---

## Pipeline in Three Steps

```
1. READ         source/cobol/PROGNAME.cbl
                source/cobol/*.cpy  (all COPYed copybooks)

2. GENERATE     output/business-docs/PROGNAME/BIZ-PROGNAME.md
                  ↳ Section 1: Purpose
                  ↳ Section 2: Program Flow (exact paragraph names + line numbers)
                  ↳ Section 3: Error Handling
                  ↳ Section 4: Migration Notes
                  ↳ Appendix A: Files
                  ↳ Appendix B: Copybooks + External Programs (all fields inline)
                  ↳ Appendix C: Hardcoded Literals
                  ↳ Appendix D: Internal Working Fields
                  ↳ Appendix E: Mermaid flowchart TD

3. CONVERT      output/business-docs/PROGNAME/BIZ-PROGNAME.docx
                output/business-docs/PROGNAME/BIZ-PROGNAME-flow.png
                  ↳ python output/business-docs/tools/_md_to_docx.py
                  ↳ mmdc (mermaid-cli) for PNG — npm install -g @mermaid-js/mermaid-cli

4. VALIDATE     output/validation/PROGNAME/PROGNAME-validation.md
                output/validation/PROGNAME/PROGNAME-validation.docx
                  ↳ Phase 1: python output/business-docs/tools/validate_doc.py PROGNAME --report
                  ↳ Phase 2: validator agent (LLM-as-judge)

5. MASTER DOC   output/MASTER-CARDDEMO.md
                output/MASTER-CARDDEMO.docx
                  ↳ Synthesises all BIZ-*.md + validation reports into one reference document
                  ↳ Run /master-doc to regenerate after adding or updating programs
```

---

## Key Paths

| Path | Contents |
|------|---------|
| `source/cobol/` | COBOL source programs and copybooks — **read-only, never modify** |
| `output/MASTER-CARDDEMO.md` | **Master reference document** — architecture, all programs, copybook catalog, risk register |
| `output/business-docs/` | Per-program business documentation |
| `output/validation/` | Phase 1 + Phase 2 validation reports (one subfolder per program) |
| `output/business-docs/DOCUMENTATION-STANDARD.md` | The complete depth rules every BIZ-*.md must follow |
| `output/business-docs/TEMPLATE.md` | Blank fill-in template |
| `output/business-docs/CBACT01C/BIZ-CBACT01C.md` | **Canonical reference implementation** |
| `output/business-docs/tools/_md_to_docx.py` | Markdown → Word converter |
| `output/business-docs/tools/generate_all.py` | Batch converter (all programs) |
| `output/business-docs/tools/validate_doc.py` | Phase 1 mechanical validator |
| `output/business-docs/tools/requirements.txt` | Python dependencies |

---

## Documentation Depth Rules (summary — full rules in DOCUMENTATION-STANDARD.md)

- **No raw COBOL statements** — describe every action in plain English; field/paragraph names in `backticks` only
- **Resolve every COPY** — read the `.cpy` file and list all fields inline with PIC and byte count; never say "see copybook"
- **Decode 88-level values** — show what each literal code means
- **Call out unused fields** — name every copybook field never referenced by the program
- **Flag COMP-3 fields** — mark with "(COMP-3 — use BigDecimal in Java)"
- **Note typos** — preserve exact misspelled field name, add "(typo)"
- **Cite line numbers** — every paragraph reference and migration note must include a source line number
- **Latent bugs** — list everything in Migration Notes: unhandled status codes, stale data, template artifacts, security issues

---

## Slash Commands

| Command | What it does |
|---------|-------------|
| `/document PROGNAME` | Generate BIZ-*.md + DOCX + PNG for one program |
| `/document-all` | Generate docs for every program missing a BIZ-*.md |
| `/convert PROGNAME` | Re-run converter on an existing BIZ-*.md |
| `/check-doc PROGNAME` | Verify a BIZ-*.md has all required sections (quick) |
| `/validate-doc PROGNAME` | Full two-phase validation: mechanical + LLM judge |
| `/master-doc` | Regenerate MASTER-CARDDEMO.md + DOCX from all BIZ-*.md and validation reports |

---

## Validation Pipeline

Every BIZ-*.md goes through two phases before being trusted as a migration reference:

```
Phase 1 — Mechanical (python tools/validate_doc.py PROGNAME --report)
  ✓ All 12 sections present
  ✓ No forbidden COBOL code blocks
  ✓ Every line number within source bounds
  ✓ Every backtick identifier exists in source or copybooks
  ✓ All COPYed copybooks documented in Appendix B
  ✓ DDnames match SELECT/ASSIGN statements
  ✓ PIC byte counts mathematically consistent
  ✓ Migration notes cite line numbers
  ✓ Mermaid diagram has classDef styles

Phase 2 — LLM Judge (validator agent)
  ✓ S1: Program flow matches actual PERFORM structure
  ✓ S2: Error handling descriptions match source DISPLAY strings
  ✓ S3: Every migration note supported by source evidence
  ✓ S4: Copybook field tables match actual .cpy files
  ✓ S5: External program call details accurate
  ✓ S6: No significant coverage omissions
```

Results are written to `output/validation/PROGNAME/PROGNAME-validation.md` and converted to `PROGNAME-validation.docx`.

### Validation Verdicts

| Phase 1 | Phase 2 | Document status |
|---------|---------|-----------------|
| PASS | PASS | Ready to use as migration reference |
| PASS | CONDITIONAL | Usable with noted caveats |
| PASS | FAIL | Semantic errors — document needs revision |
| FAIL | — | Structural errors — fix Phase 1 issues first |

---

## Agents

| Agent | When to use |
|-------|-------------|
| `documenter` | Generating or fixing a BIZ-*.md — reads source, writes full doc |
| `validator` | Phase 2 LLM-as-judge — reads source + doc side-by-side, gives PASS/FAIL/CONDITIONAL verdict |

> After adding or updating programs, run `/master-doc` to keep `output/MASTER-CARDDEMO.md` in sync.

---

## Current Status (as of April 2026)

All 44 programs have BIZ-*.md, .docx, -flow.png, and two-phase validation reports:

| Batch | Programs |
|-------|---------|
| Account batch | CBACT01C · CBACT02C · CBACT03C · CBACT04C |
| Customer / Data | CBCUS01C · CBEXPORT · CBIMPORT |
| Statements | CBPAUP0C · CBSTM03A · CBSTM03B |
| Transaction batch | CBTRN01C · CBTRN02C · CBTRN03C |
| Account online | COACCT01 · COACTUPC · COACTVWC |
| Admin / Billing | COADM01C · COBIL00C |
| Utilities | COBSWAIT · COBTUPDT · CODATE01 · CSUTLDTC |
| Cards online | COCRDLIC · COCRDSLC · COCRDUPC |
| Menu / Sign-on | COMEN01C · COSGN00C |
| Pause screens | COPAUA0C · COPAUS0C · COPAUS1C · COPAUS2C |
| Reporting | CORPT00C |
| Transactions online | COTRN00C · COTRN01C · COTRN02C · COTRTLIC · COTRTUPC |
| User management | COUSR00C · COUSR01C · COUSR02C · COUSR03C |
| DB / Audit | DBUNLDGS · PAUDBLOD · PAUDBUNL |

To add a new program: place its `.cbl` in `source/cobol/`, run `/document PROGNAME`, then `/validate-doc PROGNAME`, then `/master-doc` to update the master reference.
