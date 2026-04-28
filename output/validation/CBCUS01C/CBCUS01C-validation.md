# Validation Report: BIZ-CBCUS01C.md

**Overall: PASS** — 7 passed · 0 failed · 2 warned · 1 skipped

Source file: `178` lines   |   Document: `234` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: CBCUS01C.cbl |
| `line_number_bounds` | ✓ **PASS** | 36 line reference(s) checked, all within bounds (max: 178) |
| `backtick_identifiers` | ⚠ **WARN** | 6 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 1 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | ✓ **PASS** | All 1 Appendix A DDname(s) verified against source |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 3/6 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
6 backtick identifier(s) not found in source or copybooks

- `CUSTFILE-FILE`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Startup, loop, and shutdown phases accurately described; double-display defect correctly identified. |
| S2 Error Handling | ⚠ WARN | Doc correctly documents Z-ABEND-PROGRAM and Z-DISPLAY-IO-STATUS naming difference, but the error message for close says 'ERROR CLOSING CUSTOMER FILE' — source verified. |
| S3 Migration Notes | ✓ PASS | All 6 notes verified against source; PII/SSN concern correctly flagged. |
| S4 Copybook Fields | ✓ PASS | All 19 CVCUS01Y fields verified; PIC clauses accurate, no erroneous COMP-3 flags. |
| S5 External Calls | ✓ PASS | CEE3ABD call accurately described. |
| S6 Completeness | ⚠ WARN | The paragraph naming difference (Z- vs 9910-/9999-) is documented but the doc does not note that the close paragraph is named 9000-CUSTFILE-CLOSE while the abend utility is Z-ABEND-PROGRAM — this mixed naming within the same program is an important migration consistency note. |

### Findings

#### S1 — Program Flow

PROCEDURE DIVISION (lines 70–87): `DISPLAY 'START OF EXECUTION OF PROGRAM CBCUS01C'` → `PERFORM 0000-CUSTFILE-OPEN` → `PERFORM UNTIL END-OF-FILE = 'Y'` with inner `IF END-OF-FILE = 'N'` → `PERFORM 1000-CUSTFILE-GET-NEXT` → `IF END-OF-FILE = 'N'` → `DISPLAY CUSTOMER-RECORD` → after loop: `PERFORM 9000-CUSTFILE-CLOSE` → `DISPLAY 'END OF...'` → `GOBACK`. Matches doc steps 1–6 exactly.

`1000-CUSTFILE-GET-NEXT` (lines 92–116): On status '00', MOVE 0 TO APPL-RESULT and `DISPLAY CUSTOMER-RECORD` at line 96 — this is the **first display**. Then the main loop body at line 78 shows the **second display**. Doc correctly identifies both displays and calls this a double-display latent defect at Migration Note 1. PASS.

`0000-CUSTFILE-OPEN` (lines 118–134): Uses MOVE 8 → OPEN → MOVE 0/'MOVE 12' → IF APPL-AOK pattern. Matches doc step 2.

`9000-CUSTFILE-CLOSE` (lines 136–152): Uses arithmetic idiom `ADD 8 TO ZERO GIVING APPL-RESULT` / `SUBTRACT APPL-RESULT FROM APPL-RESULT`. Matches doc step 5.

The redundant inner guard `IF END-OF-FILE = 'N'` at line 75 inside `PERFORM UNTIL END-OF-FILE = 'Y'` is noted in Migration Note 2 — confirmed at source line 75.

#### S2 — Error Handling

Verified DISPLAY strings:
- `'ERROR OPENING CUSTFILE'` (line 129) — PASS
- `'ERROR READING CUSTOMER FILE'` (line 110) — PASS
- `'ERROR CLOSING CUSTOMER FILE'` (line 147) — PASS
- `'ABENDING PROGRAM'` (line 155) — PASS
- `'FILE STATUS IS: NNNN'` (lines 168, 172) — PASS

Important naming verification: the doc correctly notes that this program uses `Z-ABEND-PROGRAM` (line 154) and `Z-DISPLAY-IO-STATUS` (line 161) instead of `9999-ABEND-PROGRAM` and `9910-DISPLAY-IO-STATUS`. Source confirmed:
- `Z-ABEND-PROGRAM` at line 154: MOVE 0 TO TIMING, MOVE 999 TO ABCODE, CALL 'CEE3ABD' USING ABCODE, TIMING. PASS.
- `Z-DISPLAY-IO-STATUS` at line 161: identical logic to 9910-DISPLAY-IO-STATUS in other programs. PASS.

Note: Unusually, the close paragraph is named `9000-CUSTFILE-CLOSE` (line 136) which uses the standard numeric naming convention, while only the utility paragraphs use the `Z-` prefix. The doc mentions the naming difference but doesn't note this inconsistency *within* CBCUS01C itself. Minor WARN.

