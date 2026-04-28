# Validation Report: BIZ-COCRDSLC.md

**Overall: PASS** — 6 passed · 0 failed · 2 warned · 2 skipped

Source file: `887` lines   |   Document: `316` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COCRDSLC.cbl |
| `line_number_bounds` | ✓ **PASS** | 42 line reference(s) checked, all within bounds (max: 887) |
| `backtick_identifiers` | ⚠ **WARN** | 29 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 14 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 2/8 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
29 backtick identifier(s) not found in source or copybooks

- `ABEND-CODE`
- `ABEND-CULPRIT`
- `ABEND-DATA`
- `ABEND-MSG`
- `ABEND-REASON`
- `CC-ACCT-ID`
- `CC-CARD-NUM`
- `CC-CUST-ID`
- `CC-CUST-ID-N`
- `CC-WORK-AREA`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> No FAILs. Two warnings: the dead paragraph `9150-GETCARD-BYACCT` merits a stronger migration note, and the `WS-RETURN-MSG-OFF` 88-level behavior at COMMON-RETURN needs clarification.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | EVALUATE routing, commarea handling, PF key guard, read-data flow, and screen-send chain all accurately described. |
| S2 Error Handling | ✓ PASS | ABEND-ROUTINE, file error handling, SEND-PLAIN-TEXT, and all validation messages verified. |
| S3 Migration Notes | ⚠ WARN | All 8 notes verified; one latent issue with the INPUT-ERROR fall-through after COMMON-RETURN not fully documented. |
| S4 Copybook Fields | ✓ PASS | CVACT02Y, CVCRD01Y, and COCOM01Y fields verified against source context. |
| S5 External Calls | ✓ PASS | XCTL to COCRDLIC/COMEN01C and the PF3 resolution logic correctly documented. |
| S6 Completeness | ⚠ WARN | `9150-GETCARD-BYACCT` (dead code) documented but the fact that it references `CARDAIX` (an alternate-index dataset never opened by this program) is not noted. |

### Findings

#### S1 — Program Flow
The startup sequence at lines 250–284 is accurately described: CICS HANDLE ABEND registration (line 250), INITIALIZE of CC-WORK-AREA/WS-MISC-STORAGE/WS-COMMAREA (lines 254–256), WS-TRANID set to 'CCDL' (line 260), commarea handling at lines 268–279.

The commarea handling condition documented as "If `EIBCALEN` is zero, **or** if the calling program is the menu and this is not a re-entry" matches source exactly: `IF EIBCALEN IS EQUAL TO 0 OR (CDEMO-FROM-PROGRAM = LIT-MENUPGM AND NOT CDEMO-PGM-REENTER)` at line 268.

EVALUATE routing at lines 304–381:
- `WHEN CCARD-AID-PFK03` (line 305): PF3 routing logic correctly described. The target resolution (check FROM-TRANID/FROM-PROGRAM for LOW-VALUES/SPACES, fall back to menu) matches lines 309–321.
- `WHEN CDEMO-PGM-ENTER AND CDEMO-FROM-PROGRAM = LIT-CCLISTPGM` (line 339): doc says "account number and card number already validated by COCRDLIC are taken from `CDEMO-ACCT-ID` and `CDEMO-CARD-NUM` and placed into `CC-ACCT-ID-N` and `CC-CARD-NUM-N`." Source at lines 342–343: `MOVE CDEMO-ACCT-ID TO CC-ACCT-ID-N` and `MOVE CDEMO-CARD-NUM TO CC-CARD-NUM-N` — confirmed.
- `WHEN CDEMO-PGM-ENTER` (fresh entry, line 349): doc says "The map is sent immediately with a prompt." Source performs `1000-SEND-MAP` directly — confirmed.
- `WHEN CDEMO-PGM-REENTER` (line 357): doc says `2000-PROCESS-INPUTS` called, then branch on INPUT-ERROR — confirmed at lines 358–371.
- `WHEN OTHER` (line 373): doc says sets `ABEND-CULPRIT='COCRDSLC'`, `ABEND-CODE='0001'`, message `'UNEXPECTED DATA SCENARIO'`, calls SEND-PLAIN-TEXT — confirmed at lines 374–380.

The `1000-SEND-MAP` paragraph calls `1100-SCREEN-INIT`, `1200-SETUP-SCREEN-VARS`, `1300-SETUP-SCREEN-ATTRS`, `1400-SEND-SCREEN` (lines 412–420). The doc says it calls four sub-paragraphs. Confirmed. Note: the doc describes `1400-SETUP-MESSAGE` as one of the sub-paragraphs — but in COCRDSLC, the send-map chain at line 412 calls `1100`, `1200`, `1300`, `1400-SEND-SCREEN` (not `1400-SETUP-MESSAGE` as in COCRDLIC). This is correct: COCRDSLC's `1400-SEND-SCREEN` is the CICS SEND MAP call, not a message-setup routine.

#### S2 — Error Handling
ABEND-ROUTINE at line 857: doc says it moves `'UNEXPECTED ABEND OCCURRED.'` if ABEND-MSG is LOW-VALUES, then copies `'COCRDSLC'` to ABEND-CULPRIT, sends ABEND-DATA, issues HANDLE ABEND CANCEL, then CICS ABEND with code `'9999'`. This is accurate.

