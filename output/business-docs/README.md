# CardDemo Business Documentation

Deep-dive business documentation for the AWS CardDemo COBOL credit card management system.  
Each document is written for Java developers performing the COBOL → Java migration — precise enough to replace re-reading the COBOL source.

---

## What Is This?

The [AWS CardDemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo) system is an IBM mainframe credit card management platform written in COBOL (batch + CICS online).  This `business-docs/` folder contains:

- **One subfolder per program** — each containing a `.md`, a `.docx`, and a Mermaid flow diagram `.png`.
- **BUSINESS-OVERVIEW.md** — domain and flow overview (source for the master document).
- **Scaffold files** (this README, DOCUMENTATION-STANDARD.md, TEMPLATE.md, tools/).

> **Start here**: [`../MASTER-CARDDEMO.md`](../MASTER-CARDDEMO.md) is the single master reference for the entire codebase — system architecture, all 44 programs, copybook catalog, validation summary, and migration risk register. Read it before diving into individual BIZ-*.md files.

---

## Folder Structure

```
business-docs/
├── README.md                     ← you are here
├── DOCUMENTATION-STANDARD.md    ← depth rules every BIZ-*.md must follow
├── TEMPLATE.md                   ← blank fill-in template for new programs
├── BUSINESS-OVERVIEW.md          ← master system overview
│
├── CBACT01C/
│   ├── BIZ-CBACT01C.md           ← business + technical deep-dive
│   ├── BIZ-CBACT01C.docx         ← Word version (generated)
│   └── BIZ-CBACT01C-flow.png     ← Mermaid execution diagram (generated)
│
├── CBACT02C/ ...
│   └── (same three artefacts per program)
│
├── validation/
│   ├── CBACT01C/
│   │   ├── CBACT01C-validation.md   ← Phase 1 mechanical + Phase 2 LLM-judge report
│   │   └── CBACT01C-validation.docx ← Word version (generated)
│   └── (one subfolder per program — all 44 complete)
│
└── tools/
    ├── _md_to_docx.py            ← Markdown → Word converter
    ├── generate_all.py           ← batch converter (all programs)
    ├── validate_doc.py           ← Phase 1 mechanical validator
    └── requirements.txt          ← Python dependencies
```

---

## Program Inventory

| Program | Type | Domain | One-Line Summary |
|---|---|---|---|
| CBACT01C | Batch | Account | Prints all account records to a sequential report file |
| CBACT02C | Batch | Account | Cross-references accounts to cards; writes XREF records |
| CBACT03C | Batch | Account | Updates account balances from a daily transaction summary |
| CBACT04C | Batch | Account | Closes accounts that have exceeded their credit limit |
| CBCUS01C | Batch | Customer | Prints all customer records to a sequential report file |
| CBEXPORT | Batch | Data Exchange | Exports selected account/card data to a flat export file |
| CBIMPORT | Batch | Data Exchange | Imports and validates external transaction records |
| CBPAUP0C | Batch | Statement | Pause utility used between statement processing steps |
| CBSTM03A | Batch | Statement | Generates monthly statement detail lines |
| CBSTM03B | Batch | Statement | Writes formatted statement output to print file |
| CBTRN01C | Batch | Transaction | Validates and posts daily transaction records |
| CBTRN02C | Batch | Transaction | Produces transaction category summary report |
| CBTRN03C | Batch | Transaction | Rejects transactions that fail business-rule validation |
| COACCT01 | Online CICS | Account | Account inquiry — displays account details on screen |
| COACTUPC | Online CICS | Account | Account update — amends account limit, status, expiry |
| COACTVWC | Online CICS | Account | Account view — read-only account detail display |
| COADM01C | Online CICS | Admin | System administration menu controller |
| COBIL00C | Online CICS | Billing | Credit card billing inquiry and payment entry |
| COBSWAIT | Batch | Utility | Timed wait utility (pauses batch flow for N seconds) |
| COBTUPDT | Batch | Utility | Batch utility to update a control table record |
| COCRDLIC | Online CICS | Cards | Credit card list — shows all cards for an account |
| COCRDSLC | Online CICS | Cards | Credit card selection — selects a card from a list |
| COCRDUPC | Online CICS | Cards | Credit card update — changes card status or limit |
| CODATE01 | Batch | Utility | Date conversion utility (format converter) |
| COMEN01C | Online CICS | Menu | Main application menu controller |
| COPAUA0C | Online CICS | Pause | Pause screen A — displays wait message between flows |
| COPAUS0C | Online CICS | Pause | Pause screen 0 — short pause between CICS transactions |
| COPAUS1C | Online CICS | Pause | Pause screen 1 — extended pause with status message |
| COPAUS2C | Online CICS | Pause | Pause screen 2 — pause with error detail display |
| CORPT00C | Online CICS | Reporting | Report menu — launches batch report request |
| COSGN00C | Online CICS | Sign-On | User sign-on — validates credentials, sets security profile |
| COTRN00C | Online CICS | Transaction | Transaction list — shows recent transactions for an account |
| COTRN01C | Online CICS | Transaction | Transaction add — posts a new transaction from a screen |
| COTRN02C | Online CICS | Transaction | Transaction view — displays a single transaction detail |
| COTRTLIC | Online CICS | Transaction | Transaction type list — shows available transaction categories |
| COTRTUPC | Online CICS | Transaction | Transaction update — amends a pending transaction |
| COUSR00C | Online CICS | User Mgmt | User list — displays all system users |
| COUSR01C | Online CICS | User Mgmt | User add — creates a new system user |
| COUSR02C | Online CICS | User Mgmt | User update — amends user details or password |
| COUSR03C | Online CICS | User Mgmt | User delete — removes a user from the system |
| CSUTLDTC | Batch | Utility | Date validity checker and converter subroutine |
| DBUNLDGS | Batch | DB Unload | Unloads VSAM dataset contents to a flat sequential file |
| PAUDBLOD | Batch | Pause/Audit | Audit database loader — loads pause audit records |
| PAUDBUNL | Batch | Pause/Audit | Audit database unload — exports pause audit records |

