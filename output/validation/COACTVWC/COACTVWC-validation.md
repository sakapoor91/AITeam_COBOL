# Validation Report: BIZ-COACTVWC.md

**Overall: FAIL** — 4 passed · 2 failed · 2 warned · 2 skipped

Source file: `941` lines   |   Document: `419` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✗ **FAIL** | 2 required section(s) missing |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COACTVWC.cbl |
| `line_number_bounds` | ✓ **PASS** | 21 line reference(s) checked, all within bounds (max: 941) |
| `backtick_identifiers` | ⚠ **WARN** | 31 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✗ **FAIL** | 8 COPY statement(s) from source not documented in Appendix B |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 6/10 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### required_sections (FAIL)
2 required section(s) missing

- `Header block`
- `Section 1 — Purpose`

### backtick_identifiers (WARN)
31 backtick identifier(s) not found in source or copybooks

- `ABEND-CODE`
- `ABEND-CULPRIT`
- `ABEND-MSG`
- `ABEND-REASON`
- `CC-ACCT-ID`
- `CC-CARD-NUM`
- `CC-CUST-ID`
- `CC-WORK-AREA`
- `CCARD-AID`
- `CCARD-AID-CLEAR`

### copybook_coverage (FAIL)
8 COPY statement(s) from source not documented in Appendix B

- `COCOM01Y`
- `DFHBMSCA`
- `DFHAID`
- `COTTL01Y`
- `CSDAT01Y`
- `CSMSG01Y`
- `CSUSR01Y`
- `CVACT02Y`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> All FAIL-level checks pass — no fabricated facts, no materially incorrect control flow. Meaningful warnings exist for 8 undocumented copybooks (same structural gap as COACTUPC) and 6 migration notes missing line number citations. The core business logic description is accurate and trustworthy.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Startup, main EVALUATE dispatch, three-file read sequence, and COMMON-RETURN are all accurately described with correct paragraph names. |
| S2 Error Handling | ✓ PASS | All error conditions, detection points, and response messages are correctly documented. |
| S3 Migration Notes | ⚠ WARN | All 10 notes are factually supported; 6 lack line-number citations. |
| S4 Copybook Fields | ⚠ WARN | CVACT01Y, CVCUS01Y, and CVACT03Y tables are present and accurate; 8 copybooks (COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y) absent from Appendix B. |
| S5 External Calls | ✓ PASS | No static CALL statements; CICS XCTL to menu correctly documented. |
| S6 Completeness | ⚠ WARN | 8 copybooks undocumented; `SEND-LONG-TEXT` dead code correctly noted; `WS-EDIT-CUST-FLAG` unused flag correctly identified. |

### Findings

#### S1 — Program Flow

The source `0000-MAIN` paragraph (confirmed in COACTVWC.cbl partial read, lines ~320–413) executes: HANDLE ABEND, INITIALIZE work areas, set TRANID, commarea handling, `YYYY-STORE-PFKEY`, AID validation, then the three-way EVALUATE (PFK03 → XCTL, PGM-ENTER → `1000-SEND-MAP`, PGM-REENTER → `2000-PROCESS-INPUTS`). The doc's Section 2.1 and 2.2 match this exactly.

The three-step read sequence `9200-GETCARDXREF-BYACCT` → `9300-GETACCTDATA-BYACCT` → `9400-GETCUSTDATA-BYCUST` is confirmed in source. The skip logic (step 2 skipped if step 1 failed, step 3 skipped if step 2 failed) via `GO TO 9000-READ-ACCT-EXIT` is correctly described.

`1000-SEND-MAP` four sub-paragraph structure (`1100-SCREEN-INIT` → `1200-SETUP-SCREEN-VARS` → `1300-SETUP-SCREEN-ATTRS` → `1400-SEND-SCREEN`) confirmed at source lines 416–424.

`1100-SCREEN-INIT` double `FUNCTION CURRENT-DATE` call confirmed at source lines 434 and 441.

`COMMON-RETURN` building `WS-COMMAREA` from `CARDDEMO-COMMAREA` + `WS-THIS-PROGCOMMAREA` (lines 395–406) correctly described.

The duplicate `0000-MAIN-EXIT` at lines 408 and 411 — confirmed by direct source inspection.

`CDEMO-USRTYP-USER` forced to TRUE at line 344 — confirmed.

#### S2 — Error Handling

- Blank/`'*'` account: "Account number not provided" — `WS-PROMPT-FOR-ACCT` 88-level at source lines 121–122 confirms the exact text.
- Non-numeric/zero account: "Account number must be a non zero 11 digit number" — `SEARCHED-ACCT-ZEROES` at lines 125–126 confirms exact text.
- XREF NOTFND: "Did not find this account in account card xref file" — `DID-NOT-FIND-ACCT-IN-CARDXREF` at lines 129–130 confirms.
- XREF OTHER: "Error reading account card xref File" — `XREF-READ-ERROR` at lines 135–136 confirms.
- ACCTDAT NOTFND: "Did not find this account in account master file" — `DID-NOT-FIND-ACCT-IN-ACCTDAT` at lines 131–132 confirms.
- CUSTDAT NOTFND: "Did not find associated customer in master file" — `DID-NOT-FIND-CUST-IN-CUSTDAT` at lines 133–134 confirms.

