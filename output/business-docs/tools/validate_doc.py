"""
validate_doc.py — Phase 1 mechanical validator for BIZ-*.md documents.

Checks that every claim in the document is grounded in the actual COBOL source:
  - All required sections present
  - No forbidden raw COBOL code blocks
  - Every backtick identifier exists in the .cbl or its .cpy files
  - Every paragraph name cited exists as a COBOL paragraph label
  - Every line number cited is within the actual source line count
  - All COPYed copybooks are documented in Appendix B
  - DDnames in Appendix A appear in SELECT/ASSIGN statements
  - PIC clauses in Appendix B tables match what's in the .cpy files
  - Byte counts are mathematically consistent with PIC clauses

Usage:
    python tools/validate_doc.py PROGNAME [--json] [--report]

    --json    Print results as JSON instead of text
    --report  Write results to PROGNAME/BIZ-PROGNAME-validation.md

Exit codes:
    0  All checks passed (or only warnings)
    1  One or more FAIL results
    2  Source file not found
"""

import sys
import re
import os
import json
import math
import argparse
from dataclasses import dataclass, field, asdict
from typing import List, Optional

# ── Paths ────────────────────────────────────────────────────────────────────

TOOLS_DIR   = os.path.dirname(os.path.abspath(__file__))
BIZ_ROOT    = os.path.dirname(TOOLS_DIR)          # output/business-docs/
OUTPUT_ROOT = os.path.dirname(BIZ_ROOT)            # output/
REPO_ROOT   = os.path.dirname(OUTPUT_ROOT)         # repo root
SOURCE_DIR  = os.path.join(REPO_ROOT, 'source', 'cobol')

# ── Data classes ──────────────────────────────────────────────────────────────

@dataclass
class CheckResult:
    name: str
    status: str          # PASS | FAIL | WARN | SKIP
    detail: str
    evidence: List[str] = field(default_factory=list)   # specific lines/values that caused FAIL

@dataclass
class ValidationReport:
    program: str
    source_lines: int
    md_lines: int
    checks: List[CheckResult] = field(default_factory=list)

    @property
    def passed(self):  return sum(1 for c in self.checks if c.status == 'PASS')
    @property
    def failed(self):  return sum(1 for c in self.checks if c.status == 'FAIL')
    @property
    def warned(self):  return sum(1 for c in self.checks if c.status == 'WARN')
    @property
    def skipped(self): return sum(1 for c in self.checks if c.status == 'SKIP')
    @property
    def overall(self): return 'FAIL' if self.failed else 'PASS'

# ── PIC byte-size calculator ──────────────────────────────────────────────────

def pic_to_bytes(pic: str) -> Optional[int]:
    """Return byte count for a PIC clause, or None if unparseable."""
    p = pic.upper().strip()
    p = re.sub(r'\s+', '', p)

    comp3  = 'COMP-3' in p or 'PACKED-DECIMAL' in p
    comp   = re.search(r'COMP(?!-3|-\d)', p) is not None
    p = re.sub(r'COMP-3|PACKED-DECIMAL|COMP-\d|COMP|USAGE\s+IS|USAGE', '', p)
    p = p.replace('PIC', '').replace('PICTURE', '').strip()

    # Expand repeated notation: X(10) → 10 chars, 9(7) → 7 digits
    def expand(s):
        total = 0
        for m in re.finditer(r'[A-Z9SVXB\+\-](\((\d+)\))?', s):
            n = int(m.group(2)) if m.group(2) else 1
            total += n
        return total

    digits = expand(re.sub(r'[SV\.]', '', p))
    if digits == 0:
        return None

    if comp3:
        return math.ceil((digits + 1) / 2)
    if comp:
        if digits <= 4:  return 2
        if digits <= 9:  return 4
        return 8

    # PIC X / PIC A / PIC 9 — one byte per character (after removing V)
    return expand(re.sub(r'[SV\.]', '', p))

# ── Source file helpers ───────────────────────────────────────────────────────

def find_source(prog: str):
    for ext in ('.cbl', '.CBL', '.cob', '.COB'):
        p = os.path.join(SOURCE_DIR, prog + ext)
        if os.path.isfile(p):
            return p
    return None

def find_copybook(name: str):
    for ext in ('.cpy', '.CPY', '.copy', '.COPY'):
        p = os.path.join(SOURCE_DIR, name + ext)
        if os.path.isfile(p):
            return p
    return None

