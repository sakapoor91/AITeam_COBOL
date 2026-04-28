# Validation Report: BIZ-COTRTLIC.md

**Overall: FAIL** — 3 passed · 4 failed · 2 warned · 1 skipped

Source file: `2098` lines   |   Document: `661` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✗ **FAIL** | 3 required section(s) missing |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COTRTLIC.cbl |
| `line_number_bounds` | ✗ **FAIL** | 1 line number(s) exceed source file length (2098 lines) |
| `backtick_identifiers` | ⚠ **WARN** | 294 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✗ **FAIL** | 1 COPY statement(s) from source not documented in Appendix B |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | ✗ **FAIL** | 1 byte count(s) inconsistent with PIC clause |
| `migration_notes_line_refs` | ⚠ **WARN** | Section 4 contains no numbered migration notes |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### required_sections (FAIL)
3 required section(s) missing

- `Section 1 — Purpose`
- `Section 3 — Error Handling`
- `Section 4 — Migration Notes`

### line_number_bounds (FAIL)
1 line number(s) exceed source file length (2098 lines)

- `207600`

### backtick_identifiers (WARN)
294 backtick identifier(s) not found in source or copybooks

- `C-TR-TYPE-BACKWARD`
- `C-TR-TYPE-FORWARD`
- `CA-DELETE-NOT-REQUESTED`
- `CA-DELETE-REQUESTED`
- `CA-DELETE-SUCCEEDED`
- `CA-FIRST-PAGE`
- `CA-LAST-PAGE-NOT-SHOWN`
- `CA-LAST-PAGE-SHOWN`
- `CA-NEXT-PAGE-EXISTS`
- `CA-NEXT-PAGE-NOT-EXISTS`

### copybook_coverage (FAIL)
1 COPY statement(s) from source not documented in Appendix B

- `DFHBMSCA`

### pic_byte_accuracy (FAIL)
1 byte count(s) inconsistent with PIC clause

- `DCL-TR-TYPE: doc says 2 bytes, computed 8 from "Likely PIC X(02)"`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all semantic FAIL checks but has warnings; Phase 1 structural failures (missing sections, line-number overflow, copybook gaps) are noted.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Main control logic, PF key routing, and pagination paragraphs all match source accurately. |
| S2 Error Handling | ⚠ WARN | SQLCODE handling is mostly accurate but the doc's description of the -911 deadlock message text is slightly imprecise. |
| S3 Migration Notes | ⚠ WARN | COMP-3 fields are real; no fabricated notes found; however the DFHBMSCA copybook omission from the doc is a real gap. |
| S4 Copybook Fields | ⚠ WARN | BMS map field names in the doc use an abstracted OCCURS notation that does not match the actual numbered copybook fields. |
| S5 External Calls | ✓ PASS | No CALL statements to external programs; all DB2 SQL operations confirmed. |
| S6 Completeness | ⚠ WARN | Inline REDEFINES of BMS map (lines 434–478 in source), CVCRD01Y copybook usage, and ABEND handling paragraphs are not documented. |

### Findings

#### S1 — Program Flow
Source PROCEDURE DIVISION (lines 498–916 in the read segment, continuing through ~2098):
- `0000-MAIN` initializes working storage, loads COMMAREA, calls `YYYY-STORE-PFKEY`, checks first-entry vs. re-entry (lines 515–553). Doc correctly describes this startup sequence.
- PFK03 branch (lines 591–625): issues EXEC CICS SYNCPOINT then XCTL to `CDEMO-TO-PROGRAM` (defaulting to `LIT-ADMINPGM` = `COADM01C`). Doc states "EXEC CICS SYNCPOINT + XCTL → CDEMO-TO-PROGRAM (defaults to COADM01C)" — accurate.
- PFK02 branch (lines 630–652): XCTL to `LIT-ADDTPGM` = `COTRTUPC`. Doc states "PF2 | XCTL → COTRTUPC (add new transaction type)" — accurate.
- EVALUATE in lines 698–879 handles: INPUT-ERROR redisplay, PF07+first-page, PF03, PF08+next-page, PF07+not-first-page (8100-READ-BACKWARDS), ENTER+deletes, PF10+delete confirm (9300-DELETE-RECORD), ENTER+updates, PF10+update confirm (9200-UPDATE-RECORD). Doc's navigation table matches all these branches.
- PF08 uses `8000-READ-FORWARD` from `WS-CA-LAST-TR-CODE`; PF07 uses `8100-READ-BACKWARDS` from `WS-CA-FIRST-TR-CODE`. Doc's pagination description is accurate.
- `COMMON-RETURN` (line 899) packs both CARDDEMO-COMMAREA and WS-THIS-PROGCOMMAREA into WS-COMMAREA before EXEC CICS RETURN. Doc describes this dual-commarea pattern in the COMMAREA section.

