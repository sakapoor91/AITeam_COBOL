# Validation Report: BIZ-COPAUA0C.md

**Overall: PASS** — 6 passed · 0 failed · 2 warned · 2 skipped

Source file: `1026` lines   |   Document: `554` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COPAUA0C.cbl |
| `line_number_bounds` | ✓ **PASS** | 56 line reference(s) checked, all within bounds (max: 1026) |
| `backtick_identifiers` | ⚠ **WARN** | 9 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 14 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 2/10 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
9 backtick identifier(s) not found in source or copybooks

- `MQMD-CORRELID`
- `MQMD-REPLYTOQ`
- `MQPER-NOT-PERSISTENT`
- `MQRC-NO-MSG-AVAILABLE`
- `MQTM-QNAME`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> No FAILs. Three WARNs: `PA-AUTH-STATUS` / `PA-ACCOUNT-STATUS` described as "never set or checked" confirmed; `PA-CASH-BALANCE` unconditional zero correctly documented; `8500-INSERT-AUTH` IMS ISRT WHERE clause uses segment name `PAUTSUM0` not the field alias `ACCNTID` — minor doc imprecision. All core business logic accurately described.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | MAIN-PARA three-step flow, 1000-INITIALIZE trigger retrieve, 2000-MAIN-PROCESS loop structure, 5000-PROCESS-AUTH sub-chain, 6000-MAKE-DECISION credit calculation, 7100-SEND-RESPONSE MQPUT1, 8000-WRITE-AUTH-TO-DB IMS update — all accurately described with correct line references. |
| S2 Error Handling | ✓ PASS | All error location codes (M001–M005, A001–A003, C001–C003, I001–I004) and their criticality levels correctly documented. 9500-LOG-ERROR pattern, 9990-END-ROUTINE call chain, and CSSL TDQ write all verified. |
| S3 Migration Notes | ⚠ WARN | All 10 notes verified; one inaccuracy in Note 3 (inverted key description accurate but the WHERE clause field name used in ISRT is `ACCNTID`, not the doc's description of "segment `PAUTSUM0`"). This is minor. |
| S4 Copybook Fields | ⚠ WARN | `PA-AUTH-STATUS` and `PA-ACCOUNT-STATUS` in CIPAUSMY are described as "never set or checked by this program" — confirmed: no reference to either field in COPAUA0C's PROCEDURE DIVISION. All COMP-3 annotations verified. |
| S5 External Calls | ✓ PASS | MQOPEN (1100), MQGET (3100), MQPUT1 (7100), MQCLOSE (9100), IMS DLI SCHD/GU/REPL/ISRT/TERM, CICS READ, CICS SYNCPOINT, CICS WRITEQ TD — all correctly documented with correct paragraph locations. |
| S6 Completeness | ⚠ WARN | `5600-READ-PROFILE-DATA` stub correctly flagged. `PA-CASH-BALANCE` zeroed unconditionally on every approval (line 818) correctly flagged as latent bug. Decline reason codes `CARD-NOT-ACTIVE` (4200) and `ACCOUNT-CLOSED` (4300) unreachable paths correctly documented. No significant omissions. |

### Findings

#### S1 — Program Flow

**MAIN-PARA** (line 220): `PERFORM 1000-INITIALIZE THRU 1000-EXIT` → `PERFORM 2000-MAIN-PROCESS THRU 2000-EXIT` → `PERFORM 9000-TERMINATE THRU 9000-EXIT` → `EXEC CICS RETURN` — confirmed exactly at lines 222–226. Doc correctly describes this three-step sequence.

**1000-INITIALIZE** (line 230): `EXEC CICS RETRIEVE INTO(MQTM) NOHANDLE` at line 233; `IF EIBRESP = DFHRESP(NORMAL)` at line 237; `MOVE MQTM-QNAME TO WS-REQUEST-QNAME` at line 238; `MOVE MQTM-TRIGGERDATA TO WS-TRIGGER-DATA` at line 239; `MOVE 5000 TO WS-WAIT-INTERVAL` at line 242; `PERFORM 1100-OPEN-REQUEST-QUEUE` at line 244; `PERFORM 3100-READ-REQUEST-MQ` at line 246 — all confirmed. Doc correctly identifies the priming read of the first MQ message occurs inside `1000-INITIALIZE`, not the main loop.

**2000-MAIN-PROCESS loop** (line 323): `PERFORM UNTIL NO-MORE-MSG-AVAILABLE OR WS-LOOP-END` at line 326. Loop body:
1. `PERFORM 2100-EXTRACT-REQUEST-MSG` (line 328)
2. `PERFORM 5000-PROCESS-AUTH` (line 330)
3. `ADD 1 TO WS-MSG-PROCESSED` (line 332)
4. `EXEC CICS SYNCPOINT` (lines 334–336)
5. `SET IMS-PSB-NOT-SCHD TO TRUE` (line 337)
6. `IF WS-MSG-PROCESSED > WS-REQSTS-PROCESS-LIMIT` → `SET WS-LOOP-END TO TRUE` (lines 339–340), ELSE `PERFORM 3100-READ-REQUEST-MQ` (line 342)

All confirmed. The doc correctly describes the loop as: extract → process → SYNCPOINT → release IMS → check limit or read next.

**5000-PROCESS-AUTH** (line 438): `SET APPROVE-AUTH TO TRUE` at line 441; `PERFORM 1200-SCHEDULE-PSB` at line 443; `SET CARD-FOUND-XREF / FOUND-ACCT-IN-MSTR TO TRUE` at lines 445–446; conditional reads at lines 448–456; `PERFORM 6000-MAKE-DECISION` at line 459; `PERFORM 7100-SEND-RESPONSE` at line 461; `IF CARD-FOUND-XREF PERFORM 8000-WRITE-AUTH-TO-DB` at lines 463–464 — all confirmed. Doc accurately describes that IMS writes are skipped if the card was not found in XREF.

**6000-MAKE-DECISION credit calculation** (lines 657–731): Both branches confirmed:
- `IF FOUND-PAUT-SMRY-SEG`: `COMPUTE WS-AVAILABLE-AMT = PA-CREDIT-LIMIT - PA-CREDIT-BALANCE` (lines 666–667); `IF WS-TRANSACTION-AMT > WS-AVAILABLE-AMT` → `SET DECLINE-AUTH / INSUFFICIENT-FUND TO TRUE` (lines 668–670).
- Else if `FOUND-ACCT-IN-MSTR`: same pattern using `ACCT-CREDIT-LIMIT - ACCT-CURR-BAL` (lines 674–678).
- Else: `SET DECLINE-AUTH TO TRUE` with no reason flag (line 681).
- Response code assignment: `'05'` / `'00'` (lines 688 / 693) — confirmed.
- `MOVE '0000' TO PA-RL-AUTH-RESP-REASON` before EVALUATE (line 698) — confirmed; approved transactions carry `'0000'`.

**7100-SEND-RESPONSE** (line 738): Uses `MQPUT1` (line 758), not MQOPEN+MQPUT — confirmed. `MQPER-NOT-PERSISTENT` at line 749; `MOVE 50 TO MQMD-EXPIRY` at line 750; `MOVE WS-SAVE-CORRELID TO MQMD-CORRELID` at line 745; `MQPMO-OPTIONS = MQPMO-NO-SYNCPOINT + MQPMO-DEFAULT-CONTEXT` at lines 753–754 — all confirmed. The doc correctly states `MQPUT1` is a single call (open+put+close in one API call).

**8400-UPDATE-SUMMARY** (line 798): `IF NFOUND-PAUT-SMRY-SEG` → `INITIALIZE PENDING-AUTH-SUMMARY` → set account/customer IDs (lines 801–808); `MOVE ACCT-CREDIT-LIMIT TO PA-CREDIT-LIMIT` and `MOVE ACCT-CASH-CREDIT-LIMIT TO PA-CASH-LIMIT` at lines 810–811 (unconditional refresh); approved path at lines 813–818; declined path at lines 820–822; IMS REPL if `FOUND-PAUT-SMRY-SEG` else ISRT (lines 824–834) — all confirmed.

**8500-INSERT-AUTH** (line 854): `EXEC CICS ASKTIME` at line 857; `EXEC CICS FORMATTIME YYDDD(WS-CUR-DATE-X6) TIME(WS-CUR-TIME-X6) MILLISECONDS(WS-CUR-TIME-MS)` at lines 861–866; `MOVE WS-CUR-DATE-X6(1:5) TO WS-YYDDD` at line 868; `COMPUTE WS-TIME-WITH-MS = (WS-CUR-TIME-N6 * 1000) + WS-CUR-TIME-MS` at lines 871–872; inverted key computations at lines 874–875; IMS ISRT with `WHERE (ACCNTID = PA-ACCT-ID)` at lines 913–915 — all confirmed.

**Minor doc imprecision in S1/S3:** The doc says the ISRT at 8500 targets the detail segment "as a child of the summary segment." The WHERE clause at source line 915 uses `ACCNTID` as the parent key field name, which is the IMS field alias for the `PAUTSUM0` root segment key. The doc's description is functionally correct but does not mention the `ACCNTID` field alias — a migration developer would need to know this alias when reproducing the hierarchical insert logic.

#### S2 — Error Handling

All error location codes and criticality verified:

| Code | Paragraph | Level | Source Line |
|------|-----------|-------|-------------|
| M001 | 1100-OPEN-REQUEST-QUEUE | Critical | 273 |
| M004 | 7100-SEND-RESPONSE | Critical | 768 |
| M005 | 9100-CLOSE-REQUEST-QUEUE | Warning | 966 |
| A001 | 5100-READ-XREF-RECORD (NOTFND) | Warning | 494 |
| C001 | 5100-READ-XREF-RECORD (OTHER) | Critical | 502 |
| I001 | 1200-SCHEDULE-PSB | Critical | 311 |
| I003 | 8400-UPDATE-SUMMARY | Critical | 840 |

M005 error level: `SET ERR-WARNING TO TRUE` at line 967 — confirmed not critical, does not call 9990-END-ROUTINE.

**9500-LOG-ERROR** (line 983): `EXEC CICS ASKTIME` at line 986; `EXEC CICS FORMATTIME YYMMDD / TIME` at lines 990–993; `MOVE WS-CICS-TRANID TO ERR-APPLICATION` at line 996; `MOVE WS-PGM-AUTH TO ERR-PROGRAM` at line 997; `EXEC CICS WRITEQ TD QUEUE('CSSL')` at lines 1001–1006; `IF ERR-CRITICAL PERFORM 9990-END-ROUTINE` at lines 1008–1010 — all confirmed.

**9990-END-ROUTINE** (line 1016): `PERFORM 9000-TERMINATE` at line 1019; `EXEC CICS RETURN` at lines 1021–1022 — confirmed. Doc correctly describes this as the emergency exit path.

**M003 error code:** The doc mentions `M003` at section 3.3 as "MQGET failure." This was verified against source in the earlier session — the MQGET failure path in `3100-READ-REQUEST-MQ` sets `M003`. Not re-read in this pass but consistent with the documented pattern.

#### S3 — Migration Notes

**All 10 notes verified:**

1. `5600-READ-PROFILE-DATA` stub — confirmed at lines 647–651: `CONTINUE` only.
2. `PA-CASH-BALANCE` unconditionally zeroed on approval — confirmed at line 818: `MOVE 0 TO PA-CASH-BALANCE` inside the `IF AUTH-RESP-APPROVED` block with no cash-advance check.
3. Inverted IMS key scheme — confirmed at lines 874–875: `COMPUTE PA-AUTH-DATE-9C = 99999 - WS-YYDDD` and `COMPUTE PA-AUTH-TIME-9C = 999999999 - WS-TIME-WITH-MS`.
4. Hardcoded 500-message limit — confirmed: `WS-REQSTS-PROCESS-LIMIT PIC S9(4) COMP VALUE 500` at line 40; check at line 339.
5. `WS-AVAILABLE-AMT` COMP-3 — confirmed at line 62: `PIC S9(09)V99 COMP-3`.
6. Non-persistent reply with 50-unit expiry — confirmed at lines 749–750.
7. IBM MQ system copybooks not in repository — confirmed: CMQODV, CMQMDV, CMQV, CMQTML, CMQPMOV, CMQGMOV all appear as COPY statements without .cpy files in the source tree.
8. Error location codes as only diagnostic identifiers — confirmed: no structured error log with account/request key other than `ERR-EVENT-KEY` (X(20)).
9. `PA-RQ-MERCHANT-CATAGORY-CODE` misspelling — confirmed at source line 365 (UNSTRING target) and line 887 (MOVE to IMS field); the typo propagates from copybook into IMS segment.
10. No card active-status or account closed-status check — confirmed: the EVALUATE at lines 700–717 lists `WHEN CARD-NOT-ACTIVE` and `WHEN ACCOUNT-CLOSED` but no code in `5000-PROCESS-AUTH` or any sub-paragraph sets `WS-DECLINE-REASON-FLG` (or equivalent 88-level) to these values.

**Unsupported notes:** None.

**Missing notes:** None significant beyond what is documented.

#### S4 — Copybook Fields

**CIPAUSMY** (`PENDING-AUTH-SUMMARY`): `PA-AUTH-STATUS X(01)` and `PA-ACCOUNT-STATUS X(02) OCCURS 5 TIMES` are documented as "never set or checked by this program." Grep of COPAUA0C.cbl confirms zero references to either field name in the PROCEDURE DIVISION. The doc correctly flags these as dead fields in the context of COPAUA0C (they may be used by other programs in the authorization suite). All COMP-3 annotations for `PA-ACCT-ID`, `PA-CREDIT-LIMIT`, `PA-CASH-LIMIT`, `PA-CREDIT-BALANCE`, `PA-CASH-BALANCE`, `PA-APPROVED-AUTH-AMT`, `PA-DECLINED-AUTH-AMT` — all confirmed as `S9(...)V99 COMP-3` at the copybook level (consistent with migration note 5).

**CCPAURQY** fields: 18-field UNSTRING at lines 354–374 matches the Appendix B table order exactly. `WS-TRANSACTION-AMT-AN` receives the alphanumeric amount, which is then converted via NUMVAL — doc correctly notes this at the `PA-RQ-TRANSACTION-AMT` row.

**CCPAUERY** (`ERROR-LOG-RECORD`): `ERR-APPLICATION` at line 996 receives `WS-CICS-TRANID` (`'CP00'`), not `WS-PGM-AUTH` — doc says ERR-APPLICATION is set to `WS-CICS-TRANID ('CP00')`, confirmed. `ERR-PROGRAM` receives `WS-PGM-AUTH` (`'COPAUA0C'`) at line 997 — confirmed.

#### S5 — External Calls

All external calls verified:
- `MQOPEN` — `1100-OPEN-REQUEST-QUEUE` (line 262): `CALL 'MQOPEN' USING W01-HCONN-REQUEST MQM-OD-REQUEST WS-OPTIONS W01-HOBJ-REQUEST WS-COMPCODE WS-REASON` — confirmed.
- `MQGET` — `3100-READ-REQUEST-MQ`: verified in prior session.
- `MQPUT1` — `7100-SEND-RESPONSE` (line 758): `CALL 'MQPUT1' USING W02-HCONN-REPLY MQM-OD-REPLY MQM-MD-REPLY MQM-PUT-MESSAGE-OPTIONS W02-BUFFLEN W02-PUT-BUFFER WS-COMPCODE WS-REASON` — confirmed.
- `MQCLOSE` — `9100-CLOSE-REQUEST-QUEUE` (line 956): `CALL 'MQCLOSE' USING W01-HCONN-REQUEST W01-HOBJ-REQUEST MQCO-NONE WS-COMPCODE WS-REASON` — confirmed.
- `EXEC DLI SCHD PSB((PSB-NAME)) NODHABEND` — `1200-SCHEDULE-PSB` (line 293) — confirmed.
- `EXEC DLI GU` — `5500-READ-AUTH-SUMMRY`: verified in prior session.
- `EXEC DLI REPL / ISRT SEGMENT(PAUTSUM0)` — `8400-UPDATE-SUMMARY` (lines 825–833) — confirmed.
- `EXEC DLI ISRT SEGMENT(PAUTSUM0) WHERE (ACCNTID=...) SEGMENT(PAUTDTL1)` — `8500-INSERT-AUTH` (lines 913–918) — confirmed hierarchical insert.
- `EXEC DLI TERM` — `9000-TERMINATE` (line 944): `IF IMS-PSB-SCHD EXEC DLI TERM END-EXEC` — confirmed.
- `EXEC CICS SYNCPOINT` — `2000-MAIN-PROCESS` (lines 334–336) — confirmed.
- `EXEC CICS RETURN` (no TRANSID/COMMAREA) — MAIN-PARA (line 226) and 9990-END-ROUTINE (line 1021) — confirmed. Doc correctly notes the program does not re-schedule itself.

No CICS XCTL statements in COPAUA0C. No traditional batch CALL statements other than MQ API calls. Confirmed.

#### S6 — Completeness

All 11 named paragraphs covered: `MAIN-PARA`, `1000-INITIALIZE`, `1100-OPEN-REQUEST-QUEUE`, `1200-SCHEDULE-PSB`, `2000-MAIN-PROCESS`, `2100-EXTRACT-REQUEST-MSG`, `3100-READ-REQUEST-MQ`, `5000-PROCESS-AUTH`, `5100-READ-XREF-RECORD`, `5200-READ-ACCT-RECORD`, `5300-READ-CUST-RECORD`, `5500-READ-AUTH-SUMMRY`, `5600-READ-PROFILE-DATA`, `6000-MAKE-DECISION`, `7100-SEND-RESPONSE`, `8000-WRITE-AUTH-TO-DB`, `8400-UPDATE-SUMMARY`, `8500-INSERT-AUTH`, `9000-TERMINATE`, `9100-CLOSE-REQUEST-QUEUE`, `9500-LOG-ERROR`, `9990-END-ROUTINE` — all documented.

The `5000-PROCESS-AUTH` startup default `SET CARD-FOUND-XREF / FOUND-ACCT-IN-MSTR TO TRUE` at lines 445–446 before the read is an important pattern: if the XREF read sets `CARD-NFOUND-XREF` but `FOUND-ACCT-IN-MSTR` was pre-initialized to true, the subsequent `IF CARD-FOUND-XREF` guard at line 450 prevents the account read. However, the `FOUND-ACCT-IN-MSTR` flag retains its TRUE default — the 6000-MAKE-DECISION `IF FOUND-ACCT-IN-MSTR` branch would be reached with a stale flag if `5100-READ-XREF-RECORD` does not explicitly reset it. The source confirms `5100-READ-XREF-RECORD` at line 492 sets `NFOUND-ACCT-IN-MSTR TO TRUE` when NOTFND, which correctly resets the flag. The doc does not explicitly describe this flag initialization/reset pattern — it is a subtle ordering dependency worth noting but not a blocking migration concern.

`PA-AUTH-STATUS` and `PA-ACCOUNT-STATUS` absence (described above under S4) — correctly flagged in Appendix B.

### Overall Recommendation
BIZ-COPAUA0C.md is safe to use as a migration reference. The MQ trigger pattern, CICS+IMS+MQ hybrid architecture, authorization decision logic, inverted IMS key scheme, and error routing are all accurately described with correct line references. The three documented defects — `PA-CASH-BALANCE` unconditional zero, `CARD-NOT-ACTIVE`/`ACCOUNT-CLOSED` unreachable decline paths, and `5600-READ-PROFILE-DATA` stub — are all correctly identified and a Java developer is explicitly warned of them. The COMP-3 BigDecimal requirements for all monetary and count fields are correctly flagged. The document is reliable for migration use.