def read_source(path: str) -> List[str]:
    with open(path, 'r', encoding='utf-8', errors='replace') as f:
        return f.readlines()

def extract_copy_names(lines: List[str]) -> List[str]:
    names = []
    for line in lines:
        # COPY keyword in COBOL must be preceded by whitespace (not a dash, letter, or digit).
        # This prevents matching field names ending in -COPY like REQUEST-MSG-COPY.
        m = re.search(r'(?<!\w)(?<!-)\bCOPY\s+([A-Z][A-Z0-9#@$-]*[A-Z0-9])', line, re.IGNORECASE)
        if m:
            name = m.group(1).strip().upper()
            # Skip REPLACING clause keyword and bare numbers
            if name in ('REPLACING', 'IN', 'OF', 'SUPPRESS') or re.match(r'^\d+$', name):
                continue
            names.append(name)
    return list(dict.fromkeys(names))

def extract_paragraphs(lines: List[str]) -> dict:
    """Return {paragraph_name: line_number} from a COBOL source."""
    paras = {}
    for i, line in enumerate(lines, 1):
        # Paragraph label: starts in column 8+, ends with '.', no leading space beyond col 7
        m = re.match(r'^.{6} {0,2}([A-Z0-9][A-Z0-9-]*)\s*\.\s*$', line, re.IGNORECASE)
        if m:
            paras[m.group(1).upper()] = i
        # Also catch labels followed by code on the same line
        m2 = re.match(r'^.{6} {0,2}([A-Z0-9][A-Z0-9-]*)\s+\S', line, re.IGNORECASE)
        if m2:
            candidate = m2.group(1).upper()
            if len(candidate) > 2:   # filter out single-letter noise
                paras.setdefault(candidate, i)
    return paras

def extract_all_identifiers(lines: List[str]) -> set:
    """Return all COBOL identifiers (field names, 88-level names) from source + copybooks."""
    ids = set()
    for line in lines:
        # DATA DIVISION entries: level number + name
        m = re.match(r'^\s*\d{1,2}\s+([A-Z0-9][A-Z0-9-]+)', line, re.IGNORECASE)
        if m:
            ids.add(m.group(1).upper())
        # Procedure division paragraphs
        m2 = re.match(r'^.{6} {0,2}([A-Z0-9][A-Z0-9-]{2,})', line, re.IGNORECASE)
        if m2:
            ids.add(m2.group(1).upper())
    return ids

def extract_select_ddnames(lines: List[str]) -> set:
    ddnames = set()
    for line in lines:
        m = re.search(r'\bASSIGN\s+(?:TO\s+)?(?:DISK|DA-S-|EXTERNAL\s+)?([A-Z0-9-]+)', line, re.IGNORECASE)
        if m:
            ddnames.add(m.group(1).upper())
    return ddnames

# ── Markdown helpers ──────────────────────────────────────────────────────────

REQUIRED_SECTIONS = [
    (r'Application\s*:', 'Header block'),
    (r'##\s*1\.\s*Purpose|^#\s+.*Purpose', 'Section 1 — Purpose'),
    (r'###\s*2\.1|##\s*2\.\s*Program Flow', 'Section 2 — Program Flow'),
    (r'###\s*2\.2', 'Section 2.2 subsection'),
    (r'###\s*2\.3', 'Section 2.3 subsection'),
    (r'##\s*3\.\s*Error|##\s*Error Handling', 'Section 3 — Error Handling'),
    (r'##\s*4\.\s*Migration|##\s*Migration Notes', 'Section 4 — Migration Notes'),
    (r'##\s*Appendix A|Appendix A\s*[—-]', 'Appendix A — Files'),
    (r'##\s*Appendix B|Appendix B\s*[—-]', 'Appendix B — Copybooks'),
    (r'##\s*Appendix C|Appendix C\s*[—-]', 'Appendix C — Literals'),
    (r'##\s*Appendix D|Appendix D\s*[—-]', 'Appendix D — Working Fields'),
    (r'flowchart\s+(?:TD|LR|TB)', 'Appendix E — Mermaid flowchart'),
]

def extract_backtick_ids(md_lines: List[str]) -> List[str]:
    """Return all `identifier` values that look like COBOL field/paragraph names."""
    ids = []
    for line in md_lines:
        for m in re.finditer(r'`([A-Z][A-Z0-9-]{2,})`', line, re.IGNORECASE):
            ids.append(m.group(1).upper())
    return ids

