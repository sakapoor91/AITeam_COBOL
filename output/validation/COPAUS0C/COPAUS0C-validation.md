# Validation Report: BIZ-COPAUS0C.md

**Overall: PASS** — 6 passed · 0 failed · 2 warned · 2 skipped

Source file: `1032` lines   |   Document: `427` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COPAUS0C.cbl |
| `line_number_bounds` | ✓ **PASS** | 63 line reference(s) checked, all within bounds (max: 1032) |
| `backtick_identifiers` | ⚠ **WARN** | 5 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 14 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 2/10 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
5 backtick identifier(s) not found in source or copybooks

- `ABEND-CODE`
- `ABEND-CULPRIT`
- `ABEND-DATA`
- `ABEND-MSG`
- `ABEND-REASON`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Paragraph order, EIBCALEN logic, key handling, IMS paging, and CICS RETURN all match the source accurately. |
| S2 Error Handling | ✓ PASS | All six error paragraphs are documented with correct trigger conditions and accurate DISPLAY strings verified against source. |
| S3 Migration Notes | ⚠ WARN | All 10 notes are supported by the source; two latent issues not in the doc were identified. |
| S4 Copybook Fields | ✓ PASS | Sampled fields from CIPAUSMY, CIPAUDTY, CVACT03Y, COCOM01Y all match PIC clauses and COMP-3 flags correctly. |
| S5 External Calls | ✓ PASS | Both COPAUS1C (XCTL) and COMEN01C (XCTL) are correctly documented with accurate commarea fields. |
| S6 Completeness | ⚠ WARN | INITIALIZE-AUTH-DATA paragraph and `POPULATE-HEADER-INFO` are not described in Section 2; minor omissions only. |

### Findings

#### S1 — Program Flow
The document's description of `MAIN-PARA` (lines 178–257) matches the source exactly: EIBCALEN=0 path initializes commarea and sets `CDEMO-PGM-REENTER`, EIBCALEN>0 path copies commarea and branches on `CDEMO-PGM-REENTER`. The EVALUATE EIBAID structure (DFHENTER, DFHPF3, DFHPF7, DFHPF8, OTHER) at lines 224–250 is faithfully described. The doc correctly notes that `GATHER-DETAILS` resets `CDEMO-CPVS-PAGE-NUM` to 0 (line 347) and calls `GATHER-ACCOUNT-DETAILS` then `INITIALIZE-AUTH-DATA` then `PROCESS-PAGE-FORWARD`. The PF7 logic description (line 362–385) correctly captures the decrement-then-reposition pattern. The PF8 description (line 388–412) is accurate. `PROCESS-PAGE-FORWARD` loop (line 415–454) is accurately described including the WS-IDX=2 page-number increment and the extra READNEXT peek for next-page detection. `SEND-PAULST-SCREEN` SYNCPOINT-before-send logic (line 684–688) is correctly documented.

Minor discrepancy: The doc describes `GATHER-DETAILS` as calling `GET-AUTH-SUMMARY` after `GATHER-ACCOUNT-DETAILS` (Section 2.1, "`GET-AUTH-SUMMARY` is then read by calling"). In source, `GATHER-ACCOUNT-DETAILS` itself calls `GET-AUTH-SUMMARY` internally (line 785), but `GATHER-DETAILS` also checks `FOUND-PAUT-SMRY-SEG` (line 354) set by that call, which matches the documented flow. The description is effectively correct though the separation of concerns could be clearer.

#### S2 — Error Handling
All six error handlers verified against source:
- **3.1 GETCARDXREF-BYACCT** (line 812): NOTFND path uses `'Account:'` + WS-ACCT-ID + `' not found in XREF file...'` — doc says `'Account: <acctid> not found in XREF file...'` ✓. OTHER path sets WS-ERR-FLG ✓.
- **3.2 GETACCTDATA-BYACCT** (line 865): NOTFND message uses WS-CARD-RID-ACCT-ID-X (not WS-ACCT-ID) — doc says `'Account: <acctid>...'` which is a minor label approximation, not a factual error. ✓
- **3.3 GETCUSTDATA-BYCUST** (line 915): message uses `'Customer:'` + WS-CARD-RID-CUST-ID-X ✓
- **3.4 GET-AUTH-SUMMARY** (line 966): `SEGMENT-NOT-FOUND` silently sets flag, OTHER sets error ✓
- **3.5 GET-AUTHORIZATIONS** (line 459): GE/GB → AUTHS-EOF ✓. Error string `' System error while reading AUTH Details: Code:'` matches line 477 ✓
- **3.6 REPOSITION-AUTHORIZATIONS** (line 488): `' System error while repos. AUTH Details: Code:'` matches line 510 ✓
- **3.7 SCHEDULE-PSB** (line 1001): error string `' System error while scheduling PSB: Code:'` matches line 1022 ✓

