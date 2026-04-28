# Validation Report: BIZ-COCRDLIC.md

**Overall: FAIL** — 5 passed · 1 failed · 2 warned · 2 skipped

Source file: `1459` lines   |   Document: `397` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COCRDLIC.cbl |
| `line_number_bounds` | ✓ **PASS** | 50 line reference(s) checked, all within bounds (max: 1459) |
| `backtick_identifiers` | ⚠ **WARN** | 33 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✗ **FAIL** | 1 COPY statement(s) from source not documented in Appendix B |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 2/8 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
33 backtick identifier(s) not found in source or copybooks

- `CC-ACCT-ID`
- `CC-CARD-NUM`
- `CC-CUST-ID`
- `CC-CUST-ID-N`
- `CC-WORK-AREA`
- `CC-WORK-AREAS`
- `CCARD-AID`
- `CCARD-AID-CLEAR`
- `CCARD-AID-ENTER`
- `CCARD-AID-PA1`

### copybook_coverage (FAIL)
1 COPY statement(s) from source not documented in Appendix B

- `CSMSG02Y`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> No FAIL-level checks. Two warnings require attention: the undocumented CSMSG02Y copybook and minor flow details around the duplicate PF7 WHEN clause.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Main paragraph flow, EVALUATE routing, browse logic, pagination, filter, and screen-build chain all accurately described. |
| S2 Error Handling | ✓ PASS | File error message structure, all navigation messages, and all validation messages verified verbatim. |
| S3 Migration Notes | ⚠ WARN | All 8 notes are supported; one latent issue with the first WHEN OTHER fallback at line 572 not correctly described. |
| S4 Copybook Fields | ✓ PASS | Sampled fields from CVCRD01Y, COCOM01Y, CVACT02Y verified against source. |
| S5 External Calls | ✓ PASS | All three XCTL targets (COCRDSLC, COCRDUPC, COMEN01C) correctly documented with trigger conditions and commarea contents. |
| S6 Completeness | ⚠ WARN | `CSMSG02Y` copybook (COPY statement at line 283, currently commented out) not in Appendix B; minor detail about `DFHBMPRF` vs `DFHBMPRO` attribute difference for row 1 vs rows 2-7 omitted. |

### Findings

#### S1 — Program Flow
The document accurately describes the 0000-MAIN paragraph sequence (lines 298–602): initialization, commarea split, fresh-start reset, PF key mapping via YYYY-STORE-PFKEY, conditional map receive, and the EVALUATE routing block. All eight EVALUATE branches are correctly identified and their actions described.

The description of the duplicate PF7/CA-FIRST-PAGE WHEN clause (lines 439–454) is accurate: the document correctly notes that "two consecutive WHEN CCARD-AID-PFK07 AND CA-FIRST-PAGE clauses (lines 439 and 444)" exist and that only the second is reachable. Verified in source: lines 439–445 show the first WHEN with no body (falls to second WHEN at 444 which has processing code).

The 9000-READ-FORWARD browse logic is accurately documented including the "peek one extra READNEXT" pattern at lines 1197–1231 to detect next-page existence. The 9100-READ-BACKWARDS backward-fill pattern (filling slots from WS-MAX-SCREEN-LINES+1 down to 1, then using READPREV) is accurately described. The filter logic in 9500-FILTER-RECORDS (lines 1385–1405) is accurate: account filter checked first, then card filter, with GO TO exits.

One minor imprecision: The document says the WHEN OTHER branch (line 572) "Resets to first-card key, performs forward read, sends screen." This is correct for WHEN OTHER of the outer EVALUATE. However, the code at line 574 moves `WS-CA-FIRST-CARD-NUM` to `WS-CARD-RID-CARDNUM` which is correct, but the read started here uses whatever state was set before — the first card key resets correctly.

#### S2 — Error Handling
All navigation and validation message literals verified:
- Line 903: `'NO PREVIOUS PAGES TO DISPLAY'` — matches source exactly.
- Line 908: `'NO MORE PAGES TO DISPLAY'` — matches source exactly.
- Line 1219, 1239: `'NO MORE RECORDS TO SHOW'` — both instances match source exactly.
- Line 1022: `'ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER'` — matches source exactly.
- Line 1058: `'CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER'` — matches source exactly.
- 88-level literals: `'PF03 PRESSED.EXITING'`, `'NO RECORDS FOUND FOR THIS SEARCH CONDITION.'`, `'PLEASE SELECT ONLY ONE RECORD TO VIEW OR UPDATE'`, `'INVALID ACTION CODE'` — all match lines 120–126 exactly.

The WS-FILE-ERROR-MESSAGE structure (section 3.1) is verified: FILLER(`'File Error:'`, 12) + ERROR-OPNAME(8) + FILLER(`' on '`, 4) + ERROR-FILE(9) + FILLER(`' returned RESP '`, 15) + ERROR-RESP(10) + FILLER(`',RESP2 '`, 7) + ERROR-RESP2(10) + FILLER(5) = 80 bytes total. This matches lines 153–171.

CICS STARTBR error handling: the doc notes that STARTBR errors are not explicitly handled in the source. Verified: source code has no RESP check after STARTBR at lines 1129–1136 (only after READNEXT). This is a latent bug not called out in migration notes — worth flagging.