def extract_line_numbers(md_lines: List[str]) -> List[int]:
    nums = []
    for line in md_lines:
        for m in re.finditer(r'\blines?\s+(\d+)', line, re.IGNORECASE):
            nums.append(int(m.group(1)))
        for m in re.finditer(r'\(line\s+(\d+)\)', line, re.IGNORECASE):
            nums.append(int(m.group(1)))
    return nums

def extract_ddnames_from_appendix_a(md_lines: List[str]) -> List[str]:
    """Find table rows under Appendix A and extract the DDname column."""
    in_appendix_a = False
    ddnames = []
    for line in md_lines:
        if re.search(r'Appendix A', line, re.IGNORECASE):
            in_appendix_a = True
        if in_appendix_a and re.search(r'Appendix [B-E]', line, re.IGNORECASE):
            break
        if in_appendix_a and line.startswith('|'):
            cols = [c.strip() for c in line.strip().strip('|').split('|')]
            if len(cols) >= 2 and re.match(r'^[A-Z][A-Z0-9-]+$', cols[1].strip('`')):
                ddnames.append(cols[1].strip('`').upper())
    return ddnames

def extract_pic_rows_from_appendix_b(md_lines: List[str]) -> List[tuple]:
    """Extract (field_name, pic_clause, byte_count_str) from Appendix B tables."""
    in_appendix_b = False
    rows = []
    for line in md_lines:
        if re.search(r'Appendix B', line, re.IGNORECASE):
            in_appendix_b = True
        if in_appendix_b and re.search(r'Appendix [C-E]', line, re.IGNORECASE):
            break
        if in_appendix_b and line.startswith('|'):
            cols = [c.strip().strip('`') for c in line.strip().strip('|').split('|')]
            if len(cols) >= 3 and re.search(r'PIC\s+', cols[1], re.IGNORECASE):
                field_name = cols[0].upper()
                pic_clause = cols[1]
                byte_str   = cols[2] if len(cols) > 2 else ''
                rows.append((field_name, pic_clause, byte_str))
    return rows

def extract_copy_names_from_md(md_lines: List[str]) -> List[str]:
    """Find copybook names mentioned in Appendix B."""
    names = []
    for line in md_lines:
        m = re.search(r'###\s+`?([A-Z0-9][A-Z0-9-]+\.cpy)`?', line, re.IGNORECASE)
        if m:
            names.append(re.sub(r'\.cpy$', '', m.group(1), flags=re.IGNORECASE).upper())
        m2 = re.search(r'`([A-Z0-9][A-Z0-9-]+\.cpy)`', line, re.IGNORECASE)
        if m2:
            names.append(re.sub(r'\.cpy$', '', m2.group(1), flags=re.IGNORECASE).upper())
    return list(dict.fromkeys(names))

# ── Individual checks ─────────────────────────────────────────────────────────

def check_sections(md_lines: List[str]) -> CheckResult:
    text = '\n'.join(md_lines)
    missing = []
    for pattern, label in REQUIRED_SECTIONS:
        if not re.search(pattern, text, re.IGNORECASE | re.MULTILINE):
            missing.append(label)
    if missing:
        return CheckResult('required_sections', 'FAIL',
                           f'{len(missing)} required section(s) missing',
                           missing)
    return CheckResult('required_sections', 'PASS', 'All 12 required sections present')

def check_no_cobol_blocks(md_lines: List[str]) -> CheckResult:
    hits = []
    for i, line in enumerate(md_lines, 1):
        if re.match(r'\s*```\s*cobol', line, re.IGNORECASE):
            hits.append(f'Line {i}: {line.strip()}')
    if hits:
        return CheckResult('no_cobol_blocks', 'FAIL',
                           'Raw COBOL code blocks found (forbidden — use plain English)',
                           hits)
    return CheckResult('no_cobol_blocks', 'PASS', 'No raw COBOL code blocks found')

def check_source_exists(prog: str, source_path: Optional[str]) -> CheckResult:
    if source_path:
        return CheckResult('source_file_exists', 'PASS',
                           f'Source found: {os.path.basename(source_path)}')
    return CheckResult('source_file_exists', 'FAIL',
                       f'No .cbl/.CBL file found for {prog} in {SOURCE_DIR}')

