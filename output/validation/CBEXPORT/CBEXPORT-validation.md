# Validation Report: BIZ-CBEXPORT.md

**Overall: PASS** — 7 passed · 0 failed · 2 warned · 1 skipped

Source file: `582` lines   |   Document: `486` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: CBEXPORT.cbl |
| `line_number_bounds` | ✓ **PASS** | 56 line reference(s) checked, all within bounds (max: 582) |
| `backtick_identifiers` | ⚠ **WARN** | 35 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 6 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | ✓ **PASS** | All 6 Appendix A DDname(s) verified against source |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 2/10 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
35 backtick identifier(s) not found in source or copybooks

- `ACCOUNT-INPUT`
- `CARD-INPUT`
- `CUSTOMER-INPUT`
- `EXPORT-OUTPUT`
- `TRANSACTION-INPUT`
- `WS-XXX-EOF`
- `WS-XXX-OK`
- `XREF-INPUT`
- `YYYY-MM-DD`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | All five export passes, paragraph names, line numbers, and the read-then-loop pattern accurately described. |
| S2 Error Handling | ⚠ WARN | Doc correctly notes CEE3ABD called with no parameters, but one error message string for EXPORT-OUTPUT open is slightly different from what was documented. |
| S3 Migration Notes | ✓ PASS | All 10 notes verified; COMP-3/COMP conversions, PCI-DSS concerns, and hardcoded branch/region flags are all accurate. |
| S4 Copybook Fields | ✓ PASS | All CVEXPORT REDEFINES layouts verified against copybook; PIC clauses and COMP/COMP-3 flags all accurate. |
| S5 External Calls | ✓ PASS | CEE3ABD no-parameter call accurately described and distinguished from other programs. |
| S6 Completeness | ⚠ WARN | Doc notes cross-reference export display says 'CBEXPORT: Cross-references exported' but source line 387-388 says 'Cross-references exported' — minor string accuracy issue. Also missing: the EXPORT-OUTPUT file is a VSAM KSDS (ORGANIZATION IS INDEXED with RECORD KEY IS EXPORT-SEQUENCE-NUM) — doc calls it "sequential write" which is technically accurate for access mode but the VSAM KSDS nature is understated. |

### Findings

#### S1 — Program Flow

The main control paragraph `0000-MAIN-PROCESSING` (line 149–158) calls: `1000-INITIALIZE`, `2000-EXPORT-CUSTOMERS`, `3000-EXPORT-ACCOUNTS`, `4000-EXPORT-XREFS`, `5000-EXPORT-TRANSACTIONS`, `5500-EXPORT-CARDS`, `6000-FINALIZE`, then `GOBACK`. Doc lists exactly these in order. PASS.

**Timestamp generation** (`1050-GENERATE-TIMESTAMP`, lines 172–195): `ACCEPT WS-CURRENT-DATE FROM DATE YYYYMMDD` (line 175), `ACCEPT WS-CURRENT-TIME FROM TIME` (line 176). STRING operations build `WS-EXPORT-DATE` and `WS-EXPORT-TIME`, then `WS-FORMATTED-TIMESTAMP` with literal `'.00'` suffix (line 191). Doc section 2.1 step 1a and Migration Note 1 correctly describe this. PASS.

**Open files** (`1100-OPEN-FILES`, lines 198–240): All six opens use `IF NOT WS-xxx-OK` pattern with 88-level conditions. The error messages concatenate raw status — doc correctly notes this in section 3.1. Verified against source: `'ERROR: Cannot open CUSTOMER-INPUT, Status: '` (line 202), `'ERROR: Cannot open ACCOUNT-INPUT, Status: '` (line 209), `'ERROR: Cannot open XREF-INPUT, Status: '` (line 216), `'ERROR: Cannot open TRANSACTION-INPUT, Status: '` (line 223), `'ERROR: Cannot open CARD-INPUT, Status: '` (line 230), `'ERROR: Cannot open EXPORT-OUTPUT, Status: '` (line 237). PASS.

