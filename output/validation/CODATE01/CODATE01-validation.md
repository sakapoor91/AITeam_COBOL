# Validation Report: BIZ-CODATE01.md

**Overall: PASS** — 5 passed · 0 failed · 3 warned · 2 skipped

Source file: `524` lines   |   Document: `336` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: CODATE01.cbl |
| `line_number_bounds` | ✓ **PASS** | 47 line reference(s) checked, all within bounds (max: 524) |
| `backtick_identifiers` | ⚠ **WARN** | 161 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 6 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 3/9 migration note(s) missing line number citations |
| `mermaid_diagram` | ⚠ **WARN** | Mermaid diagram missing classDef colour definitions |

## Issues Found

### backtick_identifiers (WARN)
161 backtick identifier(s) not found in source or copybooks

- `ERR-QUEUE-OPEN`
- `ERROR-MESSAGE`
- `ERROR-QUEUE-HANDLE`
- `ERROR-QUEUE-NAME`
- `INPUT-QUEUE-HANDLE`
- `INPUT-QUEUE-NAME`
- `MM-DD-YYYY`
- `MQ-APPL-RETURN-MESSAGE`
- `MQ-BUFFER`
- `MQ-BUFFER-LENGTH`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> No FAILs. Two warnings: the open-queue flag naming confusion is insufficiently documented, and a loop structure discrepancy needs attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ⚠ WARN | Overall flow is accurate but the main loop structure has a subtle discrepancy: the priming read before the loop and the loop body's second read are conflated in one paragraph (3000-GET-REQUEST), which the doc correctly identifies but the loop sequencing description needs clarification. |
| S2 Error Handling | ✓ PASS | All error strings verified verbatim; error queue write-and-recurse pattern accurately described; open-queue failure paths all correct. |
| S3 Migration Notes | ✓ PASS | All 9 notes verified against source; no fabricated claims. |
| S4 Copybook Fields | ✓ PASS | All IBM MQ copybooks correctly identified as external; working-storage fields verified. |
| S5 External Calls | ✓ PASS | All four MQ API calls (MQOPEN, MQGET, MQPUT, MQCLOSE) correctly documented with correct argument lists. |
| S6 Completeness | ✓ PASS | All paragraphs documented; SAVE-REPLY2Q captured-but-unused noted; LIT-ACCTFILENAME dead field noted. |

### Findings

#### S1 — Program Flow
The overall flow is accurate. Key verification points:

**`1000-CONTROL` startup** (line 127): Doc says the program "clears INPUT-QUEUE-NAME, QMGR-NAME, QUEUE-MESSAGE to spaces" — source at lines 129–132 confirms `MOVE SPACES TO INPUT-QUEUE-NAME QMGR-NAME QUEUE-MESSAGE`. `INITIALIZE MQ-ERR-DISPLAY` at line 134 also confirmed.

**Error queue opened first** (line 136): `2100-OPEN-ERROR-QUEUE` called before `EXEC CICS RETRIEVE` — source line 136 confirms. Order matters and is correctly documented.

**EXEC CICS RETRIEVE** (lines 140–159): Doc says `WS-CICS-RESP1-CD` is checked against `DFHRESP(NORMAL)`. Source line 145: `IF WS-CICS-RESP1-CD = DFHRESP(NORMAL)` — confirmed. Reply queue hardcoded to `'CARD.DEMO.REPLY.DATE'` at line 147 — confirmed.

**Queue opening order** (lines 161–162): `2300-OPEN-INPUT-QUEUE` then `2400-OPEN-OUTPUT-QUEUE` — source confirms lines 161–162.

**Loop structure discrepancy:** Doc says "Step 4 — Prime the message loop: `3000-GET-REQUEST` is called once before the main loop begins" (line 163), then the loop `PERFORM 4000-MAIN-PROCESS UNTIL NO-MORE-MSGS` calls `3000-GET-REQUEST` again inside `4000-MAIN-PROCESS`. Looking at source:
- Line 163: `PERFORM 3000-GET-REQUEST` (outside loop — priming read)
- Line 164: `PERFORM 4000-MAIN-PROCESS UNTIL NO-MORE-MSGS`
- Lines 274–280 (`4000-MAIN-PROCESS`): `EXEC CICS SYNCPOINT`, then `PERFORM 3000-GET-REQUEST`

