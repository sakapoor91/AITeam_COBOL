# Validation Report: BIZ-PAUDBUNL.md

**Overall: FAIL** — 6 passed · 1 failed · 2 warned · 1 skipped

Source file: `317` lines   |   Document: `359` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✗ **FAIL** | 3 required section(s) missing |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: PAUDBUNL.cbl |
| `line_number_bounds` | ✓ **PASS** | 47 line reference(s) checked, all within bounds (max: 317) |
| `backtick_identifiers` | ⚠ **WARN** | 1 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 4 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | ✓ **PASS** | All 2 Appendix A DDname(s) verified against source |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | Section 4 contains no numbered migration notes |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### required_sections (FAIL)
3 required section(s) missing

- `Section 1 — Purpose`
- `Section 3 — Error Handling`
- `Section 4 — Migration Notes`

### backtick_identifiers (WARN)
1 backtick identifier(s) not found in source or copybooks

- `RETURN-CODE`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention. The Phase 1 FAIL on required sections is a header-naming artifact — the document uses "Section 1", "Section 2", etc. rather than "## 1. Purpose" format, but all substantive content is present and accurate.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | All paragraphs, PERFORM structure, GN/GNP calls, file WRITE operations, and loop conditions accurately described and verified against source. |
| S2 Error Handling | ✓ PASS | All error paths correctly identified; DISPLAY strings verified exact; non-fatal close errors correctly documented; abend message text correctly identified as using wrong program name. |
| S3 Migration Notes | ⚠ WARN | Sixteen notes — all substantively accurate. One inaccuracy: OPFIL2 record size stated as "209 bytes" in the Purpose section table but FD shows 206 bytes; Appendix A correctly states 206 bytes (internal inconsistency). |
| S4 Copybook Fields | ✓ PASS | All copybook field tables verified against actual CPY files; PIC clauses, byte sizes, COMP-3 flags, and 88-level values are accurate with minor byte-count notes. |
| S5 External Calls | ✓ PASS | CBLTDLI calls (GN and GNP only) correctly documented; no ISRT calls confirmed as absent. |
| S6 Completeness | ⚠ WARN | `9999-EXIT` paragraph omitted (minor); `WS-END-OF-AUTHDB-FLAG` interaction with `END-OF-AUTHDB` 88-level correctly documented; one undocumented active DISPLAY detail. |

### Findings

#### S1 — Program Flow

All paragraphs verified against PAUDBUNL.CBL source:

- `MAIN-PARA` at source line 157 (sequence 01220026): `ENTRY 'DLITCBL' USING PAUTBPCB` at line 158 (sequence 01225033) — correct.
- **Important:** unlike PAUDBLOD, PAUDBUNL does NOT display `'STARTING PAUDBLOD'` before `1000-INITIALIZE`; the startup DISPLAY is inside `1000-INITIALIZE` at source line 180. The doc correctly places this in Section 2.1 step 2 under `1000-INITIALIZE`.
- `PERFORM 1000-INITIALIZE THRU 1000-EXIT` at source line 161 — correct.
- Root loop: `PERFORM 2000-FIND-NEXT-AUTH-SUMMARY THRU 2000-EXIT UNTIL WS-END-OF-ROOT-SEG = 'Y'` at source lines 163–164 — correct.
- **No separate child loop paragraph**: PAUDBUNL's child loop (`3000-FIND-NEXT-AUTH-DTL`) is called from inside `2000-FIND-NEXT-AUTH-SUMMARY` — there is no top-level PERFORM for child processing like PAUDBLOD. Doc correctly describes this as called from within the root loop (Section 2.2 step 4).
- `PERFORM 4000-FILE-CLOSE THRU 4000-EXIT` at source line 166, `GOBACK` at line 170 — correct.
- `1000-INITIALIZE` at source line 173: ACCEPT CURRENT-DATE (line 176), ACCEPT CURRENT-YYDDD (line 177), commented-out ACCEPT PRM-INFO (line 179), DISPLAY `'STARTING PROGRAM PAUDBUNL::'` (line 180), separator (line 181), date (line 182), blank (line 183), period-exit at line 185, then OPEN OUTPUT OPFILE1 (line 186) and OPFILE2 (line 194) — correctly described.
- `2000-FIND-NEXT-AUTH-SUMMARY` at source line 207: INITIALIZE PAUT-PCB-STATUS (line 212), CALL CBLTDLI FUNC-GN (line 213–216), three-outcome IF structure (lines 223–246) — correctly described.
- `PA-ACCT-ID IS NUMERIC` guard at source line 232: WRITE OPFIL1-REC (line 233), INITIALIZE WS-END-OF-CHILD-SEG (line 234), child loop PERFORM (lines 235–236) — all correctly described.
- `GB` handling: SET END-OF-AUTHDB TO TRUE (line 240), MOVE 'Y' TO WS-END-OF-ROOT-SEG (line 241) — correctly described.
- `3000-FIND-NEXT-AUTH-DTL` at source line 253: CALL CBLTDLI FUNC-GNP (lines 257–260), three-outcome IF structure (lines 266–283), INITIALIZE PAUT-PCB-STATUS at end (line 284) — all correctly described.
- Child success path: SET MORE-AUTHS (line 267), ADD 1 to WS-NO-SUMRY-READ (line 268), ADD 1 to WS-AUTH-SMRY-PROC-CNT (line 269), MOVE PENDING-AUTH-DETAILS TO CHILD-SEG-REC (line 270), WRITE OPFIL2-REC (line 271) — correctly described including counter misname issue.
- `4000-FILE-CLOSE` at source line 289: CLOSE OPFILE1 (line 291), status check (lines 293–297), CLOSE OPFILE2 (line 298), status check (lines 300–304) — correctly described.
- `9999-ABEND` at source line 308: DISPLAY `'IMSUNLOD ABENDING ...'` (line 311 — wrong name confirmed), MOVE 16 TO RETURN-CODE (line 313), GOBACK (line 314) — correctly described including wrong-name note.

