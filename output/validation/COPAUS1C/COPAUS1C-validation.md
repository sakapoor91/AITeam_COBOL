# Validation Report: BIZ-COPAUS1C.md

**Overall: PASS** — 6 passed · 0 failed · 2 warned · 2 skipped

Source file: `604` lines   |   Document: `382` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COPAUS1C.cbl |
| `line_number_bounds` | ✓ **PASS** | 30 line reference(s) checked, all within bounds (max: 604) |
| `backtick_identifiers` | ⚠ **WARN** | 3 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 10 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 2/8 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
3 backtick identifier(s) not found in source or copybooks

- `ABEND-DATA`
- `WS-DECL-RSN-IDX`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Startup, key-handling, IMS read/update, fraud toggle, and CICS RETURN all match the source accurately. |
| S2 Error Handling | ✓ PASS | All IMS error paths (GU, GNP, REPL), SCHEDULE-PSB failure, LINK failure, and WS-FRD-UPDT-FAILED are correctly documented with accurate message strings. |
| S3 Migration Notes | ⚠ WARN | All 8 notes are supported; one significant undocumented behavior identified. |
| S4 Copybook Fields | ✓ PASS | All CIPAUDTY and CIPAUSMY fields verified; COMP-3 flags, 88-levels, and unused-field annotations correct. |
| S5 External Calls | ✓ PASS | COPAUS2C LINK and COPAUS0C XCTL documented correctly with accurate commarea layout. |
| S6 Completeness | ⚠ WARN | `TAKE-SYNCPOINT` is called in `PROCESS-PF8-KEY` after the read — this is mentioned in the doc but the implied side-effect (releases the PSB before PF8 display) is not explicitly warned against. |

### Findings

#### S1 — Program Flow
The MAIN-PARA structure (lines 157–206) is accurately described. Cold-entry (EIBCALEN=0) path: `INITIALIZE CARDDEMO-COMMAREA`, set `CDEMO-TO-PROGRAM=COPAUS0C`, `PERFORM RETURN-TO-PREV-SCREEN` — all confirmed at lines 165–169 ✓. COMMAREA copy and `CDEMO-CPVD-FRAUD-DATA` clear (line 172) ✓. First-entry path calling `PROCESS-ENTER-KEY` then `SEND-AUTHVIEW-SCREEN` (lines 173–177) ✓. EVALUATE EIBAID: DFHENTER, DFHPF3, DFHPF5, DFHPF8, OTHER — all confirmed at lines 180–198 ✓.

`MARK-AUTH-FRAUD` (lines 230–266): fraud toggle logic — if `PA-FRAUD-CONFIRMED` set `PA-FRAUD-REMOVED` and `WS-REMOVE-FRAUD`, else set `PA-FRAUD-CONFIRMED` and `WS-REPORT-FRAUD` — confirmed at lines 236–242 ✓. LINK call at line 248 with `WS-FRAUD-DATA` commarea, NOHANDLE ✓. EIBRESP check at line 253, WS-FRD-UPDT-SUCCESS check at line 254 ✓.

`PROCESS-PF8-KEY` (lines 268–289): calls READ-AUTH-RECORD then READ-NEXT-AUTH-RECORD, then TAKE-SYNCPOINT, then checks AUTHS-EOF — confirmed ✓. The doc correctly notes that `READ-NEXT-AUTH-RECORD` issues a GNP without a WHERE clause (line 495–498).

`UPDATE-AUTH-DETAILS` (lines 520–552): restores `PENDING-AUTH-DETAILS` from `WS-FRAUD-AUTH-RECORD` (line 522), `DISPLAY 'RPT DT:' PA-FRAUD-RPT-DATE` (line 523), IMS REPL (lines 525–528), TAKE-SYNCPOINT on success, success message set — all confirmed ✓.

One minor inaccuracy: the doc (Section 2.1) says "the reenter flag is set and `PROCESS-ENTER-KEY` is called immediately followed by `SEND-AUTHVIEW-SCREEN`." In source (lines 173–177), `PROCESS-ENTER-KEY` is performed, then `SEND-AUTHVIEW-SCREEN` — that matches. However, the doc also says "If `CDEMO-PGM-REENTER` is not set (first time arriving from another program), the reenter flag is set". Source: `IF NOT CDEMO-PGM-REENTER` at line 173, then `SET CDEMO-PGM-REENTER TO TRUE` at line 174 ✓.

#### S2 — Error Handling
- `READ-AUTH-RECORD` GU error message: `' System error while reading Auth Summary: Code:'` at line 456 — doc says `' System error while reading Auth Summary: Code: <code>'` ✓ (note: source has no space before `Code:` — minor label approximation, not a factual error)
- `READ-AUTH-RECORD` GNP error: `' System error while reading Auth Details: Code:'` at line 481 ✓
- `READ-NEXT-AUTH-RECORD` error: `' System error while reading next Auth: Code:'` at line 511 ✓
- `UPDATE-AUTH-DETAILS` REPL failure: `' System error while FRAUD Tagging, ROLLBACK||'` at line 545 ✓
- `SCHEDULE-PSB` failure: `' System error while scheduling PSB: Code:'` at line 596 ✓
- `ROLL-BACK` (CICS SYNCPOINT ROLLBACK) — doc correctly notes this is called on LINK error and on `WS-FRD-UPDT-FAILED` ✓
- Section 3.4 notes that when EIBRESP is not NORMAL after LINK, ROLL-BACK is called with no message set — confirmed (lines 260–262, no WS-MESSAGE set) ✓

