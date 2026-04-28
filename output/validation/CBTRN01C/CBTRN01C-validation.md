# Validation Report: BIZ-CBTRN01C.md

**Overall: PASS** — 7 passed · 0 failed · 2 warned · 1 skipped

Source file: `494` lines   |   Document: `387` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: CBTRN01C.cbl |
| `line_number_bounds` | ✓ **PASS** | 63 line reference(s) checked, all within bounds (max: 494) |
| `backtick_identifiers` | ⚠ **WARN** | 35 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 6 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | ✓ **PASS** | All 6 Appendix A DDname(s) verified against source |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 4/8 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
35 backtick identifier(s) not found in source or copybooks

- `ACCOUNT-FILE`
- `CARD-FILE`
- `CUSTOMER-FILE`
- `DALYTRAN-FILE`
- `TRANSACT-FILE`
- `XREF-FILE`

## Phase 2 — LLM Judge

**Verdict: CONDITIONAL** — 5 passed · 0 failed · 1 warned · 0 skipped

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ✓ PASS | Diagnostic-stub characterization, 6-file open, read/lookup/close sequence all accurate |
| S2 Error Handling | ✓ PASS | Z-DISPLAY-IO-STATUS formatter, Z-ABEND-PROGRAM, and 9000 copy-paste bug all verified |
| S3 Migration Notes | ✓ PASS | All 8 migration notes verified against source lines and copybooks |
| S4 Copybook Fields | ✓ PASS | CVTRA06Y, CVACT03Y, and CVACT01Y fields verified; no COMP-3 in account record confirmed |
| S5 External Calls | ✓ PASS | CEE3ABD called with ABCODE=999 and TIMING=0 at line 473 verified |
| S6 Completeness | ⚠ WARN | Z-ABEND-PROGRAM paragraph naming differs from 9999-ABEND-PROGRAM convention in sibling programs — not noted |

### S1 — Program Flow: PASS

The document correctly characterizes CBTRN01C as a diagnostic stub: it opens all six files (DALYTRAN, XREFFILE, TRANSACT, ACCOUNT, CUSTFILE, CARDFILE) but performs no transaction processing — it only reads DALYTRAN records sequentially, looks up the XREF record by card number (RANDOM), then looks up the ACCOUNT record by account ID (RANDOM), and displays the results. The AT END loop condition and the paragraph chain (1000-PROCESS-DALYTRAN-FILE → 1100-READ-DALYTRAN-FILE → 1200-LOOKUP-XREF → 1300-LOOKUP-ACCOUNT) are correctly described.

### S2 — Error Handling: PASS

Z-DISPLAY-IO-STATUS is verified: it formats a two-byte file-status code into WS-RETURN-MSG by moving each byte separately into display positions. Z-ABEND-PROGRAM sets ABCODE=999, TIMING=0, then calls CEE3ABD USING ABCODE, TIMING. The copy-paste bug in 9000-DALYTRAN-CLOSE is verified at line 372: the paragraph name says "DALYTRAN CLOSE" but the error message text reads "ERROR CLOSING CUSTOMER FILE" and checks CUSTFILE-STATUS instead of DALYTRAN-FILE-STATUS — a latent defect correctly documented with source line citation.

### S3 — Migration Notes: PASS

All 8 migration notes verified:
- Diagnostic-stub characterization (4 of 6 opened files never read) confirmed.
- File-conflict risk (TRANSACT, ACCOUNT, CUSTFILE, CARDFILE opened here and in CBTRN02C) correctly flagged.
- CEE3ABD with ABCODE/TIMING confirmed at line 473.
- No COMP-3 fields in CVACT01Y (display-only account record) confirmed.
- CVACT01Y ACCT-EXPIRAION-DATE typo preserved in source — documented.

### S4 — Copybook Fields: PASS

CVTRA06Y daily-transaction record fields verified. CVACT03Y XREF-ACCT-ID is PIC 9(11) display (not COMP) — correctly described. CVACT01Y ACCOUNT-RECORD has no COMP-3 or COMP fields — all display numeric or alpha. The document's note that no packed-decimal fields exist in the account record is accurate and relevant for Java BigDecimal mapping.

### S5 — External Calls: PASS

Source line 473 confirmed: `CALL 'CEE3ABD' USING ABCODE TIMING`. ABCODE is PIC S9(9) COMP initialized to 999; TIMING is PIC S9(9) COMP initialized to 0. The document correctly describes both parameters and their values. No other CALL statements exist in CBTRN01C.

### S6 — Completeness: WARN

The abend paragraph is named `Z-ABEND-PROGRAM` in CBTRN01C, whereas sibling programs CBTRN02C and CBTRN03C use `9999-ABEND-PROGRAM`. This naming inconsistency is a minor signal that CBTRN01C may have a different lineage or was written separately. The migration notes discuss the abend mechanism but do not flag the cross-program naming divergence, which could matter during Java service extraction if abend-handling is factored into a shared utility.