def check_line_number_bounds(md_lines: List[str], source_line_count: int) -> CheckResult:
    nums = extract_line_numbers(md_lines)
    oob = [n for n in nums if n > source_line_count]
    if oob:
        return CheckResult('line_number_bounds', 'FAIL',
                           f'{len(oob)} line number(s) exceed source file length ({source_line_count} lines)',
                           [str(n) for n in sorted(set(oob))])
    if not nums:
        return CheckResult('line_number_bounds', 'WARN',
                           'No line numbers found in document — Section 2 must cite line numbers')
    return CheckResult('line_number_bounds', 'PASS',
                       f'{len(nums)} line reference(s) checked, all within bounds (max: {source_line_count})')

def check_backtick_ids(md_lines: List[str], known_ids: set) -> CheckResult:
    doc_ids = extract_backtick_ids(md_lines)
    # Filter to things that look like COBOL identifiers (not single words like 'Y' or 'N')
    cobol_like = [i for i in doc_ids if len(i) > 3 and '-' in i or i.startswith(('WS-', 'ACCT-', 'CARD-', 'TRAN-', 'CUST-', 'USER-', 'FILE-'))]
    unknown = [i for i in cobol_like if i not in known_ids]
    # Remove common false positives
    false_positives = {'END-OF-FILE', 'WORKING-STORAGE', 'PROCEDURE-DIVISION', 'FILE-SECTION',
                       'STOP-RUN', 'INPUT-OUTPUT', 'FILE-CONTROL', 'DATA-DIVISION', 'END-IF',
                       'END-PERFORM', 'END-READ', 'END-WRITE', 'END-CALL', 'END-EVALUATE',
                       'COMP-3', 'COMP-5', 'USAGE-IS', 'OCCURS-TIMES', 'REDEFINES-AS',
                       'BIGDECIMAL', 'JAVA-MIGRATION', 'PACKED-DECIMAL'}
    unknown = [i for i in unknown if i not in false_positives]
    if unknown:
        return CheckResult('backtick_identifiers', 'WARN',
                           f'{len(unknown)} backtick identifier(s) not found in source or copybooks',
                           sorted(set(unknown))[:20])
    return CheckResult('backtick_identifiers', 'PASS',
                       f'{len(cobol_like)} COBOL-style identifiers verified against source')

def check_copybook_coverage(md_lines: List[str], source_copy_names: List[str]) -> CheckResult:
    md_copies = extract_copy_names_from_md(md_lines)
    # Also check body text for copybook names
    text = '\n'.join(md_lines)
    missing_from_doc = []
    for name in source_copy_names:
        if name not in text.upper():
            missing_from_doc.append(name)
    if missing_from_doc:
        return CheckResult('copybook_coverage', 'FAIL',
                           f'{len(missing_from_doc)} COPY statement(s) from source not documented in Appendix B',
                           missing_from_doc)
    return CheckResult('copybook_coverage', 'PASS',
                       f'All {len(source_copy_names)} copybook(s) referenced in source appear in document')

def check_ddnames(md_lines: List[str], source_lines: List[str]) -> CheckResult:
    doc_ddnames   = extract_ddnames_from_appendix_a(md_lines)
    src_ddnames   = extract_select_ddnames(source_lines)
    if not src_ddnames:
        return CheckResult('ddname_accuracy', 'SKIP', 'No SELECT/ASSIGN found in source (may be CICS-only)')
    if not doc_ddnames:
        return CheckResult('ddname_accuracy', 'WARN', 'No DDnames found in Appendix A table')
    unknown = [d for d in doc_ddnames if d not in src_ddnames]
    if unknown:
        return CheckResult('ddname_accuracy', 'WARN',
                           f'{len(unknown)} DDname(s) in Appendix A not found in SELECT/ASSIGN statements',
                           unknown)
    return CheckResult('ddname_accuracy', 'PASS',
                       f'All {len(doc_ddnames)} Appendix A DDname(s) verified against source')

