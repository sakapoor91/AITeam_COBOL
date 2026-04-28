# Validation Report: BIZ-COMEN01C.md

**Overall: FAIL** — 4 passed · 2 failed · 2 warned · 2 skipped

Source file: `308` lines   |   Document: `344` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✗ **FAIL** | 2 required section(s) missing |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COMEN01C.cbl |
| `line_number_bounds` | ✓ **PASS** | 4 line reference(s) checked, all within bounds (max: 308) |
| `backtick_identifiers` | ⚠ **WARN** | 6 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✗ **FAIL** | 1 COPY statement(s) from source not documented in Appendix B |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 7/8 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### required_sections (FAIL)
2 required section(s) missing

- `Header block`
- `Section 1 — Purpose`

### backtick_identifiers (WARN)
6 backtick identifier(s) not found in source or copybooks

- `ALERT-DANGER`
- `ALERT-SUCCESS`
- `LOW-VALUES`
- `WS-USER-ID`

### copybook_coverage (FAIL)
1 COPY statement(s) from source not documented in Appendix B

- `DFHAID`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> Two Phase 1 FAILs (missing required sections: Header block and Section 1 — Purpose; missing DFHAID copybook in Appendix B) are structural format issues, not semantic accuracy failures. All six semantic checks pass or warn. No semantic FAILs.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | MAIN-PARA flow, commarea guard, EIBAID dispatch, PROCESS-ENTER-KEY option parsing, BUILD-MENU-OPTIONS, and POPULATE-HEADER-INFO all accurately described with correct logic. |
| S2 Error Handling | ✓ PASS | All six error conditions correctly identified with accurate trigger conditions and response actions. |
| S3 Migration Notes | ✓ PASS | All 8 notes verified; duplicate MOVE at lines 179–180 confirmed; USRSEC dead field confirmed; two-digit year confirmed. |
| S4 Copybook Fields | ⚠ WARN | DFHAID copybook not documented in Appendix B; COMEN02Y menu table fields and CSDAT01Y fields verified; minor: `CCDA-THANK-YOU` described as "not used by COMEN01C" which is correct. |
| S5 External Calls | ✓ PASS | XCTL to all sub-programs, CICS RETURN, and CICS INQUIRE correctly documented. |
| S6 Completeness | ⚠ WARN | DFHAID copybook (line 60 in source: `COPY DFHAID.`) missing from Appendix B; DFHBMSCA missing from Appendix B — both are IBM system copybooks. `RECEIVE-MENU-SCREEN` RESP/RESP2 never tested (documented at Appendix D item 3). |

### Findings

#### S1 — Program Flow
**MAIN-PARA** (line 75) startup sequence verified:
- `SET ERR-FLG-OFF TO TRUE` at line 77 — confirmed.
- `MOVE SPACES TO WS-MESSAGE` and `ERRMSGO OF COMEN1AO` at lines 79–80 — confirmed.
- `IF EIBCALEN = 0` at line 82: moves 'COSGN00C' to CDEMO-FROM-PROGRAM, calls RETURN-TO-SIGNON-SCREEN — confirmed at lines 83–84.
- `MOVE DFHCOMMAREA(1:EIBCALEN) TO CARDDEMO-COMMAREA` at line 86 — confirmed.
- `IF NOT CDEMO-PGM-REENTER` (line 87): set to REENTER (line 88), init COMEN1AO (line 89), SEND-MENU-SCREEN (line 90) — confirmed.
- ELSE: RECEIVE-MENU-SCREEN (line 92), EVALUATE EIBAID (line 93) — confirmed.

**EVALUATE EIBAID** (lines 93–103):
- `WHEN DFHENTER` → PROCESS-ENTER-KEY (line 95) — confirmed.
- `WHEN DFHPF3` → set CDEMO-TO-PROGRAM='COSGN00C', RETURN-TO-SIGNON-SCREEN (lines 97–98) — confirmed.
- `WHEN OTHER` → set WS-ERR-FLG='Y', load CCDA-MSG-INVALID-KEY, SEND-MENU-SCREEN (lines 100–102) — confirmed.

