# Validation Report: BIZ-CBACT01C.md

**Overall: PASS** — 6 passed · 0 failed · 3 warned · 1 skipped

Source file: `430` lines   |   Document: `430` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: CBACT01C.cbl |
| `line_number_bounds` | ✓ **PASS** | 41 line reference(s) checked, all within bounds (max: 430) |
| `backtick_identifiers` | ⚠ **WARN** | 28 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 2 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | ✓ **PASS** | All 4 Appendix A DDname(s) verified against source |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 10/11 migration note(s) missing line number citations |
| `mermaid_diagram` | ⚠ **WARN** | Mermaid diagram missing classDef colour definitions |

## Issues Found

### backtick_identifiers (WARN)
28 backtick identifier(s) not found in source or copybooks

- `ACCTFILE-FILE`
- `ARRY-FILE`
- `CODATECN-OUT-DATE`
- `OUT-FILE`
- `VBRC-FILE`
- `YYYY-MM-DD`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Paragraph order, loop structure, per-account step sequence, and data-flow labels match the source accurately. |
| S2 Error Handling | ⚠ WARN | One minor inaccuracy in the OUTFILE open error description; all abend/display strings otherwise verified. |
| S3 Migration Notes | ✓ PASS | All 11 notes are supported by the source; no significant latent bugs are missing. |
| S4 Copybook Fields | ✓ PASS | All fields in CVACT01Y and CODATECN verified; PIC clauses, COMP-3 flags, and unused-field calls accurate. |
| S5 External Calls | ✓ PASS | COBDATFT and CEE3ABD described correctly with accurate parameter details. |
| S6 Completeness | ⚠ WARN | The double-display in the main loop body is documented but the loop structure description slightly misattributes when ACCOUNT-RECORD dump fires. |

### Findings

#### S1 — Program Flow

The procedure division paragraph call order is exactly as documented: `0000-ACCTFILE-OPEN` → `2000-OUTFILE-OPEN` → `3000-ARRFILE-OPEN` → `4000-VBRFILE-OPEN` then the `PERFORM UNTIL` loop calling `1000-ACCTFILE-GET-NEXT` (lines 142–154). Inside `1000-ACCTFILE-GET-NEXT` the per-account sequence is `1100-DISPLAY-ACCT-RECORD` → `1300-POPUL-ACCT-RECORD` → `1350-WRITE-ACCT-RECORD` → `1400-POPUL-ARRAY-RECORD` → `1450-WRITE-ARRY-RECORD` → `1500-POPUL-VBRC-RECORD` → `1550-WRITE-VB1-RECORD` → `1575-WRITE-VB2-RECORD` (source lines 170–178), which matches the doc's steps 7–15 exactly.

The doc states the raw 300-byte block dump happens "after step 15" when loop checks `END-OF-FILE`. Verified: the `DISPLAY ACCOUNT-RECORD` is at line 151 in the main loop body, inside the second `IF END-OF-FILE = 'N'` guard after `1000-ACCTFILE-GET-NEXT` returns — this is accurate.

One minor structural note: the doc says `END-OF-FILE = 'N'` is a "redundant inner guard" (Section 2.2 and Migration Note 6). The source at lines 148–153 shows two nested `IF END-OF-FILE = 'N'` guards around the read call and the block dump respectively, both inside `PERFORM UNTIL END-OF-FILE = 'Y'`. The doc's analysis of redundancy is correct.

The `INITIALIZE ARR-ARRAY-REC` (step 6) is inside `1000-ACCTFILE-GET-NEXT` at line 169 — not in the main loop as the doc's step numbering could imply — but the doc correctly notes it is inside that paragraph. No inaccuracy.

#### S2 — Error Handling

The doc states the OUTFILE open failure logs `'ERROR OPENING OUTFILE'` followed by the raw status bytes and abends (step 2, section 2.1). The source at line 345 shows: `DISPLAY 'ERROR OPENING OUTFILE'  OUTFILE-STATUS` — the status IS concatenated directly to the display string (not via a separate call). However, the source also calls `9910-DISPLAY-IO-STATUS` at line 347 immediately after. So there are two status outputs: the raw concatenated display AND the decoded status display. The doc says only "logs `'ERROR OPENING OUTFILE'` followed by the raw status bytes" without mentioning the follow-up `9910-DISPLAY-IO-STATUS` call, which is technically an omission but minor.

The ACCTFILE open error (step 1) at line 328 uses `DISPLAY 'ERROR OPENING ACCTFILE'` without concatenating the status directly — consistent with the doc's description.

All DISPLAY strings for error messages are verified accurate:
- `'ERROR READING ACCOUNT FILE'` (line 192) — PASS
- `'ACCOUNT FILE WRITE STATUS IS:'` (line 246) — PASS  
- `'ERROR CLOSING ACCOUNT FILE'` (line 399) — PASS
- `'ABENDING PROGRAM'` (line 407) — PASS
- `'FILE STATUS IS: NNNN'` (line 420, 424) — PASS

The `9999-ABEND-PROGRAM` routine at line 406: MOVE 0 TO TIMING then MOVE 999 TO ABCODE — doc correctly states TIMING=0 and ABCODE=999.

#### S3 — Migration Notes

