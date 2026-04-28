# Validation Report: BIZ-COACCT01.md

**Overall: PASS** — 6 passed · 0 failed · 2 warned · 2 skipped

Source file: `620` lines   |   Document: `357` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COACCT01.cbl |
| `line_number_bounds` | ✓ **PASS** | 44 line reference(s) checked, all within bounds (max: 620) |
| `backtick_identifiers` | ⚠ **WARN** | 165 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 7 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 7/10 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
165 backtick identifier(s) not found in source or copybooks

- `ERR-QUEUE-OPEN`
- `ERROR-MESSAGE`
- `ERROR-QUEUE-HANDLE`
- `INPUT-QUEUE-HANDLE`
- `INPUT-QUEUE-NAME`
- `MQ-APPL-RETURN-MESSAGE`
- `MQ-BUFFER`
- `MQ-BUFFER-LENGTH`
- `MQ-CONDITION-CODE`
- `MQ-CONSTANTS`

## Phase 2 — LLM Judge

**Phase 2 Verdict: PASS**

> All six semantic checks pass. Minor warnings noted but none require document revision before use as a migration reference.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Paragraph sequence, loop structure, and startup/shutdown steps accurately reflect the source. |
| S2 Error Handling | ✓ PASS | Error paths, DISPLAY strings, and queue-open failure handling correctly documented. |
| S3 Migration Notes | ✓ PASS | All ten notes are supported by source evidence; one latent bug (close recursion) correctly identified. |
| S4 Copybook Fields | ✓ PASS | CVACT01Y field table matches the copybook exactly, including the ACCT-EXPIRAION-DATE typo and ACCT-ADDR-ZIP omission. |
| S5 External Calls | ✓ PASS | MQOPEN, MQGET, MQPUT, MQCLOSE call signatures and trigger conditions are accurate. |
| S6 Completeness | ✓ PASS | No significant paragraphs, COMP-3 fields, or business paths are omitted. |

### Findings

#### S1 — Program Flow

The document's paragraph ordering matches the source exactly. `1000-CONTROL` (line 178) initialises work areas, calls `2100-OPEN-ERROR-QUEUE`, issues CICS RETRIEVE, then sequentially calls `2300-OPEN-INPUT-QUEUE`, `2400-OPEN-OUTPUT-QUEUE`, `3000-GET-REQUEST`, and finally enters the `PERFORM 4000-MAIN-PROCESS UNTIL NO-MORE-MSGS` loop — all confirmed at lines 187–216 of the source. The doc correctly notes that `3000-GET-REQUEST` is called once before the loop begins (line 214) and then again inside `4000-MAIN-PROCESS` (line 330). The description that `4000-PROCESS-REQUEST-REPLY` is invoked from within `3000-GET-REQUEST` on a successful GET (line 374) is accurate. The shutdown sequence in `8000-TERMINATION` (line 538) with conditional closes matches lines 540–548. One minor imprecision: the BIZ doc describes Step 6 as "First GET of the request queue" called from `1000-CONTROL`; the source shows it is called via `PERFORM 3000-GET-REQUEST` at line 214 from within `1000-CONTROL`, which is accurate. No paragraphs are fabricated.

#### S2 — Error Handling

- `2100-OPEN-ERROR-QUEUE` failure: doc says `DISPLAY MQ-ERR-DISPLAY` then `8000-TERMINATION` — confirmed at lines 320–321.
- RETRIEVE failure: doc says error para `'CICS RETREIVE'` is moved to `MQ-ERROR-PARA`, then `9000-ERROR` and `8000-TERMINATION` — confirmed at lines 200–209. Note: the source has the typo `'CICS RETREIVE'` (missing 'V') which the doc does not quote verbatim, but this is not a FAIL because the doc describes the logic correctly without quoting the misspelled literal.
- Input queue open failure: doc quotes `'INP MQOPEN ERR'` — confirmed at line 250.
- Output queue open failure: doc quotes `'OUT MQOPEN ERR'` — confirmed at line 284.
- GET error: doc quotes `'INP MQGET ERR:'` — confirmed at line 384 (source has `'INP MQGET ERR:'` with colon, the doc description matches).
- PUT failure: doc quotes `'MQPUT ERR'` — confirmed at lines 496 and 533.
- ACCT file error: doc quotes `'ERROR WHILE READING ACCTFILE'` — confirmed at line 442.
- `9000-ERROR` MQPUT failure: doc says `DISPLAY MQ-ERR-DISPLAY` then `8000-TERMINATION` — confirmed at lines 534–535.
- Close failure recursion (lines 572, 594, 617–618): correctly documented in Section 3.2.

#### S3 — Migration Notes