#### S2 — Error Handling

All DISPLAY strings verified:

- `'ERROR IN OPENING OPFILE1:' WS-OUTFL1-STATUS` at source line 190 (sequence 01965028) — exact match.
- `'ERROR IN OPENING OPFILE2:' WS-OUTFL2-STATUS` at source line 198 (sequence 01969428) — exact match.
- `'AUTH SUM  GN FAILED  :' PAUT-PCB-STATUS` at source line 244 (sequence 02230029) — exact match.
- `'KEY FEEDBACK AREA    :' PAUT-KEYFB` at source line 245 (sequence 02240048) — exact match.
- `'GNP CALL FAILED  :' PAUT-PCB-STATUS` at source line 280 (sequence 02530030) — exact match.
- `'KFB AREA IN CHILD:' PAUT-KEYFB` at source line 281 (sequence 02531048) — exact match.
- `'CHILD SEG FLAG GE : ' WS-END-OF-CHILD-SEG` at source lines 276–277 (sequences 02501044–02502050) — exact match. Doc correctly notes this fires for every root segment.
- `'CLOSING THE FILE'` at source line 290 (sequence 02631043) — exact match.
- `'ERROR IN CLOSING 1ST FILE:' WS-OUTFL1-STATUS` at source line 296 (sequence 02690030) — exact match.
- `'ERROR IN CLOSING 2ND FILE:' WS-OUTFL2-STATUS` at source line 303 (sequence 02760030) — exact match.
- `'IMSUNLOD ABENDING ...'` at source line 311 (sequence 03660030) — exact match. Doc correctly identifies this as the wrong program name.

No undocumented error paths found.

#### S3 — Migration Notes