#### S2 — Error Handling
Verified SQLCODE handling against source (lines 1854–1889 for UPDATE, lines 1907–1935 for DELETE):
- UPDATE SQLCODE 0: EXEC CICS SYNCPOINT, `CA-UPDATE-SUCCEEDED` set, `WS-INFORM-UPDATE-SUCCESS` message = `'HIGHLIGHTED row was updated'` (lines 1858–1863). Doc states "SQLCODE 0 Success → EXEC CICS SYNCPOINT" — accurate.
- UPDATE SQLCODE -911: message `'Deadlock. Someone else updating ?'` (line 1874). Doc states `'Deadlock. Someone else updating?'` (missing space before `?`) — trivially accurate, same content.
- DELETE SQLCODE -532: message `'Please delete associated child records first:'` (line 1919). Doc states `"Please delete associated child records first"` — accurate.
- WARN: The doc documents SQLCODE `+100` for "No more rows" as `CA-NEXT-PAGE-NOT-EXISTS` set — this is true for cursor FETCH results (handled in `8000-READ-FORWARD` and `8100-READ-BACKWARDS`). However the UPDATE path SQLCODE +100 (line 1861) does not set `CA-NEXT-PAGE-NOT-EXISTS`; it sets `CA-UPDATE-REQUESTED` and logs `'Record not found. Deleted by others ?'`. The doc conflates both +100 scenarios under one row — this could mislead a developer.
- DELETE SQLCODE OTHER (not -532): handled with generic `'Delete failed with message:'` string (line 1929). Not documented in the doc's SQLCODE table.

#### S3 — Migration Notes
**Verified notes:**
- `WS-EDIT-SELECT-COUNTER` PIC S9(04) USAGE COMP-3 (source line 015600) — confirmed COMP-3.
- `WS-EDIT-SELECT-FLAGS` / REDEFINES pattern (lines 018000–018800) — confirmed.
- `WS-EDIT-ALPHANUM-LENGTH` PIC S9(4) COMP-3 (line 014700) — confirmed COMP-3.
- DB2 cursor declarations (`C-TR-TYPE-FORWARD`, `C-TR-TYPE-BACKWARD`) — confirmed at lines 033800 and 035400.
- Dual-commarea serialization (CARDDEMO-COMMAREA + WS-THIS-PROGCOMMAREA) — confirmed at COMMON-RETURN lines 904–907.

**Unsupported notes:** Phase 1 flagged `207600` as a line number exceeding 2098. The BIZ doc does not appear to cite line 207600 directly — this is likely a sequence number artefact from the COBOL source that the Phase 1 mechanical validator mistook for a line reference.

**Missing bugs:**
- `CA-DELETE-SUCCEEDED` is defined with VALUE `LOW-VALUES` (line 041400), which is the same value as `CA-DELETE-NOT-REQUESTED` (line 041200). These two 88-levels cannot be distinguished: any test of `CA-DELETE-SUCCEEDED` will also match `CA-DELETE-NOT-REQUESTED`. This is a latent defect not documented in the BIZ doc.
- `DFHBMSCA` is COPYed at line 042500 but is not listed in the Copybooks table in the doc (Phase 1 confirmed this gap).