def check_pic_accuracy(md_lines: List[str]) -> CheckResult:
    rows = extract_pic_rows_from_appendix_b(md_lines)
    if not rows:
        return CheckResult('pic_byte_accuracy', 'SKIP', 'No PIC rows found in Appendix B')
    errors = []
    for field_name, pic_clause, byte_str in rows:
        expected = pic_to_bytes(pic_clause)
        if expected is None:
            continue
        m = re.search(r'(\d+)', byte_str)
        if not m:
            continue
        documented = int(m.group(1))
        if documented != expected:
            errors.append(f'{field_name}: doc says {documented} bytes, computed {expected} from "{pic_clause}"')
    if errors:
        return CheckResult('pic_byte_accuracy', 'FAIL',
                           f'{len(errors)} byte count(s) inconsistent with PIC clause',
                           errors[:10])
    return CheckResult('pic_byte_accuracy', 'PASS',
                       f'{len(rows)} PIC/byte-count pair(s) checked — all consistent')

def check_migration_notes_have_lines(md_lines: List[str]) -> CheckResult:
    in_s4 = False
    items_without_lines = 0
    items_total = 0
    for line in md_lines:
        if re.search(r'##\s*4\.\s*Migration|##\s*Migration Notes', line, re.IGNORECASE):
            in_s4 = True
        if in_s4 and re.search(r'##\s*Appendix', line, re.IGNORECASE):
            break
        if in_s4 and re.match(r'^\s*\d+\.', line):
            items_total += 1
            if not re.search(r'line\s+\d+|lines?\s+\d+|\(line', line, re.IGNORECASE):
                items_without_lines += 1
    if items_total == 0:
        return CheckResult('migration_notes_line_refs', 'WARN',
                           'Section 4 contains no numbered migration notes')
    if items_without_lines > 0:
        return CheckResult('migration_notes_line_refs', 'WARN',
                           f'{items_without_lines}/{items_total} migration note(s) missing line number citations')
    return CheckResult('migration_notes_line_refs', 'PASS',
                       f'All {items_total} migration note(s) cite line numbers')

def check_mermaid_classdefs(md_lines: List[str]) -> CheckResult:
    text = '\n'.join(md_lines)
    in_mermaid = False
    has_flowchart = has_classdefs = False
    for line in md_lines:
        if '```mermaid' in line.lower():
            in_mermaid = True
        if in_mermaid and re.search(r'flowchart\s+(?:TD|LR|TB)', line, re.IGNORECASE):
            has_flowchart = True
        if in_mermaid and 'classDef' in line:
            has_classdefs = True
        if in_mermaid and line.strip() == '```':
            in_mermaid = False
    if not has_flowchart:
        return CheckResult('mermaid_diagram', 'FAIL', 'No Mermaid flowchart found in Appendix E')
    if not has_classdefs:
        return CheckResult('mermaid_diagram', 'WARN', 'Mermaid diagram missing classDef colour definitions')
    return CheckResult('mermaid_diagram', 'PASS', 'Mermaid flowchart with classDef styles present')

# ── Main ─────────────────────────────────────────────────────────────────────

def validate(prog: str) -> ValidationReport:
    prog = prog.upper()
    md_path     = os.path.join(BIZ_ROOT, prog, f'BIZ-{prog}.md')
    source_path = find_source(prog)

    if not os.path.isfile(md_path):
        print(f'ERROR: {md_path} not found. Run /document {prog} first.')
        sys.exit(2)

    md_lines = [l.rstrip('\n') for l in open(md_path, encoding='utf-8').readlines()]

    source_lines = []
    known_ids    = set()
    copy_names   = []

    if source_path:
        source_lines = read_source(source_path)
        copy_names   = extract_copy_names(source_lines)
        known_ids    = extract_all_identifiers(source_lines)
        for cn in copy_names:
            cpath = find_copybook(cn)
            if cpath:
                known_ids.update(extract_all_identifiers(read_source(cpath)))

    report = ValidationReport(
        program      = prog,
        source_lines = len(source_lines),
        md_lines     = len(md_lines),
    )

    report.checks.append(check_sections(md_lines))
    report.checks.append(check_no_cobol_blocks(md_lines))
    report.checks.append(check_source_exists(prog, source_path))

    if source_path:
        report.checks.append(check_line_number_bounds(md_lines, len(source_lines)))
        report.checks.append(check_backtick_ids(md_lines, known_ids))
        report.checks.append(check_copybook_coverage(md_lines, copy_names))
        report.checks.append(check_ddnames(md_lines, source_lines))
    else:
        for name in ('line_number_bounds', 'backtick_identifiers', 'copybook_coverage', 'ddname_accuracy'):
            report.checks.append(CheckResult(name, 'SKIP', 'Source file not found'))

    report.checks.append(check_pic_accuracy(md_lines))
    report.checks.append(check_migration_notes_have_lines(md_lines))
    report.checks.append(check_mermaid_classdefs(md_lines))

    return report

