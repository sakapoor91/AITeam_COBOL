# Validation Report: BIZ-COUSR01C.md

**Overall: FAIL** — 4 passed · 1 failed · 3 warned · 2 skipped

Source file: `299` lines   |   Document: `333` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COUSR01C.cbl |
| `line_number_bounds` | ✓ **PASS** | 45 line reference(s) checked, all within bounds (max: 299) |
| `backtick_identifiers` | ⚠ **WARN** | 1 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✗ **FAIL** | 1 COPY statement(s) from source not documented in Appendix B |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 6/8 migration note(s) missing line number citations |
| `mermaid_diagram` | ⚠ **WARN** | Mermaid diagram missing classDef colour definitions |

## Issues Found

### backtick_identifiers (WARN)
1 backtick identifier(s) not found in source or copybooks

- `WS-USR-MODIFIED`

### copybook_coverage (FAIL)
1 COPY statement(s) from source not documented in Appendix B

- `DFHATTR`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings; Phase 1 copybook gap (DFHATTR) noted.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Startup sequence, validation order, and write path all match source accurately. |
| S2 Error Handling | ✓ PASS | All error messages match source exactly including the WRITE response handling. |
| S3 Migration Notes | ⚠ WARN | No major fabricated notes; DFHATTR commented-out copybook is a migration detail not captured. |
| S4 Copybook Fields | ✓ PASS | All documented fields verified against source. |
| S5 External Calls | ✓ PASS | No external CALLs; SKIP. |
| S6 Completeness | ⚠ WARN | DFHATTR comment and the OTHER WRITE error DISPLAY suppression are not documented. |

### Findings

#### S1 — Program Flow
Source PROCEDURE DIVISION (lines 70–299):
- `MAIN-PARA` (line 71): SET ERR-FLG-OFF, clear messages, check EIBCALEN = 0 → RETURN-TO-PREV-SCREEN (COSGN00C). Doc correctly states this.
- First-entry path (`NOT CDEMO-PGM-REENTER`): SET flag, MOVE LOW-VALUES to COUSR1AO, MOVE -1 TO FNAMEL, PERFORM SEND-USRADD-SCREEN (lines 83–87). Doc describes this as "display blank add-user form" — accurate.
- Re-entry EVALUATE (lines 90–103): DFHENTER → PROCESS-ENTER-KEY, DFHPF3 → COADM01C XCTL, DFHPF4 → CLEAR-CURRENT-SCREEN, OTHER → error message + SEND. All match the doc's navigation table.
- `PROCESS-ENTER-KEY` (lines 115–160): sequential EVALUATE checking FNAMEI → LNAMEI → USERIDI → PASSWDI → USRTYPEI for blank values. Each triggers error message + SEND-USRADD-SCREEN and returns (implicit by PERFORM of SEND). If all pass, moves to SEC-USER-DATA fields and calls WRITE-USER-SEC-FILE. Doc's validation table matches this order exactly.
- `WRITE-USER-SEC-FILE` (lines 238–274): EXEC CICS WRITE, EVALUATE on WS-RESP-CD: NORMAL → INITIALIZE-ALL-FIELDS + green STRING message, DUPKEY/DUPREC → error, OTHER → error. Doc covers all three paths.

#### S2 — Error Handling
Verified all DISPLAY strings against source:
- Blank FNAMEI (line 121): `'First Name can NOT be empty...'` — doc states same. Accurate.
- Blank LNAMEI (line 127): `'Last Name can NOT be empty...'` — doc states same. Accurate.
- Blank USERIDI (line 132): `'User ID can NOT be empty...'` — doc states same. Accurate.
- Blank PASSWDI (line 138): `'Password can NOT be empty...'` — doc states same. Accurate.
- Blank USRTYPEI (line 143): `'User Type can NOT be empty...'` — doc states same. Accurate.
- WRITE NORMAL (lines 255–258): STRING `'User '` + `SEC-USR-ID` DELIMITED BY SPACE + `' has been added ...'`. Doc states `"User xxx has been added..."` — accurate.
- WRITE DUPKEY/DUPREC (line 263): `'User ID already exist...'`. Doc states same. Accurate.
- WRITE OTHER (lines 269–272): `MOVE 'Y' TO WS-ERR-FLG`, `'Unable to Add User...'`, cursor to FNAMEL. NOTE: the DISPLAY statement for RESP/REAS is commented out at line 268 (`*DISPLAY 'RESP:' WS-RESP-CD 'REAS:' WS-REAS-CD`). Doc does not mention this suppression. Minor omission but not inaccurate.