#### S3 — Migration Notes
**Verified notes:**
- Note 1 (CXACAIX vs CCXREF): `WS-CARDXREFNAME-ACCT-PATH = 'CXACAIX'` at line 41 and `WS-CCXREF-FILE = 'CCXREF  '` at line 42 confirmed. READ at line 819 uses WS-CARDXREFNAME-ACCT-PATH. ✓
- Note 2 (commented-out XREF-ACCT-ID): Line 972 `*    MOVE XREF-ACCT-ID TO PA-ACCT-ID` confirmed ✓
- Note 3 (PSB re-scheduled on every call): SCHEDULE-PSB called from GET-AUTH-SUMMARY confirmed ✓
- Note 4 (OCCURS 20): `CDEMO-CPVS-PAUKEY-PREV-PG PIC X(08) OCCURS 20 TIMES` at line 120 ✓
- Note 5 (OCCURS 5): `CDEMO-CPVS-AUTH-KEYS PIC X(08) OCCURS 5 TIMES` at line 126 ✓
- Note 6 (WS-AUTH-AMT `-zzzzzzz9.99`): line 55 ✓
- Note 7 (CVACT02Y unused): COPY CVACT02Y at line 147; no CARD-RECORD fields referenced in PROCEDURE DIVISION ✓
- Notes 8–9 (unused account/customer fields): confirmed by inspection ✓
- Note 10 (typo "Authoriation"): line 5 confirmed ✓

**Unsupported notes:** None — all 10 notes are supported.

**Missing bugs:**
- The `ABEND-CODE/CULPRIT/REASON/MSG` fields from CSMSG02Y (Phase 1 WARN) are indeed absent from the source — the doc correctly calls them unused. However the ABEND fields are defined in CSMSG02Y copybook but not in this source — this is correctly noted in the doc.
- The STARTBR/READNEXT/READPREV-style browse from the PF7/PF8 context uses `IMS GNP` not VSAM browse — no additional latent bugs identified beyond what is documented.

#### S4 — Copybook Fields
Verified against actual copybook files:

**CIPAUSMY** (line 19–31 of cipausmy.cpy):
- `PA-ACCT-ID` PIC S9(11) COMP-3 — doc says `S9(11) COMP-3` ✓
- `PA-CREDIT-LIMIT` PIC S9(09)V99 COMP-3 ✓; `PA-CASH-LIMIT` PIC S9(09)V99 COMP-3 ✓
- `PA-APPROVED-AUTH-CNT` PIC S9(04) COMP — doc correctly notes COMP, not COMP-3 ✓
- `PA-AUTH-STATUS` PIC X(01), `PA-ACCOUNT-STATUS` PIC X(02) OCCURS 5 — doc notes "never read or used" ✓

**CIPAUDTY** (cipaudty.cpy):
- `PA-AUTHORIZATION-KEY` is a group field containing `PA-AUTH-DATE-9C` S9(05) COMP-3 and `PA-AUTH-TIME-9C` S9(09) COMP-3 ✓
- `PA-AUTH-RESP-CODE` PIC X(02) with 88 `PA-AUTH-APPROVED VALUE '00'` ✓
- `PA-TRANSACTION-AMT` PIC S9(10)V99 COMP-3, `PA-APPROVED-AMT` PIC S9(10)V99 COMP-3 ✓
- `PA-FRAUD-RPT-DATE` PIC X(08) ✓; `FILLER` PIC X(17) ✓

**COCOM01Y** inline extension:
- `CDEMO-CPVS-PAGE-NUM PIC S9(04) COMP` — doc correctly states S9(04) COMP ✓
- `CDEMO-CPVS-NEXT-PAGE-FLG` 88-levels `NEXT-PAGE-YES VALUE 'Y'` / `NEXT-PAGE-NO VALUE 'N'` confirmed at lines 123–125 ✓

No field discrepancies found.

#### S5 — External Calls
- **COPAUS1C**: `EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM) COMMAREA(CARDDEMO-COMMAREA)` at line 322–325. Doc correctly identifies this as XCTL and documents all commarea fields set before the call (CDEMO-TO-PROGRAM, CDEMO-FROM-TRANID, CDEMO-FROM-PROGRAM, CDEMO-PGM-CONTEXT, CDEMO-PGM-ENTER). Line reference 322 is accurate. ✓
- **COMEN01C**: Called from `RETURN-TO-PREV-SCREEN` (line 665–677) when PF3 pressed. Doc correctly describes this. ✓

#### S6 — Completeness
**Minor omissions:**
- `INITIALIZE-AUTH-DATA` (line 607–662) — this paragraph explicitly resets all 5 screen row slots with DFHBMPRO attribute bytes and SPACES. The doc mentions it is called (`PERFORM INITIALIZE-AUTH-DATA`) but does not describe what it does. Not a major omission for a migration developer.
- `POPULATE-HEADER-INFO` (line 726–747) — mentioned as called from `SEND-PAULST-SCREEN` but its content (FUNCTION CURRENT-DATE, title/date/time population) is not explicitly described. Also minor.
- `RECEIVE-PAULST-SCREEN` (line 711–722) — omitted from Section 2 description but its behavior is trivial (CICS RECEIVE MAP).

None of these omissions would prevent a Java developer from correctly implementing the program.

### Overall Recommendation
The document is safe to use as a migration reference. All control flow paths, error strings, COMP-3 monetary fields, and IMS interaction patterns are accurately described. The two warnings (missing description of `INITIALIZE-AUTH-DATA` internals and two migration notes without line citations) are cosmetic and do not affect correctness. The PII-in-memory note (migration note 9) and the CSMSG02Y-unused note are important for Java migration and are correctly called out.