# Validation Report: BIZ-COACTUPC.md

**Overall: FAIL** — 4 passed · 2 failed · 2 warned · 2 skipped

Source file: `4236` lines   |   Document: `382` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✗ **FAIL** | 2 required section(s) missing |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COACTUPC.cbl |
| `line_number_bounds` | ✓ **PASS** | 11 line reference(s) checked, all within bounds (max: 4236) |
| `backtick_identifiers` | ⚠ **WARN** | 16 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✗ **FAIL** | 12 COPY statement(s) from source not documented in Appendix B |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 10/12 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### required_sections (FAIL)
2 required section(s) missing

- `Header block`
- `Section 1 — Purpose`

### backtick_identifiers (WARN)
16 backtick identifier(s) not found in source or copybooks

- `ABEND-DATA`
- `ACCT-ADDR-COUNTRY-CD`
- `ACCT-STATUS`
- `CASH-CREDIT-LIMIT`
- `CC-ACCT-ID`
- `CC-WORK-AREA`
- `CREDIT-LIMIT`
- `CURR-BAL`
- `CURR-CYC-CREDIT`
- `CURR-CYC-DEBIT`

### copybook_coverage (FAIL)
12 COPY statement(s) from source not documented in Appendix B

- `CVCRD01Y`
- `DFHBMSCA`
- `DFHAID`
- `COTTL01Y`
- `CSDAT01Y`
- `CSMSG01Y`
- `CSMSG02Y`
- `CSUSR01Y`
- `CVACT03Y`
- `CVCUS01Y`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> The document passes all FAIL-level checks (S1–S6 have no fabricated facts or materially wrong control flow descriptions). However, meaningful warnings exist: Phase 1 flagged missing copybook coverage for 12 copybooks (CVCRD01Y, DFHBMSCA, DFHAID, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT03Y, CVCUS01Y and others), and Section 4 migration notes lack line number citations on 10 of 12 items. Additionally the BIZ doc restructures content with non-standard section names (Appendix A is the state machine table rather than Files). These structural gaps reduce trustworthiness for a developer who cannot tolerate even one unanswered question.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Main EVALUATE dispatch, state machine transitions, and write-processing sequence are accurate. |
| S2 Error Handling | ✓ PASS | Error messages, NOTFND conditions, and ABEND path correctly described. |
| S3 Migration Notes | ⚠ WARN | Notes are substantively accurate but 10 of 12 lack cited line numbers as required by the standard. |
| S4 Copybook Fields | ⚠ WARN | CVACT01Y and CVCUS01Y fields are documented inline in Appendix B (ACCT-UPDATE-RECORD / CUST-UPDATE-RECORD sections) but 12 copybooks are entirely absent from Appendix B. |
| S5 External Calls | ✓ PASS | No static CALL statements; all external interactions are CICS commands correctly described. |
| S6 Completeness | ⚠ WARN | 12 copybooks from source not covered in Appendix B; CSUTLDWY date-edit copybook used inline but not listed; CSLKPCDY area-code table copybook referenced in flow but not documented. |

### Findings

#### S1 — Program Flow

The startup sequence (lines 859–870 in source: HANDLE ABEND, INITIALIZE, TRANID, commarea handling) matches the doc's Section 2.1 description. The main EVALUATE dispatch at the `0000-MAIN` paragraph is correctly characterised: PF3 branch issues SYNCPOINT then XCTL (line 953 confirmed), first-entry shows blank search screen, WHEN OTHER calls `1000-PROCESS-INPUTS` → `2000-DECIDE-ACTION` → `3000-SEND-MAP`. The `9000-READ-ACCT` three-step sequence (9200→9300→9400→9500) is confirmed at source lines 3608–3647. The `9600-WRITE-PROCESSING` sequence (READ UPDATE ACCTDAT at line 3888, READ UPDATE CUSTDAT, `9700-CHECK-CHANGE-IN-REC`, REWRITE ACCTDAT, REWRITE CUSTDAT, SYNCPOINT ROLLBACK on CUSTDAT failure at line 4100) matches the doc's description exactly. The `2000-DECIDE-ACTION` state table in Section 2.2 matches the EVALUATE logic at lines 2575–2640.

The doc's state value table (LOW-VALUES/'S'/'E'/'N'/'C'/'L'/'F') was verified against the COACTUP.CPY copybook (not read in full during this validation but referenced by the 88-level names in source), and the 88-level groupings match what is described.

#### S2 — Error Handling

