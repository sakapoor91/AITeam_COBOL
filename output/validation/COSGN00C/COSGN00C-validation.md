# Validation Report: BIZ-COSGN00C.md

**Overall: FAIL** — 6 passed · 1 failed · 1 warned · 2 skipped

Source file: `260` lines   |   Document: `358` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COSGN00C.cbl |
| `line_number_bounds` | ✓ **PASS** | 42 line reference(s) checked, all within bounds (max: 260) |
| `backtick_identifiers` | ✓ **PASS** | 149 COBOL-style identifiers verified against source |
| `copybook_coverage` | ✗ **FAIL** | 3 COPY statement(s) from source not documented in Appendix B |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 3/7 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### copybook_coverage (FAIL)
3 COPY statement(s) from source not documented in Appendix B

- `DFHAID`
- `DFHBMSCA`
- `DFHATTR`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention. The Phase 1 FAIL on `copybook_coverage` was a structural documentation deficiency only: `DFHATTR` is commented out in source (`*COPY DFHATTR.` at line 59) and is not active; `DFHAID` and `DFHBMSCA` are real runtime copybooks that are present in source and documented in the footer note of the BIZ doc, but their copybook entries are absent from Appendix B proper. No content of either copybook is misrepresented; the issue is structural formatting only.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | EIBCALEN=0 path, EVALUATE EIBAID branches, PROCESS-ENTER-KEY validation, READ-USER-SEC-FILE branches, XCTL targets, PF3 plain-text exit, and CICS RETURN all match the source exactly. |
| S2 Error Handling | ✓ PASS | All five error conditions with accurate message strings and cursor positions verified against source. |
| S3 Migration Notes | ✓ PASS | All 7 notes are supported by the source; no fabrications; one minor latent issue noted below. |
| S4 Copybook Fields | ✓ PASS | CSUSR01Y, CSDAT01Y, CSMSG01Y, and COCOM01Y fields verified; all unused fields correctly annotated. |
| S5 External Calls | ✓ PASS | COADM01C and COMEN01C XCTL calls documented correctly with hardcoded literal program names at lines 232 and 237. |
| S6 Completeness | ⚠ WARN | `DFHAID` and `DFHBMSCA` copybooks missing from Appendix B section headers; `DFHATTR` is commented-out and correctly absent. One CSMSG01Y/COTTL01Y naming nuance not highlighted. |

### Findings

#### S1 — Program Flow
`MAIN-PARA` (lines 73–102) structure verified:
- `SET ERR-FLG-OFF TO TRUE` at line 75; `MOVE SPACES TO WS-MESSAGE` and `ERRMSGO OF COSGN0AO` at lines 77–78 ✓
- `EIBCALEN = 0` path (line 80): `MOVE LOW-VALUES TO COSGN0AO`, `MOVE -1 TO USERIDL OF COSGN0AI`, `PERFORM SEND-SIGNON-SCREEN` — doc correctly states "clears the output map to low values, sets cursor on the `USERIDL` field, and sends the signon screen" ✓
- On EIBCALEN=0, the program then falls through to `EXEC CICS RETURN TRANSID(WS-TRANID) COMMAREA(CARDDEMO-COMMAREA) LENGTH(LENGTH OF CARDDEMO-COMMAREA)` at lines 98–102. Doc correctly notes "returns to CICS with `TRANSID = 'CC00'` and an empty COMMAREA" — CARDDEMO-COMMAREA is in working storage and is zero-initialized; no copy from DFHCOMMAREA occurs (there is none). ✓
- EVALUATE EIBAID: `DFHENTER` → `PROCESS-ENTER-KEY`, `DFHPF3` → `CCDA-MSG-THANK-YOU` + `SEND-PLAIN-TEXT`, OTHER → set error flag + `CCDA-MSG-INVALID-KEY` + `SEND-SIGNON-SCREEN` — confirmed at lines 86–95 ✓
- `PROCESS-ENTER-KEY` (lines 108–140): CICS RECEIVE, EVALUATE TRUE for USERIDI empty / PASSWDI empty / OTHER, `FUNCTION UPPER-CASE` applied to both fields at lines 132–136, then `IF NOT ERR-FLG-ON PERFORM READ-USER-SEC-FILE` — all confirmed ✓
- `SEND-PLAIN-TEXT` (lines 162–172): issues `EXEC CICS SEND TEXT FROM(WS-MESSAGE) LENGTH(LENGTH OF WS-MESSAGE) ERASE FREEKB END-EXEC` then `EXEC CICS RETURN END-EXEC` (bare, no TRANSID) — doc says "bare CICS RETURN with no TRANSID, ending the transaction" ✓
- `POPULATE-HEADER-INFO` (lines 177–204): FUNCTION CURRENT-DATE, CICS ASSIGN APPLID and SYSID — doc describes this accurately ✓