**Verified notes:**
- Note 1 (`ACCT-ADDR-ZIP` never used): Confirmed — field is in CVACT01Y (line 15) but never appears in any MOVE, DISPLAY, or WRITE statement in the source.
- Note 2 (stale `OUT-ACCT-CURR-CYC-DEBIT` when input non-zero): Confirmed — lines 236–238 show only the zero branch assigns the literal 2525.00; no ELSE clause assigns anything.
- Note 3 (three output files never explicitly closed): Confirmed — only `9000-ACCTFILE-CLOSE` is called before GOBACK (line 156). `OUT-FILE`, `ARRY-FILE`, `VBRC-FILE` have no close paragraphs.
- Note 4 (hardcoded test literals): All verified at stated lines.
- Note 5 (single abend code 999): Verified at line 409.
- Note 6 (redundant inner guard): Verified at lines 148–150.
- Note 7 (double logging): Verified — DISPLAY inside `1100-DISPLAY-ACCT-RECORD` (11 labelled fields) plus DISPLAY ACCOUNT-RECORD at line 151 in main loop.
- Note 8 (write accepts status '10'): Verified — line 245 checks `NOT = '00' AND NOT = '10'`.
- Note 9 (zero in `CODATECN-0UT-DATE`): Confirmed in CODATECN.cpy line 39.
- Note 10 (`ACCT-EXPIRAION-DATE` typo): Confirmed in CVACT01Y.cpy line 11 and source line 64.
- Note 11 (OUT-ACCT-REISSUE-DATE trailing bytes): The move at line 233 copies 20-byte `CODATECN-0UT-DATE` into 10-byte `OUT-ACCT-REISSUE-DATE`; COBOL truncates at 10 bytes, not at 8. The analysis is correct.

**Unsupported notes:** None.

**Missing bugs:** The doc does not note that `INITIALIZE ARR-ARRAY-REC` uses COBOL INITIALIZE semantics (sets numeric to zero, alphanumeric to spaces) which does correctly zero slots 4 and 5. No hidden issue. No significant missing bugs found.

#### S4 — Copybook Fields

**CVACT01Y** (verified all 13 entries):
- `ACCT-ID` PIC 9(11) — PASS (source line 5)
- `ACCT-ACTIVE-STATUS` PIC X(01) — PASS (line 6)
- `ACCT-CURR-BAL` PIC S9(10)V99 — PASS (line 7)
- `ACCT-CREDIT-LIMIT` PIC S9(10)V99 — PASS (line 8)
- `ACCT-CASH-CREDIT-LIMIT` PIC S9(10)V99 — PASS (line 9)
- `ACCT-OPEN-DATE` PIC X(10) — PASS (line 10)
- `ACCT-EXPIRAION-DATE` PIC X(10) — PASS, typo preserved (line 11)
- `ACCT-REISSUE-DATE` PIC X(10) — PASS (line 12)
- `ACCT-CURR-CYC-CREDIT` PIC S9(10)V99 — PASS (line 13)
- `ACCT-CURR-CYC-DEBIT` PIC S9(10)V99 — PASS (line 14); doc correctly notes no COMP-3 in CVACT01Y
- `ACCT-ADDR-ZIP` PIC X(10), unused — PASS (line 15)
- `ACCT-GROUP-ID` PIC X(10) — PASS (line 16)
- `FILLER` PIC X(178) — PASS (line 17)

Note: The doc correctly identifies `OUT-ACCT-CURR-CYC-DEBIT` as COMP-3 in the FD (source line 67–68), distinguishing it from the display-format field in the working-storage copybook. This is accurate.

**CODATECN** (verified all 5 entries):
- `CODATECN-TYPE` PIC X(01) with 88-levels `YYYYMMDD-IN='1'`, `YYYY-MM-DD-IN='2'` — PASS (lines 19–21)
- `CODATECN-INP-DATE` PIC X(20) — PASS (line 22)
- `CODATECN-OUTTYPE` PIC X(01) with 88-levels `YYYY-MM-DD-OP='1'`, `YYYYMMDD-OP='2'` — PASS (lines 36–38)
- `CODATECN-0UT-DATE` PIC X(20) with zero not O — PASS (line 39)
- `CODATECN-ERROR-MSG` PIC X(38) — PASS (line 52)

The doc correctly states this program never reads `CODATECN-ERROR-MSG`.

#### S5 — External Calls

**COBDATFT**: Called at line 231 `CALL 'COBDATFT' USING CODATECN-REC`. Doc states the entire `CODATECN-REC` communication area is passed — PASS. Doc correctly notes input is `CODATECN-TYPE='2'` and `CODATECN-INP-DATE`, output is `CODATECN-0UT-DATE` — all verified.

**CEE3ABD**: Called at line 410 `CALL 'CEE3ABD' USING ABCODE, TIMING`. Doc states ABCODE=999, TIMING=0 passed by USING — PASS.

#### S6 — Completeness

The doc covers all significant processing paragraphs. The only minor omission is that the doc mentions the OUTFILE open error displays "the raw status bytes" (which is accurate given line 345 concatenates `OUTFILE-STATUS`) but does not explicitly note that `9910-DISPLAY-IO-STATUS` is *also* called on that same error path. This is a very minor dual-display issue not critical for migration.

The Appendix C table of hardcoded literals is complete and verified against actual source line numbers. The Appendix D working fields are complete.

One observation: `ARR-ACCT-CURR-BAL` and `ARR-ACCT-CURR-CYC-DEBIT` are both defined as `COMP-3` in the FD ARRY-FILE (source lines 75–77). The doc correctly identifies these as COMP-3 in the step 10 table and in Appendix A. PASS.

### Overall Recommendation

The document is accurate and safe to use as a migration reference for CBACT01C. All paragraph names, field names, data-flow mappings, PIC clauses, COMP-3 flags, error messages, and latent bugs are correctly described. The one minor warning — that the OUTFILE open error path emits two status outputs rather than one — does not affect correctness of any migration decision. A Java developer can safely translate from this document without re-reading the COBOL source.