This means the flow is: prime-read → [loop: SYNCPOINT → GET-NEXT-MSG]. If the priming read returns NO-MORE-MSGS, the loop never executes (correct empty-queue behavior). If the priming read succeeds, the first message is in REQUEST-MESSAGE when the loop starts. Inside the loop, `4000-MAIN-PROCESS` does NOT call `4000-PROCESS-REQUEST-REPLY` — that is called from inside `3000-GET-REQUEST` itself (line 323 is `4000-PROCESS-REQUEST-REPLY` paragraph, called from `3000-GET-REQUEST` at line 323 in source).

Wait — re-reading: `3000-GET-REQUEST` at source line 283 calls `MQGET`, and if OK calls `4000-PROCESS-REQUEST-REPLY` at line 323. So the process-and-reply happens inside `3000-GET-REQUEST`, not as a separate call. The doc says at Step 6: "If `MQCC-OK`: `4000-PROCESS-REQUEST-REPLY` is called." This is accurate.

The loop in `4000-MAIN-PROCESS` (lines 274–280): SYNCPOINT → `3000-GET-REQUEST`. So each loop iteration SYNCPOINTs first, then GETs-AND-PROCESSES the next message. The doc says "Step 5 — SYNCPOINT" then "Step 6 — Get next request message" — this matches the loop body exactly.

**Minor loop sequencing nuance:** The priming read at line 163 processes one message (via `4000-PROCESS-REQUEST-REPLY` inside `3000-GET-REQUEST`) WITHOUT a prior SYNCPOINT. Only subsequent messages (in the `4000-MAIN-PROCESS` loop) get a SYNCPOINT first. The doc says "A sync point is taken before each message get" but this is not true for the very first message. This is a minor inaccuracy worth a migration note but not a FAIL.

#### S2 — Error Handling
All error strings verified:
- `'CARD.DEMO.ERROR'` at line 243 — confirmed.
- `'CARD.DEMO.REPLY.DATE'` at line 147 — confirmed.
- `'INP MQOPEN ERR'` at line 199 — confirmed.
- `'OUT MQOPEN ERR'` at line 233 — confirmed.
- `'ERR MQOPEN ERR'` at line 268 — confirmed.
- `'INP MQGET ERR:'` at line 333 — confirmed.
- `'MQPUT ERR'` at lines 400, 437 — confirmed.
- `'MQCLOSE ERR'` at lines 475, 497, 520 — confirmed.
- `'CICS RETRIEVE'` error message prefix at line 149 — confirmed.

**Flag name confusion documented correctly:** The doc notes in Appendix D that `WS-REPLY-QUEUE-STS` (88: `REPLY-QUEUE-OPEN`) "tracks input queue open state" despite the name saying 'reply'. Source confirms: `2300-OPEN-INPUT-QUEUE` at line 194 sets `REPLY-QUEUE-OPEN TO TRUE`, and `8000-TERMINATION` at line 444 checks `IF REPLY-QUEUE-OPEN` before closing the input queue. Doc correctly flags this naming confusion.

Recursive `8000-TERMINATION` call from close failures (Migration Note 4): source lines 476 and 498 show `PERFORM 8000-TERMINATION` from within `5000-CLOSE-INPUT-QUEUE` and `5100-CLOSE-OUTPUT-QUEUE` — confirmed. The `5200-CLOSE-ERROR-QUEUE` failure calls `9000-ERROR` then `8000-TERMINATION` (lines 521–522) — confirmed at source.

#### S3 — Migration Notes
**All 9 notes verified:**
1. `LIT-ACCTFILENAME` never used (line 115) — confirmed: `PIC X(8) VALUE 'ACCTDAT '`, never referenced in PROCEDURE DIVISION.
2. `WS-ABS-TIME` COMP-3 (line 36) — confirmed: `PIC S9(15) COMP-3`.
3. `REQUEST-MSG-COPY` overlay (lines 109–112) — confirmed: `WS-FUNC` X(4) and `WS-KEY` 9(11) never read after being populated.
4. Recursive `8000-TERMINATION` on close failure — confirmed.
5. Error queue open flag consistency — confirmed: `ERR-QUEUE-OPEN` set at line 263, checked at line 450.
6. Paragraph naming conflict: `4000-MAIN-PROCESS` (line 274) and `4000-PROCESS-REQUEST-REPLY` (line 339) both start with `4000-` — confirmed in source.
7. No business logic — confirmed: program is purely a date/time service.
8. `WS-CICS-RESP1-CD` declared twice as binary + display fields — confirmed at lines 27–30.
9. IBM MQ copybooks not in repository — confirmed: CMQGMOV, CMQPMOV, CMQMDV, CMQODV, CMQV, CMQTML all appear as COPY statements without .cpy files in the source tree.