**Customer export loop** (lines 243–255): `PERFORM 2100-READ-CUSTOMER-RECORD` first (priming read), then `PERFORM UNTIL WS-CUSTOMER-EOF` with `PERFORM 2200-CREATE-CUSTOMER-EXP-REC` then `PERFORM 2100-READ-CUSTOMER-RECORD`. Doc section 2.2 Pass 1 correctly describes this read-then-process-then-read loop. PASS.

The same priming-read pattern is used for all five passes (lines 312–324, 376–388, 430–443, 495–508). PASS.

**Finalize** (`6000-FINALIZE`, lines 554–573): Six CLOSE statements (lines 556–561) with no error checking on any of them, then seven DISPLAY statistics. Doc section 2.3 correctly states "no error checking" (Migration Note 6). PASS.

#### S2 — Error Handling

**`9999-ABEND-PROGRAM`** (lines 576–579): `DISPLAY 'CBEXPORT: ABENDING PROGRAM'` then `CALL 'CEE3ABD'` with **no USING clause**. Doc section 3.2 correctly describes this as a no-parameters call and explains the risk (Migration Note 7). PASS.

**Read error messages**: Doc states format is `'ERROR: Reading <FILENAME>, Status: '` + two-byte status. Source examples:
- `'ERROR: Reading CUSTOMER-INPUT, Status: '` (line 263–264) — PASS
- `'ERROR: Reading ACCOUNT-INPUT, Status: '` (line 332–333) — PASS
- `'ERROR: Reading XREF-INPUT, Status: '` (line 395–396) — PASS
- `'ERROR: Reading TRANSACTION-INPUT, Status: '` (line 451–452) — PASS
- `'ERROR: Reading CARD-INPUT, Status: '` (line 515–516) — PASS

**Write error**: `'ERROR: Writing export record, Status: '` at lines 304–305, 367–368, 421–422, 486–487, 544–545 — doc section 2.2 Pass 1 mentions this. PASS.

Minor: The doc says the open error format uses `'ERROR: Cannot open <filename>, Status: '`. Verified for all six files. PASS.

The doc correctly distinguishes CBEXPORT's error handling (no formatted status decoder, no APPL-RESULT sentinel pattern) from the other five programs. PASS.

#### S3 — Migration Notes

**Verified notes:**
- Note 1 (`'.00'` hardcoded timestamp suffix): Confirmed — source line 191 `STRING WS-EXPORT-DATE ' ' WS-EXPORT-TIME '.00'`.
- Note 2 (`CUST-ID` display→`EXP-CUST-ID` COMP): Confirmed — CVEXPORT.cpy line 25 `EXP-CUST-ID PIC 9(09) COMP`. Source line 282 `MOVE CUST-ID TO EXP-CUST-ID`. PASS.
- Note 3 (`CUST-FICO-CREDIT-SCORE` display→`EXP-CUST-FICO-CREDIT-SCORE` COMP-3): Confirmed — CVEXPORT.cpy line 41 `EXP-CUST-FICO-CREDIT-SCORE PIC 9(03) COMP-3`. Source line 299 `MOVE CUST-FICO-CREDIT-SCORE TO EXP-CUST-FICO-CREDIT-SCORE`. PASS.
- Note 4 (Customer FILLER size mismatch 168 vs 134): Confirmed — CVCUS01Y.cpy line 23 `FILLER PIC X(168)`, CVEXPORT.cpy line 42 `FILLER PIC X(134)`. The analysis (only non-filler fields are mapped explicitly) is correct. PASS.
- Note 5 (`XREF-ACCT-ID` display→`EXP-XREF-ACCT-ID` COMP): Confirmed — CVEXPORT.cpy line 87 `EXP-XREF-ACCT-ID PIC 9(11) COMP`. Source line 417 `MOVE XREF-ACCT-ID TO EXP-XREF-ACCT-ID`. PASS.
- Note 6 (close no error checking): Confirmed — lines 556–561 are bare CLOSE statements with no IF checks.
- Note 7 (CEE3ABD no parameters): Confirmed — source line 579 `CALL 'CEE3ABD'` with no USING clause.
- Note 8 (Full PAN and CVV in export): Confirmed — source lines 535–537 `MOVE CARD-NUM TO EXP-CARD-NUM`, `MOVE CARD-ACCT-ID TO EXP-CARD-ACCT-ID`, `MOVE CARD-CVV-CD TO EXP-CARD-CVV-CD`.
- Note 9 (`EXPORT-SEQUENCE-NUM PIC 9(9) COMP`): Confirmed — CVEXPORT.cpy line 16 `EXPORT-SEQUENCE-NUM PIC 9(9) COMP`. Source line 68 uses it as `RECORD KEY IS EXPORT-SEQUENCE-NUM`. PASS.
- Note 10 (`EXPORT-BRANCH-ID='0001'` and `EXPORT-REGION-CODE='NORTH'` hardcoded in every pass): Confirmed — sources lines 278–279, 347–348, 411–412, 465–466 (pass 5 line 466 is `MOVE 'NORTH' TO EXPORT-REGION-CODE`), 530–531. PASS.

