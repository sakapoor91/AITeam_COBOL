# Validation Report: BIZ-CSUTLDTC.md

**Overall: PASS** — 5 passed · 0 failed · 3 warned · 2 skipped

Source file: `157` lines   |   Document: `236` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: CSUTLDTC.cbl |
| `line_number_bounds` | ✓ **PASS** | 23 line reference(s) checked, all within bounds (max: 157) |
| `backtick_identifiers` | ⚠ **WARN** | 10 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 0 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 6/8 migration note(s) missing line number citations |
| `mermaid_diagram` | ⚠ **WARN** | Mermaid diagram missing classDef colour definitions |

## Issues Found

### backtick_identifiers (WARN)
10 backtick identifier(s) not found in source or copybooks

- `FC-DATE-VALID`
- `FC-NO-ERROR`
- `RETURN-CODE`
- `WS-TIMESTAMP`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Paragraph order, PERFORM targets, CEEDAYS parameters, and EVALUATE structure all match source exactly. |
| S2 Error Handling | ✓ PASS | All nine EVALUATE branches and their result strings match source; commented-out DISPLAY noted correctly. |
| S3 Migration Notes | ⚠ WARN | Seven of eight notes are accurate; note 7 about `WS-TIMESTAMP` is misleading — the field does not exist in this program at all (no CSDAT01Y copy). |
| S4 Copybook Fields | ✓ PASS | No copybooks used; all inline Working-Storage fields verified against source with correct PIC clauses and byte sizes. |
| S5 External Calls | ✓ PASS | CEEDAYS call at line 116 documented correctly: four parameters, correct names, correct description of output not returned to caller. |
| S6 Completeness | ⚠ WARN | `MOVE WS-DATE-TO-TEST TO WS-DATE` at line 122 is a latent display bug not fully explained; `CASE-2-CONDITION-ID` REDEFINES not mentioned. |

### Findings

#### S1 — Program Flow

All paragraphs accounted for and in correct order:
- `PROCEDURE DIVISION USING` at line 88 — correct.
- `INITIALIZE WS-MESSAGE` / `MOVE SPACES TO WS-DATE` at lines 90–91 — correct.
- `PERFORM A000-MAIN THRU A000-MAIN-EXIT` at lines 93–94 — correct.
- Commented-out `DISPLAY WS-MESSAGE` at line 96 — correctly noted.
- `MOVE WS-MESSAGE TO LS-RESULT` / `MOVE WS-SEVERITY-N TO RETURN-CODE` at lines 97–98 — correct.
- `EXIT PROGRAM` at line 100, `GOBACK` commented out at line 101 — correct.
- `A000-MAIN` at line 103 through `A000-MAIN-EXIT` at line 152 — paragraph structure accurate.
- All nine EVALUATE WHEN branches at lines 129–148 documented with correct result strings.

Step 4 description states the date is copied into "Vstring-text OF WS-DATE-TO-TEST" and also into "WS-DATE". Source line 107-108 confirms: `MOVE LS-DATE TO VSTRING-TEXT OF WS-DATE-TO-TEST WS-DATE` — accurate.

#### S2 — Error Handling

No error paragraph exists (correctly documented). The EVALUATE in `A000-MAIN` handles all cases. All nine WHEN branches and their exact MOVE string literals verified against source lines 129–148:
- `'Date is valid'` (line 130), `'Insufficient'` (132), `'Datevalue error'` (134), `'Invalid Era    '` (136), `'Unsupp. Range  '` (138), `'Invalid month  '` (140), `'Bad Pic String '` (142), `'Nonnumeric data'` (144), `'YearInEra is 0 '` (146), `'Date is invalid'` (148) — all verified exact.

The distinction between the FC-INVALID-DATE naming vs. its "success" meaning is correctly highlighted.

#### S3 — Migration Notes

**Verified notes:**
- Note 1: `FC-INVALID-DATE` hex value `X'0000000000000000'` means success — confirmed at line 62 of source.
- Note 2: `OUTPUT-LILLIAN` populated but never returned — confirmed; it is only used in the CALL at lines 116–120 and never moved to LS-RESULT.
- Note 3: LS-RESULT is 80 bytes of character text — confirmed (LS-RESULT PIC X(80) at line 86).
- Note 4: Vstring-length set to full PIC length (10) regardless of content — confirmed at lines 105–106.
- Note 5: RETURN-CODE carries CEEDAYS severity — confirmed at line 98.
- Note 6: Commented-out DISPLAY at line 96 — confirmed.
- Note 8: GOBACK at line 101 is commented out — confirmed.

**Unsupported/misleading notes:**
- Note 7 states "`WS-TIMESTAMP` from `CSDAT01Y` is not used." CSUTLDTC does **not** COPY CSDAT01Y anywhere in the source — there is no such copybook reference. The field `WS-TIMESTAMP` does not exist in this program. The note should state that CSDAT01Y is not copied at all, not that a field from it "is not used." This is a documentation fabrication of sorts: the suggested rename targets `FC-DATE-VALID` / `FC-NO-ERROR` in migration note 1 are accurate suggestions but are not source field names, which correctly explains the Phase 1 backtick-identifier warnings.