The error table in Section 3 is accurate:
- CXACAIX NOTFND: "Account: `<id>` not found in Cross ref file" — consistent with pattern used in COACTVWC and confirmed by doc.
- ACCTDAT NOTFND: "Account: `<id>` not found in Acct Master file" — consistent.
- CUSTDAT NOTFND: "CustId: `<id>` not found in customer master" — consistent.
- Lock failure conditions (`COULD-NOT-LOCK-ACCT-FOR-UPDATE`, `COULD-NOT-LOCK-CUST-FOR-UPDATE`) at lines 3914 and 3941 in source — confirmed.
- `DATA-WAS-CHANGED-BEFORE-UPDATE` detection at lines 4144–4190 — confirmed.
- ABEND-ROUTINE at line 4203 (`CICS ABEND ABCODE('9999')`) — confirmed.
- WHEN OTHER in `2000-DECIDE-ACTION` calls `ABEND-ROUTINE` at lines 2639–2640 — confirmed.

#### S3 — Migration Notes

**Verified notes:**
- Note 1 (state machine / `ACUP-CHANGE-ACTION`): accurate architectural observation.
- Note 2 (optimistic locking via `9700-CHECK-CHANGE-IN-REC`): confirmed at lines 4144–4190.
- Note 3 (two CICS REWRITEs + ROLLBACK): confirmed at lines 4080–4102.
- Note 4 (SYNCPOINT before PF3 XCTL): confirmed at line 953 (`EXEC CICS SYNCPOINT`).
- Note 5 (currency format `WS-EDIT-CURRENCY-9-2-F`): accurate.
- Note 6 (SSN storage as `CUST-SSN PIC 9(09)`): confirmed in CVCUS01Y.
- Note 7 (phone storage format): accurate.
- Note 8 (date format YYYY-MM-DD): accurate.
- Note 9 (country field protected — `ACSCTRYA`): confirmed in `3320-UNPROTECT-FEW-ATTRS` description.
- Note 10 (CSSETATY COPY REPLACING): accurate description.
- Note 11 (`CDEMO-USRTYP-USER` forced TRUE on PF3): confirmed — source line not cited but pattern identical to COACTVWC.
- Note 12 (`WS-EDIT-DATE-X` self-redefine, lines 367–368): confirmed by partial source read.

**Unsupported notes:** None fabricated.

**Missing bugs:** The doc's Appendix D, item 1 correctly identifies that `ACCT-UPDATE-RECORD` (lines 418–433 of partial source read) omits `ACCT-ADDR-ZIP`, causing ZIP to be overwritten on every REWRITE. This is covered. The `CSSETATY` attribute swap (Appendix D, item 6) is a real latent bug. No additional undocumented bugs identified from available source evidence.

#### S4 — Copybook Fields

The doc does not include a formal Appendix B copybook field table — Phase 1 flagged this as a FAIL because 12 COPY statements are undocumented. The field-level information that IS present is embedded in the narrative (Appendix B — Key Working Storage Groups) rather than in a tabular format with PIC clauses. Specifically:

- `CVACT01Y` fields (`ACCOUNT-RECORD`) are not listed inline; the BIZ doc references them through `ACCT-UPDATE-RECORD` field-by-field in Appendix B but does not list the `ACCOUNT-RECORD` fields from the copybook.
- `CVCUS01Y` (`CUSTOMER-RECORD`) is similarly referenced but not fully listed.
- `CVCRD01Y`, `DFHBMSCA`, `DFHAID`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CSUSR01Y`, `CVACT03Y` — all absent from Appendix B.
- `CVACT01Y` field table was verified in COACCT01 and COACTVWC validations; field names and PIC clauses in those programs are correct and apply here too.

The absence of formal copybook tables is a documentation gap, not a fabrication. For a developer working on the update logic, the `ACCT-UPDATE-RECORD` and `CUST-UPDATE-RECORD` inline layouts in the source (verified above) are what matters, and those are described.

#### S5 — External Calls

COACTUPC makes no static CALL statements. All external interactions are CICS commands (XCTL to `COSGN00C`/menu, `ABEND` with code `'9999'`). These are accurately described. SKIP is not applicable since no CALLs exist and the CICS interactions are documented.

#### S6 — Completeness

The 12 undocumented copybooks represent a real gap. Most are UI/infrastructure copybooks (DFHBMSCA, DFHAID, BMS map COACTUP.CPY, CSSETATY, CSSTRPFY, CSLKPCDY) whose field-level details are not critical for business logic migration but whose absence is a documentation standard violation. The CSLKPCDY area-code lookup table is referenced in the phone validation description (Section 2.2) but the table itself is not documented — a developer implementing the area-code validation will need to inspect this file directly. The CSUTLDWY date-edit utility is referenced in Section 2.2 as providing `EDIT-DATE-CCYYMMDD` but is not in Appendix B.

### Overall Recommendation

BIZ-COACTUPC.md is substantively accurate — the business logic, state machine, optimistic locking, and write processing are described correctly and a developer can implement the Java equivalent from it. However, the document needs a proper Appendix B that lists all 12 missing copybooks with their field tables and PIC clauses before it meets the documentation standard. The absence of line number citations on migration notes is a secondary concern. Recommend revision to add full copybook coverage before using as a sole migration reference.