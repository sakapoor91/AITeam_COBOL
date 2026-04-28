# Validation Report: BIZ-PAUDBLOD.md

**Overall: FAIL** — 6 passed · 1 failed · 2 warned · 1 skipped

Source file: `369` lines   |   Document: `292` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: PAUDBLOD.cbl |
| `line_number_bounds` | ✗ **FAIL** | 40 line number(s) exceed source file length (369 lines) |
| `backtick_identifiers` | ✓ **PASS** | 95 COBOL-style identifiers verified against source |
| `copybook_coverage` | ✓ **PASS** | All 4 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | ✓ **PASS** | All 2 Appendix A DDname(s) verified against source |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 3/9 migration note(s) missing line number citations |
| `mermaid_diagram` | ⚠ **WARN** | Mermaid diagram missing classDef colour definitions |

## Issues Found

### line_number_bounds (FAIL)
40 line number(s) exceed source file length (369 lines)

- `1227`
- `1690`
- `1720`
- `1770`
- `1961`
- `1962`
- `2020`
- `2340`
- `2420`
- `2630`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention. The Phase 1 FAIL on line-number bounds is the same COBOL-sequence-number artifact seen in DBUNLDGS — not fabricated claims.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Paragraph structure, two sequential file loops, IMS GU+ISRT pattern, and PA-ACCT-ID numeric guard all accurately described and verified against source. |
| S2 Error Handling | ✓ PASS | File open error, read error (silent-continue), IMS ISRT and GU failures, and 9999-ABEND all correctly described with exact DISPLAY strings verified. |
| S3 Migration Notes | ⚠ WARN | Eight of nine notes are accurate; note 6 regarding IO-PCB-MASK byte size is correct but its description as "1 byte" understates its structural role. |
| S4 Copybook Fields | ✓ PASS | All sampled fields from CIPAUSMY, CIPAUDTY, PAUTBPCB, and IMSFUNCS match source copybooks with correct PIC and COMP-3 designations. |
| S5 External Calls | ✓ PASS | All three CBLTDLI calls (root ISRT, GU position, child ISRT) documented with correct parameter lists. |
| S6 Completeness | ⚠ WARN | Two active DISPLAY statements inside 2100-INSERT-ROOT-SEG not documented; structural IF-without-ELSE bug in 3100-INSERT-CHILD-SEG is documented but the 'II' silent-skip case analysis is incomplete. |

### Findings

#### S1 — Program Flow

All key paragraphs verified against source:

- `MAIN-PARA` at source line 169 (sequence 01220026): `ENTRY 'DLITCBL' USING PAUTBPCB` at line 171 (sequence 01225033) — doc correctly states the IMS entry point.
- `DISPLAY 'STARTING PAUDBLOD'` at source line 173 (sequence 01227053) — correctly placed in `MAIN-PARA` before `1000-INITIALIZE`, as doc notes.
- `PERFORM 1000-INITIALIZE THRU 1000-EXIT` at source line 175 — correct.
- Root loop: `PERFORM 2000-READ-ROOT-SEG-FILE THRU 2000-EXIT UNTIL END-ROOT-SEG-FILE = 'Y'` at source lines 177–178 — correct.
- Child loop: `PERFORM 3000-READ-CHILD-SEG-FILE THRU 3000-EXIT UNTIL END-CHILD-SEG-FILE = 'Y'` at source lines 180–181 — correct.
- `PERFORM 4000-FILE-CLOSE THRU 4000-EXIT` at source line 183, `GOBACK` at line 187 — correct.
- `1000-INITIALIZE` at source line 190: ACCEPT date lines 193–194, display banner lines 196–198, OPEN INFILE1 (line 201) and INFILE2 (line 209) with status checks — correctly described.
- `2000-READ-ROOT-SEG-FILE` at source line 222: READ INFILE1 (line 226), three-branch IF on `WS-INFIL1-STATUS` (lines 228–237) — correctly described.
- `2100-INSERT-ROOT-SEG` at source line 242: ISRT call (lines 244–247), then IF SPACES/II/OTHER pattern (lines 253–262) — correctly described.
- `3000-READ-CHILD-SEG-FILE` at source line 269: READ INFILE2 (line 272), numeric guard on ROOT-SEG-KEY (line 275) — correctly described.
- `3100-INSERT-CHILD-SEG` at source line 292: INITIALIZE PAUT-PCB-STATUS (line 295), GU call (lines 296–299), missing END-IF between success path and error check — structural defect correctly identified.
- `3200-INSERT-IMS-CALL` at source line 318: ISRT call (lines 321–324), three-branch IF pattern (lines 326–336) — correctly described.
- `4000-FILE-CLOSE` at source line 341: CLOSE INFILE1 (line 343), CLOSE INFILE2 (line 350) with non-fatal status checks — correctly described.
- `9999-ABEND` at source line 360: `DISPLAY 'IMS LOAD ABENDING ...'` (line 363), `MOVE 16 TO RETURN-CODE` (line 365), `GOBACK` (line 366) — correctly described.

