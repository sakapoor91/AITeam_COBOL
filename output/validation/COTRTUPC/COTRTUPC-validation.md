# Validation Report: BIZ-COTRTUPC.md

**Overall: FAIL** — 5 passed · 2 failed · 2 warned · 1 skipped

Source file: `1702` lines   |   Document: `651` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✗ **FAIL** | 3 required section(s) missing |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COTRTUPC.cbl |
| `line_number_bounds` | ✓ **PASS** | 118 line reference(s) checked, all within bounds (max: 1702) |
| `backtick_identifiers` | ⚠ **WARN** | 341 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✗ **FAIL** | 2 COPY statement(s) from source not documented in Appendix B |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | ✓ **PASS** | 2 PIC/byte-count pair(s) checked — all consistent |
| `migration_notes_line_refs` | ⚠ **WARN** | Section 4 contains no numbered migration notes |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### required_sections (FAIL)
3 required section(s) missing

- `Section 1 — Purpose`
- `Section 3 — Error Handling`
- `Section 4 — Migration Notes`

### backtick_identifiers (WARN)
341 backtick identifier(s) not found in source or copybooks

- `ABEND-CODE`
- `ABEND-CULPRIT`
- `ABEND-DATA`
- `ABEND-MSG`
- `ABEND-REASON`
- `CA-DELETE-SUCCEEDED`
- `CC-ACCT-ID`
- `CC-CARD-NUM`
- `CC-CUST-ID`
- `CC-WORK-AREA`

### copybook_coverage (FAIL)
2 COPY statement(s) from source not documented in Appendix B

- `DFHBMSCA`
- `DFHAID`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all semantic FAIL checks; Phase 1 structural failures (missing sections, undocumented copybooks) require attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | The state-machine flow and PF key routing match the source accurately. |
| S2 Error Handling | ⚠ WARN | SQLCODE handling is accurate but the doc incorrectly names the key used to trigger the UPDATE save. |
| S3 Migration Notes | ⚠ WARN | Real COMP-3 fields documented; ABEND-ROUTINE and CSSTRPFY are undocumented infrastructure items. |
| S4 Copybook Fields | ✓ PASS | COMMAREA extension fields and DB2 DCLGEN fields match source exactly. |
| S5 External Calls | ✓ PASS | No external CALL statements confirmed; all operations via EXEC SQL. |
| S6 Completeness | ⚠ WARN | DFHBMSCA and DFHAID copybooks undocumented (Phase 1 confirmed); ABEND-ROUTINE at line 167500 not documented. |

### Findings

#### S1 — Program Flow
Source PROCEDURE DIVISION (lines 344–1702):
- `0000-MAIN` (line 345): EXEC CICS HANDLE ABEND LABEL(ABEND-ROUTINE) (line 348). Doc does not call this out but it is infrastructure.
- INITIALIZE working storage, MOVE LIT-THISTRANID TO WS-TRANID, `YYYY-STORE-PFKEY` (lines 352–387). Doc describes startup correctly.
- COMMAREA check (lines 366–381): if EIBCALEN = 0 OR coming from ADMINPGM/LISTTPGM without re-enter → initialize and set `TTUP-DETAILS-NOT-FETCHED`. Doc states "CLEAR SCREEN, CLEAR SAVED CONTEXT, ASK USER FOR SEARCH KEYS" for first-entry — accurate.
- `0001-CHECK-PFKEYS` (line 400) validates the key is legal given current state before the main EVALUATE. Doc does not explicitly describe this validity gate but the result (invalid keys fall through to `WS-INVALID-KEY-PRESSED`) is described.
- Main EVALUATE (lines 423–556): PF03→SYNCPOINT+XCTL, first-entry→3000-SEND-MAP, PF04+TTUP-CONFIRM-DELETE→9800-DELETE-PROCESSING, PF04+TTUP-SHOW-DETAILS→ask confirm, PF05+TTUP-DETAILS-NOT-FOUND→TTUP-CREATE-NEW-RECORD, PF05+TTUP-CHANGES-OK-NOT-CONFIRMED→9600-WRITE-PROCESSING, PF12→cancel+re-fetch, OTHER→1000-PROCESS-INPUTS+2000-DECIDE-ACTION+3000-SEND-MAP. Doc's navigation table matches all branches.
- `9600-WRITE-PROCESSING` (line 1531): UPDATE; if SQLCODE +100 falls through to `9700-INSERT-RECORD` (line 1559). Doc correctly documents this INSERT fallthrough.
- `9800-DELETE-PROCESSING` (line 1624): DELETE using `TTUP-OLD-TTYP-TYPE` (not `TTUP-NEW-TTYP-TYPE`). Doc states "DELETE WHERE TR_TYPE = :DCL-TR-TYPE" — accurate; the key is `TTUP-OLD-TTYP-TYPE` as confirmed at line 1625.
- COMMON-RETURN (line 559): packs dual commarea. Consistent with COTRTLIC pattern.