---

## How to Generate DOCX and PNG Files

### Prerequisites

```bash
pip install python-docx
npm install -g @mermaid-js/mermaid-cli   # for PNG diagrams
```

### Generate one program

```bash
cd business-docs
python tools/_md_to_docx.py CBACT01C/BIZ-CBACT01C.md CBACT01C/BIZ-CBACT01C.docx
```

### Regenerate all programs

```bash
cd business-docs
python tools/generate_all.py            # skips up-to-date files
python tools/generate_all.py --force    # regenerate everything
python tools/generate_all.py CBACT01C CBACT02C   # specific programs only
python tools/generate_all.py --dry-run  # preview without writing
```

### Run validation on a program

```bash
cd business-docs
# Phase 1 only (mechanical)
python tools/validate_doc.py CBACT01C --report

# Convert validation report to DOCX
python tools/_md_to_docx.py \
  validation/CBACT01C/CBACT01C-validation.md \
  validation/CBACT01C/CBACT01C-validation.docx
```

Phase 2 (LLM judge) is run via the `/validate-doc PROGNAME` slash command in Claude Code.

---

## Validation Reports

Every `BIZ-*.md` has been validated through a two-phase pipeline. Reports live in `validation/PROGNAME/`.

### Phase 1 — Mechanical (automated)
Checks structure, line-number bounds, identifier existence, PIC byte math, copybook coverage, DDname matching, and Mermaid diagram completeness. Run with:

```bash
cd business-docs
python tools/validate_doc.py PROGNAME --report
```

### Phase 2 — LLM Judge (semantic)
An LLM reads both source and document and scores six dimensions:

| Check | What it verifies |
|---|---|
| S1 Program Flow | PERFORM/CALL structure matches Section 2 description |
| S2 Error Handling | DISPLAY strings and error triggers match Section 3 |
| S3 Migration Notes | Every note is supported by source evidence |
| S4 Copybook Fields | PIC clauses, COMP-3 flags, 88-levels match actual .cpy files |
| S5 External Calls | CALL program names and argument fields are accurate |
| S6 Completeness | No significant business logic omitted |

Verdicts: **PASS** (safe for migration), **CONDITIONAL** (usable with noted caveats), **FAIL** (needs revision before use).

### Current Validation Status (April 2026)

All 44 programs have completed both phases. One FAIL identified:

| Verdict | Programs |
|---|---|
| **PASS** | CBACT03C, CBSTM03B, COACCT01, COBTUPDT, COBSWAIT |
| **FAIL** | COBIL00C (two material errors — see validation report before using) |
| **CONDITIONAL** | All remaining 38 programs |

---

## Documentation Standard

Every `BIZ-*.md` follows the rules in [DOCUMENTATION-STANDARD.md](DOCUMENTATION-STANDARD.md).  The canonical reference implementation is [CBACT01C/BIZ-CBACT01C.md](CBACT01C/BIZ-CBACT01C.md).

Key requirements:
- All copybooks resolved inline with field-level PIC and byte count
- 88-level values decoded
- Unused fields explicitly called out
- COMP-3 fields flagged for `BigDecimal` migration
- Latent bugs listed with line numbers
- No raw COBOL statements — plain English only
- Mermaid flowchart for every program's execution flow

---

## Adding a New Program

1. Place the `.cbl` in `source/cobol/` and run `/document NEWPROG` in Claude Code — the documenter agent handles steps 2–4 automatically.
2. Run `/validate-doc NEWPROG` to generate Phase 1 + Phase 2 validation reports.
3. Run `/master-doc` to update `output/MASTER-CARDDEMO.md` with the new program.
4. Add the program to the inventory table in this README.

Or manually:
1. Copy `TEMPLATE.md` to `NEWPROG/BIZ-NEWPROG.md`.
2. Read `source/cobol/NEWPROG.cbl` **and** every `.cpy` it COPYs.
3. Fill in all sections per `DOCUMENTATION-STANDARD.md`.
4. Run `python tools/_md_to_docx.py NEWPROG/BIZ-NEWPROG.md NEWPROG/BIZ-NEWPROG.docx`.

---

## Related Documentation

| Path | Contents |
|---|---|
| `business-docs/validation/` | Two-phase validation reports for all 44 programs |
| `output/docs/RE-*.md` | Reverse-engineering reports (technical, migration-focused) |
| `source/cobol/*.cbl` | COBOL source files (read-only reference) |
| `source/cobol/*.cpy` | Copybook definitions |
| `.claude/agents/validator.md` | LLM-judge agent definition |
| `.claude/rules/documentation-depth.md` | Documentation quality rules |