**PROCESS-ENTER-KEY** option parsing: doc says "The OPTIONI field from the received map is scanned from right to left to strip trailing spaces, left-justified into WS-OPTION-X (PIC X(02) JUST RIGHT), spaces replaced with '0'." Source lines 117–124 confirm exactly this: PERFORM VARYING WS-IDX FROM LENGTH OF OPTIONI BY -1 UNTIL non-space, then MOVE `OPTIONI(1:WS-IDX)` to WS-OPTION-X, INSPECT REPLACING spaces with '0', MOVE to WS-OPTION.

**Option validation** (lines 127–143): non-numeric, out-of-range, zero test — confirmed. Admin-only check — confirmed.

**EVALUATE dispatch** (lines 146–191):
- COPAUS0C special case with CICS INQUIRE (lines 147–168) — confirmed.
- DUMMY prefix check (lines 169–176) — confirmed.
- WHEN OTHER (lines 177–190): duplicate MOVE WS-PGMNAME at lines 179–180 — confirmed.

**BUILD-MENU-OPTIONS** (lines 264–303): PERFORM from 1 to CDEMO-MENU-OPT-COUNT, STRING format, EVALUATE WS-IDX for OPTN001O through OPTN012O — confirmed. Map stops at OPTN011O being written (12th option is WHEN OTHER → CONTINUE at line 299) — confirmed.

**POPULATE-HEADER-INFO** (lines 238–257): FUNCTION CURRENT-DATE, two-digit year extraction at line 249 `WS-CURDATE-YEAR(3:2)` → WS-CURDATE-YY — confirmed.

**CICS RETURN** at line 107: `TRANSID(WS-TRANID)` `COMMAREA(CARDDEMO-COMMAREA)` — confirmed. No LENGTH specified but implicit from structure size — doc does not claim LENGTH is specified, which is correct.

#### S2 — Error Handling
All six conditions verified:

| Condition | Doc trigger | Source trigger | Match |
|-----------|------------|---------------|-------|
| No commarea | `EIBCALEN = 0` | Line 82 | Yes |
| Invalid AID | NOT ENTER/PF3 | Lines 99–102 | Yes |
| Option not valid | IS NOT NUMERIC OR > count OR = 0 | Lines 127–133 | Yes |
| Admin-only option | `CDEMO-USRTYP-USER AND = 'A'` | Lines 136–143 | Yes |
| COPAUS0C not installed | CICS INQUIRE EIBRESP != NORMAL | Lines 152–167 | Yes |
| DUMMY option | Program name `(1:5) = 'DUMMY'` | Line 169 | Yes |

Error message for invalid option: `'Please enter a valid option number...'` — source line 131 confirms. "No access - Admin Only option..." at line 141 — source says `'No access - Admin Only option... '` with trailing space — confirmed.

CICS INQUIRE at lines 148–151 uses `NOHANDLE` — confirmed. String build for "not installed" message uses CDEMO-MENU-OPT-NAME delimited by `'  '` (two spaces) — source lines 163–167 confirm. Doc says delimited by `'  '` implicitly via the source quote.

Doc note: "The program has no CICS HANDLE CONDITION or NOHANDLE on the SEND/RECEIVE MAP calls." Verified: no HANDLE or RESP on EXEC CICS SEND MAP at lines 215–220 or EXEC CICS RECEIVE MAP at lines 227–233. Correct.

#### S3 — Migration Notes
**All 8 notes verified:**
1. Pseudo-conversational state — CDEMO-PGM-CONTEXT values 0/1 — confirmed.
2. XCTL semantics — confirmed: XCTL at lines 155–159, 184–187, 201–202.
3. Menu table static data (COMEN02Y) — confirmed: COPY COMEN02Y at line 51; CDEMO-MENU-OPT-COUNT, CDEMO-MENU-OPT-NAME, CDEMO-MENU-OPT-PGMNAME, CDEMO-MENU-OPT-USRTYPE fields used in PROCESS-ENTER-KEY.
4. COPAUS0C availability check — CICS INQUIRE NOHANDLE — confirmed.
5. Duplicate MOVE lines 179–180 — confirmed: source shows identical consecutive statements.
6. DFHBMSCA color attributes — DFHRED at line 162, DFHGREEN at line 171 — confirmed.
7. Two-digit year display — WS-CURDATE-YEAR(3:2) at line 249 — confirmed.
8. USRSEC dead field — WS-USRSEC-FILE at line 39, never referenced in PROCEDURE DIVISION — confirmed.

