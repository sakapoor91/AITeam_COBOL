# Validation Report: BIZ-COTRN02C.md

**Overall: PASS** — 5 passed · 0 failed · 3 warned · 2 skipped

Source file: `783` lines   |   Document: `403` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COTRN02C.cbl |
| `line_number_bounds` | ✓ **PASS** | 47 line reference(s) checked, all within bounds (max: 783) |
| `backtick_identifiers` | ⚠ **WARN** | 5 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 10 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 4/11 migration note(s) missing line number citations |
| `mermaid_diagram` | ⚠ **WARN** | Mermaid diagram missing classDef colour definitions |

## Issues Found

### backtick_identifiers (WARN)
5 backtick identifier(s) not found in source or copybooks

- `CDEMO-CT00-INFO`
- `HIGH-VALUES`
- `LOW-VALUES`
- `NUMVAL-C`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Startup sequence, key routing, and paragraph order all match source accurately. |
| S2 Error Handling | ⚠ WARN | Error messages are mostly accurate but one subtle error-path detail is missing. |
| S3 Migration Notes | ⚠ WARN | All cited issues are real; one latent bug (ACCTDAT declared but never read) not documented. |
| S4 Copybook Fields | ✓ PASS | All sampled fields from CVTRA05Y/CVACT03Y match the doc accurately. |
| S5 External Calls | ✓ PASS | CSUTLDTC call documented with correct interface and purpose. |
| S6 Completeness | ⚠ WARN | CONFIRM field is documented but the dual-NUMVAL-C invocation pattern and the msg-num 2513 bypass in date validation are undocumented. |

### Findings

#### S1 — Program Flow
Source PROCEDURE DIVISION (lines 107–783) confirms the doc's described flow accurately:
- `MAIN-PARA` checks `EIBCALEN = 0` → redirect to COSGN00C (line 115–117). Doc correctly states this.
- First-entry path (`NOT CDEMO-PGM-REENTER`): sets flag, moves LOW-VALUES to map, checks `CDEMO-CT02-TRN-SELECTED` and pre-fills card number, then PERFORMs `SEND-TRNADD-SCREEN` (lines 121–130). Doc correctly describes this.
- Re-entry EVALUATE on EIBAID (lines 133–152): DFHENTER → PROCESS-ENTER-KEY, DFHPF3 → RETURN-TO-PREV-SCREEN, DFHPF4 → CLEAR-CURRENT-SCREEN, DFHPF5 → COPY-LAST-TRAN-DATA. All match the navigation table in the doc.
- `PROCESS-ENTER-KEY` (line 164): calls VALIDATE-INPUT-KEY-FIELDS then VALIDATE-INPUT-DATA-FIELDS then checks CONFIRMI. Doc's description is accurate.
- `ADD-TRANSACTION` (line 442): MOVE HIGH-VALUES → STARTBR → READPREV → ENDBR → ADD 1 → WRITE. Doc's "New Transaction ID Generation" pseudocode matches exactly.
- `COPY-LAST-TRAN-DATA` (line 471): first calls VALIDATE-INPUT-KEY-FIELDS, then does STARTBR/READPREV/ENDBR, then calls PROCESS-ENTER-KEY. Doc correctly documents this.

#### S2 — Error Handling
Verified DISPLAY strings against source:
- Line 598: `DISPLAY 'RESP:' WS-RESP-CD 'REAS:' WS-REAS-CD` in READ-CXACAIX-FILE OTHER path — doc does not call out this DISPLAY in error handling section, which is a minor omission.
- Line 593: NOTFND on CXACAIX → `'Account ID NOT found...'` — matches doc.
- Line 626: NOTFND on CCXREF → `'Card Number NOT found...'` — matches doc.
- Lines 736–740: DUPKEY/DUPREC on WRITE → `'Tran ID already exist...'` — matches doc.
- Lines 728–731: SUCCESS on WRITE includes `DFHGREEN` color and a STRING build containing `' Your Tran ID is '` + TRAN-ID. Doc documents this accurately.
- WARN: Lines 397–407 show that date validation error is suppressed if `CSUTLDTC-RESULT-MSG-NUM = '2513'` — this special bypass is not documented in Section 3 or the CSUTLDTC call table.

#### S3 — Migration Notes
**Verified notes:**
- `WS-TRAN-AMT` PIC `+99999999.99` (line 53) — signed display field, confirmed present.
- `FUNCTION NUMVAL-C` usage (lines 383, 456) — confirmed.
- `FUNCTION NUMVAL` usage (lines 204, 218) — confirmed.
- HIGH-VALUES STARTBR idiom for last-record retrieval (line 444) — confirmed.
- `CDEMO-CT02-INFO` inline extension of COCOM01Y (lines 72–80) — confirmed.

