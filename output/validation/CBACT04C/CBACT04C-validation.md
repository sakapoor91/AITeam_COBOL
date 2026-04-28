# Validation Report: BIZ-CBACT04C.md

**Overall: PASS** — 7 passed · 0 failed · 2 warned · 1 skipped

Source file: `652` lines   |   Document: `445` lines

> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).

## Phase 1 — Mechanical Checks

| Check | Status | Detail |
|-------|--------|--------|
| `required_sections` | ✓ **PASS** | All 12 required sections present |
| `no_cobol_blocks` | ✓ **PASS** | No raw COBOL code blocks found |
| `source_file_exists` | ✓ **PASS** | Source found: CBACT04C.cbl |
| `line_number_bounds` | ✓ **PASS** | 51 line reference(s) checked, all within bounds (max: 652) |
| `backtick_identifiers` | ⚠ **WARN** | 31 backtick identifier(s) not found in source or copybooks |
| `copybook_coverage` | ✓ **PASS** | All 5 copybook(s) referenced in source appear in document |
| `ddname_accuracy` | ✓ **PASS** | All 5 Appendix A DDname(s) verified against source |
| `pic_byte_accuracy` | – **SKIP** | No PIC rows found in Appendix B |
| `migration_notes_line_refs` | ⚠ **WARN** | 1/10 migration note(s) missing line number citations |
| `mermaid_diagram` | ✓ **PASS** | Mermaid flowchart with classDef styles present |

## Issues Found

### backtick_identifiers (WARN)
31 backtick identifier(s) not found in source or copybooks

- `ACCOUNT-FILE`
- `CURRENT-DATE`
- `DISCGRP-FILE`
- `EXP-TRAN-AMT`
- `TCATBAL-FILE`
- `TRANSACT-FILE`
- `XREF-FILE`

## Phase 2 — LLM Judge

**Phase 2 Verdict: CONDITIONAL**

> CONDITIONAL = passed on all FAIL checks but has warnings requiring attention.

| Check | Status | Summary |
|-------|--------|---------|
| S1 Program Flow | ⚠ WARN | Main loop structure is mostly accurate but the doc's description of the ELSE-branch final-account-update mechanism slightly misrepresents COBOL PERFORM UNTIL semantics. |
| S2 Error Handling | ✓ PASS | All error messages and paths verified; one important note that XREF open error also calls 9910-DISPLAY-IO-STATUS (not documented). |
| S3 Migration Notes | ✓ PASS | All 10 notes verified against source; no significant latent bugs missing. |
| S4 Copybook Fields | ✓ PASS | All copybook fields across 5 copybooks verified; PIC clauses and COMP-3/display flags accurate. |
| S5 External Calls | ✓ PASS | CEE3ABD correctly described; no other external CALLs. |
| S6 Completeness | ⚠ WARN | `PARM-LENGTH` is documented as never used — verified. `1400-COMPUTE-FEES` stub is documented. The `Z-GET-DB2-FORMAT-TIMESTAMP` utility paragraph is documented. Minor: `WS-RECORD-COUNT` increment and DISPLAY of record in main loop correctly noted. |

### Findings

#### S1 — Program Flow

The five open calls in order (`0000-TCATBALF-OPEN`, `0100-XREFFILE-OPEN`, `0200-DISCGRP-OPEN`, `0300-ACCTFILE-OPEN`, `0400-TRANFILE-OPEN`) match source lines 182–186. PASS.

Main loop (lines 188–222): The PERFORM UNTIL structure and the nested IF logic are complex. The doc states (section 2.2, "Important structural note"): "The ELSE branch of the outer loop (lines 219–221) handles the case where `END-OF-FILE` has been set to `'Y'`." 

Examining the source carefully:
```
PERFORM UNTIL END-OF-FILE = 'Y'
    IF END-OF-FILE = 'N'
        PERFORM 1000-TCATBALF-GET-NEXT
        IF END-OF-FILE = 'N'
            ... (main processing) ...
        END-IF
    ELSE
        PERFORM 1050-UPDATE-ACCOUNT   ← line 220
    END-IF
END-PERFORM
```

