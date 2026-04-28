Generate BIZ-*.md documentation for every COBOL program that does not yet have one.

## Steps

1. Find all COBOL programs:
   ```bash
   ls source/cobol/*.cbl source/cobol/*.CBL 2>/dev/null | xargs -I{} basename {} | sed 's/\.[Cc][Bb][Ll]$//'
   ```

2. Find programs that already have a BIZ-*.md:
   ```bash
   ls output/business-docs/*/BIZ-*.md 2>/dev/null | xargs -I{} basename {} | sed 's/BIZ-//' | sed 's/\.md$//'
   ```

3. Compute the difference — programs with source but no doc.

4. For each missing program, run `/document PROGNAME` (or use the documenter agent in parallel batches of 5–7).

5. After all .md files are written, run the batch converter:
   ```bash
   cd output/business-docs
   python tools/generate_all.py
   ```

6. Report: how many were generated, how many skipped, any failures.

## Note on batching
If many programs need documentation, spawn parallel documenter agents in batches.
Each agent should handle 5–7 programs to keep context manageable.
