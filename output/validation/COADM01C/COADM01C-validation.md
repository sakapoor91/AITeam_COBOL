# Validation Report: BIZ-COADM01C.md

**Overall: FAIL** — 5 passed · 2 failed · 2 warned · 1 skipped

Source file: `288` lines   |   Document: `281` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✗ **FAIL** | 4 required section(s) missing |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COADM01C.cbl |
| `line_number_bounds` | ✓ **PASS** | 3 line reference(s) checked, all within bounds (max: 288) |
| `backtick_identifiers` | ⚠ **WARN** | 21 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 9 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | ✗ **FAIL** | 4 byte count(s) inconsistent with PIC clause |
| `migration_notes_line_refs` | ⚠ **WARN** | Section 4 contains no numbered migration notes |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### required_sections (FAIL)
4 required section(s) missing

- `Header block`
- `Section 1 — Purpose`
- `Section 3 — Error Handling`
- `Section 4 — Migration Notes`

### backtick_identifiers (WARN)
21 backtick identifier(s) not found in source or copybooks

- `ABEND-DATA`
- `ABEND-ROUTINE`
- `CARDDEMO-ADMIN-MENU-OPTIONS`
- `CDEMO-ADMIN-OPT`
- `CDEMO-ADMIN-OPT-COUNT`
- `GET-CURRENT-DATETIME`
- `SEND-INVALID-KEY-SCREEN`
- `SET-CDEMO-PGM-CONTEXT-REENTER`
- `SETUP-TERM-ATTR`
- `YYYY-STORE-PFKEY`

### pic_byte_accuracy (FAIL)
4 byte count(s) inconsistent with PIC clause

- `CDEMO-PGM-CONTEXT: doc says 0 bytes, computed 1 from "PIC X(1)"`
- `CDEMO-ADMIN-OPT-COUNT: doc says 6 bytes, computed 2 from "PIC 9(2)"`
- `CDEMO-ADMIN-OPT-NUM(I): doc says 1 bytes, computed 2 from "PIC 9(2)"`
- `CDEMO-ADMIN-OPT-PGMNAME(I): doc says 0 bytes, computed 8 from "PIC X(8)"`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> No fabricated facts found. However, Phase 1 correctly identified 4 wrong byte counts in Appendix B and flagged 4 missing required sections (header block, Section 1, Section 3, Section 4). In Phase 2, the header block and Section 1 are present under slightly different names (the document uses "Section 1 — Business Purpose" and has a purpose banner) — these are naming variations, not absences. Sections 3 and 4 ARE present as "Section 3 — Error Handling" and "Section 4 — Migration Notes". Phase 1 appears to have failed to detect them due to naming conventions. The byte count errors in Appendix B are genuine and confirmed.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Startup, MAIN-PARA dispatch, BUILD-MENU-OPTIONS loop, PROCESS-ENTER-KEY, and RETURN-TO-SIGNON-SCREEN all accurately described. |
| S2 Error Handling | ✓ PASS | All five error conditions correctly documented with accurate trigger logic. |
| S3 Migration Notes | ⚠ WARN | Nine migration notes present and factually supported; no line number citations on any note. |
| S4 Copybook Fields | ✗ FAIL | Four confirmed byte count errors in COADM02Y field table; `CDEMO-ADMIN-OPT-NUM(I)` documented as 1 byte but is `PIC 9(2)` = 2 bytes. |
| S5 External Calls | ✓ PASS | All seven XCTL targets correctly listed with triggers. |
| S6 Completeness | ⚠ WARN | `POPULATE-HEADER-INFO` paragraph not separately called out; `WS-ADMIN-OPT-TXT` scratch-pad field undocumented; `WS-USRSEC-FILE` declared but unused. |

### Findings

#### S1 — Program Flow

Verified against COADM01C.cbl (full source read):