#### S3 — Migration Notes
**Verified notes:**
- `WS-USRSEC-FILE PIC X(08) VALUE 'USRSEC  '` (line 39) — confirmed; note trailing spaces in literal.
- CICS WRITE with RIDFLD (SEC-USR-ID) and KEYLENGTH — confirmed at lines 241–248.
- `SEC-USR-PWD` stored as plain text (line 157) — confirmed; no encryption or hashing.
- `DFHATTR` is commented out at line 57 (`*COPY DFHATTR`). Phase 1 flagged this as a missing copybook. The doc does not mention DFHATTR at all. This is a benign omission since it is commented out, but the migration note should confirm DFHATTR was not needed.

**Unsupported notes:** None fabricated.

**Missing bugs:**
- No latent bugs identified. The program is straightforward with no complex branching.
- The commented-out `*COPY DFHATTR` at line 57 suggests it was previously included and removed. If DFHATTR constants were ever referenced, removing the COPY would cause a compile error. Since the source compiles cleanly (implied by it being in the project), DFHATTR constants are not used. No issue.

#### S4 — Copybook Fields
Verified fields written to `SEC-USER-DATA`:
- `SEC-USR-ID` ← `USERIDI OF COUSR1AI` (line 154). Doc states `SEC-USR-ID ← USERIDI`. Accurate.
- `SEC-USR-FNAME` ← `FNAMEI OF COUSR1AI` (line 155). Doc states `SEC-USR-FNAME ← FNAMEI`. Accurate.
- `SEC-USR-LNAME` ← `LNAMEI OF COUSR1AI` (line 156). Doc states `SEC-USR-LNAME ← LNAMEI`. Accurate.
- `SEC-USR-PWD` ← `PASSWDI OF COUSR1AI` (line 157). Doc states `SEC-USR-PWD ← PASSWDI`. Accurate.
- `SEC-USR-TYPE` ← `USRTYPEI OF COUSR1AI` (line 158). Doc states `SEC-USR-TYPE ← USRTYPEI`. Accurate.
- `INITIALIZE-ALL-FIELDS` (lines 287–295) clears: USERIDI, FNAMEI, LNAMEI, PASSWDI, USRTYPEI, WS-MESSAGE + sets FNAMEL cursor to -1. Doc states "Clear all fields" for success path — accurate.

#### S5 — External Calls
No CALL statements in COUSR01C.cbl. Single EXEC CICS WRITE operation. SKIP.

#### S6 — Completeness
- Commented-out `*COPY DFHATTR` at line 57: the doc's Copybooks table does not list DFHATTR. Consistent with the commented-out state, but should note "DFHATTR was provisioned but not used" for completeness.
- `RETURN-TO-PREV-SCREEN` (lines 165–178): commented-out MOVEs at lines 172–173 (`*MOVE WS-USER-ID TO CDEMO-USER-ID`, `*MOVE SEC-USR-TYPE TO CDEMO-USER-TYPE`) suggest the developer once intended to propagate user ID/type to the COMMAREA on exit. This dead-comment could indicate a missing capability. Not documented.
- The `SEND-USRADD-SCREEN` paragraph always uses ERASE (line 192) — unconditional, unlike COUSR00C. Not significant for migration but worth noting that no partial-update path exists here.

### Overall Recommendation
BIZ-COUSR01C.md is accurate and complete enough for safe migration use. The document correctly captures all validation rules, field mappings, error messages, and navigation paths. Two minor additions would improve it: (1) note that the WRITE OTHER path suppresses the RESP/REAS DISPLAY (commented out at line 268), meaning unexpected write errors are invisible to operators in the COBOL version — the Java replacement should implement proper logging; (2) note the plain-text password storage (confirmed at line 157 — SEC-USR-PWD is stored as-is) as a security migration concern that the Java implementation must address.