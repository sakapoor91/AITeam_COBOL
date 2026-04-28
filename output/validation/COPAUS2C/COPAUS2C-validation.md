# Validation Report: BIZ-COPAUS2C.md

**Overall: FAIL** — 5 passed · 1 failed · 2 warned · 2 skipped

Source file: `244` lines   |   Document: `315` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✗ **FAIL** | 2 required section(s) missing |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COPAUS2C.cbl |
| `line_number_bounds` | ✓ **PASS** | 1 line reference(s) checked, all within bounds (max: 244) |
| `backtick_identifiers` | ⚠ **WARN** | 30 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 2 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 9/10 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### required_sections (FAIL)
2 required section(s) missing

- `Header block`
- `Section 1 — Purpose`

### backtick_identifiers (WARN)
30 backtick identifier(s) not found in source or copybooks

- `ACCT-ID`
- `ACQR-COUNTRY-CODE`
- `APPROVED-AMT`
- `AUTH-FRAUD`
- `AUTH-ID-CODE`
- `AUTH-RESP-CODE`
- `AUTH-RESP-REASON`
- `AUTH-TS`
- `AUTH-TYPE`
- `CARD-EXPIRY-DATE`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention. The Phase 1 FAIL on `required_sections` was due to the absence of the standard header block and Section 1 header — the document does have purpose and flow content; this is a structural formatting deficiency only.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | ASKTIME/FORMATTIME, time complement reversal, field mapping, INSERT, SQLCODE branching, FRAUD-UPDATE, and CICS RETURN all match the source exactly. |
| S2 Error Handling | ✓ PASS | SQLCODE=0, SQLCODE=-803, and non-zero SQLCODE branches are correctly documented with accurate message strings. |
| S3 Migration Notes | ✓ PASS | All 10 notes are supported by the source; no fabrications; one additional latent issue noted below. |
| S4 Copybook Fields | ✓ PASS | CIPAUDTY fields verified; all PA-* host variable mappings match the source; COMP-3 flags correctly identified. |
| S5 External Calls | – SKIP | COPAUS2C has no outgoing CALL or XCTL — it is a leaf subroutine. |
| S6 Completeness | ⚠ WARN | The doc misidentifies the invocation mechanism: COPAUS2C is reached via CICS LINK (from COPAUS1C), not via XCTL. The doc repeatedly says "called via XCTL" which is incorrect. |

### Findings

#### S1 — Program Flow
MAIN-PARA flow confirmed:
- `EXEC CICS ASKTIME NOHANDLE ABSTIME(WS-ABS-TIME)` at lines 91–94 ✓
- `EXEC CICS FORMATTIME ABSTIME(WS-ABS-TIME) MMDDYY(WS-CUR-DATE) DATESEP NOHANDLE` at lines 95–100 ✓
- `MOVE WS-CUR-DATE TO PA-FRAUD-RPT-DATE` at line 101 ✓
- Date extraction: `PA-AUTH-ORIG-DATE(1:2)→WS-AUTH-YY`, `(3:2)→WS-AUTH-MM`, `(5:2)→WS-AUTH-DD` at lines 103–105 ✓
- Time complement reversal: `COMPUTE WS-AUTH-TIME = 999999999 - PA-AUTH-TIME-9C` at line 107 ✓
- Time component extraction: WS-AUTH-TIME-AN(1:2)→HH, (3:2)→MI, (5:2)→SS, (7:3)→SSS at lines 108–111 ✓
- All 26 field MOVE statements (lines 113–139) match doc's field mapping table exactly ✓
- `WS-AUTH-TS` structure with `'-'` and `'.'` FILLERs and trailing `'000'` at lines 38–51 ✓
- INSERT statement (lines 141–198) uses `TIMESTAMP_FORMAT(:AUTH-TS,'YY-MM-DD HH24.MI.SSNNNNNN')` and `CURRENT DATE` for `FRAUD_RPT_DATE` ✓
- SQLCODE=0 → success (lines 199–201), SQLCODE=-803 → FRAUD-UPDATE (lines 202–204), other → error (lines 205–214) ✓
- `FRAUD-UPDATE` paragraph: UPDATE with SET AUTH_FRAUD=:AUTH-FRAUD, FRAUD_RPT_DATE=CURRENT DATE WHERE CARD_NUM and AUTH_TS (lines 221–229) ✓
- `EXEC CICS RETURN END-EXEC` (no TRANSID) at lines 218–219 ✓

