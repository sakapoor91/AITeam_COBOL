"""
generate_all.py — Batch converter: processes every BIZ-*.md found in
program subdirectories under output/business-docs/ and produces a matching .docx
and (if mmdc is available) a -flow.png companion file.

Usage:
    cd EvolutionAI/output/business-docs
    python tools/generate_all.py [--dry-run] [--force] [PROG1 PROG2 ...]

    --dry-run   Print what would be converted without writing files.
    --force     Regenerate even when an up-to-date DOCX already exists.
    PROG1 ...   Optional: only process these program names (e.g. CBACT01C).

Exit code:
    0   All conversions succeeded.
    1   One or more conversions failed (details printed to stderr).
"""

import argparse
import os
import sys
import importlib.util
import subprocess

# ── Locate the converter module relative to this script ─────────────────────
TOOLS_DIR   = os.path.dirname(os.path.abspath(__file__))
BIZ_ROOT    = os.path.dirname(TOOLS_DIR)
CONVERTER   = os.path.join(TOOLS_DIR, '_md_to_docx.py')

def load_converter():
    spec = importlib.util.spec_from_file_location('_md_to_docx', CONVERTER)
    mod  = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod

def find_md_files(filter_progs=None):
    """Walk business-docs/PROGNAME/BIZ-PROGNAME.md entries."""
    results = []
    for entry in sorted(os.listdir(BIZ_ROOT)):
        prog_dir = os.path.join(BIZ_ROOT, entry)
        if not os.path.isdir(prog_dir):
            continue
        if entry == 'tools':
            continue
        if filter_progs and entry.upper() not in [p.upper() for p in filter_progs]:
            continue
        md_name = f'BIZ-{entry}.md'
        md_path = os.path.join(prog_dir, md_name)
        if os.path.isfile(md_path):
            results.append((entry, md_path))
    return results

def is_up_to_date(md_path, docx_path):
    if not os.path.isfile(docx_path):
        return False
    return os.path.getmtime(docx_path) >= os.path.getmtime(md_path)

def main():
    parser = argparse.ArgumentParser(description='Batch BIZ-*.md → DOCX converter')
    parser.add_argument('--dry-run', action='store_true')
    parser.add_argument('--force',   action='store_true')
    parser.add_argument('programs',  nargs='*', metavar='PROG')
    args = parser.parse_args()

    converter = load_converter()
    entries   = find_md_files(args.programs if args.programs else None)

    if not entries:
        print('No BIZ-*.md files found. Run the documentation generation first.')
        sys.exit(0)

    ok = skipped = failed = 0

    for prog, md_path in entries:
        docx_path = os.path.splitext(md_path)[0] + '.docx'

        if not args.force and is_up_to_date(md_path, docx_path):
            print(f'  [skip]  {prog}  (DOCX is newer than MD)')
            skipped += 1
            continue

        if args.dry_run:
            print(f'  [dry]   {prog}  →  {os.path.basename(docx_path)}')
            ok += 1
            continue

        print(f'  [conv]  {prog} ...')
        try:
            converter.convert(md_path, docx_path)
            ok += 1
        except Exception as exc:
            print(f'  [FAIL]  {prog}: {exc}', file=sys.stderr)
            failed += 1

    print(f'\nDone: {ok} converted, {skipped} skipped, {failed} failed.')
    sys.exit(1 if failed else 0)

if __name__ == '__main__':
    main()
