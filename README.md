# AITeam_COBOL

**AI-Driven Business Documentation Pipeline for Legacy COBOL**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Docs](https://img.shields.io/badge/Docs-44%2F44-brightgreen.svg)](output/business-docs/)
[![Validated](https://img.shields.io/badge/Validated-44%2F44-brightgreen.svg)](output/validation/)

---

## What This Project Does

AITeam_COBOL uses **multi-agent AI** to reverse-engineer legacy COBOL banking programs into deep, field-accurate business documentation — plain English documents that a Java developer can trust instead of re-reading COBOL.

- **Source**: [AWS CardDemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo) — a COBOL/CICS credit card management application (44 programs, 84 copybooks)
- **Output**: One `BIZ-*.md` per program with inline copybook fields, paragraph-level flow, 88-level decodes, COMP-3 flags, latent bug catalogue, and a Mermaid execution diagram
- **Validation**: Every document passes a two-phase pipeline — mechanical (structure, line bounds, PIC math) then LLM-as-judge (semantic accuracy)

---

## Quick Start

```bash
git clone https://github.com/sakapoor91/AITeam_COBOL.git
cd AITeam_COBOL

# Document a single program
/document CBACT01C

# Validate it
/validate-doc CBACT01C

# Regenerate the master reference
/master-doc
```

---

## Slash Commands

| Command | What it does |
|---------|-------------|
| `/document PROGNAME` | Generate BIZ-*.md + DOCX + PNG for one program |
| `/document-all` | Generate docs for every program missing a BIZ-*.md |
| `/convert PROGNAME` | Re-run the MD→DOCX converter on an existing BIZ-*.md |
| `/check-doc PROGNAME` | Quick check that all required sections are present |
| `/validate-doc PROGNAME` | Full two-phase validation: mechanical + LLM judge |
| `/master-doc` | Regenerate MASTER-CARDDEMO.md from all BIZ-*.md files |

---

## Agents

| Agent | Model | Role |
|-------|-------|------|
| `documenter` | Sonnet | Reads COBOL source + copybooks, writes BIZ-*.md |
| `validator` | Opus | LLM-as-judge — reads source + doc side-by-side, gives PASS/FAIL/CONDITIONAL |

---

## Output: All 44 Programs

Every program has a `BIZ-*.md`, `.docx`, `-flow.png`, and two-phase validation report.

| Program | Domain | Business Function |
|---------|--------|-----------------|
| CBACT01C | Account Batch | Print all account records to report |
| CBACT02C | Account Batch | Cross-reference accounts to cards |
| CBACT03C | Account Batch | Update account balances from transaction summary |
| CBACT04C | Account Batch | Close over-limit accounts |
| CBCUS01C | Customer Batch | Print all customer records to report |
| CBEXPORT | Data Exchange | Export account/card data to flat file |
| CBIMPORT | Data Exchange | Import and validate external transactions |
| CBPAUP0C | Statement | Pause utility between statement steps |
| CBSTM03A | Statement | Generate monthly statement detail lines |
| CBSTM03B | Statement | Write formatted statement to print file |
| CBTRN01C | Transaction Batch | Validate and post daily transactions |
| CBTRN02C | Transaction Batch | Transaction category summary report |
| CBTRN03C | Transaction Batch | Reject failing transactions |
| COACCT01 | Account Online | Account inquiry screen |
| COACTUPC | Account Online | Account update (limit, status, expiry) |
| COACTVWC | Account Online | Account view (read-only) |
| COADM01C | Admin | System administration menu |
| COBIL00C | Billing | Billing inquiry and payment entry |
| COBSWAIT | Utility | Timed wait between batch steps |
| COBTUPDT | Utility | Update control table record |
| COCRDLIC | Cards | Credit card list for an account |
| COCRDSLC | Cards | Credit card selection |
| COCRDUPC | Cards | Credit card update |
| CODATE01 | Utility | Date format conversion |
| COMEN01C | Menu | Main application menu |
| COPAUA0C | Pause | Pause screen A |
| COPAUS0C | Pause | Short pause between CICS transactions |
| COPAUS1C | Pause | Extended pause with status message |
| COPAUS2C | Pause | Pause with error detail |
| CORPT00C | Reporting | Report menu — batch report request |
| COSGN00C | Sign-On | User login — credential validation |
| COTRN00C | Transaction Online | Transaction list for an account |
| COTRN01C | Transaction Online | Post new transaction from screen |
| COTRN02C | Transaction Online | View single transaction detail |
| COTRTLIC | Transaction Online | Transaction type list |
| COTRTUPC | Transaction Online | Amend pending transaction |
| COUSR00C | User Mgmt | User list |
| COUSR01C | User Mgmt | Add user |
| COUSR02C | User Mgmt | Update user |
| COUSR03C | User Mgmt | Delete user |
| CSUTLDTC | Utility | Date validation/conversion subroutine |
| DBUNLDGS | DB Unload | Unload VSAM to sequential flat file |
| PAUDBLOD | Audit | Load pause audit records to DB |
| PAUDBUNL | Audit | Export pause audit records |

---

## Validation Pipeline

Every `BIZ-*.md` is verified in two phases before being trusted as a migration reference:

```
Phase 1 — Mechanical  (python output/business-docs/tools/validate_doc.py PROGNAME --report)
  ✓ All 12 sections present
  ✓ No raw COBOL code blocks
  ✓ Every line number within source bounds
  ✓ Every backtick identifier exists in source or copybooks
  ✓ All COPYed copybooks documented in Appendix B
  ✓ PIC byte counts mathematically consistent

Phase 2 — LLM Judge  (validator agent)
  ✓ Program flow matches actual PERFORM structure
  ✓ Error handling matches source DISPLAY strings
  ✓ Every migration note supported by source evidence
  ✓ Copybook field tables match actual .cpy files
  ✓ No significant coverage omissions
```

| Phase 1 | Phase 2 | Status |
|---------|---------|--------|
| PASS | PASS | Ready to use as migration reference |
| PASS | CONDITIONAL | Usable with noted caveats |
| PASS | FAIL | Semantic errors — needs revision |
| FAIL | — | Structural errors — fix Phase 1 first |

---

## Project Structure

```
AITeam_COBOL/
├── .claude/
│   ├── CLAUDE.md                      # Project brain — loaded into every Claude session
│   ├── agents/
│   │   ├── documenter.md              # Generates BIZ-*.md from COBOL source
│   │   └── validator.md               # LLM-as-judge Phase 2 accuracy check
│   ├── commands/                      # Slash commands (/document, /validate-doc, etc.)
│   ├── hooks/
│   │   └── check-doc-sections.sh      # Pre-commit: blocks BIZ-*.md missing required sections
│   ├── rules/
│   │   └── documentation-depth.md     # 10 non-negotiable documentation quality rules
│   └── settings.json
│
├── source/cobol/                      # Original COBOL source — read-only reference
│   ├── *.cbl / *.CBL                  # 44 COBOL programs
│   └── *.cpy / *.CPY                  # 84 copybooks
│
├── output/
│   ├── MASTER-CARDDEMO.md             # Single master reference: all programs, copybook catalog, risk register
│   ├── business-docs/                 # Deep business docs — all 44 programs
│   │   ├── DOCUMENTATION-STANDARD.md  # Depth rules every BIZ-*.md must follow
│   │   ├── TEMPLATE.md                # Blank fill-in template for new programs
│   │   ├── BUSINESS-OVERVIEW.md       # System overview across all 8 domains
│   │   ├── tools/                     # MD→DOCX converter, batch generator, Phase 1 validator
│   │   └── PROGNAME/                  # One folder per program (44 total)
│   │       ├── BIZ-PROGNAME.md        # Deep-dive business doc
│   │       ├── BIZ-PROGNAME.docx      # Word version (generated, gitignored)
│   │       └── BIZ-PROGNAME-flow.png  # Mermaid diagram (generated, gitignored)
│   └── validation/                    # Two-phase validation reports
│       └── PROGNAME/
│           ├── PROGNAME-validation.md
│           └── PROGNAME-validation.docx  # (generated, gitignored)
│
└── README.md
```

---

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.

## Acknowledgments

- [AWS CardDemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo) for the reference COBOL application
- [Anthropic Claude](https://www.anthropic.com/) for the AI models powering the documentation and validation pipeline