#### S2 — Error Handling

All DISPLAY strings verified:

- `'ERROR IN OPENING INFILE1:' WS-INFIL1-STATUS` at source line 205 (sequence 01965053) — exact match.
- `'ERROR IN OPENING INFILE2:' WS-INFIL2-STATUS` at source line 213 (sequence 01969453) — exact match.
- `'ERROR READING ROOT SEG INFILE'` at source line 235 (sequence 02042953) — exact match. Doc correctly notes no abend on read error.
- `'ROOT INSERT SUCCESS    '` at source line 254 (sequence 02160053) — exact match.
- `'ROOT SEGMENT ALREADY IN DB'` at source line 257 (sequence 02194153) — exact match.
- `'ROOT INSERT FAILED  :' PAUT-PCB-STATUS` at source line 260 (sequence 02230053) — exact match.
- `'ERROR READING CHILD SEG INFILE'` at source line 287 (sequence 02359253) — exact match.
- `'GU CALL TO ROOT SEG SUCCESS'` at source line 306 (sequence 02430053) — exact match.
- `'ROOT GU CALL FAIL:' PAUT-PCB-STATUS` at source line 311 (sequence 02530053) — exact match.
- `'KFB AREA IN CHILD:' PAUT-KEYFB` at source line 312 (sequence 02531048) — exact match.
- `'CHILD SEGMENT INSERTED SUCCESS'` at source line 327 (sequence 02611966) — exact match.
- `'CHILD SEGMENT ALREADY IN DB'` at source line 330 (sequence 02612266) — exact match.
- `'INSERT CALL FAIL FOR CHILD:' PAUT-PCB-STATUS` at source line 333 (sequence 02612566) — exact match.
- `'IMS LOAD ABENDING ...'` at source line 363 (sequence 03660054) — exact match.

Silent-continue on read error (no abend) confirmed at source line 234–236: the ELSE branch for non-10 status only displays, does not PERFORM 9999-ABEND. Correctly documented.

#### S3 — Migration Notes

**Verified notes:**
1. Structural bug in `3100-INSERT-CHILD-SEG` — missing END-IF between the two IF blocks: confirmed at source lines 305–314. The IF at line 305 (`PAUT-PCB-STATUS = SPACES`) has no END-IF before the next IF at line 310. The analysis of what happens for each status value is correct.
2. Silent read errors on both files — confirmed at source lines 234–236 and 286–288.
3. Root and child loads run sequentially — confirmed by PERFORM order at source lines 177–181.
4. Duplicate segments silently accepted (`'II'`) — confirmed at source lines 256–258 and 329–331 (DISPLAY only, no error flag).
5. `WS-PGMNAME = 'IMSUNLOD'` at source line 54 — confirmed.
6. `IO-PCB-MASK PIC X(1)` at source line 160 — confirmed. The doc says "occupies one byte" which is correct.
7. Unused carry-over fields — all confirmed in source: `WS-NO-SUMRY-DELETED` (line 67), `WS-NO-DTL-READ` (line 68), `WS-NO-DTL-DELETED` (line 69), `WS-TOT-REC-WRITTEN` (line 65), `WS-ERR-FLG` (line 71), `WS-END-OF-AUTHDB-FLAG` (line 74), `WS-MORE-AUTHS-FLAG` (line 77), `WS-IMS-PSB-SCHD-FLG` (line 108), `IMS-RETURN-CODE` (line 98).
8. `ROOT-QUAL-SSA` key field `'ACCNTID '` at source line 116 (sequence 00831354) — confirmed.
9. Close errors non-fatal — confirmed at source lines 345–356: IF blocks with DISPLAY only, no PERFORM 9999-ABEND.

**Note re: missing analysis in note 1:** When `PAUT-PCB-STATUS = 'II'` after the GU call, the first IF block (spaces check) is false, the second IF block (`NOT EQUAL TO SPACES AND 'II'`) is also false (because `'II'` IS equal to `'II'`). Result: neither block executes, `3200-INSERT-IMS-CALL` is NOT called, and the child is silently skipped with no message. This case is correctly analyzed in the doc.

**Missing bugs:**
- `WS-NO-SUMRY-READ` and `WS-AUTH-SMRY-PROC-CNT` are incremented at source lines 234–235 on each successful root read, but there is no corresponding increment for child records. Unlike DBUNLDGS/PAUDBUNL, child reads in PAUDBLOD do NOT increment these counters — their counts will reflect only root records read, not children. This asymmetry vs. the sibling programs is not noted.