**Unsupported notes:** None fabricated.

**Missing bugs:**
- `WS-ACCTDAT-FILE` is declared at line 40 (`PIC X(08) VALUE 'ACCTDAT '`) and `CVACT01Y` is copied (line 89), but no `EXEC CICS READ DATASET(WS-ACCTDAT-FILE)` exists anywhere in the source. The ACCTDAT file is described in the doc as "used to validate that the account exists" but the source never actually reads it. This is a documentation inaccuracy and a latent dead-code issue not called out in migration notes.
- The error-suppression for CSUTLDTC result message number `'2513'` (lines 400, 420) means future leap-year-ambiguous dates silently pass validation. This is undocumented.

#### S4 — Copybook Fields
Verified against CVTRA05Y and CVACT03Y (from COPY statements in source):
- `TRAN-ID`, `TRAN-TYPE-CD`, `TRAN-CAT-CD`, `TRAN-SOURCE`, `TRAN-DESC`, `TRAN-AMT`, `TRAN-CARD-NUM`, `TRAN-MERCHANT-ID`, `TRAN-MERCHANT-NAME`, `TRAN-MERCHANT-CITY`, `TRAN-MERCHANT-ZIP`, `TRAN-ORIG-TS`, `TRAN-PROC-TS` — all field names cited in "TRAN-RECORD Fields Written" table are confirmed moved from screen fields at lines 451–465. Field names match.
- `XREF-CARD-NUM`, `XREF-ACCT-ID` from CVACT03Y — confirmed used at lines 206, 209, 220, 223.
- `CDEMO-CT02-INFO` extension fields (`CDEMO-CT02-TRNID-FIRST` PIC X(16), `CDEMO-CT02-TRNID-LAST` PIC X(16), `CDEMO-CT02-PAGE-NUM` PIC 9(08), `CDEMO-CT02-NEXT-PAGE-FLG` PIC X(01), `CDEMO-CT02-TRN-SEL-FLG` PIC X(01), `CDEMO-CT02-TRN-SELECTED` PIC X(16)) — all confirmed at lines 73–80. Doc's Copybooks table describes these accurately.
- No COMP-3 fields are present in the main transaction record data fields used here. Doc correctly omits COMP-3 flags for these fields.

#### S5 — External Calls
One external CALL in source:
- `CALL 'CSUTLDTC' USING CSUTLDTC-DATE, CSUTLDTC-DATE-FORMAT, CSUTLDTC-RESULT` (lines 393–395 and 413–415). Called twice — once for TORIGDTI and once for TPROCDTI.
- Doc's External Program Calls table: program `CSUTLDTC`, mechanism `CALL 'CSUTLDTC'`, purpose "Validates TORIGDTI and TPROCDTI dates against 'YYYY-MM-DD' format". All fields confirmed accurate.
- The `CSUTLDTC-PARM` working storage area (lines 62–69) is not described in the copybook table (it is inline WS, not a COPY), but this is a minor omission, not a fabrication.

#### S6 — Completeness
- `INITIALIZE-ALL-FIELDS` paragraph (line 762) is not mentioned in the flow description. It is called after a successful WRITE and on PF4 clear. The successful-write path mentions "Clear all fields" which is functionally correct.
- The `WS-DATE-FORMAT PIC X(10) VALUE 'YYYY-MM-DD'` literal (line 60) is used in both CSUTLDTC calls — not in the doc's Appendix C/hardcoded literals.
- `READPREV-TRANSACT-FILE` ENDFILE handling (line 688): moves `ZEROS TO TRAN-ID` — meaning if the file is empty, the new transaction gets ID 1. This edge case is not documented.
- `SEND-TRNADD-SCREEN` contains an embedded `EXEC CICS RETURN` (line 530), meaning the program returns from within the send paragraph. This pseudo-conversational pattern is not explicitly called out but is implied by the CICS transaction design note.

### Overall Recommendation
The document is materially accurate and safe to use as a primary migration reference for COTRN02C. The one factual inaccuracy — that ACCTDAT is described as "used to validate that the account exists" when the source never issues a READ against it — should be corrected before the document is handed to the Java developer, as it would lead to unnecessary ACCTDAT integration work. The CSUTLDTC message-2513 bypass and the READPREV ENDFILE edge case are latent behavioral details worth noting in migration notes.