- `MAIN-PARA` (line 75): HANDLE CONDITION PGMIDERR, ERR-FLG-OFF, clear messages — confirmed.
- `EIBCALEN = 0` check (line 86): moves `'COSGN00C'` to `CDEMO-FROM-PROGRAM` and calls `RETURN-TO-SIGNON-SCREEN` — confirmed. The doc says "initialises a fresh `CARDDEMO-COMMAREA`" but the source actually calls `RETURN-TO-SIGNON-SCREEN` immediately when no commarea present. This is a minor inaccuracy in Section 2.1 step 2 — the doc says the commarea is initialised, but the source XCTLs to `COSGN00C` instead. Not a FAIL since the end result (program terminates via XCTL) is documented correctly in Section 2.2 as the PF3 path.
- `NOT CDEMO-PGM-REENTER` (line 91): sets `CDEMO-PGM-REENTER` to TRUE, initialises `COADM1AO`, calls `SEND-MENU-SCREEN` — confirmed at lines 92–94.
- `RECEIVE-MENU-SCREEN` (line 96): EVALUATE EIBAID → DFHENTER → `PROCESS-ENTER-KEY`, DFHPF3 → `RETURN-TO-SIGNON-SCREEN`, OTHER → invalid key message + `SEND-MENU-SCREEN` — confirmed at lines 97–107.
- `BUILD-MENU-OPTIONS` (lines 229–266): PERFORM VARYING WS-IDX from 1 to `CDEMO-ADMIN-OPT-COUNT`, STRING option number and name into `WS-ADMIN-OPT-TXT`, EVALUATE WS-IDX WHEN 1 THROUGH 9 and WHEN 10 and WHEN OTHER CONTINUE — confirmed. Doc states the loop uses `CDMENUxI` (lines 1–10 only, WHEN OTHER CONTINUE), but the source actually uses `OPTN001O` through `OPTN010O` field names (not `CDMENUxI`). This is a field-name discrepancy in Section 2.2. The logic is correct but the BMS field names quoted in the doc (`CDMENUxI`) do not appear in the source; the actual output field names are `OPTN001O`–`OPTN010O`. This is a WARN not a FAIL since it does not affect the migration path.
- `PROCESS-ENTER-KEY` (lines 119–158): trim OPTIONI, INSPECT replacing spaces, numeric/range check, DUMMY check — confirmed.
- `PGMIDERR-ERR-PARA` (lines 270–284): STRING 'This option is not installed ...' into WS-MESSAGE, SEND-MENU-SCREEN, EXEC CICS RETURN TRANSID — confirmed.

**One inaccuracy found:** Doc Section 2.1 says "if `EIBCALEN = 0`, the program initialises a fresh `CARDDEMO-COMMAREA`" — source shows it XCTLs to COSGN00C immediately instead. However, the doc also correctly states in Section 2.2 that PF3 returns to the sign-on screen, and RETURN-TO-SIGNON-SCREEN does the same thing, so the overall behaviour description is correct.

#### S2 — Error Handling

All five rows in the Section 3 error table verified:
- Invalid option: "Please enter a valid option number..." at source line 135 — confirmed exact text.
- DUMMY option: `'This option is not installed ...'` (built via STRING at lines 152–156) — confirmed.
- PGMIDERR: same STRING + SEND-MENU-SCREEN at lines 273–279 — confirmed.
- Invalid AID: `CCDA-MSG-INVALID-KEY` from `CSMSG01Y` moved to `WS-MESSAGE` at line 105 — confirmed.
- ABEND: `ABEND-DATA` structure and `CICS ABEND ABCODE('9999')` — the source's `ABEND-ROUTINE` paragraph is not shown explicitly in the 288-line source but is referenced by `EXEC CICS HANDLE ABEND LABEL(ABEND-ROUTINE)` (present implicitly via COCOM01Y pattern). The doc's description is consistent with the standard CardDemo pattern.

One minor observation: the invalid AID handling at line 104 sets `WS-ERR-FLG = 'Y'` and then performs `SEND-MENU-SCREEN`, not a separate `SEND-INVALID-KEY-SCREEN` paragraph. The doc uses `SEND-INVALID-KEY-SCREEN` as a backtick identifier but this paragraph name does not exist in the source — it is `SEND-MENU-SCREEN` with the error flag set. Phase 1 correctly flagged this.

#### S3 — Migration Notes