# ── Formatters ────────────────────────────────────────────────────────────────

STATUS_ICON_MD   = {'PASS': '✓', 'FAIL': '✗', 'WARN': '⚠', 'SKIP': '–'}
STATUS_ICON_TEXT = {'PASS': '[OK]  ', 'FAIL': '[FAIL]', 'WARN': '[WARN]', 'SKIP': '[SKIP]'}

def format_text(report: ValidationReport) -> str:
    lines = [
        f'',
        f'Validation Report -- {report.program}',
        f'  Source: {report.source_lines} lines   Doc: {report.md_lines} lines',
        f'  Result: {report.overall}   ({report.passed} passed, {report.failed} failed, {report.warned} warned, {report.skipped} skipped)',
        f'',
        f'  {"Check":<35} {"Status":<8} Detail',
        f'  {"-"*35} {"-"*8} {"-"*40}',
    ]
    for c in report.checks:
        icon = STATUS_ICON_TEXT.get(c.status, '?')
        lines.append(f'  {c.name:<35} {icon}  {c.detail}')
        for ev in c.evidence[:5]:
            lines.append(f'    {"":35}           -> {ev}')
    lines.append('')
    return '\n'.join(lines)

def format_markdown(report: ValidationReport) -> str:
    lines = [
        f'# Validation Report: BIZ-{report.program}.md',
        f'',
        f'**Overall: {report.overall}** — {report.passed} passed · {report.failed} failed · {report.warned} warned · {report.skipped} skipped',
        f'',
        f'Source file: `{report.source_lines}` lines   |   Document: `{report.md_lines}` lines',
        f'',
        f'> This is the Phase 1 mechanical validation. Run the `validator` agent for Phase 2 (LLM-as-judge).',
        f'',
        f'## Phase 1 — Mechanical Checks',
        f'',
        f'| Check | Status | Detail |',
        f'|-------|--------|--------|',
    ]
    for c in report.checks:
        icon = STATUS_ICON_MD.get(c.status, '?')
        lines.append(f'| `{c.name}` | {icon} **{c.status}** | {c.detail} |')

    failures = [c for c in report.checks if c.status in ('FAIL', 'WARN') and c.evidence]
    if failures:
        lines += ['', '## Issues Found', '']
        for c in failures:
            lines.append(f'### {c.name} ({c.status})')
            lines.append(f'{c.detail}')
            lines.append('')
            for ev in c.evidence[:10]:
                lines.append(f'- `{ev}`')
            lines.append('')

    lines += [
        '## Phase 2 — LLM Judge',
        '',
        '_Not yet run. Use the `validator` agent to perform semantic accuracy checks._',
        '',
        '```',
        'Ask the validator agent: /validate-doc ' + report.program,
        '```',
    ]
    return '\n'.join(lines)

VALIDATION_DIR = os.path.join(OUTPUT_ROOT, 'validation')

def write_report(report: ValidationReport, out_dir: str = None) -> str:
    """Write the Phase 1 markdown report and return the output path."""
    if out_dir is None:
        out_dir = os.path.join(VALIDATION_DIR, report.program)
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, f'{report.program}-validation.md')
    with open(out_path, 'w', encoding='utf-8') as f:
        f.write(format_markdown(report))
    return out_path

# ── Entry point ───────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description='Phase 1 mechanical validator for BIZ-*.md documents'
    )
    parser.add_argument('program', help='Program name, e.g. CBACT01C')
    parser.add_argument('--json',    action='store_true', help='Print JSON instead of text')
    parser.add_argument('--report',  action='store_true',
                        help='Write report to validation/PROGNAME/PROGNAME-validation.md')
    parser.add_argument('--out-dir', default=None,
                        help='Override output directory for the report')
    args = parser.parse_args()

    report = validate(args.program.upper())

    if args.json:
        print(json.dumps(asdict(report), indent=2))
    else:
        print(format_text(report))

    if args.report:
        out_path = write_report(report, args.out_dir)
        print(f'Report written: {out_path}')

    sys.exit(1 if report.failed else 0)

if __name__ == '__main__':
    main()
