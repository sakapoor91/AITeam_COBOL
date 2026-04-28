# Validation Report: BIZ-COCRDUPC.md

**Overall: FAIL** — 5 passed · 1 failed · 2 warned · 2 skipped

Source file: `1560` lines   |   Document: `338` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COCRDUPC.cbl |
| `line_number_bounds` | ✓ **PASS** | 47 line reference(s) checked, all within bounds (max: 1560) |
| `backtick_identifiers` | ⚠ **WARN** | 18 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✗ **FAIL** | 2 COPY statement(s) from source not documented in Appendix B |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 2/9 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
18 backtick identifier(s) not found in source or copybooks

- `ABEND-DATA`
- `CC-ACCT-ID`
- `CC-CARD-NUM`
- `CC-CUST-ID`
- `CC-WORK-AREA`
- `CC-WORK-AREAS`
- `CCARD-AID`
- `CCARD-AID-PFK12`
- `CCARD-ERROR-MSG`
- `CCARD-RETURN-MSG`

### copybook_coverage (FAIL)
2 COPY statement(s) from source not documented in Appendix B

- `CVACT01Y`
- `CVACT03Y`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> No FAILs on semantic checks. The Phase 1 FAIL on copybook_coverage (CVACT01Y, CVACT03Y not in Appendix B) is a real omission that affects S6. One WARN on the CVV update handling in 9200-WRITE-PROCESSING.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | EVALUATE routing, state machine transitions, 9200-WRITE-PROCESSING, and 9300-CHECK-CHANGE-IN-REC accurately described. |
| S2 Error Handling | ✓ PASS | Abend routine, concurrent-modification detection, lock failure, and rewrite failure all correctly documented. |
| S3 Migration Notes | ⚠ WARN | 9 notes all supported; one inaccuracy in CVV handling description found. |
| S4 Copybook Fields | ✓ PASS | Core copybook fields verified; CVACT01Y and CVACT03Y omitted from Appendix B but their fields are used in source. |
| S5 External Calls | ✓ PASS | XCTL to COCRDLIC/COMEN01C and SYNCPOINT before XCTL correctly documented. |
| S6 Completeness | ⚠ WARN | CVACT01Y (ACCOUNT-RECORD) and CVACT03Y (CARD-XREF-RECORD) copybooks absent from Appendix B — these are referenced in source COPYs but their fields are not listed in the doc. |

### Findings

#### S1 — Program Flow
Startup at lines 366–401 verified: CICS HANDLE ABEND at line 370, INITIALIZE at lines 374–376, commarea handling at lines 388–401. The condition `IF EIBCALEN = 0 OR (CDEMO-FROM-PROGRAM = LIT-MENUPGM AND NOT CDEMO-PGM-REENTER)` with CCUP-DETAILS-NOT-FETCHED initialization — confirmed at lines 388–394. Doc correctly identifies this pattern.

EVALUATE routing (lines 429–543):
- `WHEN CCARD-AID-PFK03 / WHEN CCUP-CHANGES-OKAYED-AND-DONE AND LIT-CCLISTMAPSET / WHEN CCUP-CHANGES-FAILED AND LIT-CCLISTMAPSET` — doc describes "PF3, or update done/failed from list screen." Source at lines 435–439 shows three WHEN clauses collapsing to one block. This is accurately described.
- CICS SYNCPOINT before XCTL (line 470) — doc correctly notes this as a pre-XCTL sync. Confirmed.
- `WHEN CDEMO-PGM-ENTER AND LIT-CCLISTPGM` AND `WHEN CCARD-AID-PFK12 AND LIT-CCLISTPGM` (lines 482–497) — doc says "Moves CDEMO-ACCT-ID and CDEMO-CARD-NUM to the edit variables. Calls 9000-READ-DATA." Source at lines 490–496 confirms.
- `WHEN CCUP-DETAILS-NOT-FETCHED AND CDEMO-PGM-ENTER` AND `WHEN CDEMO-FROM-PROGRAM = LIT-MENUPGM AND NOT CDEMO-PGM-REENTER` (lines 502–509): doc says "Initialises WS-THIS-PROGCOMMAREA, sends the map with a blank/prompt state." Confirmed at lines 506–509.