**Unsupported notes:** None.
**Missing bugs:** None significant beyond what is documented.

#### S4 — Copybook Fields
DFHAID copybook (line 60: `COPY DFHAID.`) provides `DFHENTER`, `DFHPF3` constants used in EVALUATE EIBAID — not documented in Appendix B. This is a structural omission (also caught by Phase 1 FAIL). As an IBM system copybook, its fields (`DFHENTER`, `DFHPF3`, etc.) are well-known but should be listed.

COMEN02Y fields verified: `CDEMO-MENU-OPT-COUNT` = 11, `CDEMO-MENU-OPT-PGMNAME(n)`, `CDEMO-MENU-OPT-USRTYPE(n)` — all used correctly in source. The 11-row menu table in Appendix A matches the COMEN02Y data (COPAUS0C as option 11 with CICS INQUIRE check) — confirmed.

CSDAT01Y fields: `WS-CURDATE-YEAR 9(04)`, `WS-CURDATE-MONTH 9(02)`, `WS-CURDATE-DAY 9(02)`, `WS-CURTIME-HOURS 9(02)`, etc. — used at lines 247–257. Doc table matches usage pattern.

`CCDA-THANK-YOU` (from COTTL01Y) described as "not used by COMEN01C" — confirmed: SEND-MENU-SCREEN uses CCDA-TITLE01 and CCDA-TITLE02 (lines 242–243) but not CCDA-THANK-YOU or the renamed field.

CSUSR01Y fields described as "never populated or referenced" — confirmed: no reference to `SEC-USR-*` fields in PROCEDURE DIVISION.

#### S5 — External Calls
All XCTL transfers verified:
- `RETURN-TO-SIGNON-SCREEN` paragraph (lines 196–203): XCTL to CDEMO-TO-PROGRAM. When PF3 pressed: target = COSGN00C (line 97). When EIBCALEN=0: CDEMO-TO-PROGRAM defaults to COSGN00C via guard at lines 198–200 — confirmed.
- WHEN OTHER XCTL (lines 184–187): `XCTL PROGRAM(CDEMO-MENU-OPT-PGMNAME(WS-OPTION)) COMMAREA(CARDDEMO-COMMAREA)` — confirmed.
- COPAUS0C XCTL (lines 155–159): same pattern but gated on CICS INQUIRE — confirmed.
- CICS RETURN (line 107) with `TRANSID(WS-TRANID)` and `COMMAREA(CARDDEMO-COMMAREA)` — confirmed.

#### S6 — Completeness
DFHAID (line 60) and DFHBMSCA (line 61) are not in Appendix A or B. Both are IBM system copybooks providing AID codes and BMS terminal attribute constants. Their absence is a format compliance issue but not a migration hazard since these constants are well-known.

The `SEND-MENU-SCREEN` paragraph uses `EXEC CICS SEND MAP('COMEN1A') MAPSET('COMEN01') FROM(COMEN1AO) ERASE` (lines 215–220). Doc Appendix B correctly identifies the map and mapset names.

`WS-REAS-CD` declared at line 44 but WS-RESP-CD/WS-REAS-CD from RECEIVE MAP are never tested — confirmed and documented at Appendix D item 3.

OPTN012O written only in the WHEN 12 branch which is unreachable (CDEMO-MENU-OPT-COUNT = 11 so the loop never reaches 12) — this is subtly documented at Appendix A note about OCCURS 12 but only 11 populated.

### Overall Recommendation
BIZ-COMEN01C.md is safe to use as a migration reference despite the Phase 1 structural FAILs (missing header block format and DFHAID in Appendix B). The business logic — commarea guard, pseudo-conversational state management, menu routing, admin access control, and COPAUS0C availability check — is all accurately described. The duplicate MOVE defect, dead USRSEC field, and unhandled SEND/RECEIVE MAP errors are all documented. A Java developer can implement this program directly from the document.