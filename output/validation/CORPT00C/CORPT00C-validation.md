# Validation Report: BIZ-CORPT00C.md

**Overall: PASS** — 6 passed · 0 failed · 2 warned · 2 skipped

Source file: `649` lines   |   Document: `373` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: CORPT00C.cbl |
| `line_number_bounds` | ✓ **PASS** | 52 line reference(s) checked, all within bounds (max: 649) |
| `backtick_identifiers` | ⚠ **WARN** | 7 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 8 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 2/10 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
7 backtick identifier(s) not found in source or copybooks

- `WRITE-JOBSUB-TDQ`
- `YYYY-01-01`
- `YYYY-12-31`
- `YYYY-MM-01`
- `YYYY-MM-DD`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Startup, screen receive, EVALUATE EIBAID, all three report paths, TDQ write loop, and GO TO RETURN-TO-CICS structure are accurately documented. |
| S2 Error Handling | ✓ PASS | All six error conditions with accurate message strings verified against source. |
| S3 Migration Notes | ⚠ WARN | All 10 notes are supported; one latent date-validation bug not mentioned in the doc. |
| S4 Copybook Fields | ✓ PASS | CVTRA05Y and CSDAT01Y fields verified; all unused fields correctly annotated. |
| S5 External Calls | ✓ PASS | CSUTLDTC CALL documented correctly with accurate input/output fields and the 2513 ignore behavior. |
| S6 Completeness | ⚠ WARN | `INITIALIZE-ALL-FIELDS` paragraph and `GO TO RETURN-TO-CICS` within `SEND-TRNRPT-SCREEN` have business implications not fully explained. |

### Findings

#### S1 — Program Flow
MAIN-PARA (lines 163–202) structure verified:
- Flags initialized: `SET ERR-FLG-OFF`, `SET TRANSACT-NOT-EOF`, `SET SEND-ERASE-YES` at lines 165–167 ✓
- EIBCALEN=0 path: `MOVE 'COSGN00C' TO CDEMO-TO-PROGRAM; PERFORM RETURN-TO-PREV-SCREEN` at lines 173–174 ✓
- First-entry check `IF NOT CDEMO-PGM-REENTER` at line 177, sets flag, clears map to LOW-VALUES, `MOVE -1 TO MONTHLYL`, `PERFORM SEND-TRNRPT-SCREEN` ✓ — doc correctly notes cursor is positioned at `MONTHLYL` ✓
- EVALUATE EIBAID: DFHENTER → PROCESS-ENTER-KEY, DFHPF3 → RETURN-TO-PREV-SCREEN with COMEN01C, OTHER → error with CCDA-MSG-INVALID-KEY — all at lines 184–195 ✓
- `DISPLAY 'PROCESS ENTER KEY'` at line 210 — doc correctly flags as debugging artifact ✓
- Monthly path (lines 213–238): obtains CURRENT-DATE, builds YYYY-MM-01 start date, increments month (with year rollover), uses DATE-OF-INTEGER/INTEGER-OF-DATE arithmetic for month-end — all confirmed ✓
- Yearly path (lines 239–255): `WS-START-DATE-MM='01'`, `WS-START-DATE-DD='01'`, `WS-END-DATE-MM='12'`, `WS-END-DATE-DD='31'` ✓
- Custom path (lines 256–436): empty field checks with EVALUATE TRUE (SDTMMI, SDTDDI, SDTYYYYI, EDTMMI, EDTDDI, EDTYYYYI), NUMVAL-C conversions, numeric/range checks, CSUTLDTC calls, PARM date assignments ✓
- `SUBMIT-JOB-TO-INTRDR` (lines 462–510): CONFIRMI check, Y/y → loop, N/n → INITIALIZE-ALL-FIELDS + re-display, OTHER → string error — all confirmed ✓
- TDQ write loop: `PERFORM VARYING WS-IDX FROM 1 BY 1 UNTIL WS-IDX > 1000 OR END-LOOP-YES OR ERR-FLG-ON` at line 498. Loop terminates on `JCL-RECORD = '/*EOF' OR SPACES OR LOW-VALUES` at lines 502–504. `PERFORM WIRTE-JOBSUB-TDQ` ✓
- `SEND-TRNRPT-SCREEN` ends with `GO TO RETURN-TO-CICS` (line 580) — the doc describes this correctly at Section 2.3 ✓

Minor inaccuracy: the doc (Section 2.2, paragraph "Step 5") states "Any other AID — set error flag, cursor on `MONTHLYL`". Confirmed at lines 191–194 (`MOVE -1 TO MONTHLYL OF CORPT0AI`) ✓.

#### S2 — Error Handling
- **3.1 Empty date**: messages `'Start Date - Month can NOT be empty...'` etc. confirmed at lines 261, 268, 275, 282, 289, 296 ✓
- **3.2 Non-numeric date**: messages `'Start Date - Not a valid Month...'` etc. confirmed at lines 331, 340, 348, 356, 364, 373 ✓
- **3.3 CSUTLDTC failure**: `IF CSUTLDTC-RESULT-SEV-CD = '0000' CONTINUE ELSE IF CSUTLDTC-RESULT-MSG-NUM NOT = '2513'` — confirmed at lines 396–399, 416–419. Messages `'Start Date - Not a valid date...'` and `'End Date - Not a valid date...'` ✓
- **3.4 Confirmation missing**: `STRING 'Please confirm to print the ' WS-REPORT-NAME ' report...'` at lines 466–470 ✓
- **3.5 Confirmation invalid**: `STRING '"' CONFIRMI '" is not a valid value to confirm...'` at lines 485–489 ✓
- **3.6 TDQ write failure**: `DISPLAY 'RESP:' WS-RESP-CD 'REAS:' WS-REAS-CD` at line 529, `MOVE 'Unable to Write TDQ (JOBS)...' TO WS-MESSAGE` at line 531 ✓