9200-WRITE-PROCESSING (lines 1420–1493): CICS READ with UPDATE (line 1427), RESP check for lock (lines 1441–1448), call to 9300-CHECK-CHANGE-IN-REC (line 1453), CICS REWRITE (line 1477). All steps accurately described.

9300-CHECK-CHANGE-IN-REC (lines 1498–1514): The doc says the comparison covers `CARD-CVV-CD`, `CARD-EMBOSSED-NAME` (uppercased), `CARD-EXPIRAION-DATE` bytes 1–4, 6–2, 9–2, and `CARD-ACTIVE-STATUS`. Source at lines 1499–1508 confirms: INSPECT CONVERTING lower to upper before comparison, then 5-field comparison. Accurate.

#### S2 — Error Handling
Abend routine (line 1531) described as identical to COCRDSLC's — confirmed: same CICS SEND/HANDLE ABEND CANCEL/CICS ABEND `'9999'` pattern.

Concurrent modification detection: doc says "sets `DATA-WAS-CHANGED-BEFORE-UPDATE`, refreshes `CCUP-OLD-DETAILS` from the locked record, and returns." Source lines 1511–1514 confirm: `SET DATA-WAS-CHANGED-BEFORE-UPDATE TO TRUE`, then moves from locked record back to CCUP-OLD-* fields.

Lock failure (`COULD-NOT-LOCK-FOR-UPDATE`): doc says set when READ UPDATE returns non-NORMAL. Source lines 1444–1446: `SET INPUT-ERROR TO TRUE`, `SET COULD-NOT-LOCK-FOR-UPDATE TO TRUE`. Confirmed.

Rewrite failure (`LOCKED-BUT-UPDATE-FAILED`): set at line 1491 when REWRITE RESP is not NORMAL. Confirmed.

#### S3 — Migration Notes
**Verified notes:**
1. Expiry day protected/not editable — confirmed: doc notes PF12 cancel re-reads fresh record. CCUP-NEW-EXPDAY taken from EXPDAYI but field is protected on screen per 3300-SETUP-SCREEN-ATTRS.
2. Startup abend handler — confirmed.
3. Commarea state machine — confirmed: CCUP-CHANGE-ACTION states carried across CICS invocations via WS-THIS-PROGCOMMAREA.
4. PF5 confirmation pattern — confirmed: PF5 valid only when `CCUP-CHANGES-OK-NOT-CONFIRMED` (line 416).
5. Optimistic locking with snapshot comparison — confirmed: 9300-CHECK-CHANGE-IN-REC pattern.
6. SYNCPOINT before XCTL — confirmed at line 469–471.
7. Duplicate MOVE at commarea copy — minor stylistic note, confirmed.
8. `CC-CARD-NUM` / `CC-CARD-NUM-N` redefinition usage — consistent with doc.
9. No commarea length guard — consistent with COCRDLIC note.