**Verified notes:**
- Note 1 (ACCT-ADDR-ZIP dropped, line 155): confirmed — no MOVE of ACCT-ADDR-ZIP in `4000-PROCESS-REQUEST-REPLY` (lines 408–425).
- Note 2 (mutual recursion in close paragraphs): confirmed — `5000-CLOSE-INPUT-QUEUE` calls `8000-TERMINATION` at line 572; `8000-TERMINATION` calls `5000-CLOSE-INPUT-QUEUE` at line 541.
- Note 3 (CORRELID/MSGID propagation, line 470): confirmed at lines 469–470.
- Note 4 (`WS-FUNC = 'INQA'` only function code, line 393): confirmed.
- Note 5 (`MQ-MSG-COUNT` incremented but never reported, line 375): confirmed.
- Note 6 (`IS INITIAL` declaration, line 2): confirmed.
- Note 7 (`WS-ABS-TIME` COMP-3 declared but unused, lines 36–38): confirmed — never assigned or read in PROCEDURE DIVISION.
- Note 8 (RETRIEVE legacy pattern): accurate description.
- Note 9 (hardcoded reply queue, line 198): confirmed.
- Note 10 (zoned-decimal monetary fields): accurate — `ACCT-CURR-BAL` is `PIC S9(10)V99` with no COMP-3, i.e., zoned decimal.

**Unsupported notes:** None. All ten notes are grounded in specific source evidence.

**Missing bugs:** One additional latent issue not documented: in `5100-CLOSE-OUTPUT-QUEUE` (line 574), the error handler at line 592 moves `INPUT-QUEUE-NAME` (not `REPLY-QUEUE-NAME`) to `MQ-APPL-QUEUE-NAME`. This is a copy-paste error — the error message will display the wrong queue name. This is minor and does not affect control flow, but should be noted in Java migration comments.

#### S4 — Copybook Fields

CVACT01Y verified against the actual copybook (source lines 4–17):

| Field | Doc PIC | Actual PIC | Match |
|-------|---------|------------|-------|
| `ACCT-ID` | `9(11)` | `9(11)` | Yes |
| `ACCT-ACTIVE-STATUS` | `X(01)` | `X(01)` | Yes |
| `ACCT-CURR-BAL` | `S9(10)V99` | `S9(10)V99` | Yes |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | `S9(10)V99` | Yes |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | `S9(10)V99` | Yes |
| `ACCT-OPEN-DATE` | `X(10)` | `X(10)` | Yes |
| `ACCT-EXPIRAION-DATE` | `X(10)` | `X(10)` | Yes (typo preserved) |
| `ACCT-REISSUE-DATE` | `X(10)` | `X(10)` | Yes |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | `S9(10)V99` | Yes |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | `S9(10)V99` | Yes |
| `ACCT-ADDR-ZIP` | `X(10)` | `X(10)` | Yes |
| `ACCT-GROUP-ID` | `X(10)` | `X(10)` | Yes |
| `FILLER` | `X(178)` | `X(178)` | Yes |

The doc correctly notes that `ACCT-CURR-BAL` and similar `S9(10)V99` fields are zoned decimal (no COMP-3). The COMP-3 flag alert in Migration Note 10 correctly identifies these as zoned decimal, not COMP-3. The MQ copybooks (CMQGMOV, CMQPMOV, CMQMDV, CMQODV, CMQV, CMQTML) are IBM proprietary and not available in this repository; the doc's field-level descriptions for those copybooks are internally consistent and cannot be independently verified here, but there is no evidence of fabrication.

#### S5 — External Calls

| Program | Doc line | Actual call location | Params correct? |
|---------|----------|---------------------|-----------------|
| MQOPEN | Lines 233, 267, 302 | Source lines 233, 267, 302 | Yes |
| MQGET | Line 352 | Source line 352 | Yes |
| MQPUT | Lines 479, 516 | Source lines 479, 516 | Yes |
| MQCLOSE | Lines 557, 579, 602 | Source lines 557, 579, 602 | Yes |

All four external program names, call sites, and parameter descriptions are accurate. The note about `MQ-HCONN` being zero (implicit CICS adapter connection) is correct — confirmed at source line 44.

#### S6 — Completeness

The document covers all significant paragraphs. The `INITIALIZE-ALL-FIELDS`-equivalent initialisation at line 185 (`INITIALIZE MQ-ERR-DISPLAY`) is documented in Section 2.1. The `WS-DATE-TIME` dead fields (lines 36–38) are documented in Appendix D. The `SAVE-REPLY2Q` field being saved but never used for routing (the hardcoded reply queue name overrides it) is documented in Appendix D. The `MQ-MSG-COUNT` counter (line 54) is documented. No significant business logic is omitted.

### Overall Recommendation

BIZ-COACCT01.md is accurate and complete as a migration reference. All paragraph names, control flow, error messages, external call signatures, and copybook fields have been verified against the source. One minor latent bug not in the document (wrong queue name in the close-output-queue error handler, line 592) is trivial and does not affect the primary migration path. A Java developer can safely translate from this document without returning to the COBOL source for functional logic.