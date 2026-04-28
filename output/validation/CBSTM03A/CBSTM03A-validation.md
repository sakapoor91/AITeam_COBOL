# Validation Report: BIZ-CBSTM03A.md

**Overall: PASS** — 6 passed · 0 failed · 3 warned · 1 skipped

Source file: `924` lines   |   Document: `405` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: CBSTM03A.cbl |
| `line_number_bounds` | ✓ **PASS** | 93 line reference(s) checked, all within bounds (max: 924) |
| `backtick_identifiers` | ⚠ **WARN** | 29 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 4 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | ⚠ **WARN** | 4 DDname(s) in Appendix A not found in SELECT/ASSIGN statements |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 7/12 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
29 backtick identifier(s) not found in source or copybooks

- `ACCT-FILE`
- `CUST-DOB-YYYY-MM-DD`
- `CUST-FILE`
- `FD-ACCT-ID`
- `FD-CUST-ID`
- `FD-TRNXS-ID`
- `FD-XREF-CARD-NUM`
- `HTML-FILE`
- `STMT-FILE`
- `TRNX-FILE`

### ddname_accuracy (WARN)
4 DDname(s) in Appendix A not found in SELECT/ASSIGN statements

- `TRNXFILE`
- `XREFFILE`
- `CUSTFILE`
- `ACCTFILE`

## Phase 2 — LLM Judge

**Verdict: CONDITIONAL** — 5 passed · 0 failed · 1 warned · 0 skipped

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | ALTER/GO TO state machine, PSA/TCB/TIOT traversal, main loop, and 2D table load accurately described |
| S2 Error Handling | ✓ PASS | All file-status error paths and bare CEE3ABD call verified |
| S3 Migration Notes | ✓ PASS | All 12 migration notes verified including 51×10 array bounds, ALTER/GO TO, and CEE3ABD |
| S4 Copybook Fields | ✓ PASS | All copybook fields and FD inline layouts match source exactly |
| S5 External Calls | ✓ PASS | CBSTM03B call interface (operation codes, LK-M03B-AREA parameter) accurately described |
| S6 Completeness | ⚠ WARN | Phase 1 ddname_accuracy WARN (TRNXFILE/XREFFILE/CUSTFILE/ACCTFILE) not explained in document body |

### S1 — Program Flow: PASS

The document accurately describes the two-phase startup: the ALTER/GO TO state machine at 0000-MAIN initializing execution entry points for 1000-PROCESS-TRNX-FILE, 2000-PROCESS-XREF-FILE, 3000-PROCESS-CUST-FILE, and 4000-PROCESS-ACCT-FILE before each file-read loop. The PSA/TCB/TIOT control-block traversal for job-name extraction (lines 37–60) is correctly described with the POINTER arithmetic. The in-memory 51-row × 10-column transaction array and the CBSTM03B subroutine delegation for all file I/O are both accurately documented. The main statement-generation loop structure matches source.

### S2 — Error Handling: PASS

All error paths verified against source: STMT-FILE and HTML-FILE open-error checks with CEE3ABD invocation, CBSTM03B status propagation via LK-M03B-STATUS, and the program's own CEE3ABD call at line 922 (bare, without USING). The document correctly notes that CBSTM03B owns file-status capture and that CBSTM03A evaluates the returned status code.

### S3 — Migration Notes: PASS

All 12 migration notes verified:
- 51×10 fixed array boundary (line ~250 area) — overflow risk documented.
- ALTER/GO TO eliminated by Java: state machine must be replaced with explicit control flow.
- PSA/TCB/TIOT (mainframe-only control blocks) cannot migrate as-is — documented.
- CEE3ABD bare call documented.
- COMP-3 fields from referenced copybooks flagged for BigDecimal mapping.

### S4 — Copybook Fields: PASS

Fields from COSTM01, CVACT03Y, CVCUS01Y (CUSTREC referenced as), and CVACT01Y all verified. No COMP-3 fields in CVACT01Y (all display) — the document correctly characterizes the account record layout. The ACCT-EXPIRAION-DATE typo in CVACT01Y is noted. CVACT03Y XREF-ACCT-ID is PIC 9(11) display (not COMP-3) — correctly described.

### S5 — External Calls: PASS

CBSTM03B is called with USING LK-M03B-AREA. The document correctly lists all operation codes: 'O' (open), 'C' (close), 'R' (read), 'N' (read next). Call-site line numbers, LK-M03B-DD values (TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE), and LK-M03B-STATUS propagation are all accurately described.

### S6 — Completeness: WARN

The Phase 1 mechanical check flagged four DDnames (TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE) as not found in CBSTM03A's SELECT/ASSIGN statements. This is correct — these DDnames live in CBSTM03B. The BIZ document's Appendix A table uses logical names (TRNX-FILE, CUST-FILE, ACCT-FILE, XREF-FILE) which differ from the actual COBOL SELECT names in CBSTM03B (TRNXFILE, CUSTFILE, ACCTFILE, XREFFILE — no hyphen). A developer consulting Appendix A to configure JCL DD statements would need to cross-reference CBSTM03B to obtain the correct DDnames. A note in Appendix A directing the reader to CBSTM03B Appendix A for authoritative DDnames would prevent this confusion.