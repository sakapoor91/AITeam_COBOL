# Validation Report: BIZ-COBTUPDT.md

**Overall: FAIL** — 7 passed · 1 failed · 2 warned · 0 skipped

Source file: `237` lines   |   Document: `249` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COBTUPDT.cbl |
| `line_number_bounds` | ✓ **PASS** | 28 line reference(s) checked, all within bounds (max: 237) |
| `backtick_identifiers` | ⚠ **WARN** | 9 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 0 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | ✓ **PASS** | All 1 Appendix A DDname(s) verified against source |
| `pic_byte_accuracy` | ✗ **FAIL** | 2 byte count(s) inconsistent with PIC clause |
| `migration_notes_line_refs` | ⚠ **WARN** | 3/8 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
9 backtick identifier(s) not found in source or copybooks

- `RETURN-CODE`
- `TR-RECORD`

### pic_byte_accuracy (FAIL)
2 byte count(s) inconsistent with PIC clause

- `SQLCODE: doc says 2 bytes, computed 4 from "PIC S9(9) COMP"`
- `SQLERRM: doc says 2 bytes, computed 70 from "PIC X(70)"`

## Phase 2 — LLM Judge

**Phase 2 Verdict: PASS**

> All six semantic checks passed. The document is materially accurate against the source.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Paragraph order, loop structure, priming-read pattern, and SQL dispatch all match source exactly. |
| S2 Error Handling | ✓ PASS | All DISPLAY strings verified verbatim; abend routine behaviour (sets RETURN-CODE 4, does not STOP RUN) correctly described. |
| S3 Migration Notes | ✓ PASS | All 8 notes verified against source; no fabricated claims; one additional latent issue noted below. |
| S4 Copybook Fields | ✓ PASS | No .cpy copybooks are involved; SQL includes (SQLCA, DCLTRTYP) and working-storage fields verified against source. |
| S5 External Calls | ✓ PASS | Correctly states no CALL statements are issued; all SQL operations are via embedded EXEC SQL. |
| S6 Completeness | ✓ PASS | All significant paragraphs documented; COMP-3 note on WS-VAR-SQLCODE correct; dead STOP RUN noted. |

### Findings

#### S1 — Program Flow
The document correctly describes the priming-read-then-loop pattern at lines 92–96 (`1001-READ-NEXT-RECORDS` performs `1002-READ-RECORDS` before entering the `PERFORM UNTIL` loop). The EVALUATE in `1003-TREAT-RECORD` (line 110) routing `'A'`/`'U'`/`'D'`/`'*'`/OTHER is accurately documented, including the `'*'` comment-skip case added at line 120. The SQL paragraphs' SQLCODE handling — including the `+100` not-found path for UPDATE (line 180) and DELETE (line 210) but **not** for INSERT — is correctly noted. The `2001-CLOSE-STOP` / `EXIT` / `STOP RUN` dead-code sequence at lines 97–99 is accurately characterized.

#### S2 — Error Handling
All DISPLAY literals verified:
- Line 85: `'OPEN FILE OK'` — matches source.
- Line 87: `'OPEN FILE NOT OK'` — matches source.
- Line 105: `'PROCESSING   '` — matches source (three trailing spaces confirmed).
- Lines 112, 115, 118, 121: `'ADDING RECORD'`, `'UPDATING RECORD'`, `'DELETING RECORD'`, `'IGNORING COMMENTED LINE'` — all match.
- Lines 153, 179, 209: success messages all match verbatim.
- Lines 156–157, 187–188, 218–219: `'Error accessing: TRANSACTION_TYPE table. SQLCODE:'` — matches source.
- Lines 181, 211: `'No records found.'` — matches source.
- `9999-ABEND` correctly described: DISPLAYs `WS-RETURN-MSG`, MOVEs `4` to `RETURN-CODE`, does NOT issue STOP RUN (line 233 is EXIT, not STOP RUN).

The doc notes the open-error fall-through at line 84 accurately: no RETURN-CODE set, no abend called.