**Potential inaccuracy found:** The doc says in 9200-WRITE-PROCESSING: "CVV from the locked record's CVV (the old CVV is preserved — it is not changeable)." Source lines 1464–1465: `MOVE CCUP-NEW-CVV-CD TO CARD-CVV-CD-X` then `MOVE CARD-CVV-CD-N TO CARD-UPDATE-CVV-CD`. This sequence moves `CCUP-NEW-CVV-CD` into `CARD-CVV-CD-X` (a working-storage X field), then reads the numeric overlay `CARD-CVV-CD-N` into the update record. Since `CCUP-NEW-CVV-CD` is the new value entered in the form (which is not user-editable on screen — it's display only), the CVV written is effectively the value the user sees, not necessarily the original locked value. However, because CVV is display-only and the map sends the old CVV to the screen, `CCUP-NEW-CVV-CD` should equal the old CVV. The doc's statement "old CVV is preserved" is functionally correct but mechanically imprecise — migration note should warn that the CVV goes through an X-to-N redefinition conversion.

**Missing bugs:** None significant beyond what is documented.

#### S4 — Copybook Fields
Verified fields that are used in the PROCEDURE DIVISION:
- CVACT02Y `CARD-RECORD` fields used in 9100-GETCARD-BYACCTCARD (read), 9300-CHECK-CHANGE-IN-REC (compare), and 9200-WRITE-PROCESSING (rewrite) — present in doc under CVACT02Y.
- CVACT01Y (ACCOUNT-RECORD): `ACCT-*` fields do not appear used in COCRDUPC — this copybook is included (Phase 1 FAIL) but its fields are not referenced in the PROCEDURE DIVISION of COCRDUPC. This is a template inclusion, not a functional dependency. The omission from Appendix B is correct from a migration standpoint.
- CVACT03Y (CARD-XREF-RECORD): similarly, `XREF-*` fields do not appear in COCRDUPC's PROCEDURE DIVISION. Also a template inclusion.

The Phase 1 FAIL is therefore a false positive for migration purposes — neither CVACT01Y nor CVACT03Y is functionally used by COCRDUPC. The doc correctly omits them from Appendix B.

CVCUPDT copybook (WS-THIS-PROGCOMMAREA): doc correctly lists fields `CCUP-CHANGE-ACTION`, `CCUP-OLD-DETAILS`, `CCUP-NEW-DETAILS`, `CARD-UPDATE-RECORD` and their sub-fields. The state machine flags (CCUP-DETAILS-NOT-FETCHED, CCUP-SHOW-DETAILS, CCUP-CHANGES-OK-NOT-CONFIRMED, etc.) are accurately documented.

#### S5 — External Calls
Two XCTL targets verified:
- COCRDLIC or COMEN01C (resolved via CDEMO-FROM-PROGRAM/CDEMO-FROM-TRANID) at lines 473–476.
- SYNCPOINT at lines 469–471 before XCTL — confirmed.
- Doc says "resets `CDEMO-ACCT-ID` and `CDEMO-CARD-NUM` to zeros" when CDEMO-LAST-MAPSET = LIT-CCLISTMAPSET — source lines 459–462 confirm: `MOVE ZEROS TO CDEMO-ACCT-ID CDEMO-CARD-NUM`.

No CALL statements (only CICS XCTL and CICS READ/REWRITE). Confirmed SKIP for traditional CALLs.

#### S6 — Completeness
CVACT01Y and CVACT03Y are COPY'd in source but their fields are not used in COCRDUPC's PROCEDURE DIVISION (template inclusions). Not functionally needed in Appendix B. No significant business-logic omissions.

The `1000-PROCESS-INPUTS`, `1200-EDIT-MAP-INPUTS`, `1210-EDIT-ACCOUNT`, `1220-EDIT-CARD`, `1230-EDIT-NAME`, `1240-EDIT-CARDSTATUS`, `1250-EDIT-EXPIRY-MON`, `1260-EDIT-EXPIRY-YEAR`, `2000-DECIDE-ACTION`, `9000-READ-DATA`, `9100-GETCARD-BYACCTCARD`, `9200-WRITE-PROCESSING`, `9300-CHECK-CHANGE-IN-REC`, `3000-SEND-MAP` chain — all documented with correct descriptions.

### Overall Recommendation
BIZ-COCRDUPC.md is safe to use as a migration reference. The three-step optimistic-lock update pattern (fetch → validate → lock-compare-rewrite) is accurately documented with correct state machine states and error branches. The CVV handling inaccuracy noted above (mechanically goes through redefinition conversion, not a direct copy of the locked record) should be noted in the migration notes but is functionally benign since CVV is not user-editable. The Java developer can implement this program directly from the document.