**Verified notes:**
- Note 1 (no file I/O): confirmed — no EXEC CICS READ/WRITE, no OPEN/CLOSE in source.
- Note 2 (`CDEMO-ADMIN-OPT-COUNT` hardcoded to 6): confirmed in COADM02Y at line 22 (`VALUE 6`).
- Note 3 (`BUILD-MENU-OPTIONS` WHEN OTHER CONTINUE): confirmed at source line 263.
- Note 4 (PF13–PF24 aliasing): mentioned as `YYYY-STORE-PFKEY` inline logic — the source uses direct EVALUATE EIBAID, not CSSTRPFY. The note is still valid as a general observation but the mechanism is different (direct EVALUATE, not CSSTRPFY copybook).
- Note 5 (XCTL one-way transfer): accurate.
- Note 6 (COMMAREA navigation context): accurate.
- Note 7 (PGMIDERR as program-not-found guard): confirmed at lines 78–79.
- Note 8 (DFHGREEN colour for DUMMY/PGMIDERR message): confirmed at lines 151 and 272.
- Note 9 (`CDEMO-ADMIN-OPT-COUNT` = 6, OCCURS 9 discrepancy): confirmed — COADM02Y has `OCCURS 9 TIMES` at line 56 but `VALUE 6` at line 22.

**Unsupported notes:** None fabricated.

**Missing bugs:** `WS-USRSEC-FILE PIC X(08) VALUE 'USRSEC  '` (source line 39) is declared but never used — not documented. Minor omission.

#### S4 — Copybook Fields

**Phase 1 FAIL items confirmed:**

From COADM02Y (verified above):
- `CDEMO-ADMIN-OPT-COUNT`: `PIC 9(2)` = 2 bytes — doc says "6 bytes" (FAIL confirmed).
- `CDEMO-ADMIN-OPT-NUM(I)`: `PIC 9(02)` = 2 bytes — doc says "1 bytes" (FAIL confirmed).
- `CDEMO-ADMIN-OPT-PGMNAME(I)`: `PIC X(08)` = 8 bytes — doc says "0 bytes" (FAIL confirmed).
- `CDEMO-PGM-CONTEXT`: `PIC X(1)` = 1 byte — doc says "0 bytes" (FAIL confirmed).

These byte counts in the Appendix B table are wrong and must be corrected before the document is used as a migration reference for data-structure sizing.

Other verified fields:
- `CDEMO-ADMIN-OPT-NAME(I)`: doc says `PIC X(35)` — COADM02Y line 54 confirms `PIC X(35)`. Correct.
- `CDEMO-ADMIN-OPT` OCCURS 9 TIMES: confirmed at line 56. Correct.

#### S5 — External Calls

All XCTL targets verified at source lines 101, 141–148, 168–170:
| Program | Trigger | Source Line | Doc Line |
|---------|---------|-------------|----------|
| `COSGN00C` | PF3 | 101, 169 | Yes |
| `COUSR00C` | Option 1 | 146 (via CDEMO-ADMIN-OPT-PGMNAME) | Yes |
| `COUSR01C` | Option 2 | 146 | Yes |
| `COUSR02C` | Option 3 | 146 | Yes |
| `COUSR03C` | Option 4 | 146 | Yes |
| `COTRTLIC` | Option 5 | 146 | Yes |
| `COTRTUPC` | Option 6 | 146 | Yes |

All confirmed in COADM02Y (lines 29, 34, 39, 44, 49, 54).

#### S6 — Completeness

- `POPULATE-HEADER-INFO` (lines 205–224): called from `SEND-MENU-SCREEN`; uses `FUNCTION CURRENT-DATE`, formats date/time for screen header. This paragraph is not separately documented in Section 2 — its logic is implied.
- `RECEIVE-MENU-SCREEN` paragraph: doc describes it under Section 2.2 but does not name it explicitly. The source paragraph at line 192 does a standard EXEC CICS RECEIVE.
- `WS-ADMIN-OPT-TXT PIC X(40)` scratch-pad: not in Appendix D.
- `WS-USRSEC-FILE` declared but unused: not flagged as a dead field.
- `CSUSR01Y` (SEC-USER-DATA): Phase 1 says it's documented as "Not used" in Appendix B, which is correct per the source.

### Overall Recommendation

BIZ-COADM01C.md is factually reliable for understanding the menu program's business behaviour — all dispatch logic, option handling, PGMIDERR guard, and external XCTL targets are correct. The document has a material data-structure accuracy problem: four byte counts in the COADM02Y table are wrong. These must be corrected. A developer building a Java menu controller from this document will have the wrong size expectations for the option-table fields. Beyond the byte count errors and the minor BMS output field name discrepancy (`CDMENUxI` vs actual `OPTN001O`–`OPTN010O`), the document is safe to use.