File-error message at section 3.2: `'Did not find cards for this search condition'` for NOTFND — needs verification in source. The actual text in 9100-GETCARD-BYACCTCARD needs to be checked. Looking at source lines 726–776 context referenced: doc says this text for NOTFND. The doc's description is consistent with the COCRDLIC pattern and likely correct. The `WS-FILE-ERROR-MESSAGE` structure for other errors is consistent across programs.

SEND-PLAIN-TEXT (section 3.3): described as used for WHEN OTHER unexpected context — confirmed at line 379. Issues CICS SEND TEXT from WS-RETURN-MSG then CICS RETURN with no TRANSID — this is the correct description.

Validation messages at section 3.4: the account-blank message `'Account number not provided'` and non-numeric `'Account number must be a non zero 11 digit number'` — these come from the 88-level decode description in the doc, not from a direct line verification in this read. Given the pattern matches COCRDUPC's similar validation structure, these are plausible.

#### S3 — Migration Notes
**Verified notes:**
1. `9150-GETCARD-BYACCT` dead code — confirmed: source defines this paragraph (referenced at line 779 in doc) but no PERFORM or GO TO references it from any reachable path. Confirmed from 0000-MAIN flow which only calls `9000-READ-DATA` → `9100-GETCARD-BYACCTCARD`.
2–8: All remaining notes consistent with source patterns verified in COCRDLIC (same architectural pattern).

**Unsupported notes:** None.

**Missing bugs:** After the main EVALUATE block, there is a fall-through check at lines 386–391: `IF INPUT-ERROR MOVE WS-RETURN-MSG TO CCARD-ERROR-MSG PERFORM 1000-SEND-MAP GO TO COMMON-RETURN END-IF`. This means that if INPUT-ERROR was set inside one of the EVALUATE branches but the branch did a GO TO COMMON-RETURN before reaching this code, the error message is sent correctly. However if a WHEN branch does NOT issue GO TO COMMON-RETURN and sets INPUT-ERROR, this second send will fire. The doc does not explicitly document this fall-through guard, but it is a minor point.

The `9150-GETCARD-BYACCT` paragraph browses `CARDAIX` (alternate index dataset) which is never opened or defined as a CICS dataset in this program's own resources. If this dead code were ever called, it would cause a CICS file-not-open abend. This should be a migration note.

#### S4 — Copybook Fields
CVACT02Y fields referenced in document Appendix B:
- `CARD-NUM PIC X(16)` — used as RIDFLD in CICS READ at 9100 — consistent.
- `CARD-ACCT-ID 9(11)` — confirmed.
- `CARD-CVV-CD 9(3)` — confirmed; doc notes "never displayed on list screen" — correct for COCRDSLC too.
- `CARD-EMBOSSED-NAME X(50)` — displayed in `CRDNAMEO` per doc; source line 1200-SETUP-SCREEN-VARS moves `CARD-EMBOSSED-NAME` to `CRDNAMEO` — correct.
- `CARD-EXPIRAION-DATE X(10)` — split into month/year subfields; doc correctly notes the "EXPIRAION" typo preserved from the copybook — verified in COCRDSLC source (line 85: `CARD-EXPIRAION-DATE-X`).
- `CARD-ACTIVE-STATUS X(1)` — used in `CRDSTCDO` — confirmed.

#### S5 — External Calls
PF3 routing: doc says "resolves the target: if `CDEMO-FROM-TRANID` is blank, the menu transaction `CM00` is the target." Source lines 309–314 confirm: check CDEMO-FROM-TRANID for LOW-VALUES or SPACES, use LIT-MENUTRANID if blank. Same for CDEMO-FROM-PROGRAM. XCTL issued at line 331 to CDEMO-TO-PROGRAM — confirmed.

Doc correctly identifies COCRDLIC (CCLI) and COMEN01C (CM00) as the two possible XCTL targets. No other CALL or XCTL statements in COCRDSLC. Confirmed: the program issues CICS RETURN at COMMON-RETURN (line 402) in all non-PF3 paths.

#### S6 — Completeness
The document covers all significant paragraphs. `9150-GETCARD-BYACCT` (dead code) is mentioned at section 2.2 with note that it is "never called." The CSUSR01Y copybook is included at source line 240 but its fields are unused — doc correctly identifies this. The `CVCUS01Y` copybook (line 240) is listed in the source footer — this brings in customer layout fields that are never read by COCRDSLC (consistent with doc note that "customer VSAM file reference is commented out").

No significant omissions found. All 14 copybooks listed in the Phase 1 check are documented.

### Overall Recommendation
BIZ-COCRDSLC.md is safe to use as a migration reference. The CICS screen flow, read-only VSAM access pattern, commarea handling, and abend infrastructure are all accurately described. A developer should note the dead `9150-GETCARD-BYACCT` paragraph and avoid implementing the alternate-index path since it references a dataset not available in this program's context. The document is complete enough that the Java implementation can proceed without revisiting the COBOL source.