**Verified notes:**
1. `WS-PGMNAME = 'IMSUNLOD'` at source line 54 (sequence 00280026) — confirmed.
2. `ACCEPT PRM-INFO FROM SYSIN` commented out at source line 179 — confirmed. All PRM-INFO subfields never populated — confirmed.
3. Child counter misname: WS-NO-SUMRY-READ and WS-AUTH-SMRY-PROC-CNT incremented on child reads (source lines 268–269) while WS-NO-DTL-READ never set — confirmed.
4. `PA-ACCT-ID IS NUMERIC` silent skip at source line 232 — confirmed; no error DISPLAY in the non-numeric path.
5. `CHILD SEG FLAG GE` DISPLAY fires per root — confirmed; it is inside the GE branch of `3000-FIND-NEXT-AUTH-DTL` which executes for every root.
6. `PAUT-PCB-STATUS` initialized before GN but at end of GNP paragraph — confirmed (line 212 before GN; line 284 at end after GNP processing).
7. `WS-NO-CHKP`, `WS-TOT-REC-WRITTEN`, `WS-NO-SUMRY-DELETED`, `WS-NO-DTL-DELETED` declared and never incremented — confirmed.
8. `WS-IMS-PSB-SCHD-FLG` declared and never used — confirmed at source line 106.
9. Unused IMSFUNCS function codes — confirmed: only FUNC-GN (line 213) and FUNC-GNP (line 257) are actually called.
10. OPFIL2-REC record length: FD at source lines 46–48 shows `ROOT-SEG-KEY PIC S9(11) COMP-3` (6 bytes) + `CHILD-SEG-REC PIC X(200)` (200 bytes) = **206 bytes** — confirmed.
11. `PA-ACCT-ID` COMP-3 BigDecimal — confirmed.
12. CIPAUSMY monetary fields COMP-3 — confirmed.
13. CIPAUDTY monetary fields COMP-3 — confirmed.
14. No CBLTDLI checkpoint calls — confirmed; no CHKP call exists in source.
15. `PA-MERCHANT-CATAGORY-CODE` typo — confirmed in CIPAUDTY.cpy line 36.
16. `CURRENT-DATE` is YYMMDD (PIC 9(06)) — confirmed at source line 55.

**Inaccuracy found:**
- Section 1 (Purpose) table states `OPFILE2` writes "One **209-byte** record." The FD at source lines 46–48 defines `OPFIL2-REC` as 6 + 200 = **206 bytes**. Appendix A correctly states "FIXED 206" — but the Purpose section's "209 bytes" is wrong. This is a factual error that could cause a Java developer to size the output buffer incorrectly if only reading the Purpose section.

**Missing bugs:**
- `WS-MORE-AUTHS-FLAG` / `MORE-AUTHS` 88-level is SET at source line 267 on every successful child read but is never subsequently tested in any IF or PERFORM UNTIL condition. This is documented in Appendix D as "not tested" — correct, but not called out as a migration note.

#### S4 — Copybook Fields

All tables verified against actual .cpy source files:

**CIPAUSMY (CIPAUSMY.cpy):**
| Field | Doc PIC | CPY PIC | Match |
|-------|---------|---------|-------|
| `PA-ACCT-ID` | S9(11) COMP-3, 6 bytes | `PIC S9(11) COMP-3` | PASS |
| `PA-CUST-ID` | 9(09), 9 bytes | `PIC 9(09)` | PASS |
| `PA-AUTH-STATUS` | X(01), 1 byte | `PIC X(01)` | PASS |
| `PA-ACCOUNT-STATUS` OCCURS 5 | X(02), 2 ea (10 total) | `PIC X(02) OCCURS 5 TIMES` | PASS |
| `PA-CREDIT-LIMIT` | S9(09)V99 COMP-3, 6 bytes | `PIC S9(09)V99 COMP-3` | PASS |
| `PA-CASH-LIMIT` | S9(09)V99 COMP-3, 6 bytes | `PIC S9(09)V99 COMP-3` | PASS |
| `PA-CREDIT-BALANCE` | S9(09)V99 COMP-3, 6 bytes | `PIC S9(09)V99 COMP-3` | PASS |
| `PA-CASH-BALANCE` | S9(09)V99 COMP-3, 6 bytes | `PIC S9(09)V99 COMP-3` | PASS |
| `PA-APPROVED-AUTH-CNT` | S9(04) COMP, 2 bytes | `PIC S9(04) COMP` | PASS |
| `PA-DECLINED-AUTH-CNT` | S9(04) COMP, 2 bytes | `PIC S9(04) COMP` | PASS |
| `FILLER` | X(34), 34 bytes | `PIC X(34)` | PASS |