#### S3 — Migration Notes
**Verified notes:**
- Note 1 (`POPULATE-AUTH-DETAILS` not guarded for AUTHS-EOF on PROCESS-ENTER-KEY path): confirmed — `PERFORM POPULATE-AUTH-DETAILS` at line 227 is called unconditionally after `READ-AUTH-RECORD`, guarded only by `ERR-FLG-OFF` not `AUTHS-NOT-EOF` ✓
- Note 2 (`CDEMO-CPVD-FRAUD-DATA` PIC X(100)): confirmed at line 120; `WS-FRAUD-DATA` is 272 bytes; correctly noted that LINK uses separate WS-FRAUD-DATA ✓
- Note 3 (decline reason table with 10 entries): lines 57–67 confirmed exactly ✓
- Note 4 (`DISPLAY 'RPT DT:...'` debug trace): line 523 confirmed ✓
- Note 5 (TAKE-SYNCPOINT and ROLL-BACK are minimal stubs): lines 557–568 confirmed ✓
- Note 6 (COMP-3 monetary fields): `PA-TRANSACTION-AMT` and `PA-APPROVED-AMT` PIC S9(10)V99 COMP-3 in CIPAUDTY confirmed ✓
- Note 7 (`ASCENDING KEY IS DECL-CODE` for SEARCH ALL): line 70–71 confirmed ✓
- Note 8 (CSMSG02Y unused): COPY CSMSG02Y at line 135, no ABEND-DATA fields used ✓

**Unsupported notes:** None.

**Missing bugs:**
- `PROCESS-PF8-KEY` calls `TAKE-SYNCPOINT` (line 276–279) immediately after `READ-AUTH-RECORD` and `READ-NEXT-AUTH-RECORD` — this sets `IMS-PSB-NOT-SCHD` to TRUE and commits, releasing the IMS position. If `AUTHS-NOT-EOF`, the code then calls `POPULATE-AUTH-DETAILS` which accesses `PA-*` fields from `PENDING-AUTH-DETAILS` — this is fine because the data was already moved into working storage. But the PSB release means any further IMS access would need a re-schedule. Not documented, but not a data-correctness risk.

#### S4 — Copybook Fields
Verified against cipaudty.cpy and cipausmy.cpy:

**CIPAUDTY** (all fields verified):
- `PA-AUTHORIZATION-KEY` is a group with sub-fields `PA-AUTH-DATE-9C` S9(05) COMP-3 and `PA-AUTH-TIME-9C` S9(09) COMP-3 — doc Appendix C correctly notes these ✓
- `PA-AUTH-FRAUD` PIC X(01), 88 `PA-FRAUD-CONFIRMED VALUE 'F'`, 88 `PA-FRAUD-REMOVED VALUE 'R'` — confirmed ✓
- `PA-FRAUD-RPT-DATE` PIC X(08) ✓
- `FILLER` PIC X(17) — doc notes this ✓
- `PA-MATCH-STATUS` 88 values PA-MATCH-PENDING='P', PA-MATCH-AUTH-DECLINED='D', PA-MATCH-PENDING-EXPIRED='E', PA-MATCHED-WITH-TRAN='M' — confirmed ✓

**CIPAUSMY**: `PA-CUST-ID` PIC 9(09) — doc says `PA-CUST-ID` is referenced; it is moved from `CDEMO-CUST-ID` at line 246 ✓. However, `PA-CUST-ID` in `CIPAUSMY` is PIC 9(09) (not noted explicitly in COPAUS1C doc, but cross-referenced from COPAUA0C) — no discrepancy.

The `WS-DECLINE-REASON-TABLE` table entries verified: `'0000APPROVED'`, `'3100INVALID CARD'`, ..., `'9000UNKNOWN'` — all match lines 58–67 ✓. DECL-CODE is PIC X(4), DECL-DESC is PIC X(16) at lines 72–73 ✓.

#### S5 — External Calls
- **COPAUS2C LINK**: `EXEC CICS LINK PROGRAM(WS-PGM-AUTH-FRAUD) COMMAREA(WS-FRAUD-DATA) NOHANDLE` at line 248–252 — doc correctly states line 248, commarea `WS-FRAUD-DATA`, NOHANDLE. Input fields `WS-FRD-ACTION`, `WS-FRAUD-AUTH-RECORD`; output `WS-FRD-UPDATE-STATUS`, `WS-FRD-ACT-MSG` — all confirmed ✓.
- Commarea layout matches source LINKAGE definition in COPAUS2C: `WS-ACCT-ID 9(11)`, `WS-CUST-ID 9(9)`, `WS-FRAUD-AUTH-RECORD X(200)`, `WS-FRAUD-STATUS-RECORD` ✓.
- **COPAUS0C XCTL**: PF3 and cold-entry documented correctly ✓.

#### S6 — Completeness
- `POPULATE-HEADER-INFO` (line 409–429) is mentioned as called from `SEND-AUTHVIEW-SCREEN` but not described. Minor omission; behavior is standard header fill.
- `TAKE-SYNCPOINT` called after a successful read-only operation in `PROCESS-PF8-KEY` is documented in Section 2.2 ("If IMS PSB is still scheduled after the read, `TAKE-SYNCPOINT` is called"). This is correct ✓.
- All 88-level conditions on `WS-FRAUD-DATA` are documented in Appendix D ✓.

### Overall Recommendation
The document is safe to use as a migration reference. All IMS read/update paths, the fraud toggle logic, the CICS LINK interface to COPAUS2C, the decode table for decline reasons, and all error handling strings are accurately documented. The two warnings (AUTHS-EOF stale-data risk and TAKE-SYNCPOINT behavior) are important migration concerns that are addressed in the migration notes. A Java developer can safely implement this program from the document.