The ELSE at line 219 fires when `END-OF-FILE = 'N'` is false *within the loop body* — meaning `END-OF-FILE` was just set to `'Y'` by `1000-TCATBALF-GET-NEXT`. The PERFORM UNTIL condition is checked at the top of each iteration; this ELSE fires on the last iteration where the read sets EOF=Y. The doc's explanation is functionally correct. The doc's description of this as "before the loop truly exits" captures the correct outcome but could be read as implying the ELSE fires *after* the PERFORM UNTIL exits — it actually fires on the iteration that causes the exit condition to become true *on the next check*. This is a WARN not a FAIL as the functional description of the final account update being guaranteed is accurate.

Account-boundary detection (lines 194–206): Doc describes correctly — `IF TRANCAT-ACCT-ID NOT= WS-LAST-ACCT-NUM` triggers the boundary check. If not first time, calls `1050-UPDATE-ACCOUNT`, then resets `WS-TOTAL-INT`, updates `WS-LAST-ACCT-NUM`, and calls `1100-GET-ACCT-DATA` and `1110-GET-XREF-DATA`. Source lines 194–205 confirm this sequence. PASS.

Key placement detail: The DISCGRP key assembly at source lines 210–212 (`MOVE ACCT-GROUP-ID TO FD-DIS-ACCT-GROUP-ID`, `MOVE TRANCAT-CD TO FD-DIS-TRAN-CAT-CD`, `MOVE TRANCAT-TYPE-CD TO FD-DIS-TRAN-TYPE-CD`) happens OUTSIDE the account-change IF block. This means for EVERY row (same account or new account), the DISCGRP key is reassembled and `1200-GET-INTEREST-RATE` is called. The doc documents this correctly in step 13 (the key is built from current-row fields and interest rate is fetched per row). PASS.

The `DIS-INT-RATE NOT = 0` check at line 214 gates both `1300-COMPUTE-INTEREST` and `1400-COMPUTE-FEES`. Doc's step 14 correctly states this — PASS. Note: `1400-COMPUTE-FEES` is called only when rate ≠ 0, not unconditionally. The doc's mermaid diagram shows this correctly.

`Z-GET-DB2-FORMAT-TIMESTAMP` paragraph (lines 613–626): Doc section 3.4 describes it accurately — `FUNCTION CURRENT-DATE` called, DB2 timestamp assembled with hardcoded `'-'` and `'.'` separators, `DB2-REST = '0000'`. All MOVE statements verified. PASS.

#### S2 — Error Handling

**Verified error messages:**
- `'ERROR OPENING TRANSACTION CATEGORY BALANCE'` (line 245) — PASS
- `'ERROR OPENING CROSS REF FILE'` (line 263) with `XREFFILE-STATUS` concatenated directly — doc section 2.1 step 3 correctly identifies this inconsistency as Migration Note 1 — PASS
- `'ERROR OPENING DALY REJECTS FILE'` (line 281) — doc correctly identifies this as a copy-paste artifact with misspelled 'DALY' (Migration Note 2) — PASS
- `'ERROR OPENING ACCOUNT MASTER FILE'` (line 300) — PASS
- `'ERROR OPENING TRANSACTION FILE'` (line 318) — PASS
- `'ERROR READING TRANSACTION CATEGORY FILE'` (line 342) — PASS
- `'ACCOUNT NOT FOUND: '` with `FD-ACCT-ID` (line 375) — PASS
- `'ACCOUNT NOT FOUND: '` with `FD-XREF-ACCT-ID` (line 397) — PASS
- `'ERROR READING ACCOUNT FILE'` (line 386) — PASS
- `'ERROR READING XREF FILE'` (line 408, note: source says `'ERROR READING XREF FILE'` not `'ERROR READING CROSS REF FILE'`) — doc says `'ERROR READING XREF FILE'` in section 3.1 non-fatal paths and Appendix C. PASS.
- `'DISCLOSURE GROUP RECORD MISSING'` (line 418) — PASS
- `'TRY WITH DEFAULT GROUP CODE'` (line 419) — PASS
- `'ERROR READING DISCLOSURE GROUP FILE'` (line 431) — PASS
- `'ERROR READING DEFAULT DISCLOSURE GROUP'` (line 455) — PASS
- `'ERROR RE-WRITING ACCOUNT FILE'` (line 365) — PASS
- `'ERROR WRITING TRANSACTION RECORD'` (line 510) — PASS