**Error in doc (S6):** Section 2.1 states "CICS dispatches via XCTL". The program is actually invoked via CICS LINK (from COPAUS1C line 248). A program reached via LINK retains the caller's task context and can return. A program reached via XCTL does not return. This distinction is critical for migration — the RETURN at line 218 returns control to the LINK caller (COPAUS1C). The doc's later statement in Section 2.3 ("Because COPAUS2C issues a plain `EXEC CICS RETURN` (no `TRANSID`), control returns to CICS and the task ends") is **incorrect** for a LINK invocation; RETURN returns to the calling program, not terminating the task. The commarea output fields (WS-FRD-UPDT-SUCCESS, WS-FRD-ACT-MSG) ARE readable by the caller after LINK returns.

#### S2 — Error Handling
- INSERT SQLCODE=0: `SET WS-FRD-UPDT-SUCCESS TO TRUE; MOVE 'ADD SUCCESS' TO WS-FRD-ACT-MSG` at lines 200–201 ✓
- SQLCODE=-803: `PERFORM FRAUD-UPDATE` at line 204 ✓
- Other SQLCODE: `SET WS-FRD-UPDT-FAILED; MOVE SQLCODE TO WS-SQLCODE; MOVE SQLSTATE TO WS-SQLSTATE; STRING ' SYSTEM ERROR DB2: CODE:' WS-SQLCODE ', STATE: ' WS-SQLSTATE INTO WS-FRD-ACT-MSG` at lines 206–214 ✓
- UPDATE SQLCODE=0: `SET WS-FRD-UPDT-SUCCESS; MOVE 'UPDT SUCCESS' TO WS-FRD-ACT-MSG` at lines 231–232 ✓
- UPDATE non-zero: error string `' UPDT ERROR DB2: CODE:' WS-SQLCODE ', STATE: ' WS-SQLSTATE` at lines 239–241 — doc states the same format pattern, though it generalizes the prefix as "error message", which is slightly imprecise. No impact on migration. ✓
- ASKTIME/FORMATTIME NOHANDLE: confirmed, silently suppressed ✓

#### S3 — Migration Notes
**Verified notes:**
- Note 1 (plain CICS RETURN — see S6 caveat): the RETURN is real, though the task-end characterization is wrong for a LINK invocation ✓
- Note 2 (time complement `999999999 - PA-AUTH-TIME-9C`): line 107 confirmed ✓
- Note 3 (COMP-3 monetary amounts `PA-TRANSACTION-AMT`, `PA-APPROVED-AMT` PIC S9(10)V99 COMP-3): confirmed in CIPAUDTY ✓
- Note 4 (MERCHANT-NAME as variable-length, `LENGTH OF PA-MERCHANT-NAME` = 22): `MOVE LENGTH OF PA-MERCHANT-NAME TO MERCHANT-NAME-LEN` at line 130 ✓
- Note 5 (FRAUD_RPT_DATE discrepancy — WS-CUR-DATE moved to PA-FRAUD-RPT-DATE but SQL uses CURRENT DATE): confirmed — lines 101 and 194 ✓
- Note 6 (AUTHFRDS DCLGEN not in repo): `EXEC SQL INCLUDE AUTHFRDS END-EXEC` at line 69 ✓
- Note 7 (SQLCA mapping): `EXEC SQL INCLUDE SQLCA END-EXEC` at line 65 ✓
- Note 8 ("CATAGORY" misspelling): `PA-MERCHANT-CATAGORY-CODE` and `MERCHANT-CATAGORY-CODE` at lines 125–126 ✓
- Note 9 (No EIBCALEN guard): no EIBCALEN check anywhere in PROCEDURE DIVISION ✓
- Note 10 (DFHBMSCA commented out): `*COPY DFHBMSCA.` at line 61 ✓

