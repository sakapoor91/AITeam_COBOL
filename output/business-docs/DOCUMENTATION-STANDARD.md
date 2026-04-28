# BIZ-*.md Documentation Standard

Version 1.0 — established April 2026  
Applies to: every `BIZ-<PROGRAMNAME>.md` file in this repository.

---

## Purpose

Each `BIZ-<PROGRAMNAME>.md` file is the **single source of truth** for a COBOL program during migration to Java.  Developers translating to Java must be able to trust this document instead of re-reading the COBOL source.  Field-level decisions — types, nullability, COMP-3 conversion, boundary conditions — must be answerable from the document alone.

---

## Required Sections (in order)

### Header block
A metadata block at the very top of the file (not a markdown heading), formatted as:

```
Application : AWS CardDemo
Source File : PROGRAMNAME.cbl
Type        : [Batch COBOL | Online CICS COBOL]
Source Banner: [exact first comment line from the .cbl file, verbatim]
```

---

### Section 1 — Purpose

- What the program reads: **exact DDnames** (e.g. `ACCTFILE-FILE`) decoded to their business meaning.
- What the program writes: one sentence per output file.
- External programs called: list with business purpose.
- Note if hardcoded literals are test data (e.g. a hardcoded account ID used in a date conversion call).

---

### Section 2 — Program Flow

Split into three subsections:

**2.1 Startup** — steps from `PROCEDURE DIVISION` entry to the first record read.  
**2.2 Per-Record Loop** (or Per-Transaction, Per-Statement, etc.) — one step per logical action; each step includes the **exact paragraph name** and **source line number**.  
**2.3 Shutdown** — steps from loop exit to `STOP RUN`.

Rules:
- Every paragraph name cited must be exact (copy-paste from source).
- Every step must reference its source line number.
- Describe _what happens_ in plain English — no raw COBOL statements.
- Field names may appear inline as `backtick-quoted identifiers`.

---

### Section 3 — Error Handling

Describe each error-handling routine individually:
- The paragraph name and line number where it lives.
- What triggers it (which status codes, which conditions).
- Exact logic in plain English (e.g. "displays the file status byte as two decimal digits, then calls `ABEND-ROUTINE`").
- The exact DISPLAY string literals used, if any.

---

### Section 4 — Migration Notes

A numbered list of latent bugs, surprising behaviors, and migration pitfalls discovered in the source.  Examples of items that must appear:

- Unhandled file-status codes (only `'00'` and `'10'` handled — everything else falls through).
- Fields that exist in a copybook but are never read or written by this program.
- COMP-3 packed decimal fields that need `BigDecimal` in Java.
- Stale data risk: output fields populated in a prior iteration that are re-used if the next call fails.
- Implicit file close at `STOP RUN` (no explicit CLOSE before STOP RUN).
- Redundant guards, unreachable code, or unusual arithmetic idioms.
- Template copy artifacts (e.g. `'10'` EOF accepted on a WRITE — this is a copy-paste error).
- Typos in field names (preserve the misspelling with a note).

---

### Appendix A — Files

A table with one row per file:

| Logical Name | DDname | Organization | Recording | Key Field | Direction | Contents |
|---|---|---|---|---|---|---|

- **Logical Name**: the FD or SELECT name in the COBOL source.
- **DDname**: the ASSIGN target (JCL/CICS dataset name).
- **Organization**: SEQUENTIAL, INDEXED, RELATIVE, etc.
- **Recording**: FIXED, VARIABLE, etc. (omit if not specified).
- **Key Field**: primary key field name for INDEXED files; "N/A" for SEQUENTIAL.
- **Direction**: INPUT / OUTPUT / I-O / EXTEND.
- **Contents**: plain English (e.g. "Master account records, one per account").

---

### Appendix B — Copybooks and External Programs

**One table per copybook** copied by this program.  For each copybook:
- State the `COPY` statement DDname and the level-01 group name it defines.
- Include every field: name, PIC clause, byte count, and notes.
- Flag COMP-3 fields explicitly.
- Call out any fields present in the copybook but **not used** by this program.
- Show 88-level values with their literal values and plain-English meaning.

Then a subsection for each **external program** called (`CALL` statement):
- Paragraph name and line number of the call.
- Input fields set before the call.
- Output fields read after the call.
- Error/status fields that are **not** checked (call these out explicitly).

---

### Appendix C — Hardcoded Literals

A table of every hardcoded value that appears in the PROCEDURE DIVISION:

| Paragraph | Line | Value | Usage | Classification |
|---|---|---|---|---|

**Classification** values: `Test data`, `Business rule`, `Display message`, `System constant`.

---

### Appendix D — Internal Working Fields

A table of every working-storage field that is not part of a copybook and is not written to an output file:

| Field | PIC | Bytes | Purpose |
|---|---|---|---|

---

### Appendix E — Execution at a Glance

A Mermaid `flowchart TD` diagram showing the complete execution flow.  Requirements:
- Three main subgraphs: Startup, Per-Record Loop (or equivalent), Shutdown.
- Every paragraph name shown as a node.
- Data flow arrows labelled (e.g. "reads `ACCT-ID`", "writes `XREF-RECORD`").
- Loop-back arrow from the end of the loop body to the loop test.
- Colour-coded node styles: startup nodes in one colour, loop nodes in another, error/abend nodes in red.

---

## Content Depth Rules

| Rule | Detail |
|---|---|
| Resolve all COPY statements | Read the `.cpy` file and list all fields inline — never say "see copybook". |
| Decode 88-level values | Show what each code means (e.g. `'2'` → `YYYY-MM-DD-IN`). |
| Call out unused fields | Name every copybook field that is never referenced by the program. |
| Flag COMP-3 fields | Mark with "(COMP-3 — use BigDecimal in Java)". |
| Note typos | If a field name has an obvious typo (e.g. `ACCT-EXPIRAION-DATE`, `CODATECN-0UT-DATE`), preserve the exact spelling and add "(typo)". |
| No raw COBOL statements | Never include `MOVE`, `WRITE`, `IF`, `CALL`, `PERFORM` etc. as literal code blocks. Describe the action in English. Field names and paragraph names may appear as `backtick code`. |
| External program parameters | Always state which output fields the caller does **not** check — these are migration risks. |
| Write-status checks | If a program accepts `'10'` (EOF) on a WRITE, call it out as a template copy artifact. |
| Exact DISPLAY literals | Quote the exact string from every `DISPLAY` statement. |
| Identify latent bugs | All items in Migration Notes must cite line numbers. |

---

## What NOT to Include

- Raw COBOL code blocks (use plain English descriptions instead).
- Speculative features or future requirements.
- Content that can be derived directly from reading the current source without analysis (e.g., simple field lists without notes).

---

## File Naming and Folder Structure

Each program lives in its own subfolder:

```
business-docs/
  PROGRAMNAME/
    BIZ-PROGRAMNAME.md      ← this document
    BIZ-PROGRAMNAME.docx    ← generated by tools/_md_to_docx.py
    BIZ-PROGRAMNAME-flow.png ← Mermaid diagram PNG (generated alongside DOCX)
```

Generate the DOCX and PNG from the MD:

```bash
cd business-docs
python tools/_md_to_docx.py PROGRAMNAME/BIZ-PROGRAMNAME.md PROGRAMNAME/BIZ-PROGRAMNAME.docx
# or regenerate all:
python tools/generate_all.py
```

---

## Reference Implementation

`CBACT01C/BIZ-CBACT01C.md` is the canonical exemplar for this standard.  When in doubt, match its depth and structure.