One observation: The doc states the XREF open error at line 263 "concatenates `XREFFILE-STATUS` directly rather than using `9910-DISPLAY-IO-STATUS`." However the source at line 264–265 shows that `MOVE XREFFILE-STATUS TO IO-STATUS` and `PERFORM 9910-DISPLAY-IO-STATUS` are called after the concatenated display. So the error path uses BOTH the concatenated display AND the formatted status decoder. The doc only mentions the raw concatenation but not the subsequent formatted display — this is the same minor issue noted in CBACT01C OUTFILE open. WARN.

The INVALID KEY non-fatal behaviour for `1100-GET-ACCT-DATA` and `1110-GET-XREF-DATA` is correctly documented (section 3.1, Migration Note 3).

#### S3 — Migration Notes

**Verified notes:**
- Note 1 (XREF open inconsistent error format, line 263): Confirmed — `DISPLAY 'ERROR OPENING CROSS REF FILE'   XREFFILE-STATUS` at line 263 does concatenate directly. The follow-up `9910-DISPLAY-IO-STATUS` call also exists (lines 264–265) but doc's point about raw concatenation being inconsistent with other paths is accurate.
- Note 2 (`'DALY REJECTS FILE'` at line 281): Confirmed — source line 281 reads `DISPLAY 'ERROR OPENING DALY REJECTS FILE'`.
- Note 3 (INVALID KEY non-fatal, stale data): Confirmed — lines 373–376 and 395–398. No abend in INVALID KEY path.
- Note 4 (`1400-COMPUTE-FEES` stub): Confirmed — source lines 518–520 contain only `EXIT`.
- Note 5 (`WS-MONTHLY-INT` and `WS-TOTAL-INT` display format): Confirmed in source lines 168–169 — no USAGE clause, so display format.
- Note 6 (divisor 1200): Confirmed — source line 465 `= ( TRAN-CAT-BAL * DIS-INT-RATE) / 1200`.
- Note 7 (final account update via ELSE path): Confirmed — source lines 219–220.
- Note 8 (`TRAN-AMT` display in CVTRA05Y, COMP-3 in CVEXPORT): CVTRA05Y.cpy line 10 confirms `TRAN-AMT PIC S9(09)V99` — no COMP-3. PASS.
- Note 9 (`WS-TRANID-SUFFIX` 6-digit counter): Confirmed at source line 173 `PIC 9(06) VALUE 0`.
- Note 10 (`1200-A-GET-DEFAULT-INT-RATE` read without explicit KEY IS clause): Confirmed at source line 444 — `READ DISCGRP-FILE INTO DIS-GROUP-RECORD` with no `KEY IS` clause. PASS.

**Unsupported notes:** None.

**Missing bugs:** One additional latent issue not documented: `PARM-LENGTH` is never checked or validated before accessing `PARM-DATE`. If the JCL PARM is shorter than 10 bytes, `PARM-DATE` will contain garbage. The doc notes `PARM-LENGTH` is "never checked" (Appendix D) — this is documented. PASS.

#### S4 — Copybook Fields

**CVTRA01Y** (5 fields verified):
- `TRANCAT-ACCT-ID` PIC 9(11) — PASS (line 6)
- `TRANCAT-TYPE-CD` PIC X(02) — PASS (line 7)
- `TRANCAT-CD` PIC 9(04) — PASS (line 8)
- `TRAN-CAT-BAL` PIC S9(09)V99 — PASS (line 9); doc states "display format — not COMP-3" — PASS
- `FILLER` PIC X(22) — PASS (line 10)