**Unsupported notes:** None.

**Missing bugs:** One additional type conversion not documented but worth noting: `ACCT-CURR-CYC-DEBIT` (display S9(10)V99 in CVACT01Y) is moved to `EXP-ACCT-CURR-CYC-DEBIT` (S9(10)V99 **COMP** — binary). Doc's Pass 2 account field mapping table at row `ACCT-CURR-CYC-DEBIT` correctly states "Type change" and identifies it as `COMP` — PASS. This is documented.

#### S4 — Copybook Fields

**CVEXPORT** (all REDEFINES layouts verified):

Common header fields (lines 10–19):
- `EXPORT-REC-TYPE` PIC X(1) — PASS (line 10)
- `EXPORT-TIMESTAMP` PIC X(26) with REDEFINES providing `EXPORT-DATE` X(10), `EXPORT-DATE-TIME-SEP` X(1), `EXPORT-TIME` X(15) — PASS (lines 11–15); doc notes the REDEFINES provides `EXPORT-DATE`, `EXPORT-DATE-TIME-SEP`, `EXPORT-TIME` — PASS
- `EXPORT-SEQUENCE-NUM` PIC 9(9) COMP — PASS (line 16)
- `EXPORT-BRANCH-ID` PIC X(4) — PASS (line 17)
- `EXPORT-REGION-CODE` PIC X(5) — PASS (line 18)
- `EXPORT-RECORD-DATA` PIC X(460) — PASS (line 19)

`EXPORT-CUSTOMER-DATA` REDEFINES (lines 24–42) — all 16 fields verified including `EXP-CUST-ID PIC 9(09) COMP`, `EXP-CUST-FICO-CREDIT-SCORE PIC 9(03) COMP-3`, `EXP-CUST-ADDR-LINES OCCURS 3`, `EXP-CUST-PHONE-NUMS OCCURS 2`, `FILLER PIC X(134)`. All match doc's Appendix B table. PASS.

`EXPORT-ACCOUNT-DATA` REDEFINES (lines 47–60) — all 12 fields verified including `EXP-ACCT-CURR-BAL S9(10)V99 COMP-3`, `EXP-ACCT-CASH-CREDIT-LIMIT S9(10)V99 COMP-3`, `EXP-ACCT-CURR-CYC-DEBIT S9(10)V99 COMP`, `EXP-ACCT-CREDIT-LIMIT S9(10)V99` (display — no COMP-3), `EXP-ACCT-CURR-CYC-CREDIT S9(10)V99` (display). PASS.

