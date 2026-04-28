# Validation Report: BIZ-COTRN01C.md

**Overall: PASS** — 5 passed · 0 failed · 3 warned · 2 skipped

Source file: `330` lines   |   Document: `298` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COTRN01C.cbl |
| `line_number_bounds` | ✓ **PASS** | 39 line reference(s) checked, all within bounds (max: 330) |
| `backtick_identifiers` | ⚠ **WARN** | 2 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 8 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 3/8 migration note(s) missing line number citations |
| `mermaid_diagram` | ⚠ **WARN** | Mermaid diagram missing classDef colour definitions |

## Issues Found

### backtick_identifiers (WARN)
2 backtick identifier(s) not found in source or copybooks

- `CDEMO-CT00-INFO`
- `LOW-VALUES`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Startup, EIBCALEN check, commarea copy, first-entry auto-read, EVALUATE EIBAID (ENTER/PF3/PF4/PF5/OTHER), PROCESS-ENTER-KEY validation, READ-TRANSACT-FILE, and CICS RETURN all match the source accurately. |
| S2 Error Handling | ✓ PASS | All three error conditions (blank TRNIDINI, NOTFND, OTHER) with accurate message strings, and the two silently-ignored conditions (RECEIVE, RETURN-TO-PREV-SCREEN) are correctly documented. |
| S3 Migration Notes | ✓ PASS | All 8 notes are supported by the source; no fabrications detected. |
| S4 Copybook Fields | ✓ PASS | CVTRA05Y, CDEMO-CT01-INFO, and internal working fields all verified; all 14 transaction fields correctly documented as displayed; dead-code fields correctly identified. |
| S5 External Calls | ✓ PASS | COTRN00C (PF5 XCTL), COMEN01C (PF3 fallback XCTL), COSGN00C (EIBCALEN=0 and RETURN-TO-PREV-SCREEN fallback) all documented correctly. |
| S6 Completeness | ⚠ WARN | Mermaid diagram missing `classDef` colour definitions (Phase 1 WARN confirmed). `INITIALIZE-ALL-FIELDS` paragraph content is described but the fact that it also clears `WS-MESSAGE` (line 326) is not called out — minor omission with no migration impact. |

### Findings

#### S1 — Program Flow
`MAIN-PARA` (lines 86–139) structure verified:
- `SET ERR-FLG-OFF TO TRUE` at line 88; `SET USR-MODIFIED-NO TO TRUE` at line 89; `MOVE SPACES TO WS-MESSAGE ERRMSGO OF COTRN1AO` at lines 91–92 — doc Step 1 correct ✓
- EIBCALEN=0: `MOVE 'COSGN00C' TO CDEMO-TO-PROGRAM`, `PERFORM RETURN-TO-PREV-SCREEN` at lines 95–96 ✓
- Commarea copy: `MOVE DFHCOMMAREA(1:EIBCALEN) TO CARDDEMO-COMMAREA` at line 98 ✓
- First-entry path (NOT CDEMO-PGM-REENTER): `SET CDEMO-PGM-REENTER TO TRUE`, `MOVE LOW-VALUES TO COTRN1AO`, `MOVE -1 TO TRNIDINL OF COTRN1AI` at lines 100–102 ✓
- `IF CDEMO-CT01-TRN-SELECTED NOT = SPACES AND LOW-VALUES` → `MOVE CDEMO-CT01-TRN-SELECTED TO TRNIDINI OF COTRN1AI`, `PERFORM PROCESS-ENTER-KEY` at lines 103–108 ✓
- `PERFORM SEND-TRNVIEW-SCREEN` at line 109 (always called, whether or not a transaction was pre-loaded) ✓
- EVALUATE EIBAID: DFHENTER, DFHPF3 (with CDEMO-FROM-PROGRAM fallback to COMEN01C), DFHPF4 (CLEAR-CURRENT-SCREEN), DFHPF5 (COTRN00C via RETURN-TO-PREV-SCREEN), OTHER — all confirmed at lines 113–132 ✓

`PROCESS-ENTER-KEY` (lines 144–192) verified:
- EVALUATE TRUE: `WHEN TRNIDINI OF COTRN1AI = SPACES OR LOW-VALUES` → `MOVE 'Y' TO WS-ERR-FLG`, message `'Tran ID can NOT be empty...'`, `MOVE -1 TO TRNIDINL`, `PERFORM SEND-TRNVIEW-SCREEN` at lines 147–152 ✓
- WHEN OTHER: `MOVE -1 TO TRNIDINL`, `CONTINUE` at lines 154–155 ✓
- `IF NOT ERR-FLG-ON` → clear 13 detail fields (SPACES), `MOVE TRNIDINI OF COTRN1AI TO TRAN-ID`, `PERFORM READ-TRANSACT-FILE` at lines 158–173 ✓
- `IF NOT ERR-FLG-ON` → populate 14 fields from TRAN-RECORD at lines 176–191, `PERFORM SEND-TRNVIEW-SCREEN` at line 191 ✓
- Field count: TRAN-AMT → WS-TRAN-AMT → TRNAMTI at lines 177 and 183; TRAN-ID, TRAN-CARD-NUM, TRAN-TYPE-CD, TRAN-CAT-CD, TRAN-SOURCE, TRAN-DESC, TRAN-ORIG-TS, TRAN-PROC-TS, TRAN-MERCHANT-ID, TRAN-MERCHANT-NAME, TRAN-MERCHANT-CITY, TRAN-MERCHANT-ZIP — all 14 moves confirmed at lines 178–190 ✓