**CVACT03Y** — see CBACT03C verification. All verified.

**CVTRA02Y** (6 fields verified):
- `DIS-ACCT-GROUP-ID` PIC X(10) — PASS (line 6)
- `DIS-TRAN-TYPE-CD` PIC X(02) — PASS (line 7)
- `DIS-TRAN-CAT-CD` PIC 9(04) — PASS (line 8)
- `DIS-INT-RATE` PIC S9(04)V99 — PASS (line 9); doc states "display format, not COMP-3" — PASS
- `FILLER` PIC X(28) — PASS (line 10)

**CVACT01Y** — see CBACT01C verification. Field usage table (which fields are used vs. unused) verified: `ACCT-ACTIVE-STATUS`, `ACCT-CREDIT-LIMIT`, `ACCT-CASH-CREDIT-LIMIT`, `ACCT-OPEN-DATE`, `ACCT-EXPIRAION-DATE`, `ACCT-REISSUE-DATE`, `ACCT-ADDR-ZIP` marked unused — all confirmed; none appear in MOVE statements within CBACT04C source.

**CVTRA05Y** (14 fields verified):
- `TRAN-ID` PIC X(16) — PASS (line 5)
- `TRAN-TYPE-CD` PIC X(02) — PASS (line 6)
- `TRAN-CAT-CD` PIC 9(04) — PASS (line 7)
- `TRAN-SOURCE` PIC X(10) — PASS (line 8)
- `TRAN-DESC` PIC X(100) — PASS (line 9)
- `TRAN-AMT` PIC S9(09)V99 — PASS, no COMP-3 (line 10)
- `TRAN-MERCHANT-ID` PIC 9(09) — PASS (line 11)
- `TRAN-MERCHANT-NAME` PIC X(50) — PASS (line 12)
- `TRAN-MERCHANT-CITY` PIC X(50) — PASS (line 13)
- `TRAN-MERCHANT-ZIP` PIC X(10) — PASS (line 14)
- `TRAN-CARD-NUM` PIC X(16) — PASS (line 15)
- `TRAN-ORIG-TS` PIC X(26) — PASS (line 16)
- `TRAN-PROC-TS` PIC X(26) — PASS (line 17)
- `FILLER` PIC X(20) — PASS (line 18)

Doc correctly states `FILLER PIC X(20)` not referenced. PASS.

#### S5 — External Calls

**CEE3ABD**: Called at line 632 `CALL 'CEE3ABD' USING ABCODE, TIMING`. Doc cites paragraph `9999-ABEND-PROGRAM`, line 632 — PASS.

No other CALL statements in the source (no COBDATFT in this program). PASS.

#### S6 — Completeness

All significant paragraphs documented including `Z-GET-DB2-FORMAT-TIMESTAMP` (section 3.4) which is non-trivial. The DB2 timestamp REDEFINES structure in working storage (lines 151–165) is noted and its field naming (`DB2-STREEP-1` for the date separator) is unusual — doc calls these `'-'` date separators which is functionally correct.

`WS-RECORD-COUNT` increment at line 192 and `DISPLAY TRAN-CAT-BAL-RECORD` at line 193 are covered in step 8. PASS.

Commented-out DISPLAY statements at lines 207–209 are not mentioned in the doc — these are pure debugging artifacts with no migration impact; omitting them is acceptable.

### Overall Recommendation

The document is accurate and safe to use as a migration reference for CBACT04C. The most critical migration concerns — the INVALID KEY non-fatal paths causing stale data propagation, the unimplemented fees stub, the final-account ELSE-branch update, the interest formula and precision, and the DISCGRP error message misnomer — are all correctly documented with source line citations. The S1 warning about the PERFORM UNTIL ELSE semantics is explanatory nuance, not a factual error. A Java developer can safely translate from this document but should verify the PERFORM UNTIL ELSE boundary handling independently when implementing the final-account update.