**CIPAUDTY (CIPAUDTY.cpy):**
| Field | Doc PIC | CPY PIC | Match |
|-------|---------|---------|-------|
| `PA-AUTH-DATE-9C` | S9(05) COMP-3, 3 bytes | `PIC S9(05) COMP-3` | PASS — 3 bytes correct for 5-digit packed |
| `PA-AUTH-TIME-9C` | S9(09) COMP-3, 5 bytes | `PIC S9(09) COMP-3` | PASS — 5 bytes correct for 9-digit packed |
| `PA-AUTH-RESP-CODE` | X(02), 2 bytes + 88 `PA-AUTH-APPROVED` = `'00'` | `PIC X(02)` + `88 PA-AUTH-APPROVED VALUE '00'` | PASS |
| `PA-TRANSACTION-AMT` | S9(10)V99 COMP-3, 6 bytes | `PIC S9(10)V99 COMP-3` — 7 bytes packed (12 digits) | WARN — doc says 6 bytes; correct is 7 bytes |
| `PA-APPROVED-AMT` | S9(10)V99 COMP-3, 6 bytes | `PIC S9(10)V99 COMP-3` — 7 bytes | WARN — doc says 6 bytes; correct is 7 bytes |
| `PA-MATCH-STATUS` | X(01) + 88 P/D/E/M | All 4 values confirmed | PASS |
| `PA-AUTH-FRAUD` | X(01) + 88 F/R | Both values confirmed | PASS |
| `PA-FRAUD-RPT-DATE` | X(08), 8 bytes | `PIC X(08)` | PASS |
| `FILLER` | X(17), 17 bytes | `PIC X(17)` | PASS |

**PAUTBPCB (PAUTBPCB.CPY):**
All 9 fields verified — PASS. `PAUT-KEYFB X(255)` — PASS.

**IMSFUNCS (IMSFUNCS.cpy):**
All 10 entries verified. `PARMCOUNT PIC S9(05) COMP-5 VALUE +4` — PASS.

#### S5 — External Calls

PAUDBUNL makes only two CBLTDLI calls, both documented:

| Call | Paragraph | Source Line | Params | Verified |
|------|-----------|-------------|--------|---------|
| GN (root read) | `2000-FIND-NEXT-AUTH-SUMMARY` | 213–216 | `FUNC-GN, PAUTBPCB, PENDING-AUTH-SUMMARY, ROOT-UNQUAL-SSA` | PASS |
| GNP (child read) | `3000-FIND-NEXT-AUTH-DTL` | 257–260 | `FUNC-GNP, PAUTBPCB, PENDING-AUTH-DETAILS, CHILD-UNQUAL-SSA` | PASS |

No ISRT, REPL, or DLET calls exist in source — confirmed. The doc's Appendix B IMSFUNCS table correctly marks FUNC-ISRT as "Not used" — verified.

#### S6 — Completeness

- `9999-EXIT` paragraph (source line 316) contains only `EXIT` — acceptable omission.
- `1000-EXIT`, `2000-EXIT`, `3000-EXIT`, `4000-EXIT` — all EXIT-only paragraphs, acceptable omissions.
- The `WS-END-OF-AUTHDB-FLAG` interaction with `END-OF-AUTHDB` 88-level: source line 240 `SET END-OF-AUTHDB TO TRUE` (which sets `WS-END-OF-AUTHDB-FLAG = 'Y'`) followed immediately by `MOVE 'Y' TO WS-END-OF-ROOT-SEG` at line 241. The doc correctly describes both. The double-set is a redundancy but not a bug.
- `WS-MORE-AUTHS-FLAG` SET at source line 267 but never tested — documented in Appendix D, not in migration notes. Minor.
- The OPFIL2 record-size discrepancy ("209-byte" in Purpose vs. "206-byte" in Appendix A) is the most significant completeness/accuracy issue.
- Appendix C lists all major hardcoded literals. The table entries for `ROOT-UNQUAL-SSA` and `CHILD-UNQUAL-SSA` cite "line 831" for both — this is the COBOL sequence number pattern. The actual source lines are 111–117. Minor cosmetic issue.
- No significant business logic paths are undocumented.

### Overall Recommendation

The document is accurate and comprehensive enough to serve as a migration reference. The most important correction needed is the OPFIL2 record-size discrepancy in the Purpose section (stated as 209 bytes, actually 206 bytes from the FD) — a Java developer sizing output buffers from that table would allocate 3 extra bytes per record. The `PA-TRANSACTION-AMT` and `PA-APPROVED-AMT` byte sizes (6 vs. correct 7 bytes for S9(10)V99 COMP-3) are minor but should be corrected in the CIPAUDTY table to prevent incorrect binary parsing. All business logic, IMS call structure, counter misname warnings, and migration risks are correctly and completely documented. The Phase 1 FAIL on required sections reflects a header-naming difference (uses "Section N" not "## N.") and is not a content deficiency.