#### S2 — Error Handling
Verified against source:
- SQLCODE 0 on UPDATE (line 1556): EXEC CICS SYNCPOINT → `TTUP-CHANGES-OKAYED-AND-DONE`. Doc states "EXEC CICS SYNCPOINT (after UPDATE/INSERT/DELETE)" — accurate.
- SQLCODE +100 on UPDATE (line 1558): falls to `9700-INSERT-RECORD`. Doc: "Falls through to 9700-INSERT-RECORD (creates new type code)" — accurate.
- SQLCODE -911 on UPDATE (line 1561): sets `COULD-NOT-LOCK-REC-FOR-UPDATE` → `WS-RETURN-MSG` message `'Could not lock record for update'` (line 18200 of source) → `TTUP-CHANGES-OKAYED-LOCK-ERROR`. Doc says `"Could not lock record for update"; state set to TTUP-CHANGES-OKAYED-LOCK-ERROR` — accurate.
- SQLCODE -532 on DELETE (line 1638): message `'Please delete associated child records first:'` (line 1641). Doc states same message — accurate.
- WARN: The BIZ doc Navigation table states "PF5 (changes validated, TTUP-CHANGES-OK-NOT-CONFIRMED) → UPDATE". The source at line 514 shows `WHEN CCARD-AID-PFK05 AND TTUP-CHANGES-OK-NOT-CONFIRMED → 9600-WRITE-PROCESSING`. The flow through `1000-PROCESS-INPUTS` (ENTER key path) is what sets the state to `TTUP-CHANGES-OK-NOT-CONFIRMED` (line 775). So the described flow is correct in substance: the user first presses ENTER to validate, then PF5 to save. This is accurately documented.
- WARN: `9100-GET-TRANSACTION-TYPE` SELECT SQLCODE +100 triggers `WS-RECORD-NOT-FOUND` message `'No record found for this key in database'` (source line 017600). Doc states `'+100 Row not found on SELECT → Error: "No record found for this key in database"'` — accurate.

#### S3 — Migration Notes
**Verified notes:**
- `WS-EDIT-ALPHANUM-LENGTH` PIC S9(4) COMP-3 (line 005600) — confirmed COMP-3.
- `TTUP-CHANGE-ACTION` state machine with 88-levels — all state values (`K`, `X`, `S`, `R`, `V`, `9`, `8`, `7`, `6`, `E`, `N`, `L`, `F`, `C`, `B`) confirmed at source lines 029800–032700.
- `CSUTLDWY` generic date edit variables COPY (line 007600) — confirmed; CCYYMMDD date validation variables.
- `CSMSG02Y` abend variables (line 027400) — confirmed; defines ABEND-DATA, ABEND-CULPRIT, ABEND-CODE, ABEND-MSG.
- `DCLTRCAT` EXEC SQL INCLUDE (line 028800) — confirmed; DCLTRCAT is defined but no direct SQL executed against TRANSACTION_CATEGORY table in the examined procedures.

**Unsupported notes:** None fabricated.

