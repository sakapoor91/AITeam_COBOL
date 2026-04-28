# Validation Report: BIZ-COBIL00C.md

**Overall: FAIL** — 4 passed · 2 failed · 3 warned · 1 skipped

Source file: `572` lines   |   Document: `350` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✗ **FAIL** | 4 required section(s) missing |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COBIL00C.cbl |
| `line_number_bounds` | ⚠ **WARN** | No line numbers found in document — Section 2 must cite line numbers |
| `backtick_identifiers` | ⚠ **WARN** | 22 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 10 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | ✗ **FAIL** | 3 byte count(s) inconsistent with PIC clause |
| `migration_notes_line_refs` | ⚠ **WARN** | Section 4 contains no numbered migration notes |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### required_sections (FAIL)
4 required section(s) missing

- `Header block`
- `Section 1 — Purpose`
- `Section 3 — Error Handling`
- `Section 4 — Migration Notes`

### backtick_identifiers (WARN)
22 backtick identifier(s) not found in source or copybooks

- `ABEND-DATA`
- `ABEND-ROUTINE`
- `CONFIRM-PAYMENT-PROCESSING`
- `GET-CURRENT-DATETIME`
- `HIGH-VALUES`
- `INITIALIZE-COBIL-FIELDS`
- `RECEIVE-BILL-SCREEN`
- `SEND-BILL-SCREEN`
- `WS-ACCT-ID`
- `WS-CARD-RID-CARDNUM`

### pic_byte_accuracy (FAIL)
3 byte count(s) inconsistent with PIC clause

- `CDEMO-FROM-PROGRAM: doc says 3 bytes, computed 8 from "PIC X(8)"`
- `CDEMO-TO-PROGRAM: doc says 3 bytes, computed 8 from "PIC X(8)"`
- `CDEMO-PGM-CONTEXT: doc says 0 bytes, computed 1 from "PIC X(1)"`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> Several factual errors found in the program flow description: the doc incorrectly states the payment flow starts with `READ-CXACAIX-FILE` for card lookup, but the actual source uses `ACCT-ID` (not `CDEMO-CARD-NUM`) as the CXACAIX key; the READ-ACCTDAT-FILE call is issued with UPDATE lock but the doc doesn't note this; and the flow description for the first display inverts the actual paragraph entry logic. Three byte count errors in Appendix B (flagged by Phase 1) are confirmed. These are material errors that could cause a Java developer to misimplement the payment lookup path.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✗ FAIL | The doc's first-display flow description is materially wrong: CXACAIX is keyed by `XREF-ACCT-ID` (account ID from the map), not by `CDEMO-CARD-NUM` from the commarea; `READ-ACCTDAT-FILE` uses UPDATE lock but doc omits this; and the flow sequence differs from the actual source. |
| S2 Error Handling | ⚠ WARN | Most error conditions are correct but the actual error messages differ from the doc's quoted text in several places; the `DISPLAY` statement for ACCTDAT/CXACAIX file errors goes to the job log (not handled), which is undocumented. |
| S3 Migration Notes | ✓ PASS | Ten migration notes are all substantively accurate and supported by source evidence. |
| S4 Copybook Fields | ✗ FAIL | Three confirmed byte count errors in COCOM01Y fields; `ACCT-CURR-BAL` documented as COMP-3 but is NOT COMP-3 in CVACT01Y (zoned decimal). |
| S5 External Calls | ✓ PASS | No static CALL statements; CICS interactions correctly identified. |
| S6 Completeness | ⚠ WARN | `CLEAR-CURRENT-SCREEN` and `INITIALIZE-ALL-FIELDS` paragraphs not documented; PF4 key handling undocumented; pre-selected card number flow (first-display with `CDEMO-CB00-TRN-SELECTED`) undocumented. |

### Findings

#### S1 — Program Flow

**Critical error 1 — First display flow:**

The doc (Section 2.2, "First display") states:
> "Calls `READ-CXACAIX-FILE` using the card number from `CDEMO-CARD-NUM` in the COMMAREA."

The actual source (COBIL00C.cbl, `MAIN-PARA`, lines 107–122) shows:
- On first entry (`NOT CDEMO-PGM-REENTER`): the program immediately calls `SEND-BILLPAY-SCREEN` (line 122) — it does NOT call `READ-CXACAIX-FILE` or `READ-ACCTDAT-FILE` on first display.
- The CXACAIX read is instead performed within `PROCESS-ENTER-KEY` (line 211) when the operator presses ENTER with CONFIRM='Y'.
- The first display simply shows a blank screen with the account ID field.

