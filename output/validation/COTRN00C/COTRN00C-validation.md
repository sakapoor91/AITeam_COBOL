# Validation Report: BIZ-COTRN00C.md

**Overall: PASS** — 5 passed · 0 failed · 3 warned · 2 skipped

Source file: `699` lines   |   Document: `418` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COTRN00C.cbl |
| `line_number_bounds` | ✓ **PASS** | 64 line reference(s) checked, all within bounds (max: 699) |
| `backtick_identifiers` | ⚠ **WARN** | 4 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 8 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 2/10 migration note(s) missing line number citations |
| `mermaid_diagram` | ⚠ **WARN** | Mermaid diagram missing classDef colour definitions |

## Issues Found

### backtick_identifiers (WARN)
4 backtick identifier(s) not found in source or copybooks

- `HIGH-VALUES`
- `LOW-VALUES`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Startup, EIBCALEN check, commarea copy, first-entry auto-load, EVALUATE EIBAID, all paging paths, selection logic, and CICS RETURN all match the source accurately. |
| S2 Error Handling | ✓ PASS | All six error conditions (STARTBR NOTFND/OTHER, READNEXT ENDFILE/OTHER, READPREV ENDFILE/OTHER, ENDBR no-check, RECEIVE no-check, RETURN-TO-PREV-SCREEN) are correctly documented with accurate message strings. |
| S3 Migration Notes | ⚠ WARN | All 10 notes are supported by the source; one latent behaviour (commented-out GTEQ on STARTBR) not documented. |
| S4 Copybook Fields | ✓ PASS | CVTRA05Y, CSDAT01Y, COCOM01Y, and CDEMO-CT00-INFO fields all verified; unused-field annotations are correct. |
| S5 External Calls | ✓ PASS | COTRN01C (XCTL), COMEN01C (XCTL), and COSGN00C (XCTL fallback) all documented correctly with accurate commarea fields. |
| S6 Completeness | ⚠ WARN | Mermaid diagram missing `classDef` colour definitions (Phase 1 WARN confirmed). `INITIALIZE-TRAN-DATA` paragraph structure (EVALUATE WS-IDX with individual MOVE SPACES for each slot) not described; minor omission only. |

### Findings

#### S1 — Program Flow
`MAIN-PARA` (lines 95–141) verified:
- Flag initialisation at lines 97–100: `SET ERR-FLG-OFF`, `SET TRANSACT-NOT-EOF`, `SET NEXT-PAGE-NO`, `SET SEND-ERASE-YES` — doc Step 1 correct ✓
- `MOVE SPACES TO WS-MESSAGE ERRMSGO OF COTRN0AO` at lines 102–103; `MOVE -1 TO TRNIDINL OF COTRN0AI` at line 105 ✓
- EIBCALEN=0 → `MOVE 'COSGN00C' TO CDEMO-TO-PROGRAM`, `PERFORM RETURN-TO-PREV-SCREEN` at lines 108–109 ✓
- Commarea copy: `MOVE DFHCOMMAREA(1:EIBCALEN) TO CARDDEMO-COMMAREA` at line 111 ✓
- First-entry path (NOT CDEMO-PGM-REENTER): `SET CDEMO-PGM-REENTER TO TRUE`, `MOVE LOW-VALUES TO COTRN0AO`, `PERFORM PROCESS-ENTER-KEY`, `PERFORM SEND-TRNLST-SCREEN` at lines 113–116 ✓
- EVALUATE EIBAID: DFHENTER, DFHPF3, DFHPF7, DFHPF8, OTHER — all confirmed at lines 120–134 ✓

`PROCESS-ENTER-KEY` (lines 146–229) verified:
- EVALUATE TRUE scanning SEL0001I through SEL0010I (lines 149–182), storing sel flag and selected TRN-ID ✓
- `WHEN 'S' WHEN 's'` XCTL block at lines 186–195 ✓
- Invalid selection message `'Invalid selection. Valid value is S'` at line 199 ✓
- Note: the commented-out `* PERFORM SEND-TRNLST-SCREEN` at line 202 means that after setting the invalid-selection message, execution falls through to the TRNIDINI validation check and then calls `PROCESS-PAGE-FORWARD` — the page is refreshed anyway. The doc (Section 2.2 Step "Any other selection value") does not call out this fall-through behaviour, but it does not misstate it either. Minor omission only.
- TRNIDINI blank → `MOVE LOW-VALUES TO TRAN-ID` at line 207 ✓
- TRNIDINI non-blank, numeric → `MOVE TRNIDINI OF COTRN0AI TO TRAN-ID` at line 210 ✓
- TRNIDINI non-blank, non-numeric → `MOVE 'Y' TO WS-ERR-FLG`, message `'Tran ID must be Numeric ...'` at lines 212–214, `PERFORM SEND-TRNLST-SCREEN` at line 217 ✓
- `MOVE 0 TO CDEMO-CT00-PAGE-NUM`, `PERFORM PROCESS-PAGE-FORWARD` at lines 224–225 ✓