**Unsupported notes:** None.

**Missing bugs:**
- The doc's Appendix D note 2 states "WS-FRD-UPDATE-STATUS output never inspected by caller because COPAUS2C uses EXEC CICS RETURN (not a CALL), and its caller used XCTL." This is factually wrong in two ways: (1) the caller uses CICS LINK (not XCTL), and (2) after LINK RETURN, the commarea fields ARE readable by COPAUS1C (which does read WS-FRD-UPDT-SUCCESS at line 254 and WS-FRD-ACT-MSG at line 257). This is an actual documentation error that would mislead a migration developer.

#### S4 — Copybook Fields
CIPAUDTY fields used as SQL host variables, verified against cipaudty.cpy:
- `PA-CARD-NUM` PIC X(16) → CARD-NUM ✓
- `PA-AUTH-TYPE` PIC X(04) → AUTH-TYPE ✓
- `PA-CARD-EXPIRY-DATE` PIC X(04) → CARD-EXPIRY-DATE ✓
- `PA-AUTH-TIME-9C` PIC S9(09) COMP-3 used in time complement computation ✓
- `PA-AUTH-ORIG-DATE` PIC X(06) used for YY/MM/DD extraction ✓
- `PA-TRANSACTION-AMT` PIC S9(10)V99 COMP-3, `PA-APPROVED-AMT` PIC S9(10)V99 COMP-3 — doc correctly flags these as BigDecimal ✓
- `PA-MERCHANT-CATAGORY-CODE` PIC X(04) — misspelling preserved correctly ✓
- `PA-POS-ENTRY-MODE` PIC 9(02) — doc says "2-digit POS mode" ✓
- `PA-MATCH-STATUS` PIC X(01) with 88-levels confirmed in cipaudty.cpy ✓
- `PA-AUTH-FRAUD` PIC X(01) with 88-levels `PA-FRAUD-CONFIRMED='F'`, `PA-FRAUD-REMOVED='R'` ✓
- `FILLER` PIC X(17) at end of CIPAUDTY ✓

#### S5 — External Calls
SKIP — COPAUS2C is a leaf subroutine with no CALL, LINK, or XCTL to other programs.

#### S6 — Completeness
**Significant error — invocation mechanism:** The document states "CICS dispatches via XCTL" in the Mermaid diagram and Section 2.3 describes the RETURN as ending the task. However, COPAUS1C invokes COPAUS2C via `EXEC CICS LINK PROGRAM(WS-PGM-AUTH-FRAUD) COMMAREA(WS-FRAUD-DATA) NOHANDLE` at line 248. Under LINK:
- COPAUS2C runs in the same CICS task as COPAUS1C.
- `EXEC CICS RETURN` in COPAUS2C returns to COPAUS1C at the statement after the LINK.
- The commarea (WS-FRAUD-DATA) is shared, so WS-FRD-UPDT-SUCCESS and WS-FRD-ACT-MSG are directly readable by COPAUS1C after return.
- Appendix D note 2 in the BIZ doc claiming "WS-FRD-UPDATE-STATUS ... not accessible after the RETURN" is **incorrect** and would cause a Java developer to misdesign the service interface.

The correct Java design is: COPAUS2C maps to a synchronous `@Service` method that accepts the auth record and action, performs DB2 operations, and returns a status DTO — exactly as note 1 describes. The issue is only with the XCTL/task-end language; the Java recommendation in note 1 is correct.

### Overall Recommendation
The document is mostly safe to use as a migration reference — the DB2 SQL logic, field mappings, timestamp reconstruction, COMP-3 fields, and SQLCODE handling are all accurate. However, there is one significant error: the document incorrectly describes COPAUS2C as invoked via XCTL and incorrectly states the commarea output fields are inaccessible to the caller. A developer reading Appendix D note 2 would wrongly conclude the caller cannot receive the success/failure status. This section should be corrected before use: COPAUS2C is invoked via CICS LINK, returns to COPAUS1C, and the WS-FRD-UPDATE-STATUS / WS-FRD-ACT-MSG fields are directly read by COPAUS1C after the call returns.