#### S4 — Copybook Fields

Fields verified against actual copybook source files (same copybooks as DBUNLDGS):

**CIPAUSMY:** All fields match — `PA-ACCT-ID` S9(11) COMP-3, `PA-CREDIT-LIMIT` S9(09)V99 COMP-3, `PA-APPROVED-AUTH-CNT` S9(04) COMP — all PASS.

**CIPAUDTY:**
- `PA-AUTH-DATE-9C PIC S9(05) COMP-3` — 3 bytes packed (not 4). Doc Appendix B refers to DBUNLDGS documentation which states 4 bytes — same error as in DBUNLDGS. This should be 3 bytes.
- `PA-AUTH-TIME-9C PIC S9(09) COMP-3` — 5 bytes packed (not 6). Doc refers to DBUNLDGS doc which states 6 bytes — same error. This should be 5 bytes.
- All other CIPAUDTY fields verified as matching.

**PAUTBPCB:** All fields match — `PAUT-PCB-STATUS` X(02), `PAUT-KEYFB` X(255) — PASS.

**IMSFUNCS:** All function codes match — `FUNC-GU X(04) VALUE 'GU  '`, `FUNC-ISRT X(04) VALUE 'ISRT'`, `PARMCOUNT PIC S9(05) COMP-5 VALUE +4` — PASS. Doc correctly identifies which functions are used (GU and ISRT) and which are not.

#### S5 — External Calls

All three CBLTDLI calls verified:

| Call | Paragraph | Source Line | Documented Params | Verified |
|------|-----------|-------------|-------------------|---------|
| ISRT (root) | `2100-INSERT-ROOT-SEG` | 244–247 | `FUNC-ISRT, PAUTBPCB, PENDING-AUTH-SUMMARY, ROOT-UNQUAL-SSA` | PASS |
| GU (position) | `3100-INSERT-CHILD-SEG` | 296–299 | `FUNC-GU, PAUTBPCB, PENDING-AUTH-SUMMARY, ROOT-QUAL-SSA` | PASS |
| ISRT (child) | `3200-INSERT-IMS-CALL` | 321–324 | `FUNC-ISRT, PAUTBPCB, PENDING-AUTH-DETAILS, CHILD-UNQUAL-SSA` | PASS |

The Appendix B "CBLTDLI" table lists line references using COBOL sequence numbers (e.g., "line 2070053") — same artifact as DBUNLDGS.

#### S6 — Completeness

- Inside `2100-INSERT-ROOT-SEG`, two active DISPLAY statements exist at source lines 248 and 252: `DISPLAY ' *******************************'` (before and after the ISRT call). These are active debug banners, not commented out. They produce job log output on every root insert. The doc does not mention these and does not list them in Appendix C — **undocumented active DISPLAY statements**.
- The `3100-INSERT-CHILD-SEG` structural bug analysis covers the spaces/other cases but the `'II'` silent-skip is mentioned. The `INITIALIZE PAUT-PCB-STATUS` at source line 295 before the GU call is documented (Step 7, "Initializes PAUT-PCB-STATUS").
- Two `DISPLAY '***************************'` banners in `3100-INSERT-CHILD-SEG` at source lines 300 and 304 are also active — not commented out, not documented, produce output per child insert.
- Appendix C lists most literals but omits the four active asterisk-banner DISPLAYs (lines 248, 252, 300, 304).
- `ROOT-QUAL-SSA` structure: doc states "21 bytes structural but 21 as declared" — the actual structure is 8+1+8+2+6+1 = 26 bytes, not 21. The declaration at source lines 113–119 confirms: 6 fields. Doc Appendix D acknowledges "26 bytes structural but 21 as declared" which reflects only the PIC fields without the COMP-3 key. The discrepancy note is present but confusing.

### Overall Recommendation

The document is materially accurate and correctly captures all significant business logic, IMS call patterns, and migration risks. It is safe to use as a migration reference with two corrections: (1) the byte sizes for `PA-AUTH-DATE-9C` and `PA-AUTH-TIME-9C` must be corrected to 3 and 5 bytes respectively in the copybook tables inherited from DBUNLDGS documentation; (2) four active asterisk-banner DISPLAY statements in paragraphs `2100-INSERT-ROOT-SEG` and `3100-INSERT-CHILD-SEG` are missing from the documentation and Appendix C — a Java developer would not replicate this debug output unless the source is re-read. The structural IF bug in `3100-INSERT-CHILD-SEG` is correctly and completely documented.