One precision note: Doc Section 3.6 quotes `CCDA-MSG-INVALID-KEY` as `'Invalid key pressed. Please see below...'`. Source in CSMSG01Y confirms the literal is `'Invalid key pressed. Please see below...         '` (50 chars with trailing spaces) — the doc's abbreviation is acceptable; no migration impact.

#### S2 — Error Handling
- **3.1 Missing user ID**: `MOVE 'Please enter User ID ...' TO WS-MESSAGE` at line 120; cursor `MOVE -1 TO USERIDL OF COSGN0AI` at line 121 ✓
- **3.2 Missing password**: `MOVE 'Please enter Password ...' TO WS-MESSAGE` at line 125; cursor on PASSWDL at line 126 ✓
- **3.3 Wrong password**: `MOVE 'Wrong Password. Try again ...' TO WS-MESSAGE` at line 242–243; cursor on PASSWDL at line 244. Doc correctly notes "Does not set `WS-ERR-FLG`" — confirmed: no `MOVE 'Y' TO WS-ERR-FLG` in this branch (lines 241–245) ✓
- **3.4 User not found (RESP=13)**: `MOVE 'User not found. Try again ...' TO WS-MESSAGE` at line 249; `MOVE 'Y' TO WS-ERR-FLG` at line 248; cursor on USERIDL at line 250 ✓
- **3.5 USRSEC unavailable (OTHER)**: `MOVE 'Unable to verify the User ...' TO WS-MESSAGE` at line 254; cursor on USERIDL at line 255 ✓
- **3.6 Invalid key**: `MOVE 'Y' TO WS-ERR-FLG` at line 92; `MOVE CCDA-MSG-INVALID-KEY TO WS-MESSAGE` at line 93 ✓

#### S3 — Migration Notes
**Verified notes:**
- Note 1 (plaintext password comparison at line 223): `IF SEC-USR-PWD = WS-USER-PWD` at line 223 — plaintext byte-for-byte compare confirmed ✓
- Note 2 (WS-USER-PWD in working storage only): MOVE at line 136 confirmed; no COMMAREA field for password ✓
- Note 3 (no lockout mechanism): no counter, no lockout logic in source ✓
- Note 4 (USRSEC error silently swallowed): WS-REAS-CD captured at line 218 but never referenced again in PROCEDURE DIVISION ✓
- Note 5 (CDEMO-CUST-ID, CDEMO-ACCT-ID, CDEMO-CARD-NUM not populated): no MOVE to these fields anywhere in source ✓
- Note 6 (CDEMO-LAST-MAP and CDEMO-LAST-MAPSET not set): confirmed ✓
- Note 7 (version comment at line 259): `* Ver: CardDemo_v1.0-15-g27d6c6f-68 Date: 2022-07-19 23:12:33 CDT` at line 259 confirmed ✓

**Unsupported notes:** None.

**Missing latent issue:**
- `CCDA-MSG-THANK-YOU` (used at PF3, line 89) comes from `CSMSG01Y` and reads `'Thank you for using CardDemo application...      '` (50 chars). `CCDA-THANK-YOU` in `COTTL01Y` reads `'Thank you for using CCDA application... '` (40 chars) and is not used. The BIZ doc correctly routes through `CSMSG01Y`, but the Appendix B entry for `COTTL01Y` marks `CCDA-THANK-YOU` as "not used by COSGN00C" — confirmed, but the distinction between the two similarly-named fields across two copybooks is a potential developer confusion point not explicitly called out in the migration notes.

#### S4 — Copybook Fields
Verified against source copybooks:

**CSUSR01Y**: `SEC-USR-ID PIC X(08)`, `SEC-USR-FNAME PIC X(20)`, `SEC-USR-LNAME PIC X(20)`, `SEC-USR-PWD PIC X(08)`, `SEC-USR-TYPE PIC X(01)`, `SEC-USR-FILLER PIC X(23)` — doc Appendix B table matches exactly ✓. Total = 80 bytes ✓.
- `SEC-USR-FNAME` and `SEC-USR-LNAME` marked "never used or displayed" — confirmed; no PROCEDURE DIVISION reference ✓
- `SEC-USR-TYPE` used at line 227 to MOVE to `CDEMO-USER-TYPE` ✓; 88-level `CDEMO-USRTYP-ADMIN` drives the XCTL branch at line 230 ✓

