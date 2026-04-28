# Validation Report: BIZ-COUSR00C.md

**Overall: PASS** — 5 passed · 0 failed · 3 warned · 2 skipped

Source file: `695` lines   |   Document: `356` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COUSR00C.cbl |
| `line_number_bounds` | ✓ **PASS** | 42 line reference(s) checked, all within bounds (max: 695) |
| `backtick_identifiers` | ⚠ **WARN** | 1 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 8 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 2/8 migration note(s) missing line number citations |
| `mermaid_diagram` | ⚠ **WARN** | Mermaid diagram missing classDef colour definitions |

## Issues Found

### backtick_identifiers (WARN)
1 backtick identifier(s) not found in source or copybooks

- `HIGH-VALUES`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | All paragraphs, key routing, and pagination logic match the source accurately. |
| S2 Error Handling | ⚠ WARN | Error messages are accurate but a subtle STARTBR NOTFND behavior is misrepresented. |
| S3 Migration Notes | ⚠ WARN | Documented issues are real; one latent pagination bug is not documented. |
| S4 Copybook Fields | ✓ PASS | All sampled CSUSR01Y fields and COMMAREA extension fields match source exactly. |
| S5 External Calls | ✓ PASS | No external CALL statements; SKIP. |
| S6 Completeness | ⚠ WARN | INITIALIZE-USER-DATA paragraph and SEND-ERASE-FLG state management are undocumented. |

### Findings

#### S1 — Program Flow
Source PROCEDURE DIVISION (lines 97–695):
- `MAIN-PARA` (line 98): initializes flags, checks EIBCALEN = 0 → RETURN-TO-PREV-SCREEN to COSGN00C (lines 110–112). Doc states this correctly.
- First-entry path (`NOT CDEMO-PGM-REENTER`): sets flag, MOVE LOW-VALUES to map, calls PROCESS-ENTER-KEY, then SEND-USRLST-SCREEN (lines 115–119). Doc describes: "STARTBR + page forward + send list" — accurate.
- Re-entry EVALUATE (lines 122–137): DFHENTER → PROCESS-ENTER-KEY, DFHPF3 → COADM01C, DFHPF7 → PROCESS-PF7-KEY, DFHPF8 → PROCESS-PF8-KEY, OTHER → error message. All match doc's navigation table.
- `PROCESS-ENTER-KEY` (lines 149–232): checks 10 SEL fields (SEL0001I–SEL0010I) in EVALUATE, moves selection and user ID to COMMAREA, then EVALUATE on `CDEMO-CU00-USR-SEL-FLG` — 'U'/'u' → XCTL COUSR02C, 'D'/'d' → XCTL COUSR03C, OTHER → error message "Invalid selection. Valid values are U and D". Then repositions by USRIDINI if not blank, then calls PROCESS-PAGE-FORWARD. Doc accurately describes all these steps.
- `PROCESS-PF7-KEY` (lines 237–255): checks CDEMO-CU00-PAGE-NUM > 1 before calling PROCESS-PAGE-BACKWARD; if page 1, shows "You are already at the top of the page..." message. Doc states "PF7 | STARTBR at CDEMO-CU00-USRID-FIRST; READPREV to scroll backward" — accurate.
- `PROCESS-PF8-KEY` (lines 260–277): checks NEXT-PAGE-YES before calling PROCESS-PAGE-FORWARD; if false, shows "You are already at the bottom of the page..." message. Doc describes PF8 correctly.
- `PROCESS-PAGE-FORWARD` (lines 282–331): STARTBR, optional READNEXT skip (when not ENTER/PF7/PF3), loop 1–10 READNEXT + POPULATE-USER-DATA, peek-ahead READNEXT to set NEXT-PAGE flag. Doc describes this correctly.
- `PROCESS-PAGE-BACKWARD` (lines 336–379): STARTBR, optional READPREV skip, loop 10 down to 1 READPREV + POPULATE-USER-DATA. Doc states "STARTBR at CDEMO-CU00-USRID-FIRST; READPREV to scroll backward" — accurate.

#### S2 — Error Handling
Verified DISPLAY strings and error conditions:
- STARTBR-USER-SEC-FILE NOTFND (lines 600–606): sets `USER-SEC-EOF`, message `'You are at the top of the page...'`, calls SEND-USRLST-SCREEN. WARN: the source continues past the CONTINUE at line 600 to set USER-SEC-EOF — this means NOTFND on STARTBR is treated as "already at top" and the screen is sent with that message, but the READNEXT loop still runs. The doc does not document this behavior.
- READNEXT ENDFILE (lines 634–640): `USER-SEC-EOF = 'Y'`, message `'You have reached the bottom of the page...'`. Not explicitly listed in the doc's error handling but implied by pagination.
- READPREV ENDFILE (lines 670–674): `USER-SEC-EOF = 'Y'`, message `'You have reached the top of the page...'`. Same implicit coverage.
- STARTBR OTHER (line 608): `DISPLAY 'RESP:'... 'REAS:'...` + error flag + `'Unable to lookup User...'` + send screen. Doc does not document this DISPLAY.
- Process-Enter-Key OTHER selection code (line 212): message `'Invalid selection. Valid values are U and D'`. Doc states this in the navigation table — accurate.

