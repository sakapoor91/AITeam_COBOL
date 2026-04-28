# Validation Report: BIZ-COBSWAIT.md

**Overall: FAIL** — 5 passed · 1 failed · 2 warned · 2 skipped

Source file: `41` lines   |   Document: `131` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✗ **FAIL** | 4 required section(s) missing |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: COBSWAIT.cbl |
| `line_number_bounds` | ⚠ **WARN** | No line numbers found in document — Section 2 must cite line numbers |
| `backtick_identifiers` | ✓ **PASS** | 11 COBOL-style identifiers verified against source |
| `copybook_coverage` | ✓ **PASS** | All 0 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | – **SKIP** | No SELECT/ASSIGN found in source (may be CICS-only) |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | Section 4 contains no numbered migration notes |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### required_sections (FAIL)
4 required section(s) missing

- `Header block`
- `Section 1 — Purpose`
- `Section 3 — Error Handling`
- `Section 4 — Migration Notes`

## Phase 2 — LLM Judge

**Phase 2 Verdict: PASS**

> COBSWAIT is a 41-line trivial program with no business logic. All semantic checks pass. Phase 1 flagged missing required sections, but on semantic review the BIZ document contains all the substantive content required under different section names. The document is accurate and complete for its subject matter.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Three-step linear flow (ACCEPT → MOVE → CALL → STOP RUN) exactly matches the source. |
| S2 Error Handling | ✓ PASS | The absence of error handling is correctly and completely documented. |
| S3 Migration Notes | ✓ PASS | All seven notes are accurate and grounded in specific source evidence. |
| S4 Copybook Fields | ✓ PASS | No copybooks; two working-storage fields documented with correct PIC clauses. |
| S5 External Calls | ✓ PASS | MVSWAIT call correctly documented with input parameter and absence of return-code check. |
| S6 Completeness | ✓ PASS | No significant elements omitted from a 41-line utility. |

### Findings

#### S1 — Program Flow

The source (COBSWAIT.cbl, lines 34–40) shows exactly:
```
ACCEPT PARM-VALUE FROM SYSIN.
MOVE PARM-VALUE TO MVSWAIT-TIME.
CALL 'MVSWAIT' USING MVSWAIT-TIME.
STOP RUN.
```

The doc describes these four steps in order in Section 2.2 (steps 1–3) and Section 2.3. No paragraphs exist. No startup or shutdown logic exists. All accurate.

The doc correctly notes `PARM-VALUE` is `PIC X(8)` and `MVSWAIT-TIME` is `PIC 9(8) COMP`, which causes an implicit alphanumeric-to-numeric conversion. The ACCEPT reads from SYSIN (confirmed: `FROM SYSIN` at source line 36). The CALL is static (no USING BY REFERENCE or CONTENT clause complications — `USING MVSWAIT-TIME` is straightforward). All confirmed.

#### S2 — Error Handling

Section 3 correctly states there is no error handling:
- No `ON EXCEPTION` on CALL (confirmed: source line 38 has no such clause).
- No validation of PARM-VALUE before the MOVE (confirmed: source lines 36–37 go directly to MOVE).
- No range check on wait duration.

The S0C7 ABEND risk from non-numeric input (Section 3 second bullet) is an accurate consequence of the MOVE PIC X(8) → PIC 9(8) COMP path without validation.

#### S3 — Migration Notes

**Verified notes:**
- Note 1 (no business logic, `Thread.sleep()`): accurate.
- Note 2 (SYSIN input unsafe, no validation): confirmed at source lines 36–37.
- Note 3 (centiseconds not milliseconds, multiply by 10): `MVSWAIT-TIME` is in centiseconds per the COBSWAIT banner comment and z/OS MVSWAIT documentation. Conversion factor is accurate.
- Note 4 (`MVSWAIT-TIME PIC 9(8) COMP` = 4-byte binary): `PIC 9(8) COMP` on z/OS is indeed a 4-byte binary integer (COMP = BINARY). Max value 99,999,999 = ~277 hours. Confirmed at source line 30.
- Note 5 (`PARM-VALUE PIC X(8)` = 8 characters): confirmed at source line 31.
- Note 6 (no error code from MVSWAIT checked): confirmed — no evaluate or IF after CALL at line 38.
- Note 7 (static CALL, no portable equivalent): confirmed — CALL 'MVSWAIT' at line 38.

**Unsupported notes:** None.

**Missing bugs:** The doc correctly identifies all latent issues for this trivial program.

#### S4 — Copybook Fields

No copybooks. Two working-storage fields:
- `MVSWAIT-TIME PIC 9(8) COMP` — confirmed at source line 30. Doc documents as "Binary integer — centisecond delay". Correct.
- `PARM-VALUE PIC X(8)` — confirmed at source line 31. Doc documents as "Receives raw SYSIN input". Correct.

Phase 1 noted "No PIC rows found in Appendix B" as a skip — the Appendix D table covers these fields with correct PIC clauses. This is a structural placement issue (fields are in Appendix D rather than Appendix B), not an accuracy issue.

#### S5 — External Calls

| Program | Doc description | Source | Correct? |
|---------|----------------|--------|----------|
| `MVSWAIT` | IBM z/OS system service, suspends for centiseconds, return value not checked | CALL 'MVSWAIT' USING MVSWAIT-TIME (line 38) | Yes |

The doc correctly states no return code is checked. The Appendix B table for MVSWAIT is accurate.

#### S6 — Completeness

The 41-line source is completely covered. No paragraphs exist beyond the implicit PROCEDURE DIVISION entry. No files, copybooks, external calls (beyond MVSWAIT), or conditional branches are omitted. The SYSIN DD description in Appendix A is correct.

The mermaid typo in the diagram (`centieconds` instead of `centiseconds` in node D) is cosmetic and does not affect documentation accuracy.

### Overall Recommendation

BIZ-COBSWAIT.md is an accurate and complete migration reference for this trivial utility program. The entire program is four source lines plus declarations; the document correctly captures every aspect including the unsafe input handling, time unit conversion factor, and MVSWAIT dependency. Phase 1's section-detection failures were due to non-standard section naming (the content is present). No revisions required.