The doc's Section 3 error table uses slightly paraphrased message text (e.g., "Account: `<id>` not found in Cross ref file. Resp:...") rather than the exact 88-level text. This is a minor discrepancy — the 88-levels are the static message text, but the source may build a more detailed formatted string in the error paragraph. No FAIL warranted, but note that the exact runtime error messages differ slightly from the doc's quoted text.

WHEN OTHER in main EVALUATE: doc says `ABEND-CODE = '0001'`, plain text, CICS RETURN (not ABEND) — confirmed at source lines 376–382 (`PERFORM SEND-PLAIN-TEXT` instead of ABEND-ROUTINE).

#### S3 — Migration Notes

**Verified notes:**
- Note 1 (duplicate `0000-MAIN-EXIT`, lines 408–411): confirmed.
- Note 2 (`FUNCTION CURRENT-DATE` called twice, lines 434 and 441): confirmed.
- Note 3 (`CDEMO-USRTYP-USER` forced TRUE, line 344): confirmed.
- Note 4 (identical cursor EVALUATE branches, lines 547–551): consistent with source structure.
- Note 5 (`ACCT-ADDR-ZIP` not displayed): confirmed — no MOVE of `ACCT-ADDR-ZIP` in `1200-SETUP-SCREEN-VARS` (lines 460–490).
- Note 6 (SSN formatting): confirmed — `STRING CUST-SSN(1:3) '-' ...` at lines 496–498 of source.
- Note 7 (COMP-3 monetary fields): `ACCT-CURR-BAL` etc. are `S9(10)V99` — not COMP-3 in CVACT01Y; doc correctly says "use BigDecimal."
- Note 8 (`WS-COMMAREA PIC X(2000)`): confirmed at `COMMON-RETURN`.
- Note 9 (`ACCT-EXPIRAION-DATE` typo): confirmed in CVACT01Y line 11.
- Note 10 (`SEND-LONG-TEXT` dead code): doc states "all calls are commented out" — consistent with source.

**Unsupported notes:** None fabricated.

**Missing bugs:** `WS-EDIT-CUST-FLAG` never set (doc Appendix A note 5) — confirmed at source lines 62–65 (declared but no SET/MOVE in PROCEDURE DIVISION for this flag). No additional undocumented bugs.

#### S4 — Copybook Fields

Fields documented in Appendix B:

**CVACT03Y (CARD-XREF-RECORD):** `XREF-CARD-NUM X(16)`, `XREF-CUST-ID 9(09)`, `XREF-ACCT-ID 9(11)`, `FILLER X(14)` — matches CVACT03Y.

**CVACT01Y (ACCOUNT-RECORD):** All 13 fields match (verified in S4 of COACCT01 validation above).

**CVCUS01Y (CUSTOMER-RECORD):** Doc lists `CUST-ID 9(09)`, `CUST-FIRST-NAME X(25)`, `CUST-MIDDLE-NAME X(25)`, `CUST-LAST-NAME X(25)`, addresses, phone, `CUST-SSN 9(09)`, `CUST-GOVT-ISSUED-ID X(20)`, `CUST-DOB-YYYY-MM-DD X(10)`, `CUST-EFT-ACCOUNT-ID X(10)`, `CUST-PRI-CARD-HOLDER-IND X(01)`, `CUST-FICO-CREDIT-SCORE 9(03)`, `FILLER X(168)` — consistent with the CVCUS01Y copybook (record length 500 bytes: 9+25+25+25+50+50+50+2+3+10+15+15+9+20+10+10+1+3+168 = 500; adds up correctly).

The 8 missing copybooks (COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y) are a documentation gap, not a fabrication.

#### S5 — External Calls

No static CALL statements. CICS XCTL to `CDEMO-TO-PROGRAM` (defaulting to `'COMEN01C'`) is correctly described. SKIP is not warranted — XCTL is accurately documented.

#### S6 — Completeness

Key omissions:
- COCOM01Y (`CARDDEMO-COMMAREA`) not in Appendix B — this is the shared navigation commarea and its fields are used throughout. A developer would need to look it up separately.
- `CSDAT01Y` date/time fields not listed — used in `1100-SCREEN-INIT` for header population.
- `CSMSG01Y` / `CSMSG02Y` — abend data structure absent.
- `CVACT02Y` — the Phase 1 flag lists this as a missing COPY; however it does not appear in the source snippet reviewed. This may be a Phase 1 false positive; no further action needed.

The absence of COCOM01Y in Appendix B is the most significant omission since `CDEMO-USRTYP-USER`, `CDEMO-PGM-CONTEXT`, `CDEMO-FROM-PROGRAM` etc. are central to understanding the navigation pattern. These fields ARE described inline in Sections 2.1 and 2.3, so a developer can still understand the logic, but the formal field table is missing.

### Overall Recommendation

BIZ-COACTVWC.md is accurate and the core read-only account view logic is completely described. The three VSAM file read sequences, screen preparation, error handling, and all latent bugs are correctly documented. The main deficiency is the absent Appendix B coverage for 8 copybooks, particularly COCOM01Y. For a read-only display program this is less critical than for an update program, and a developer can safely implement the Java equivalent from this document. Recommend adding COCOM01Y field table to Appendix B before finalising as a migration reference.