**CSDAT01Y**: Fields verified. `WS-CURDATE-N REDEFINES WS-CURDATE PIC 9(08)` exists but is not used by COSGN00C (unlike CORPT00C which uses it for INTEGER-OF-DATE). Doc notes it as "Numeric overlay of date" — correct ✓. `WS-TIMESTAMP` marked "defined but not used by this program" — confirmed; no reference in COSGN00C PROCEDURE DIVISION ✓.

**CSMSG01Y**: `CCDA-MSG-THANK-YOU PIC X(50) VALUE 'Thank you for using CardDemo application...      '` and `CCDA-MSG-INVALID-KEY PIC X(50) VALUE 'Invalid key pressed. Please see below...         '` — both doc descriptions accurate ✓.

**COCOM01Y fields**: `CDEMO-USER-ID`, `CDEMO-FROM-TRANID`, `CDEMO-FROM-PROGRAM`, `CDEMO-USER-TYPE`, `CDEMO-PGM-CONTEXT` all confirmed as set before XCTL at lines 224–228 ✓. Unused fields (CDEMO-CUST-ID, CDEMO-ACCT-ID, CDEMO-CARD-NUM, CDEMO-LAST-MAP, CDEMO-LAST-MAPSET) confirmed unused ✓.

#### S5 — External Calls
- **COADM01C**: `EXEC CICS XCTL PROGRAM('COADM01C') COMMAREA(CARDDEMO-COMMAREA) END-EXEC` at lines 231–234. Doc says "Paragraph `READ-USER-SEC-FILE`, line 231 — CICS XCTL". Condition: `IF CDEMO-USRTYP-ADMIN` at line 230 ✓. Program name is a **hardcoded literal** `'COADM01C'` (not a variable) — doc Appendix C correctly lists this at line 231 as a hardcoded literal ✓.
- **COMEN01C**: `EXEC CICS XCTL PROGRAM('COMEN01C') COMMAREA(CARDDEMO-COMMAREA) END-EXEC` at lines 236–239. Condition: ELSE branch (not admin) ✓. Doc line reference 236 is accurate ✓.
- Both XCTL calls use hardcoded string literals, not variables. Doc correctly identifies this in Appendix C ✓.
- Neither XCTL has a NOHANDLE clause — CICS will raise an exception if the program is not found. Not noted in doc, but minor.

#### S6 — Completeness
- **Missing Appendix B entries for `DFHAID` and `DFHBMSCA`**: Both are COPYed at lines 57–58 of source (both active; only `DFHATTR` at line 59 is commented out). The BIZ doc footer mentions them but they have no Appendix B section. This is a structural deficiency that triggered the Phase 1 FAIL. Content-wise, neither copybook has fields used directly in PROCEDURE DIVISION (DFHBMSCA attribute constants are not referenced in COSGN00C's procedure code; DFHAID constants DFHENTER and DFHPF3 are used implicitly via EVALUATE EIBAID). A migration developer has no material gap from this omission, but the Phase 1 FAIL should be resolved by adding brief Appendix B sections for both.
- **`DFHATTR` commented out**: `*COPY DFHATTR.` at line 59 — correctly absent from doc. The Phase 1 validator incorrectly flagged it because it appears in the source file; but as a comment it is not active. ✓
- **`POPULATE-HEADER-INFO`** is accurately summarised in flow description ✓.
- **No `CDEMO-PGM-REENTER` check**: Unlike other CardDemo programs, COSGN00C does NOT check `CDEMO-PGM-REENTER`. On EIBCALEN>0 it goes directly to `EVALUATE EIBAID`. This is correct behaviour for a signon screen (every entry should be treated as interactive), but it is not explicitly called out as a design difference. Not a migration risk but worth a note.

### Overall Recommendation
The document is safe to use as a migration reference. All authentication paths, error messages, copybook field layouts, and XCTL targets are accurately documented. The two warnings are structural: (1) DFHAID and DFHBMSCA need Appendix B sections added, and (2) the absence of a `CDEMO-PGM-REENTER` check compared to other programs is not called out explicitly. The security-critical notes (plaintext password, no lockout) are correctly included. A Java developer can implement this program safely from the document.