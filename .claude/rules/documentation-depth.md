# Documentation Depth Rules

These rules define the quality bar for every `BIZ-*.md` file.
Full detail is in `business-docs/DOCUMENTATION-STANDARD.md`.

## Rules

1. **All nine sections are mandatory.** Every BIZ-*.md must contain: header block, Section 1 Purpose, Section 2 Program Flow (with subsections 2.1/2.2/2.3), Section 3 Error Handling, Section 4 Migration Notes, and Appendices A through E. A document missing any section is incomplete.

2. **No raw COBOL code blocks.** Never include `MOVE`, `WRITE`, `IF`, `PERFORM`, `CALL`, or other COBOL statements in a fenced code block. Describe every action in plain English. Field names and paragraph names may appear as inline `backtick identifiers`.

3. **Every COPY statement must be resolved inline.** Read the actual `.cpy` file and list every field with its PIC clause and byte count. Never write "see copybook" or "fields defined in COPYBOOK-NAME".

4. **Every paragraph reference must cite the source line number.** "Paragraph `OPEN-FILES` opens the account file" is incomplete. "`OPEN-FILES` (line 87) opens the account file for input" is correct.

5. **Every 88-level value must be decoded by name and literal.** Show what the literal means (e.g. `CODATECN-TYPE VALUE '2'` → `YYYY-MM-DD-IN`), not just the field name.

6. **Unused copybook fields must be called out explicitly.** Name every field that exists in a copybook but is never referenced by the program. This is a migration risk — unused fields cause confusion during Java translation.

7. **Every COMP-3 field must be flagged.** Mark every USAGE COMP-3 / PACKED-DECIMAL field with "(COMP-3 — use BigDecimal in Java)". These are the most common source of migration data-loss bugs.

8. **Typos in field names must be preserved and noted.** If the COBOL source has `ACCT-EXPIRAION-DATE` (missing letter), write the exact misspelled name in the document and add "(typo)". Do not silently correct it.

9. **Migration Notes must cite line numbers.** Every item in Section 4 must reference the source line where the issue occurs. "Unhandled status code" is useless without a line number.

10. **Security-relevant fields must be flagged.** Plaintext passwords, hardcoded credentials, and PII fields (account numbers, card numbers, SSNs) must be called out in Section 4 with a security migration note.

## Rationale
A BIZ-*.md that omits any of these details forces developers to go back to the COBOL source — defeating the purpose of the document. The document must be the single source of truth for field-level migration decisions.