#### S3 — Migration Notes
**Verified notes:**
- Note 1 (`WIRTE-JOBSUB-TDQ` typo): paragraph name at line 515 confirmed ✓
- Note 2 (hardcoded JCL in JOB-DATA-1): lines 82–125 contain 16 literal 80-byte JCL records ✓
- Note 3 (`DISPLAY 'PROCESS ENTER KEY'` debug): line 210 ✓
- Note 4 (month arithmetic via INTEGER-OF-DATE): lines 223–230 confirmed ✓
- Note 5 (message 2513 silently ignored): lines 399, 419 ✓
- Note 6 (CSUTLDTC result fields as character comparisons): `CSUTLDTC-RESULT-SEV-CD PIC X(04)` at line 133 ✓
- Note 7 (JOB-DATA-2 OCCURS 1000): line 127 ✓
- Note 8 (`WS-TRANSACT-FILE` and `WS-TRANSACT-EOF` never used): lines 40–46; neither referenced in PROCEDURE DIVISION ✓
- Note 9 (`WS-TRAN-AMT` and `WS-TRAN-DATE` unused): lines 77–78; confirmed unused ✓
- Note 10 (`CVTRA05Y` entirely unused): COPY CVTRA05Y at line 146; no TRAN-* fields referenced in PROCEDURE DIVISION ✓

**Unsupported notes:** None.

**Missing bugs:**
- The custom date path does NOT guard `SUBMIT-JOB-TO-INTRDR` with `IF NOT ERR-FLG-ON` after the CSUTLDTC calls for the second date. The code at lines 429–436 checks `IF NOT ERR-FLG-ON` before calling SUBMIT, but individual date-validation `PERFORM SEND-TRNRPT-SCREEN` calls within the custom EVALUATE block do not set a flag that would prevent reaching line 429 after the screen send. However, `PERFORM SEND-TRNRPT-SCREEN` ends with `GO TO RETURN-TO-CICS`, which issues `EXEC CICS RETURN` — so execution never continues past SEND-TRNRPT-SCREEN. This is a control-flow nuance that should be documented for migration, as a Java implementation cannot use `GO TO` to emulate this.

#### S4 — Copybook Fields
**CVTRA05Y** fields verified in doc Appendix B — all 14 fields listed as "never referenced" are confirmed unused in CORPT00C PROCEDURE DIVISION ✓.

**CSDAT01Y** fields verified against csdat01y.cpy:
- `WS-CURDATE-YEAR PIC 9(04)`, `WS-CURDATE-MONTH PIC 9(02)`, `WS-CURDATE-DAY PIC 9(02)` ✓
- `WS-CURDATE-N REDEFINES WS-CURDATE PIC 9(08)` — used in monthly arithmetic at line 229 ✓
- Doc specifically notes `WS-CURDATE-N` usage for INTEGER-OF-DATE calculation ✓

**CORPT00 BMS fields** (not verifiable from copybook file but consistent with source references):
- `MONTHLYI`, `YEARLYI`, `CUSTOMI` — doc correctly describes all three as single-char non-blank triggers ✓
- `CONFIRMI` PIC X(1) — confirmed at line 464 ✓

#### S5 — External Calls
- **CSUTLDTC**: `CALL 'CSUTLDTC' USING CSUTLDTC-DATE CSUTLDTC-DATE-FORMAT CSUTLDTC-RESULT` at lines 392–394 (start date) and 412–414 (end date) ✓
- Input: `CSUTLDTC-DATE PIC X(10)`, `CSUTLDTC-DATE-FORMAT PIC X(10) = 'YYYY-MM-DD'` ✓
- Output: `CSUTLDTC-RESULT-SEV-CD PIC X(04)`, `CSUTLDTC-RESULT-MSG-NUM PIC X(04)` ✓
- `CSUTLDTC-RESULT-MSG PIC X(61)` never read — doc correctly notes this ✓
- Note that this is a native COBOL CALL (not CICS LINK) — confirmed by `CALL 'CSUTLDTC'` syntax ✓

#### S6 — Completeness
- **`GO TO RETURN-TO-CICS` in `SEND-TRNRPT-SCREEN`** (line 580): The doc mentions this at Section 2.3 ("which ends with `GO TO RETURN-TO-CICS`") ✓. However, the migration implication — that every code path that calls `PERFORM SEND-TRNRPT-SCREEN` will immediately issue a CICS RETURN and not continue to the next statement — is critically important for Java migration and is not explicitly called out as a migration note. The Java equivalent of each `PERFORM SEND-TRNRPT-SCREEN` must also include an immediate `return` from the service method.
- **`INITIALIZE-ALL-FIELDS` (line 633–646)**: The paragraph clears all input map fields. The doc mentions it is called on success and on N-confirmation, but does not describe its content. This is a minor omission.
- The JCL PARM slot structure (`PARM-START-DATE-1`, `PARM-END-DATE-1` embedded within `JOB-DATA-1` FILLER lines) is correctly documented in Appendix D ✓.

### Overall Recommendation
The document is safe to use as a migration reference for all three report paths, the TDQ write mechanism, and the date validation flow. The two warnings are important for Java migration: (1) the `GO TO RETURN-TO-CICS` pattern means every `PERFORM SEND-TRNRPT-SCREEN` is an early exit — the Java implementation must replicate this with explicit returns; and (2) the hardcoded JCL is environment-specific and will need replacement. These are noted in the doc's migration notes, though the full implication of the GO TO pattern deserves a dedicated migration note.