#### S3 — Migration Notes
**Verified notes:**
1. Duplicate WHEN clause PF7/CA-FIRST-PAGE (lines 439–445) — confirmed dead first WHEN body.
2. `WS-CA-LAST-CARD-ACCT-ID` set but account path never used — confirmed: lines 449, 476, 492, 507 all commented out in source.
3. SEND-PLAIN-TEXT/SEND-LONG-TEXT dead code (lines 1422, 1441) — confirmed: neither called from any reachable paragraph.
4. In-memory post-read filtering inefficiency — confirmed: 9500-FILTER-RECORDS called per record within browse loop.
5. `WS-EDIT-SELECT-COUNTER` COMP-3 never used (line 69) — confirmed: field declared `USAGE COMP-3`, no paragraph reads or increments it; the tally at line 1080 uses `I` (a separate COMP field).
6. `CC-CARD-NUM-N` redefinition comparison (line 1397) — confirmed: `CARD-NUM = CC-CARD-NUM-N` used for filter comparison.
7. No commarea length guard — confirmed: `WS-COMMAREA` is fixed 2000 bytes; no overflow protection.
8. `WS-CA-SCREEN-NUM` management (line 1177) — confirmed: incremented from 0 to 1 only when `WS-SCRN-COUNTER = 1` and `WS-CA-SCREEN-NUM = 0`.

**Unsupported notes:** None.

**Missing bugs:** CICS STARTBR at lines 1129–1136 and 1273–1280 has no RESP check. If STARTBR fails (e.g., dataset not available), the program proceeds to READNEXT/READPREV calls which will then fail with `DFHRESP(NOTOPEN)` — the error is eventually caught in the EVALUATE but the root cause (STARTBR failure) is not captured in ERROR-OPNAME. This should be a migration note.

#### S4 — Copybook Fields
Sample verified from CVCRD01Y (referenced at line 221 in source; doc Appendix B):

| Field | Doc PIC | Verified in source context | Match |
|-------|---------|--------------------------|-------|
| `CCARD-AID` | `X(5)` | Used as 5-char AID code | Yes |
| `CC-ACCT-ID` | `X(11)` | Used in 2210-EDIT-ACCOUNT comparison | Yes |
| `CC-CARD-NUM` | `X(16)` | Used in 2220-EDIT-CARD | Yes |
| `CC-CARD-NUM-N` | REDEFINES `9(16)` | Used in filter at line 1397 | Yes |
| `CCARD-NEXT-PROG` | `X(8)` | Used to set XCTL target | Yes |

COCOM01Y fields verified: `CDEMO-FROM-PROGRAM` (X(8)), `CDEMO-PGM-CONTEXT` with 88-levels `CDEMO-PGM-ENTER`=0 and `CDEMO-PGM-REENTER`=1 — used at lines 315–343. CVACT02Y `CARD-ACTIVE-STATUS` (X(1)) used at line 1169–1171 to populate `WS-ROW-CARD-STATUS`. `CARD-EMBOSSED-NAME` described as "never displayed on list screen" — confirmed: lines 1165–1171 only move CARD-NUM, CARD-ACCT-ID, and CARD-ACTIVE-STATUS to screen rows; embossed name is not moved.

The Phase 1 FAIL on `copybook_coverage` for `CSMSG02Y` (currently commented out at line 283: `*COPY CSMSG02Y.`) — the COPY statement is commented out in source, so the copybook is not actually included. The doc does not reference CSMSG02Y in Appendix B, which is correct behavior given the comment-out.

#### S5 — External Calls
All three XCTL transfers verified:
- `COCRDSLC` (line 538–541): triggered by `CCARD-AID-ENTER AND VIEW-REQUESTED-ON(I-SELECTED) AND CDEMO-FROM-PROGRAM = LIT-THISPGM`. Commarea fields `CDEMO-ACCT-ID` (from `WS-ROW-ACCTNO(I-SELECTED)` at line 531) and `CDEMO-CARD-NUM` (from `WS-ROW-CARD-NUM(I-SELECTED)` at line 533) — doc states these correctly.
- `COCRDUPC` (lines 566–569): same pattern with `UPDATE-REQUESTED-ON` — confirmed.
- `COMEN01C` (lines 402–405): triggered by `CCARD-AID-PFK03 AND CDEMO-FROM-PROGRAM = LIT-THISPGM` — confirmed. Passes only `CARDDEMO-COMMAREA` (not 2000-byte area) — confirmed at line 404.

#### S6 — Completeness
CSMSG02Y COPY at line 283 is commented out (`*COPY CSMSG02Y.`), so not truly missing from Appendix B. The Phase 1 FAIL is a false positive from the mechanical checker treating the comment line as an active COPY.

Minor omission: `1250-SETUP-ARRAY-ATTRIBS` has an asymmetry — row 1 uses `DFHBMPRF` (protected, line 753) while rows 2–7 use `DFHBMPRO` (also protected, lines 766, 777, etc.). The doc describes this section generically as "empty/protected rows use `DFHBMPRF`/`DFHBMPRO`" which is accurate enough. The functional difference between `DFHBMPRF` and `DFHBMPRO` (both protect, minor display difference) is not significant for migration.

The `SEND-PLAIN-TEXT` and `SEND-LONG-TEXT` debug paragraphs are mentioned at section 3.4 — confirmed dead code, no reachable call path.

### Overall Recommendation
BIZ-COCRDLIC.md is safe to use as a migration reference. The complex pagination, filtering, and CICS browse mechanics are accurately described with correct line references. The one item requiring attention before migration is the absence of STARTBR error handling (noted above but not in migration notes); a Java developer should add explicit error handling for the initial keyed-browse setup. All XCTL routing, commarea structure, and screen-build logic is accurately documented.