#### S3 — Migration Notes
**Verified notes:**
- `WS-RESP-CD` PIC S9(09) COMP (line 50) and `WS-REAS-CD` PIC S9(09) COMP (line 51) — COMP (binary) not COMP-3. Doc correctly refers to these as response code fields without claiming COMP-3.
- `CDEMO-CU00-INFO` inline extension (lines 67–75): all fields confirmed.
- `SEC-USR-ID` PIC X(08) used as STARTBR/READNEXT/READPREV RIDFLD — confirmed.
- `WS-USER-DATA` array (lines 56–65) OCCURS 10 TIMES with USER-SEL/USER-ID/USER-NAME/USER-TYPE — defined in source but the POPULATE-USER-DATA paragraph (line 384) does NOT use this array at all. It moves directly from SEC-USER-DATA to individual screen fields (USRID01I, FNAME01I, etc.). The WS-USER-DATA array appears unused. Not documented.

**Unsupported notes:** None fabricated.

**Missing bugs:**
- `WS-USER-DATA` (lines 56–65) is declared but never populated or read in any paragraph. Dead code — migration note needed.
- `PROCESS-PAGE-BACKWARD` (line 352) fills rows starting at index 10 going DOWN (`WS-IDX = 10`, loop until `WS-IDX <= 0`, compute `WS-IDX - 1` each iteration). READPREV reads records in reverse order (newest-first from start key). This fills rows 10..1 from start-key backwards, resulting in ascending order display. However, the first READ (before the loop) is only skipped when `EIBAID NOT = DFHENTER AND DFHPF8` — for PF7 from row 1 of page 2, this correctly skips past the current boundary record. This logic is subtle and not documented explicitly.

#### S4 — Copybook Fields
Verified against CSUSR01Y (from source COPY at line 81):
- `SEC-USR-ID` PIC X(08) — confirmed as RIDFLD in STARTBR at line 591.
- `SEC-USR-FNAME` — moved to FNAME01I–FNAME10I in POPULATE-USER-DATA (lines 390, etc.) — confirmed.
- `SEC-USR-LNAME` — moved to LNAME01I–LNAME10I — confirmed.
- `SEC-USR-TYPE` — moved to UTYPE01I–UTYPE10I — confirmed.
- CDEMO-CU00-INFO extension fields:
  - `CDEMO-CU00-USRID-FIRST` PIC X(08): SET when WS-IDX=1 at line 389 — confirmed.
  - `CDEMO-CU00-USRID-LAST` PIC X(08): SET when WS-IDX=10 at line 435 — confirmed.
  - `CDEMO-CU00-PAGE-NUM` PIC 9(08): incremented at line 309 — confirmed.
  - `CDEMO-CU00-NEXT-PAGE-FLG` PIC X(01) with 88s NEXT-PAGE-YES/NO — confirmed at lines 71–73.
  - `CDEMO-CU00-USR-SEL-FLG` PIC X(01) — confirmed at line 74.
  - `CDEMO-CU00-USR-SELECTED` PIC X(08) — confirmed at line 75.
All fields match the doc's COMMAREA Extension table exactly.

#### S5 — External Calls
No CALL statements in COUSR00C.cbl. All data access via EXEC CICS READ/STARTBR/READNEXT/READPREV. SKIP.

#### S6 — Completeness
- `INITIALIZE-USER-DATA` paragraph (line 446): clears all 10 rows of screen fields before repopulating. Called in PROCESS-PAGE-FORWARD line 295 and PROCESS-PAGE-BACKWARD line 348. Not documented in Section 2. Java developer must know that on each page load, all 10 slots are blanked before re-fill.
- `SEND-ERASE-FLG` (lines 46–48): controls whether `ERASE` is included in EXEC CICS SEND (lines 528–543). Set to `'N'` (SEND-ERASE-NO) in PF7/PF8 error paths (lines 253, 275) so the screen retains its content when showing boundary messages. This conditional erase is not documented — a Java developer needs to know to conditionally refresh vs. partial-update.
- `HIGH-VALUES` reference at line 263 (PROCESS-PF8-KEY): `MOVE HIGH-VALUES TO SEC-USR-ID` when USRID-LAST is blank. Phase 1 flagged HIGH-VALUES as not found in source — this is confirmed present at line 263. The Phase 1 check was wrong (HIGH-VALUES is a COBOL figurative constant, not a user-defined identifier). Doc correctly uses this as the "STARTBR at HIGH-VALUES" concept but Phase 1 false-flagged it.

### Overall Recommendation
BIZ-COUSR00C.md is materially accurate and safe for migration use. Two items should be added before handoff: (1) the `WS-USER-DATA` array is declared but completely unused — this should be noted as dead code so the Java developer does not implement it; (2) the conditional ERASE logic (`SEND-ERASE-FLG`) in SEND-USRLST-SCREEN should be documented as a behavioral detail affecting how the screen refreshes on pagination boundary messages. All core business logic, field mappings, and navigation paths are correctly described.