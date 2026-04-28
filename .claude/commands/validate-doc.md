Run the full two-phase validation pipeline on a BIZ-*.md document: $ARGUMENTS

## Phase 1 — Mechanical checks (runs automatically)

```bash
cd output/business-docs
python tools/validate_doc.py $ARGUMENTS --report
```

This checks:
- All 12 required sections present
- No raw COBOL code blocks
- Source file exists
- Every line number cited is within the actual source line count
- Every backtick identifier exists in the .cbl or its .cpy files
- All COPYed copybooks documented in Appendix B
- DDnames in Appendix A appear in SELECT/ASSIGN statements
- PIC clause byte counts are mathematically consistent
- Section 4 migration notes cite line numbers
- Mermaid diagram has classDef colour definitions

Results are written to `output/validation/$ARGUMENTS/$ARGUMENTS-validation.md`.

## Phase 2 — LLM-as-judge (use the validator agent)

After Phase 1 completes, invoke the `validator` agent for semantic accuracy checks:
- S1: Program flow matches actual PERFORM structure
- S2: Error handling descriptions match source DISPLAY strings
- S3: Every migration note is supported by source evidence
- S4: Copybook field tables match the actual .cpy files
- S5: External program call details are accurate
- S6: No significant omissions in coverage

The validator reads both the source and the document, cites specific line numbers for every finding, and appends its verdict to the validation report.

## Phase 3 — Convert to DOCX

After both phases complete:

```bash
python output/business-docs/tools/_md_to_docx.py \
  output/validation/$ARGUMENTS/$ARGUMENTS-validation.md \
  output/validation/$ARGUMENTS/$ARGUMENTS-validation.docx
```

## Combined verdict

| Phase 1 | Phase 2 | Document status |
|---------|---------|-----------------|
| PASS | PASS | Ready to use as migration reference |
| PASS | CONDITIONAL | Usable with noted caveats |
| PASS | FAIL | Semantic errors — document needs revision |
| FAIL | — | Structural errors — fix Phase 1 issues first |

## Batch validation

To validate all programs with existing docs:
```bash
cd output/business-docs
for prog in $(ls -d */ | sed 's|/||' | grep -v tools); do
  python tools/validate_doc.py $prog --report 2>/dev/null && echo "OK: $prog" || echo "FAIL: $prog"
done
```