`PF3 branch` (lines 116–122): `IF CDEMO-FROM-PROGRAM = SPACES OR LOW-VALUES MOVE 'COMEN01C' TO CDEMO-TO-PROGRAM ELSE MOVE CDEMO-FROM-PROGRAM TO CDEMO-TO-PROGRAM END-IF PERFORM RETURN-TO-PREV-SCREEN` — doc description accurate ✓.

`PF4 → CLEAR-CURRENT-SCREEN` (lines 301–304): `PERFORM INITIALIZE-ALL-FIELDS. PERFORM SEND-TRNVIEW-SCREEN.` ✓

`PF5 → COTRN00C` (line 126–127): `MOVE 'COTRN00C' TO CDEMO-TO-PROGRAM PERFORM RETURN-TO-PREV-SCREEN` ✓

`RETURN-TO-PREV-SCREEN` (lines 197–208): `IF CDEMO-TO-PROGRAM = LOW-VALUES OR SPACES MOVE 'COSGN00C' TO CDEMO-TO-PROGRAM`, sets CDEMO-FROM-TRANID, CDEMO-FROM-PROGRAM, CDEMO-PGM-CONTEXT=0, then XCTL ✓.

#### S2 — Error Handling
- **3.1 Blank TRNIDINI**: message `'Tran ID can NOT be empty...'` at line 149 — exact match ✓; cursor `MOVE -1 TO TRNIDINL` at line 151 ✓; `MOVE 'Y' TO WS-ERR-FLG` at line 148 ✓
- **3.1 NOTFND**: `MOVE 'Transaction ID NOT found...' TO WS-MESSAGE` at line 285–286; cursor at line 287; `MOVE 'Y' TO WS-ERR-FLG` at line 284 ✓
- **3.1 OTHER**: `DISPLAY 'RESP:' WS-RESP-CD 'REAS:' WS-REAS-CD` at line 290; `MOVE 'Unable to lookup Transaction...' TO WS-MESSAGE` at lines 292–293 ✓
- **3.2 RECEIVE never checks WS-RESP-CD**: lines 232–238 confirmed — RESP and RESP2 captured but no EVALUATE follows ✓
- **3.3 RETURN-TO-PREV-SCREEN fallback to COSGN00C**: lines 199–201 confirmed ✓

All message strings in doc match source character-for-character ✓.

#### S3 — Migration Notes
**Verified notes:**
- Note 1 (READ UPDATE but never REWRITE): `UPDATE` at line 275 confirmed; no REWRITE or UNLOCK anywhere in PROCEDURE DIVISION ✓
- Note 2 (RECEIVE never checks WS-RESP-CD): lines 232–238 confirmed ✓
- Note 3 (WS-USR-MODIFIED dead code): `SET USR-MODIFIED-NO TO TRUE` at line 89; no `SET USR-MODIFIED-YES` or `USR-MODIFIED-YES` anywhere in PROCEDURE DIVISION ✓
- Note 4 (unused COCOM01Y commarea fields): CDEMO-CUST-ID, CDEMO-ACCT-ID, CDEMO-CARD-NUM, CDEMO-LAST-MAP, CDEMO-LAST-MAPSET — none referenced in PROCEDURE DIVISION ✓
- Note 5 (CDEMO-CT01-INFO — TRNID-FIRST, TRNID-LAST, PAGE-NUM, NEXT-PAGE-FLG, TRN-SEL-FLG never used; CDEMO-CT01-TRN-SELECTED IS used): confirmed — only `CDEMO-CT01-TRN-SELECTED` is referenced at lines 103–106; all other CT01-INFO fields absent from PROCEDURE DIVISION ✓
- Note 6 (TRAN-AMT → WS-TRAN-AMT PIC `+99999999.99`): line 177 (`MOVE TRAN-AMT TO WS-TRAN-AMT`) and line 183 (`MOVE WS-TRAN-AMT TO TRNAMTI`) confirmed ✓
- Note 7 (all CVTRA05Y fields displayed): confirmed — all 13 data fields plus WS-TRAN-AMT for TRAN-AMT are displayed; only `FILLER PIC X(20)` is not displayed ✓. Doc note 7 says "All fields are displayed by this program. There are no unused transaction fields in COTRN01C" — this is accurate; the FILLER is not a named business field ✓.
- Note 8 (SEND-TRNVIEW-SCREEN always uses ERASE): `ERASE` at line 223 confirmed; no conditional erase flag in this program ✓