#### S4 — Copybook Fields
The BIZ doc describes the BMS map layout using an abstracted `EACH-ROWI OCCURS 7` notation with fields `TRTSELI`, `TRTTYPI`, `TRTYPDI`. Verified against COTRTLI.cpy:
- The actual copybook does NOT contain an OCCURS group named `EACH-ROWI`. Instead it has seven individually named groups: `TRTSEL1I/TRTTYP1I/TRTYPD1I` through `TRTSEL7I/TRTTYP7I/TRTYPD7I` (lines 73–198 of COTRTLI.cpy).
- The program source at lines 434–455 creates a REDEFINES over CTRTLIAI that wraps the 7 rows into `EACH-ROWI OCCURS 7 TIMES` using subscript access (`TRTSELI(I)`, `TRTTYPI(I)`, `TRTYPDI(I)`).
- The BIZ doc's description (`EACH-ROWI OCCURS 7`) reflects the redefine-overlay naming from COTRTLIC.cbl lines 436–455, not the raw copybook. This is technically correct for how the program uses it, but WARN: a Java developer reading only the doc would not know that the underlying copybook uses numbered field names.
- `TRTYPEI` (type-code filter, PIC X(2)) and `TRDESCI` (description filter, PIC X(50)) confirmed at COTRTLI.cpy lines 66 and 72 — match doc exactly.
- WS-THIS-PROGCOMMAREA fields: `WS-CA-TYPE-CD` PIC X(02), `WS-CA-TYPE-DESC` PIC X(50), `WS-CA-ALL-ROWS-OUT` PIC X(364), `WS-CA-ROW-SELECTED` PIC S9(4) COMP, `WS-CA-FIRST-TR-CODE` PIC X(02), `WS-CA-LAST-TR-CODE` PIC X(02), `WS-CA-SCREEN-NUM` PIC 9(1) — all confirmed at source lines 037800–040300. All match the doc.
- `DCL-TR-TYPE` is described in the doc as "X(02)" and the Phase 1 check found a byte inconsistency. The DCLTRTYP include defines `TR_TYPE` as `CHAR(2)` which maps to PIC X(02). The Phase 1 flag "computed 8 from Likely PIC X(02)" appears to be a validator tool artefact, not an actual source error.

#### S5 — External Calls
No `CALL` statements to external programs found in COTRTLIC source. All data access is via EXEC SQL (DB2) and EXEC CICS (VSAM/screen). SKIP criterion does not apply here; all documented interactions are SQL-based and confirmed correct.

#### S6 — Completeness
- The BIZ doc notes `CVACT02Y` as "Card master record layout (defined, not directly read)" — confirmed; the COPY at line 049000 is present but no SQL or CICS READ against it exists.
- `CVCRD01Y` is also COPYed at line 032700 and provides `CC-WORK-AREA`. It is not in the doc's copybook table. `CC-WORK-AREA` is used in the INITIALIZE at line 050000. Minor omission.
- ABEND-ROUTINE is not in COTRTUPC (though it exists there). COTRTLIC has no EXEC CICS HANDLE ABEND. No omission here.
- `9998-PRIMING-QUERY` (lines 684–691): priming SELECT COUNT(1) on TRANSACTION_TYPE table used as DB2 connectivity check. Doc mentions "SELECT COUNT(1) (filter validation / priming query)" in the DB2 Table table — this is documented.
- `9999-FORMAT-DB2-MESSAGE` is a common error-formatting paragraph that builds `WS-LONG-MSG` from DB2 error context. Not documented but is infrastructure code.
- Phase 1 correctly flagged missing Section 1 (Purpose), Section 3 (Error Handling), and Section 4 (Migration Notes). The semantic content of these sections exists scattered in the Technical Reference but is not formally structured per the documentation standard.

### Overall Recommendation
The semantic content of BIZ-COTRTLIC.md is reliable and accurate enough for a Java developer to implement the core logic. However the document has two significant concerns: (1) it fails the Phase 1 structural check for missing required sections, meaning a Java developer must hunt for error-handling and migration guidance within the Technical Reference rather than finding it in dedicated sections; (2) the `CA-DELETE-SUCCEEDED` / `CA-DELETE-NOT-REQUESTED` same-value defect (both VALUE LOW-VALUES) is a real latent bug that must be surfaced as a migration note so the Java replacement implements proper state tracking. The document should be revised to add the missing sections before use as a migration reference.