`PROCESS-PF7-KEY` (lines 234–252) verified:
- `IF CDEMO-CT00-TRNID-FIRST = SPACES OR LOW-VALUES MOVE LOW-VALUES TO TRAN-ID ELSE MOVE CDEMO-CT00-TRNID-FIRST TO TRAN-ID` at lines 236–240 ✓
- `SET NEXT-PAGE-YES TO TRUE` at line 242 ✓
- `IF CDEMO-CT00-PAGE-NUM > 1 PERFORM PROCESS-PAGE-BACKWARD ELSE MOVE 'You are already at the top of the page...' TO WS-MESSAGE` at lines 245–251 ✓

`PROCESS-PF8-KEY` (lines 257–274) verified:
- `IF CDEMO-CT00-TRNID-LAST = SPACES OR LOW-VALUES MOVE HIGH-VALUES TO TRAN-ID ELSE MOVE CDEMO-CT00-TRNID-LAST TO TRAN-ID` at lines 259–263 ✓
- `IF NEXT-PAGE-YES PERFORM PROCESS-PAGE-FORWARD ELSE MOVE 'You are already at the bottom of the page...' TO WS-MESSAGE` at lines 267–273 ✓

`PROCESS-PAGE-FORWARD` (lines 279–328) verified:
- Skip-record logic: `IF EIBAID NOT = DFHENTER AND DFHPF7 AND DFHPF3 PERFORM READNEXT-TRANSACT-FILE` at lines 285–287 ✓
- Initialize-then-fill loop at lines 289–303 ✓
- PAGE-NUM increment and next-page peek at lines 305–319 ✓
- ENDBR then SEND at lines 322–326 ✓

`PROCESS-PAGE-BACKWARD` (lines 333–376): `READPREV` loop fills slots 10 down to 1 at line 349 (`MOVE 10 TO WS-IDX`); decrements PAGE-NUM via SUBTRACT at line 364 — doc description is accurate ✓.

#### S2 — Error Handling
- **STARTBR NOTFND** (lines 605–611): `CONTINUE` followed by `SET TRANSACT-EOF TO TRUE`, message `'You are at the top of the page...'` — the `CONTINUE` is a no-op statement; execution continues to `SET TRANSACT-EOF` and subsequent statements in the same WHEN block. Doc's description is functionally accurate ✓.
- **STARTBR OTHER** (lines 612–618): `DISPLAY 'RESP:' WS-RESP-CD 'REAS:' WS-REAS-CD`, `MOVE 'Y' TO WS-ERR-FLG`, message `'Unable to lookup transaction...'` ✓
- **READNEXT ENDFILE** (lines 639–645): `CONTINUE` then `SET TRANSACT-EOF`, message `'You have reached the bottom of the page...'` — same CONTINUE pattern ✓
- **READNEXT OTHER** (lines 646–652): same DISPLAY + ERR pattern, message `'Unable to lookup transaction...'` ✓
- **READPREV ENDFILE** (lines 673–679): `CONTINUE` then `SET TRANSACT-EOF`, message `'You have reached the top of the page...'` ✓
- **READPREV OTHER** (lines 680–686): message `'Unable to lookup transaction...'` ✓
- **ENDBR** (lines 692–696): no RESP check — doc Section 3.4 correctly notes this ✓
- **RECEIVE** (lines 556–562): RESP/RESP2 captured but not checked — doc Section 3.5 correctly notes this ✓

#### S3 — Migration Notes
**Verified notes:**
- Note 1 (RECEIVE never checks WS-RESP-CD): lines 556–562 confirmed ✓
- Note 2 (ENDBR has no error check): lines 692–696 confirmed ✓
- Note 3 (skip-record logic on non-ENTER/PF7/PF3): `EIBAID NOT = DFHENTER AND DFHPF7 AND DFHPF3` at line 285 confirmed ✓
- Note 4 (CDEMO-CT00-PAGE-NUM PIC 9(08) arithmetic overflow risk): `PIC 9(08)` at line 65 confirmed ✓
- Note 5 (timestamp year extraction two-digit): `WS-TIMESTAMP-DT-YYYY(3:2)` at line 385 confirmed ✓
- Note 6 (commented-out `*ERASE` at line 546): confirmed ✓
- Note 7 (CDEMO-CT00-INFO inline extension not in COCOM01Y): lines 62–70 confirmed ✓
- Note 8 (unused CVTRA05Y fields): `TRAN-TYPE-CD`, `TRAN-CAT-CD`, etc. — POPULATE-TRAN-DATA (lines 381–445) uses only TRAN-ID, TRAN-AMT, TRAN-DESC, TRAN-ORIG-TS ✓
- Note 9 (WS-TRAN-AMT PIC `+99999999.99`): line 56 confirmed ✓
- Note 10 (no EXEC CICS HANDLE ABEND): confirmed; no HANDLE ABEND in source ✓

**Unsupported notes:** None.