**Missing bugs:**
- `TTUP-REVIEW-NEW-RECORD` (value `'V'`) is defined as an 88-level at line 030600 but is never SET anywhere in the source. This is a dead state code — potentially an incomplete implementation. Not documented.
- `ABEND-ROUTINE` at line 167500: catches unexpected abends, sends ABEND-DATA to screen, then issues EXEC CICS HANDLE ABEND CANCEL. This means abend data is displayed to the end user directly — a security/UX concern for migration. Not documented.
- `CSSTRPFY` is COPYed inline at line 167100 in the PROCEDURE DIVISION (unusual location). Not listed in the copybook table.

#### S4 — Copybook Fields
Verified WS-THIS-PROGCOMMAREA (lines 029400–033500):
- `TTUP-CHANGE-ACTION` PIC X(1): confirmed, all 88-levels match doc table exactly.
- `TTUP-OLD-TTYP-TYPE` PIC X(02): confirmed at line 033000.
- `TTUP-OLD-TTYP-TYPE-DESC` PIC X(50): confirmed at line 033100. Doc calls it `TTUP-OLD-TTYP-TYPE-DESC` — match.
- `TTUP-NEW-TTYP-TYPE` PIC X(02): confirmed at line 033400.
- `TTUP-NEW-TTYP-TYPE-DESC` PIC X(50): confirmed at line 033500.
- Doc's commarea table uses slightly different names: `TTUP-OLD-TTYP-TYPE` vs actual `TTUP-OLD-TTYP-TYPE` (exact match), `TTUP-OLD-TTYP-TYPE-DESC` vs actual `TTUP-OLD-TTYP-TYPE-DESC` (exact match). All correct.
- `DCL-TR-TYPE` and `DCL-TR-DESCRIPTION` come from `EXEC SQL INCLUDE DCLTRTYP` — host variable types match standard DB2 CHAR(2) and VARCHAR mapping. No discrepancy.

#### S5 — External Calls
No CALL statements exist in COTRTUPC.cbl. All external data access is via EXEC SQL against DB2 and EXEC CICS for screen management. SKIP does not apply; confirmed no external CALLs.

#### S6 — Completeness
- `DFHBMSCA` (line 025700) and `DFHAID` (line 025800) are COPYed but not in the doc's copybook table. Phase 1 confirmed this gap. Both are needed by the Java developer to understand attribute byte constants used in `3000-SEND-MAP`.
- `CSSTRPFY` is COPYed inline in the PROCEDURE DIVISION at line 167100 — not documented. It provides PF-key mapping logic referenced as `YYYY-STORE-PFKEY`. The BIZ doc mentions it in the copybook table — but Phase 1 did not flag it because it is an inline COPY in the PROCEDURE DIVISION rather than DATA DIVISION.
- `CVCRD01Y` (line 024100): COPYed, defines `CC-WORK-AREA` used in INITIALIZE at line 035200. Not in the doc's copybook table (same gap as COTRTLIC). Minor omission.
- ABEND-ROUTINE paragraph (lines 167500–169400): sends ABEND-DATA to screen, cancels CICS HANDLE ABEND. Not documented in error handling or Section 3.
- The doc describes the screen key `PF3 → EXEC CICS SYNCPOINT + XCTL`. Source confirms SYNCPOINT at lines 453–455. Accurate.

### Overall Recommendation
BIZ-COTRTUPC.md contains accurate semantic information about the core business logic and state machine, making it usable as a migration reference for the update/insert/delete operations. Two corrections are needed before handing to a Java developer: (1) the DFHBMSCA and DFHAID copybooks must be added to the copybook table as their attribute byte constants drive screen formatting; (2) the ABEND-ROUTINE behavior (raw abend data displayed to end users) should be called out as a migration risk so the Java replacement implements proper error handling. The missing required sections (Purpose, Error Handling, Migration Notes) from the structural perspective are the most significant gap — the semantic content is distributed in the Technical Reference but is not structured for quick consumption.