#### S3 — Migration Notes

**Verified notes:**
- Note 1 (double display — active DISPLAY at line 96): Confirmed — `DISPLAY CUSTOMER-RECORD` at source line 96 is live (not commented out like in CBACT02C). Two displays per record.
- Note 2 (redundant inner guard at line 75): Confirmed at source line 75.
- Note 3 (Z-ABEND-PROGRAM vs 9999 naming): Confirmed — all error calls in CBCUS01C use `PERFORM Z-DISPLAY-IO-STATUS` and `PERFORM Z-ABEND-PROGRAM` (lines 112–113, 131–132, 149–150).
- Note 4 (168-byte FILLER in raw display): Confirmed in CVCUS01Y.cpy line 23 — `FILLER PIC X(168)`.
- Note 5 (CUST-SSN and CUST-GOVT-ISSUED-ID in raw display): Confirmed — CVCUS01Y.cpy lines 17 and 18: `CUST-SSN PIC 9(09)` and `CUST-GOVT-ISSUED-ID PIC X(20)`. Both included in group-level DISPLAY.
- Note 6 (generic abend code 999): Confirmed at line 157.

**Unsupported notes:** None.

**Missing bugs:** None significant. Same simple read-and-display pattern as CBACT02C/CBACT03C.

#### S4 — Copybook Fields

**CVCUS01Y** (verified all 19 entries against copybook):
- `CUST-ID` PIC 9(09) — PASS (line 5)
- `CUST-FIRST-NAME` PIC X(25) — PASS (line 6)
- `CUST-MIDDLE-NAME` PIC X(25) — PASS (line 7)
- `CUST-LAST-NAME` PIC X(25) — PASS (line 8)
- `CUST-ADDR-LINE-1` PIC X(50) — PASS (line 9)
- `CUST-ADDR-LINE-2` PIC X(50) — PASS (line 10)
- `CUST-ADDR-LINE-3` PIC X(50) — PASS (line 11)
- `CUST-ADDR-STATE-CD` PIC X(02) — PASS (line 12)
- `CUST-ADDR-COUNTRY-CD` PIC X(03) — PASS (line 13)
- `CUST-ADDR-ZIP` PIC X(10) — PASS (line 14)
- `CUST-PHONE-NUM-1` PIC X(15) — PASS (line 15)
- `CUST-PHONE-NUM-2` PIC X(15) — PASS (line 16)
- `CUST-SSN` PIC 9(09) — PASS (line 17); doc flags as highly sensitive PII — PASS
- `CUST-GOVT-ISSUED-ID` PIC X(20) — PASS (line 18)
- `CUST-DOB-YYYY-MM-DD` PIC X(10) — PASS (line 19)
- `CUST-EFT-ACCOUNT-ID` PIC X(10) — PASS (line 20)
- `CUST-PRI-CARD-HOLDER-IND` PIC X(01) — PASS (line 21)
- `CUST-FICO-CREDIT-SCORE` PIC 9(03) — PASS (line 22); no COMP-3 in CVCUS01Y (COMP-3 is only in CVEXPORT for the export context) — doc correctly shows this as plain `9(03)` for CBCUS01C
- `FILLER` PIC X(168) — PASS (line 23)

Total: 9+25+25+25+50+50+50+2+3+10+15+15+9+20+10+10+1+3+168 = 500 bytes. PASS.

No COMP-3 fields in CVCUS01Y. Doc makes no erroneous COMP-3 claims. PASS.

#### S5 — External Calls

**CEE3ABD**: Called at line 158 `CALL 'CEE3ABD' USING ABCODE, TIMING` inside `Z-ABEND-PROGRAM`. Doc cites paragraph `Z-ABEND-PROGRAM`, line 158 — PASS. Parameters ABCODE=999, TIMING=0 — PASS.

No other CALL statements. PASS.

#### S6 — Completeness

All paragraphs documented. The Appendix C hardcoded literals table is complete — verified against source: banner strings at lines 71/85, result codes at lines 119/122/124/95/99/101, error messages at lines 129/110/147, abend code 999 at line 157, status display prefix at lines 168/172. PASS.

The mixed naming convention within CBCUS01C (standard `9000-CUSTFILE-CLOSE` alongside `Z-` prefixed utilities) is mentioned in Migration Note 3 in terms of the overall suite naming difference, but the specific observation that CBCUS01C itself has inconsistent naming conventions is not explicitly called out. This is minor — the function of all paragraphs is correctly described.

### Overall Recommendation

The document is accurate and complete for CBCUS01C. The double-display defect (same as CBACT03C pattern), PII/SSN security concern, and utility paragraph naming differences are all correctly documented. The field table is exactly matched to the copybook. A Java developer can safely translate from this document without re-reading the COBOL source, with particular attention to the PII masking requirements in any logging implementation.