**Unsupported notes:** None.

**Missing issues:** None found. All behaviour observed in source is captured in the doc.

#### S4 — Copybook Fields
**CVTRA05Y fields**: All 14 data fields confirmed populated at lines 178–190. The ordering in doc matches source ordering ✓.
- `TRAN-TYPE-CD PIC X(02)` → `TTYPCDI` at line 180 ✓
- `TRAN-CAT-CD PIC 9(04)` → `TCATCDI` at line 181 ✓
- `TRAN-SOURCE PIC X(10)` → `TRNSRCI` at line 182 ✓
- `TRAN-ORIG-TS PIC X(26)` → `TORIGDTI` at line 185 (sent raw, no date extraction) ✓
- `TRAN-PROC-TS PIC X(26)` → `TPROCDTI` at line 186 ✓
- `FILLER PIC X(20)` correctly noted as "not used" ✓

**CDEMO-CT01-INFO extension** (lines 53–61): all six fields confirmed; PIC clauses match doc Appendix B table ✓. Only `CDEMO-CT01-TRN-SELECTED PIC X(16)` is used in PROCEDURE DIVISION ✓.

**Internal working fields (Appendix D)**:
- `WS-USR-MODIFIED PIC X(01)` — doc says "Dead code — set to 'N' at startup and never changed" — confirmed ✓
- `WS-TRAN-DATE PIC X(08) VALUE '00/00/00'` at line 50 — doc says "Declared but never populated in COTRN01C; the timestamp is sent raw" — confirmed; no MOVE to WS-TRAN-DATE in PROCEDURE DIVISION ✓
- `WS-TRAN-AMT PIC +99999999.99` at line 49 — receives TRAN-AMT at line 177 ✓

#### S5 — External Calls
- **COTRN00C (PF5)**: `MOVE 'COTRN00C' TO CDEMO-TO-PROGRAM` at line 126, `PERFORM RETURN-TO-PREV-SCREEN` at line 127 → XCTL ✓
- **COMEN01C (PF3 fallback)**: `MOVE 'COMEN01C' TO CDEMO-TO-PROGRAM` at line 117 when `CDEMO-FROM-PROGRAM = SPACES OR LOW-VALUES` ✓
- **COSGN00C (EIBCALEN=0 and RETURN-TO-PREV-SCREEN fallback)**: `MOVE 'COSGN00C' TO CDEMO-TO-PROGRAM` at lines 95 and 200 — both documented correctly ✓
- All transfers are XCTL via RETURN-TO-PREV-SCREEN; commarea passed is CARDDEMO-COMMAREA; CDEMO-FROM-TRANID and CDEMO-FROM-PROGRAM are set before transfer ✓

Minor accuracy check: Doc Section 2.2 "PF3 key" says "returns to `CDEMO-FROM-PROGRAM` if set; otherwise to `COMEN01C`". Source checks `CDEMO-FROM-PROGRAM = SPACES OR LOW-VALUES` — "blank" is the correct condition; "if set" in the doc is an acceptable paraphrase ✓.

#### S6 — Completeness
- **Mermaid diagram missing `classDef`**: Phase 1 WARN confirmed — the diagram uses `style` directives for individual nodes but no `classDef` group colour definitions. Functionally readable; purely cosmetic.
- **`INITIALIZE-ALL-FIELDS`** (lines 309–326): doc PF4 description says "calls `INITIALIZE-ALL-FIELDS` to blank all input fields and clears `WS-MESSAGE`". Source confirms: `MOVE SPACES TO ... WS-MESSAGE` at line 326. Doc is correct ✓. The exact list of 14 fields cleared (same as PROCESS-ENTER-KEY's clear block plus TRNIDINI) is not enumerated in the doc — minor omission; no migration impact.
- **`POPULATE-HEADER-INFO`** is described accurately as called from SEND-TRNVIEW-SCREEN; content (FUNCTION CURRENT-DATE, title/date/time) mirrors COTRN00C pattern confirmed at lines 243–262 ✓.

### Overall Recommendation
The document is safe to use as a migration reference. All display paths, read-with-update behaviour, error strings, dead-code identification, and commarea field layout are accurately documented. The single warning (Mermaid diagram missing `classDef`) is cosmetic and has no impact on correctness. The most important migration notes — the unnecessary UPDATE lock and the WS-USR-MODIFIED dead code — are correctly called out and will guide Java developers toward the right implementation choices.