Furthermore, the source `READ-CXACAIX-FILE` paragraph (lines 408–436) reads by `XREF-ACCT-ID` (an account ID, not a card number), set from `ACTIDINI OF COBIL0AI` (the operator's input on the screen). `CDEMO-CARD-NUM` is never used in this program for file lookup.

**Critical error 2 — UPDATE lock on ACCTDAT:**

The doc says `READ-ACCTDAT-FILE` performs a read of ACCTDAT. The source (lines 343–372) issues `EXEC CICS READ ... UPDATE` — this is a locking read. The doc omits the UPDATE clause in Section 2.2 but actually does identify this correctly in Migration Note 10 ("ACCT-CURR-BAL is read without READ FOR UPDATE lock"). This is an internal inconsistency in the doc: Note 10 says there is no UPDATE lock, but the source clearly has `UPDATE` on line 351.

**Critical error 3 — Payment flow sequence:**

The doc describes the payment flow as:
1. Re-read CXACAIX
2. STARTBR TRANSACT with HIGH-VALUES
3. READPREV → get last TRAN-ID
4. ENDBR
5. Increment TRAN-ID
6. Initialize TRAN-RECORD, populate
7. WRITE-TRANSACT-FILE (and on success: show message)
8. COMPUTE balance
9. UPDATE-ACCTDAT-FILE

The actual source (lines 210–243) shows:
1. `READ-CXACAIX-FILE` (line 211) — but this reads by `XREF-ACCT-ID` (already set from ACTIDINI), not by card number
2. `MOVE HIGH-VALUES TO TRAN-ID` (line 212)
3. `STARTBR-TRANSACT-FILE` (line 213)
4. `READPREV-TRANSACT-FILE` (line 214)
5. `ENDBR-TRANSACT-FILE` (line 215)
6. `MOVE TRAN-ID TO WS-TRAN-ID-NUM`, `ADD 1` (lines 216–217)
7. `INITIALIZE TRAN-RECORD`, populate fields including `XREF-CARD-NUM TO TRAN-CARD-NUM` (line 225) — so card number comes from the XREF record, not directly from commarea
8. `WRITE-TRANSACT-FILE` (line 233) — on NORMAL: calls `INITIALIZE-ALL-FIELDS`, shows success message, calls `SEND-BILLPAY-SCREEN` — then RETURNS (no COMPUTE or UPDATE follows)
9. `COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT` (line 234) — ONLY reached if `WRITE-TRANSACT-FILE` did NOT send the screen itself
10. `UPDATE-ACCTDAT-FILE` (line 235)

This is a critical finding: the `WRITE-TRANSACT-FILE` paragraph itself (lines 510–547) calls `INITIALIZE-ALL-FIELDS`, shows the "Payment successful" message, and calls `SEND-BILLPAY-SCREEN` on success (lines 524–532). Control then returns to `PROCESS-ENTER-KEY` at line 234 and executes COMPUTE + UPDATE-ACCTDAT-FILE. So the doc's description that the WRITE happens, then COMPUTE, then UPDATE is the correct execution sequence — but it misses the fact that the success message display and SEND happen inside WRITE-TRANSACT-FILE BEFORE the COMPUTE and UPDATE.

**The latent bug described in the doc is inverted:** The doc states the DUPREC branch is safe (balance not yet computed). This is correct. But the doc also says "if TRANSACT write succeeds but ACCTDAT REWRITE fails, the transaction record is committed but the balance is not reduced." This IS the actual risk and is correctly documented.

**Minor flow error:** The doc says the balance check (`ACCT-CURR-BAL <= ZEROS`) happens in `SEND-BILL-SCREEN`. In the source it happens in `PROCESS-ENTER-KEY` at lines 197–205, after `READ-ACCTDAT-FILE`.

#### S2 — Error Handling

Error table accuracy:
- "Account ID NOT found..." for NOTFND on ACCTDAT (line 361) — exact text `'Account ID NOT found...'`. Doc says "Account not found" — close but not exact.
- "Unable to lookup Account..." for OTHER on ACCTDAT (line 368) — not in doc table.
- "Account ID NOT found..." for NOTFND on CXACAIX (line 424) — same text. Doc says "Card not found".
- "Unable to lookup XREF AIX file..." for OTHER on CXACAIX (line 432) — not in doc table.
- "Transaction ID NOT found..." for STARTBR NOTFND (line 457) — not in doc table.
- "Tran ID already exist..." for DUPKEY/DUPREC (line 537) — confirmed exact text.
- "Payment successful. Your Transaction ID is ..." (lines 527–530) — confirmed.
- `DISPLAY 'RESP:' WS-RESP-CD 'REAS:' WS-REAS-CD` on file errors (lines 366, 398, 430, 461, 491, 541) — these DISPLAY statements to job log are not documented.
- `DFHRESP(ENDFILE)` on READPREV: `MOVE ZEROS TO TRAN-ID` (line 488) — the doc does not document this branch (empty TRANSACT file case).

#### S3 — Migration Notes

**Verified notes:**
- Note 1 (COMP-3 / BigDecimal): partially accurate — see S4 below for COMP-3 flag issue.
- Note 2 (transaction atomicity absent): confirmed — separate CICS WRITE and REWRITE with no explicit SYNCPOINT between them.
- Note 3 (non-atomic transaction ID generation): confirmed — STARTBR/READPREV/ENDBR sequence at lines 212–215.
- Note 4 (backward browse for max TRAN-ID): confirmed.
- Note 5 (always full balance payment): confirmed.
- Note 6 (all metadata hardcoded): confirmed at lines 219–229.
- Note 7 (TRAN-ORIG-TS and TRAN-PROC-TS same timestamp): confirmed at lines 231–232.
- Note 8 (CONFIRM field → checkbox): accurate.
- Note 9 (TRAN-CARD-NUM from XREF-CARD-NUM): confirmed at line 225 (`MOVE XREF-CARD-NUM TO TRAN-CARD-NUM`).
- Note 10 (no UPDATE lock on initial read): **incorrect** — `READ-ACCTDAT-FILE` at lines 343–354 uses `UPDATE` clause (line 351). The doc's Note 10 is factually wrong about the absence of a lock.

**Unsupported notes:** Note 10 states the read has no UPDATE lock — source line 351 contradicts this.

**Missing bugs:** `DFHRESP(ENDFILE)` on empty TRANSACT browse (line 488): sets TRAN-ID to ZEROS, then increments to 1, which is probably correct behavior for an empty file — but undocumented.

#### S4 — Copybook Fields

**Phase 1 FAIL items confirmed (byte count errors):**

From COCOM01Y (not read in full but standard CardDemo copybook):
- `CDEMO-FROM-PROGRAM PIC X(8)` = 8 bytes — doc says 3 bytes (FAIL confirmed).
- `CDEMO-TO-PROGRAM PIC X(8)` = 8 bytes — doc says 3 bytes (FAIL confirmed).
- `CDEMO-PGM-CONTEXT PIC X(1)` = 1 byte — doc says 0 bytes (FAIL confirmed).

**COMP-3 flag error:**

Doc (Appendix B, CVACT01Y table) states `ACCT-CURR-BAL` is `S9(10)V99 COMP-3`. The actual CVACT01Y (verified above) shows:
```
05  ACCT-CURR-BAL  PIC S9(10)V99.
```
No COMP-3 / USAGE clause — this is a **zoned decimal** field, not COMP-3. The same applies to `ACCT-CREDIT-LIMIT`, `ACCT-CASH-CREDIT-LIMIT`, `ACCT-CURR-CYC-CREDIT`, `ACCT-CURR-CYC-DEBIT`. The doc incorrectly marks all of these as COMP-3. For migration purposes, the advice to use BigDecimal is still correct, but the storage format information is wrong and could mislead a developer about how the data is encoded on disk.

`TRAN-AMT PIC S9(09)V99` — no COMP-3 in CVTRA05Y (verified above). Doc marks this as COMP-3. Same error.

#### S5 — External Calls

No static CALL statements in COBIL00C. CICS interactions (XCTL to `CDEMO-TO-PROGRAM`/`'COMEN01C'`, ASKTIME/FORMATTIME) are correctly described.

#### S6 — Completeness

**Undocumented paragraphs:**
- `CLEAR-CURRENT-SCREEN` (lines 552–555): called when CONFIRM = 'N' — calls `INITIALIZE-ALL-FIELDS` + `SEND-BILLPAY-SCREEN`. Doc only says "INITIALIZE-COBIL-FIELDS then SEND-BILL-SCREEN" but `INITIALIZE-COBIL-FIELDS` doesn't exist by that name.
- `INITIALIZE-ALL-FIELDS` (lines 560–566): clears ACTIDINI, CURBALI, CONFIRMI, WS-MESSAGE. Not documented.
- `POPULATE-HEADER-INFO` (lines 319–338): not mentioned.
- `PF4 key handling`: source lines 136–137 show `WHEN DFHPF4: PERFORM CLEAR-CURRENT-SCREEN`. The doc does not document PF4 as a valid key.

**Undocumented first-display with pre-selected account:**
Source lines 116–121 show: if `CDEMO-CB00-TRN-SELECTED` is not blank, move it to `ACTIDINI` and call `PROCESS-ENTER-KEY` before displaying the screen. This is a pre-population path from another program (e.g., transaction list) that the doc does not describe.

### Overall Recommendation

BIZ-COBIL00C.md has material errors that require correction before it can be used as a sole migration reference. The two most critical issues are: (1) the first-display flow description is wrong (it does not call CXACAIX on first display; that only happens on ENTER confirmation), and (2) the COMP-3 flag on `ACCT-CURR-BAL` and `TRAN-AMT` is incorrect (these are zoned decimal in the source, not COMP-3). Additionally, PF4 handling and the pre-selected account flow are completely undocumented. The migration notes and overall architecture description are correct, but a developer relying solely on this document would implement the payment flow incorrectly. The document requires targeted revision on Sections 2.1, 2.2 (first display flow), and the CVACT01Y COMP-3 flags.