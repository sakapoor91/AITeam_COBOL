# Validation Report: BIZ-DBUNLDGS.md

**Overall: FAIL** — 4 passed · 1 failed · 4 warned · 1 skipped

Source file: `366` lines   |   Document: `370` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: DBUNLDGS.cbl |
| `line_number_bounds` | ✗ **FAIL** | 61 line number(s) exceed source file length (366 lines) |
| `backtick_identifiers` | ⚠ **WARN** | 2 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 6 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | ⚠ **WARN** | No DDnames found in Appendix A table |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 7/10 migration note(s) missing line number citations |
| `mermaid_diagram` | ⚠ **WARN** | Mermaid diagram missing classDef colour definitions |

## Issues Found

### line_number_bounds (FAIL)
61 line number(s) exceed source file length (366 lines)

- `380`
- `1160`
- `1220`
- `1230`
- `1260`
- `1270`
- `1340`
- `1342`
- `1470`
- `1540`

### backtick_identifiers (WARN)
2 backtick identifier(s) not found in source or copybooks

- `PADFL-`
- `RETURN-CODE`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention. The Phase 1 FAIL on line-number bounds is a systematic artifact of the document using COBOL sequence numbers (columns 73–80) rather than physical file line numbers — not a fabricated claim.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | MAIN-PARA, ENTRY DLITCBL, three-phase PERFORM structure, GN/GNP/ISRT logic, and PA-ACCT-ID numeric guard all accurately described. |
| S2 Error Handling | ✓ PASS | All three error paths (root GN failure, child GNP failure, GSAM insert failure) and 9999-ABEND paragraph accurately documented with correct DISPLAY strings verified. |
| S3 Migration Notes | ⚠ WARN | All ten notes are substantively accurate; notes 3 and 5 contain one minor inaccuracy each regarding counter increment context. |
| S4 Copybook Fields | ✓ PASS | All sampled fields from CIPAUSMY, CIPAUDTY, PAUTBPCB, PASFLPCB, and PADFLPCB match source with correct PIC clauses and COMP-3 flags. |
| S5 External Calls | ⚠ WARN | CBLTDLI calls documented correctly; Appendix B cites fabricated sequence numbers (e.g., "line 2710200") that exceed the 366-line file — these are COBOL card sequence numbers, not file line numbers. |
| S6 Completeness | ⚠ WARN | The ENTRY 'DLITCBL' statement and the WS-END-OF-CHILD-SEG initialization inside the GN success block are described; the PAUT-PCB-STATUS reset after each GNP iteration is documented. Minor: OPFIL2-REC declared in Working-Storage (not File Section) not highlighted as unusual. |

### Findings

#### S1 — Program Flow

The overall structure is accurately described. Source-verified key points:

- `MAIN-PARA` at source line 164 (sequence 01410000): `ENTRY 'DLITCBL' USING PAUTBPCB PASFLPCB PADFLPCB` at lines 165–167. Doc correctly states the IMS entry point.
- `PERFORM 1000-INITIALIZE THRU 1000-EXIT` at source line 170 — correct.
- Main loop: `PERFORM 2000-FIND-NEXT-AUTH-SUMMARY THRU 2000-EXIT UNTIL WS-END-OF-ROOT-SEG = 'Y'` at source lines 172–173 — correct.
- `PERFORM 4000-FILE-CLOSE THRU 4000-EXIT` at source line 175, then `GOBACK` at line 179 — correct. The doc places GOBACK at "line 1540" (COBOL sequence number), which is the actual sequence number in the source card columns.
- `1000-INITIALIZE` at source line 182: ACCEPT CURRENT-DATE (line 185), ACCEPT CURRENT-YYDDD (line 186), commented-out ACCEPT PRM-INFO (line 188), DISPLAYs (lines 189–192) — all correctly described.
- The three-outcome logic in `2000-FIND-NEXT-AUTH-SUMMARY` (spaces / `'GB'` / other) is accurately described and maps to source lines 232–256.
- Child loop `3000-FIND-NEXT-AUTH-DTL` with GNP / three-outcome logic maps accurately to source lines 267–295.
- `WS-END-OF-CHILD-SEG` is initialized before the child loop via `INITIALIZE WS-END-OF-CHILD-SEG` at source line 244 (sequence 02180000) — doc correctly notes this.
- The `PA-ACCT-ID IS NUMERIC` guard at source line 241 (sequence 02160000) — correctly documented.
- `PAUT-PCB-STATUS` reset after each GNP at source line 295 (sequence 02680000) — correctly documented.

**Line-number reference issue:** The document cites many line references as 4–7 digit numbers (e.g., "line 1570", "line 2710200", "line 2722101") that correspond to COBOL card sequence numbers, not physical file line numbers. The physical source is 366 lines. This caused the Phase 1 FAIL. The descriptions themselves are accurate; only the line-number citation format is misleading.

#### S2 — Error Handling

All error paths verified:

- Root GN failure: `DISPLAY 'AUTH SUM  GN FAILED  :' PAUT-PCB-STATUS` at source line 254 (sequence 02280000) — doc text matches exactly.
- `DISPLAY 'KEY FEEDBACK AREA    :' PAUT-KEYFB` at source line 255 (sequence 02290000) — exact match.
- Child GNP failure: `DISPLAY 'GNP CALL FAILED  :' PAUT-PCB-STATUS` at source line 291 (sequence 02640000) — exact match.
- `DISPLAY 'KFB AREA IN CHILD:' PAUT-KEYFB` at source line 292 (sequence 02650000) — exact match.
- GSAM root insert failure: `DISPLAY 'GSAM PARENT FAIL :' PASFL-PCB-STATUS` at source line 312 (sequence 02711501) — exact match.
- `DISPLAY 'KFB AREA IN GSAM:' PASFL-KEYFB` at source line 313 — exact match.
- GSAM child insert failure: `DISPLAY 'GSAM PARENT FAIL :' PADFL-PCB-STATUS` at source line 331 (sequence 02722201) — doc correctly notes this says "PARENT" for a child insert (copy-paste error in source).
- `9999-ABEND`: `DISPLAY 'DBUNLDGS ABENDING ...'` at source line 360 (sequence 02950000), `MOVE 16 TO RETURN-CODE` at line 362, `GOBACK` at line 363 — all correctly described.
- Note on missing `CEE3ABD` call — correct, program uses GOBACK with RETURN-CODE=16 only.

GE branch of child loop: `DISPLAY 'CHILD SEG FLAG GE : ' WS-END-OF-CHILD-SEG` at source lines 287–288 (sequences 02600000–02610000) — correctly documented.

#### S3 — Migration Notes

**Verified notes:**
1. All COBOL file I/O commented out — confirmed: SELECT/FD/OPEN/CLOSE all commented out (source lines 26–50, 195–209, 340–355).
2. `WS-PGMNAME = 'IMSUNLOD'` at source line 58 (sequence 00380000) — confirmed.
3. `PA-ACCT-ID IS NUMERIC` guard at source line 241 — confirmed. **Minor inaccuracy**: doc says counters `WS-NO-SUMRY-READ` and `WS-AUTH-SMRY-PROC-CNT` are incremented "before the guard" which is true (lines 234–235); however, the doc implies skipped records inflate these counters, which is correct behavior but the phrasing could mislead — the counters count GN retrievals regardless of numeric test.
4. `PAUT-PCB-STATUS` reset after every GNP at source line 295 — confirmed.
5. `3200-INSERT-CHILD-SEG-GSAM` message says "GSAM PARENT FAIL" — confirmed at source line 331.
6. `WS-NO-SUMRY-DELETED`, `WS-NO-DTL-READ`, `WS-NO-DTL-DELETED`, `WS-TOT-REC-WRITTEN` — all declared in source (lines 71–73, 69) with VALUE 0 and never subsequently SET — confirmed.
7. Unused carry-over fields from IMSUNLOD template — confirmed for `WS-EXPIRY-DAYS` (line 62), `WS-DAY-DIFF` (line 63), `IDX` (line 64), `WS-CURR-APP-ID` (line 65), `WS-AUTH-DATE` (line 61), `WS-NO-CHKP` (line 67), `WS-IMS-PSB-SCHD-FLG` (line 110), `IMS-RETURN-CODE` (line 100).
8. `PRM-INFO` never populated — confirmed at source line 188 (`*    ACCEPT PRM-INFO FROM SYSIN`).
9. `PASFLPCB` and `PADFLPCB` are GSAM PCBs — confirmed by their COPY statements and usage.
10. `ROOT-UNQUAL-SSA` is unqualified 9-byte SSA — confirmed at source lines 115–117 (`'PAUTSUM0'` + space).

**Missing bugs:**
- The doc does not note that both `WS-NO-SUMRY-READ` and `WS-AUTH-SMRY-PROC-CNT` are incremented on BOTH root and child successful reads (source lines 234–235 for roots, lines 278–279 for children). These counters are thus not a pure root count — they count total segments processed. This is documented in Appendix D for the child counter issue in the sister program PAUDBUNL but not called out in DBUNLDGS migration notes.

#### S4 — Copybook Fields

Fields verified against actual copybook source files:

**CIPAUSMY (source file CIPAUSMY.cpy):**
| Field | Doc PIC | CPY PIC | Match |
|-------|---------|---------|-------|
| `PA-ACCT-ID` | S9(11) COMP-3, 6 bytes | `PIC S9(11) COMP-3` | PASS |
| `PA-CUST-ID` | 9(09), 9 bytes | `PIC 9(09)` | PASS |
| `PA-AUTH-STATUS` | X(01), 1 byte | `PIC X(01)` | PASS |
| `PA-ACCOUNT-STATUS` | X(02) OCCURS 5, 10 bytes | `PIC X(02) OCCURS 5 TIMES` | PASS |
| `PA-CREDIT-LIMIT` | S9(09)V99 COMP-3, 6 bytes | `PIC S9(09)V99 COMP-3` | PASS |
| `PA-APPROVED-AUTH-CNT` | S9(04) COMP, 2 bytes | `PIC S9(04) COMP` | PASS |
| `PA-APPROVED-AUTH-AMT` | S9(09)V99 COMP-3, 6 bytes | `PIC S9(09)V99 COMP-3` | PASS |
| `FILLER` | X(34), 34 bytes | `PIC X(34)` | PASS |