`EXPORT-TRANSACTION-DATA` REDEFINES (lines 65–79) — all 14 fields verified including `EXP-TRAN-AMT S9(09)V99 COMP-3`, `EXP-TRAN-MERCHANT-ID PIC 9(09) COMP`. PASS.

`EXPORT-CARD-XREF-DATA` REDEFINES (lines 84–88) — `EXP-XREF-ACCT-ID PIC 9(11) COMP` — PASS; `FILLER PIC X(427)` — PASS.

`EXPORT-CARD-DATA` REDEFINES (lines 93–100) — `EXP-CARD-ACCT-ID PIC 9(11) COMP`, `EXP-CARD-CVV-CD PIC 9(03) COMP`, `FILLER PIC X(373)` — all match doc. PASS.

One precision: Doc says `EXP-XREF-ACCT-ID` is "Binary 8-byte integer" (Migration Note 5). On z/OS, `PIC 9(11) COMP` occupies 8 bytes (double-word). This is correct. PASS.

#### S5 — External Calls

**CEE3ABD**: Called at line 579 `CALL 'CEE3ABD'` with no USING clause. Doc section 3.2 and Appendix B clearly distinguish this from other programs and warn about the undefined abend code. PASS.

No other CALL statements in CBEXPORT. PASS.

#### S6 — Completeness

**Minor string accuracy**: The doc says Pass 3 ends with displaying `'CBEXPORT: XRefs Exported: '` + counter. Source line 387–388 shows `DISPLAY 'CBEXPORT: Cross-references exported: '  WS-XREF-RECORDS-EXPORTED`. The doc's Appendix A finalize section and section 2.3 show `'CBEXPORT: XRefs Exported: '` (source line 568 in finalize) — which is correct for the final summary. But the per-pass display at line 387 says `'CBEXPORT: Cross-references exported: '`. The doc's section 2.2 Pass 3 ends with: "displays `'CBEXPORT: XRefs Exported: '`" but the actual per-pass display is `'CBEXPORT: Cross-references exported: '`. Minor string mismatch — WARN.

**EXPORT-OUTPUT file type**: Source lines 65–69 show `SELECT EXPORT-OUTPUT ... ORGANIZATION IS INDEXED ... ACCESS MODE IS SEQUENTIAL ... RECORD KEY IS EXPORT-SEQUENCE-NUM`. This is a VSAM KSDS with sequential access. Doc Appendix A calls it "VSAM KSDS — indexed, sequential write" in the table row. PASS.

**CUSTOMER-INPUT record key**: Source line 38 `RECORD KEY IS CUST-ID` — doc Appendix A says `CUST-ID 9(09)`. PASS. Source line 44 `RECORD KEY IS ACCT-ID` — doc says `ACCT-ID 9(11)`. PASS. Source line 50 `RECORD KEY IS XREF-CARD-NUM` — doc says `XREF-CARD-NUM X(16)`. PASS. Source line 56 `RECORD KEY IS TRAN-ID` — doc says `TRAN-ID X(16)`. PASS. Source line 62 `RECORD KEY IS CARD-NUM` — doc says `CARD-NUM X(16)`. PASS.

The `DISPLAY 'CBEXPORT: Export Date: '` and `'CBEXPORT: Export Time: '` at lines 168–169 after `1100-OPEN-FILES` are not mentioned in the doc. Minor omission — no migration impact.

### Overall Recommendation

The document is accurate and safe to use as a migration reference for CBEXPORT. All five entity-type export passes, the COMP/COMP-3 type conversion mappings, the PCI-DSS PAN/CVV concerns, the no-parameter CEE3ABD call risk, and the hardcoded branch/region metadata are all correctly documented. The minor per-pass display string discrepancy ('Cross-references exported' vs 'XRefs Exported') is trivial. A Java developer can safely implement all five export entity paths from this document, paying particular attention to the binary field type conversions documented in the CVEXPORT copybook section and the PCI-DSS compliance concerns for card data.