**Missing bugs:**
- `MOVE WS-DATE-TO-TEST TO WS-DATE` at line 122: WS-DATE-TO-TEST is a group item whose first 2 bytes are `Vstring-length` (S9(4) BINARY — the binary value 10 = `X'000A'`). When this group is MOVEd to WS-DATE (PIC X(10)), the first 2 bytes of WS-DATE receive the binary representation of the length (typically `X'000A'`), not printable digits. The resulting WS-DATE embedded in WS-MESSAGE and returned in LS-RESULT will show two non-printable characters followed by 8 characters of date text, not the clean 10-char date string. The doc says "in practice identical" which is incorrect — this is a latent display defect. It should be flagged as a migration bug.

#### S4 — Copybook Fields

No copybooks are used by CSUTLDTC. All Working-Storage fields are inline. Verification of key fields against source:

| Field | Doc PIC | Source PIC | Match |
|-------|---------|------------|-------|
| `Vstring-length` in `WS-DATE-TO-TEST` | S9(4) BINARY | `PIC S9(4) BINARY` (line 26) | PASS |
| `OUTPUT-LILLIAN` | S9(9) BINARY, 4 bytes | `PIC S9(9) USAGE IS BINARY` (line 41) | PASS |
| `WS-SEVERITY` | X(04), 4 bytes | `PIC X(04)` (line 43) | PASS |
| `WS-MSG-NO` | X(04), 4 bytes | `PIC X(04)` (line 46) | PASS |
| `WS-RESULT` | X(15), 15 bytes | `PIC X(15)` (line 49) | PASS |
| `WS-DATE` | X(10), 10 bytes | `PIC X(10) VALUE SPACES` (line 52) | PASS |
| `WS-DATE-FMT` | X(10), 10 bytes | `PIC X(10)` (line 55) | PASS |
| `FEEDBACK-CODE` group | 12 bytes | `SEVERITY` S9(4) BINARY + `MSG-NO` S9(4) BINARY + `CASE-SEV-CTL` X + `FACILITY-ID` XXX + `I-S-INFO` S9(9) BINARY = 4+4+1+3+4 = 16 bytes (not 12) | WARN — doc states 12 bytes but structure computes to 16 bytes |
| `FILLER 'Mesg Code:'` | 11 bytes | `PIC X(11) VALUE 'Mesg Code:'` (line 45) | PASS |
| `FILLER 'TstDate:'` | 9 bytes | `PIC X(09) VALUE 'TstDate:'` (line 51) | PASS |

Note: `CASE-2-CONDITION-ID` REDEFINES of `CASE-1-CONDITION-ID` (lines 74–77) is not mentioned in Appendix D, which is a minor omission but does not affect migration.

The FEEDBACK-CODE byte count discrepancy in Appendix D (states 12 bytes but the structure is FEEDBACK-TOKEN-VALUE at 8 bytes + I-S-INFO S9(9) BINARY at 4 bytes = 12 bytes for the outer group; the inner breakdown is correct when read carefully). On re-examination: `FEEDBACK-TOKEN-VALUE` (8 bytes via 88-level values) + `I-S-INFO` (S9(9) BINARY = 4 bytes) = 12 bytes total for `FEEDBACK-CODE`. The doc is correct.

#### S5 — External Calls

CEEDAYS call at lines 116–120 fully documented:
- Input 1: `WS-DATE-TO-TEST` — variable-length structure, populated from `LS-DATE` — PASS.
- Input 2: `WS-DATE-FORMAT` — variable-length format mask, populated from `LS-DATE-FORMAT` — PASS.
- Output 3: `OUTPUT-LILLIAN` — PIC S9(9) BINARY, never returned to caller — PASS.
- Output 4: `FEEDBACK-CODE` — 12-byte token with sub-structure — PASS.

No other external programs or services are called.

#### S6 — Completeness

- The commented-out `DISPLAY WS-MESSAGE` debug line (line 96) is correctly noted.
- The `GOBACK` commented out (line 101) is correctly noted.
- `CASE-2-CONDITION-ID REDEFINES CASE-1-CONDITION-ID` (lines 74–77) providing `CLASS-CODE` and `CAUSE-CODE` fields is not mentioned in the document. These are secondary views of the feedback token that a Java developer might want to know about, but they are unused in this program.
- The latent display bug in `MOVE WS-DATE-TO-TEST TO WS-DATE` (line 122) is described as "in practice identical" but is actually a display corruption issue (first 2 bytes of WS-DATE in the result message will be binary, not printable).
- All significant paragraphs are documented: `A000-MAIN` and `A000-MAIN-EXIT`.

### Overall Recommendation

The document is accurate enough to serve as a migration reference for the primary logic path: CEEDAYS parameter construction, feedback code classification, and the return mechanism are all correctly described. Two issues require attention before use: (1) migration note 7 references a non-existent copybook (CSDAT01Y) and should be removed or rewritten; (2) the `MOVE WS-DATE-TO-TEST TO WS-DATE` at line 122 is a real latent display defect that the document mischaracterizes as harmless — the Java replacement should move `Vstring-text` (not the full group) into the display field. A Java developer working from this document alone would produce a correct translation of the business logic but might replicate the display defect.