**Missing latent issue:**
- `STARTBR-TRANSACT-FILE` at line 593–600 has `GTEQ` commented out (`* GTEQ` at line 597). Without GTEQ, STARTBR uses exact-match positioning — if the key does not exist, CICS will raise NOTFND. With GTEQ it would position at the next key greater than or equal to the requested value. The NOTFND branch handles this by setting TRANSACT-EOF and showing "You are at the top of the page" — which is functionally correct but semantically misleading (it means the exact key wasn't found, not that the file is empty or exhausted from the top). A Java developer who implements generic `>=` positioning would produce different results for partial-key searches. This should be called out as a migration note.

#### S4 — Copybook Fields
**CVTRA05Y**: Fields in doc Appendix B verified:
- `TRAN-ID PIC X(16)` — key field used as RIDFLD ✓
- `TRAN-AMT PIC S9(09)V99` — doc says `S9(09)V99` ✓ (note: doc omits COMP-3 or DISPLAY declaration; `TRAN-AMT` in CVTRA05Y is display format, not COMP-3 — no migration concern since WS-TRAN-AMT receives it as edited output)
- `TRAN-ORIG-TS PIC X(26)` — used for date extraction ✓
- `FILLER PIC X(20)` noted as "not used" ✓
- All unused fields correctly annotated ✓

**CSDAT01Y**: `WS-TIMESTAMP` used at line 384 to receive `TRAN-ORIG-TS`; subfields `WS-TIMESTAMP-DT-YYYY`, `WS-TIMESTAMP-DT-MM`, `WS-TIMESTAMP-DT-DD` used at lines 385–387. Doc correctly identifies these ✓.

**CDEMO-CT00-INFO extension**: `CDEMO-CT00-TRNID-FIRST PIC X(16)`, `CDEMO-CT00-TRNID-LAST PIC X(16)`, `CDEMO-CT00-PAGE-NUM PIC 9(08)`, `CDEMO-CT00-NEXT-PAGE-FLG PIC X(01)` with 88s `NEXT-PAGE-YES/NEXT-PAGE-NO`, `CDEMO-CT00-TRN-SEL-FLG PIC X(01)`, `CDEMO-CT00-TRN-SELECTED PIC X(16)` — all confirmed at lines 63–70 ✓.

**Appendix D internal fields**: `WS-REC-COUNT` and `WS-PAGE-NUM` marked "never used" — confirmed; no PROCEDURE DIVISION reference for either ✓.

#### S5 — External Calls
- **COTRN01C via XCTL** (lines 192–195): `EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM) COMMAREA(CARDDEMO-COMMAREA) END-EXEC` after `MOVE 'COTRN01C' TO CDEMO-TO-PROGRAM` at line 188. Doc correctly identifies this as XCTL with `CDEMO-CT00-TRN-SELECTED` carrying the transaction ID ✓. Pre-XCTL fields set: `CDEMO-TO-PROGRAM='COTRN01C'` (line 188), `CDEMO-FROM-TRANID=WS-TRANID` (line 189), `CDEMO-FROM-PROGRAM=WS-PGMNAME` (line 190), `CDEMO-PGM-CONTEXT=0` (line 191) ✓.
- **COMEN01C via XCTL** (RETURN-TO-PREV-SCREEN, line 518–521): `MOVE 'COMEN01C' TO CDEMO-TO-PROGRAM` at line 123, then `PERFORM RETURN-TO-PREV-SCREEN` ✓.
- **COSGN00C fallback** (RETURN-TO-PREV-SCREEN, lines 512–513): `IF CDEMO-TO-PROGRAM = LOW-VALUES OR SPACES MOVE 'COSGN00C' TO CDEMO-TO-PROGRAM` — doc Section 3.6 correctly describes this ✓.

#### S6 — Completeness
- **Mermaid diagram missing `classDef`**: Phase 1 WARN confirmed. The diagram uses `style` directives for individual nodes but has no `classDef` group definitions. Minor formatting issue; diagram is functionally readable.
- **`INITIALIZE-TRAN-DATA` paragraph** (lines 450–505): uses the same EVALUATE WS-IDX structure as POPULATE-TRAN-DATA to MOVE SPACES to the four fields of each slot. Doc Step 3 of PROCESS-PAGE-FORWARD says "Clears the ten display slots by calling `INITIALIZE-TRAN-DATA` for index 1 through 10" — this accurately describes what it does, though the implementation detail (that it uses an EVALUATE on WS-IDX rather than a table reference) is not shown. No migration impact.
- **`PROCESS-PAGE-BACKWARD` commarea update logic** (lines 359–368): the page-number decrement uses `SUBTRACT 1 FROM CDEMO-CT00-PAGE-NUM` guarded by `IF NEXT-PAGE-YES AND TRANSACT-NOT-EOF AND CDEMO-CT00-PAGE-NUM > 1` — if those conditions fail, sets page to 1. Doc Section 2.2 says "decrements `CDEMO-CT00-PAGE-NUM`" without capturing the fallback-to-1 logic. Minor omission.

### Overall Recommendation
The document is safe to use as a migration reference. All paging mechanics, selection logic, browse error handling, and commarea field layout are accurately documented. The two warnings are minor: (1) the Mermaid diagram lacks `classDef` declarations, and (2) the commented-out `GTEQ` on STARTBR is an undocumented behaviour difference that affects search semantics. A Java developer implementing this program should be informed that the browse uses exact-key positioning, not greater-than-or-equal positioning.