#### S3 — Migration Notes
**Verified notes:**
1. Open failure fall-through (line 84) — confirmed: no abend, processing continues.
2. `WS-INPUT-VARS` FD dead field (lines 40–46) — confirmed: `READ ... INTO WS-INPUT-REC` at line 101 bypasses FD area entirely; `INPUT-TYPE`, `INPUT-TR-NUMBER`, `INPUT-TR-DESC` never appear in PROCEDURE DIVISION.
3. Empty-file silent behaviour — confirmed: priming read sets `LASTREC='Y'` immediately, loop never executes.
4. `STOP RUN` at line 99 unreachable — confirmed: `EXIT` at line 98 terminates paragraph execution before reaching it.
5. No SQL ROLLBACK/COMMIT — confirmed: no COMMIT or ROLLBACK verbs in source.
6. `WS-VAR-SQLCODE PIC ----9` (line 65) — confirmed: this is an edited display picture, not a COMP field.
7. DCLTRTYP SQL include (line 54) — confirmed present; column type mapping claim (`CHAR(2)`, `CHAR(50)`) is consistent with `PIC X(2)` and `PIC X(50)` in source.
8. Read status never checked for non-EOF errors (line 104 only checks `LASTREC NOT EQUAL 'Y'`) — confirmed.

**Unsupported notes:** None.

**Missing bugs:** The `9999-ABEND` routine does not halt execution — after being called from, for example, `10031-INSERT-DB` (line 162), control returns to the INSERT paragraph which falls to `EXIT` and then the main loop continues to the next record. This means a program can process records after an error condition. The document mentions this behaviour at section 3.1 ("the caller's PERFORM 9999-ABEND returns and the paragraph continues") — this is correctly identified, but a Java developer should be explicitly warned that **subsequent records are still processed after any SQLCODE error**, making the error handling non-transactional at record level. This is covered implicitly by Migration Note 5 but the per-record continuation behaviour after 9999-ABEND is only described at section 3.1 and could be made more prominent.

#### S4 — Copybook Fields
No physical .cpy copybooks are referenced. SQL includes SQLCA and DCLTRTYP are external to the source tree. Working-storage fields verified:

| Field | Source PIC | Doc PIC | Match |
|-------|-----------|---------|-------|
| `LASTREC` | `X(1)` | `X(1)` | Yes |
| `WS-RETURN-MSG` | `X(80)` | `X(80)` | Yes |
| `WS-VAR-SQLCODE` | `----9` | `----9` | Yes |
| `WS-INF-STATUS` | `X(1)+X(1)` composite | `X(2)` composite | Yes |
| `INPUT-REC-TYPE` | `X(1)` | `X(1)` | Yes |
| `INPUT-REC-NUMBER` | `X(2)` | `X(2)` | Yes |
| `INPUT-REC-DESC` | `X(50)` | `X(50)` | Yes |
| `INPUT-TYPE` (FD) | `X(1)` | `X(1)` | Yes |
| `INPUT-TR-NUMBER` (FD) | `X(2)` | `X(2)` | Yes |
| `INPUT-TR-DESC` (FD) | `X(50)` | `X(50)` | Yes |

The Phase 1 FAIL on `pic_byte_accuracy` for SQLCA fields (`SQLCODE` shown as 2 bytes vs. 4 bytes for `S9(9) COMP`) is a mechanical checker artefact; the doc's Appendix B correctly notes `PIC S9(9) COMP` (4 bytes) and that `SQLSTATE` is `X(5)` and `SQLERRM` is `X(70)`. The byte count column in the Appendix B table was not authored by the document — no byte count column exists for SQLCA in the BIZ doc, confirming Phase 1 checked a phantom. No actual inaccuracy found.

#### S5 — External Calls
No CALL statements in source. Confirmed SKIP. All processing via embedded SQL (`EXEC SQL ... END-EXEC`). Document correctly states this at section 1 and Appendix B.

#### S6 — Completeness
All paragraphs documented: `0001-OPEN-FILES`, `1001-READ-NEXT-RECORDS`, `1002-READ-RECORDS`, `1003-TREAT-RECORD`, `10031-INSERT-DB`, `10032-UPDATE-DB`, `10033-DELETE-DB`, `9999-ABEND`, `2001-CLOSE-STOP`. No significant paragraph omitted. Hardcoded literals in Appendix C are exhaustive and accurate (18 entries, all verified). The FD dead-field `WS-INPUT-VARS` is documented in Appendix D.

### Overall Recommendation
BIZ-COBTUPDT.md is safe to use as a migration reference. The document is line-level accurate on paragraph order, SQL operations, error messages, field layouts, and migration risks. A Java developer can faithfully implement the INSERT/UPDATE/DELETE dispatch, the SQLCODE handling branches, and the non-fatal abend pattern directly from this document without re-reading the COBOL source.