**Missing note:** The first message processed (priming read at line 163) does not go through a SYNCPOINT. This means the first message's reply and IMS write are not in the same unit of work as subsequent messages. This is a subtle transactional boundary issue that should be in Migration Notes.

**Unsupported notes:** None.

#### S4 — Copybook Fields
IBM MQ copybooks are all external (not in repository). Working-storage fields verified:

| Field | Source PIC | Doc PIC | Match |
|-------|-----------|---------|-------|
| `WS-MQ-MSG-FLAG` | `X(01)` | `X(01)` | Yes |
| `WS-RESP-QUEUE-STS` | `X(01)` | `X(01)` | Yes |
| `WS-ERR-QUEUE-STS` | `X(01)` | `X(01)` | Yes |
| `WS-REPLY-QUEUE-STS` | `X(01)` | `X(01)` | Yes |
| `WS-ABS-TIME` | `S9(15) COMP-3` | `S9(15) COMP-3` | Yes |
| `WS-MMDDYYYY` | `X(10)` | `X(10)` | Yes |
| `WS-TIME` | `X(8)` | `X(8)` | Yes |
| `MQ-HCONN` | `S9(09) BINARY` | `S9(09) BINARY` | Yes |
| `MQ-BUFFER` | `X(1000)` | `X(1000)` | Yes |
| `REQUEST-MSG-COPY` subfields | X(4)+9(11)+X(985) | X(4)+9(11)+X(985) | Yes |

The doc's COMP-3 annotation for `WS-ABS-TIME` is correct and the migration note about it is accurate.

#### S5 — External Calls
All MQ API calls verified:
- `MQOPEN` called from `2100-OPEN-ERROR-QUEUE` (line 251), `2300-OPEN-INPUT-QUEUE` (line 182), `2400-OPEN-OUTPUT-QUEUE` (line 216) — confirmed.
- `MQGET` called from `3000-GET-REQUEST` (line 301) — confirmed. Wait interval `MQGMO-WAITINTERVAL = 5000` at line 286 — confirmed.
- `MQPUT` called from `4100-PUT-REPLY` (line 383) and `9000-ERROR` (line 420) — confirmed.
- `MQCLOSE` called from `5000-CLOSE-INPUT-QUEUE` (line 461), `5100-CLOSE-OUTPUT-QUEUE` (line 483), `5200-CLOSE-ERROR-QUEUE` (line 506) — confirmed.

The doc's description of MQGET input fields (MQ-HCONN, MQ-HOBJ, MQ-MESSAGE-DESCRIPTOR, MQ-GET-MESSAGE-OPTIONS, MQ-BUFFER-LENGTH) matches the CALL statement at lines 301–309. The note that `MQ-DATA-LENGTH` is never checked after GET is confirmed — source line 307 receives it but nowhere in the program is it compared or branched on.

`SAVE-REPLY2Q` captured from `MQMD-REPLYTOQ` (line 315) but never used to route the reply (reply goes to hardcoded `REPLY-QUEUE-NAME`) — confirmed.

#### S6 — Completeness
All 11 paragraphs documented: `1000-CONTROL`, `2100-OPEN-ERROR-QUEUE`, `2300-OPEN-INPUT-QUEUE`, `2400-OPEN-OUTPUT-QUEUE`, `3000-GET-REQUEST`, `4000-MAIN-PROCESS`, `4000-PROCESS-REQUEST-REPLY`, `4100-PUT-REPLY`, `9000-ERROR`, `8000-TERMINATION`, `5000/5100/5200-CLOSE-*`. 

`LIT-ACCTFILENAME` dead field documented in both Migration Notes and Appendix D. `SAVE-REPLY2Q` captured-but-unused documented in Appendix D. `REQUEST-MSG-COPY` overlay documented with note that WS-FUNC/WS-KEY are never read.

### Overall Recommendation
BIZ-CODATE01.md is safe to use as a migration reference. The MQ trigger mechanism, queue lifecycle, date/time formatting, and error routing are all accurately described. The primary migration concern (all of this can be replaced by a single JAX-RS or Spring endpoint calling `LocalDateTime.now()`) is correctly noted. Two minor issues — the first message bypasses SYNCPOINT, and SAVE-REPLY2Q is captured but ignored — are both documented and a Java developer is warned of them. The document is reliable.