**CIPAUDTY (source file CIPAUDTY.cpy):**
| Field | Doc PIC | CPY PIC | Match |
|-------|---------|---------|-------|
| `PA-AUTH-DATE-9C` | S9(05) COMP-3, 4 bytes | `PIC S9(05) COMP-3` — 3 bytes packed (5 digits) | WARN — doc says 4 bytes; correct packed size for S9(05) COMP-3 is 3 bytes |
| `PA-AUTH-TIME-9C` | S9(09) COMP-3, 6 bytes | `PIC S9(09) COMP-3` — 5 bytes packed (9 digits) | WARN — doc says 6 bytes; correct packed size for S9(09) COMP-3 is 5 bytes |
| `PA-TRANSACTION-AMT` | S9(10)V99 COMP-3, 7 bytes | `PIC S9(10)V99 COMP-3` — 7 bytes packed (12 digits) | PASS |
| `PA-APPROVED-AMT` | S9(10)V99 COMP-3, 7 bytes | `PIC S9(10)V99 COMP-3` | PASS |
| `PA-MATCH-STATUS` | X(01), 88-levels P/D/E/M | All 4 88-level values confirmed in CPY | PASS |
| `PA-AUTH-FRAUD` | X(01), 88-levels F/R | Both 88-level values confirmed in CPY | PASS |

**PAUTBPCB (source file PAUTBPCB.CPY):**
All fields match: `PAUT-DBDNAME` X(08), `PAUT-SEG-LEVEL` X(02), `PAUT-PCB-STATUS` X(02), `PAUT-PCB-PROCOPT` X(04), FILLER S9(05) COMP, `PAUT-SEG-NAME` X(08), `PAUT-KEYFB-NAME` S9(05) COMP, `PAUT-NUM-SENSEGS` S9(05) COMP, `PAUT-KEYFB` X(255) — all PASS.

**PASFLPCB (source file PASFLPCB.CPY):**
All fields match including `PASFL-KEYFB PIC X(100)` — PASS.

**PADFLPCB (source file PADFLPCB.CPY):**
Doc states "`PADFL-KEYFB` is 255 bytes" — verified: `PIC X(255)` in source — PASS.

#### S5 — External Calls

All CBLTDLI calls documented correctly:
- GN call: `FUNC-GN, PAUTBPCB, PENDING-AUTH-SUMMARY, ROOT-UNQUAL-SSA` — verified at source line 222 (sequence 01970000).
- GNP call: `FUNC-GNP, PAUTBPCB, PENDING-AUTH-DETAILS, CHILD-UNQUAL-SSA` — verified at source line 267 (sequence 02410000).
- ISRT (root): `FUNC-ISRT, PASFLPCB, PENDING-AUTH-SUMMARY` — verified at source lines 302–304 (sequence 02710400).
- ISRT (child): `FUNC-ISRT, PADFLPCB, PENDING-AUTH-DETAILS` — verified at source lines 321–323 (sequence 02721200).

The Appendix B cites these with sequence numbers as "line numbers" (e.g., "line 1970", "line 2410", "line 2710400", "line 2721200"). The sequence numbers used consistently in the doc reflect actual COBOL source card sequence values visible in columns 73–80, not physical line numbers. This is internally consistent within the document but confused the Phase 1 checker.

#### S6 — Completeness

- `9999-EXIT` paragraph (source line 365) is not documented but contains only `EXIT` — acceptable omission.
- `1000-EXIT` paragraph containing only `EXIT` — acceptable omission.
- The fact that `OPFIL1-REC` and `OPFIL2-REC` are declared in Working-Storage (source lines 53–56, sequence 00361000–00364000), not in a commented-out File Section, is noted in the doc and correctly explained.
- The `WS-MORE-AUTHS-FLAG` / `MORE-AUTHS` 88-level set inside the child success path (source line 277, sequence 02510000) is not documented in the flow description but is included in Appendix D.
- Counter double-increment (both root and child reads increment `WS-NO-SUMRY-READ` and `WS-AUTH-SMRY-PROC-CNT`) is not highlighted as a migration concern in this document's migration notes, though it is noted for PAUDBUNL. Worth adding.

### Overall Recommendation

The document is materially accurate and suitable for use as a migration reference. The primary caveat is that all cited "line numbers" are COBOL card sequence numbers (visible in source columns 73–80), not physical file line numbers — a Java developer who tries to navigate the 366-line file using the cited numbers will be confused. The byte-size discrepancies for `PA-AUTH-DATE-9C` (3 not 4 bytes) and `PA-AUTH-TIME-9C` (5 not 6 bytes) in the CIPAUDTY table are errors that must be corrected before the child segment is parsed in Java. All business logic, IMS call structure, error handling, and migration risks are correctly identified.