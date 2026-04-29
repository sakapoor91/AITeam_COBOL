#!/usr/bin/env python3
"""
cobol_ir.py — COBOL Intermediate Representation extractor.

Reads a COBOL program and all its copybooks, then writes a JSON IR to output/ir/.
The IR is consumed by:
  - the documenter agent (as pre-digested structured input, reducing hallucination)
  - validate_doc.py Phase 1 (deterministic field/paragraph/byte checks)

IR contents
-----------
  program           program name (uppercase)
  source_file       relative path to .cbl
  source_lines      total raw line count
  select_assigns    {logical_file_name: ddname} from ENVIRONMENT DIVISION
  file_section      [{dd_name, ddname, logical_name, record_names}] from DATA/FILE SECTION
  data_items        flat list of all data items (own + copybook fields)
  copy_statements   copybook names referenced (in order)
  copybooks         {cpy_name: [data_items]} — per-copybook field lists
  paragraphs        [{name, line_start, line_end}]
  perform_graph     {paragraph: [called_paragraphs]}  — only non-empty entries
  call_statements   [{caller, callee, line}]
  field_references  {field_name: [paragraph_names]} — only referenced fields
  unreferenced_fields  [field_names never mentioned in Procedure Division]
  comp3_fields      [field names with COMP-3 / PACKED-DECIMAL usage]
  level_88_values   {parent_field: [{name, value}]}

Usage
-----
  python cobol_ir.py CBACT01C
  python cobol_ir.py CBACT01C --stdout
  python cobol_ir.py --all
"""

import sys
import re
import os
import json
import argparse
from pathlib import Path
from typing import Optional

# ── Paths ──────────────────────────────────────────────────────────────────────

TOOLS_DIR   = Path(__file__).parent
BIZ_ROOT    = TOOLS_DIR.parent
OUTPUT_ROOT = BIZ_ROOT.parent
REPO_ROOT   = OUTPUT_ROOT.parent
SOURCE_DIR  = REPO_ROOT / 'source' / 'cobol'
IR_DIR      = OUTPUT_ROOT / 'ir'

# ── COBOL reserved words — excluded from field-reference scanning ──────────────

RESERVED = {
    'ACCEPT', 'ACCESS', 'ADD', 'ADDRESS', 'ADVANCING', 'AFTER', 'ALL',
    'ALPHABETIC', 'ALSO', 'ALTER', 'AND', 'APPLY', 'ARE', 'AREA', 'AREAS',
    'ASSIGN', 'AT', 'AUTHOR', 'BEFORE', 'BEGINNING', 'BINARY', 'BY',
    'CALL', 'CANCEL', 'CLOSE', 'COLUMN', 'COMPUTE', 'CONFIGURATION',
    'CONTAINS', 'CONTINUE', 'CONTROL', 'COPY', 'COUNT', 'CURRENCY',
    'DATA', 'DATE', 'DAY', 'DECIMAL', 'DELETE', 'DEPENDING', 'DISPLAY',
    'DIVIDE', 'DIVISION', 'ELSE', 'END', 'ENVIRONMENT', 'EQUAL',
    'ERROR', 'EVALUATE', 'EXIT', 'EXTERNAL', 'FALSE', 'FILE', 'FILLER',
    'FOR', 'FROM', 'FUNCTION', 'GO', 'GOBACK', 'HIGH', 'HIGH-VALUES',
    'IDENTIFICATION', 'IF', 'IN', 'INDEX', 'INITIALIZE', 'INPUT',
    'INTO', 'INVALID', 'IS', 'KEY', 'LABEL', 'LEADING', 'LENGTH',
    'LESS', 'LINKAGE', 'LOCAL', 'LOW', 'LOW-VALUES', 'MOVE', 'MULTIPLY',
    'NOT', 'NULL', 'NULLS', 'NUMERIC', 'OBJECT', 'OCCURS', 'OF', 'ON',
    'OPEN', 'OR', 'OTHER', 'OUTPUT', 'OVERFLOW', 'PERFORM', 'PLUS',
    'POINTER', 'PROCEDURE', 'PROGRAM', 'QUOTES', 'READ', 'RECORD',
    'REDEFINES', 'RELATIVE', 'REMAINDER', 'RETURN', 'REWRITE', 'SEARCH',
    'SECTION', 'SELECT', 'SEND', 'SET', 'SIZE', 'SORT', 'SPACE', 'SPACES',
    'START', 'STATUS', 'STOP', 'STRING', 'SUBTRACT', 'TALLYING', 'TEST',
    'THEN', 'THROUGH', 'THRU', 'TO', 'TRUE', 'TYPE', 'UNSTRING', 'UNTIL',
    'UPON', 'USAGE', 'USING', 'VALUE', 'VALUES', 'VARYING', 'WHEN',
    'WITH', 'WRITE', 'ZERO', 'ZEROES', 'ZEROS', 'COMP', 'COMP-1', 'COMP-2',
    'COMP-3', 'COMP-4', 'PACKED-DECIMAL', 'COMPUTATIONAL',
    'WORKING-STORAGE', 'PICTURE', 'PIC',
}

# ── Line reading ───────────────────────────────────────────────────────────────

def read_cobol_lines(path: Path) -> list:
    """
    Read fixed-format COBOL file.
    Returns list of (line_num, indicator, content) where:
      line_num  1-based original line number
      indicator col 7 character (' ' normal, '*' comment, '-' continuation, '/' page, 'D' debug)
      content   cols 8-72 with trailing spaces stripped
    """
    result = []
    try:
        text = path.read_text(encoding='utf-8', errors='replace')
    except OSError:
        return result
    for i, raw in enumerate(text.splitlines(), 1):
        padded = raw.ljust(7)
        indicator = padded[6]
        content   = padded[7:72].rstrip() if len(padded) > 7 else ''
        result.append((i, indicator, content))
    return result


def logical_lines(cobol_lines: list) -> list:
    """
    Merge continuation lines (indicator '-') into the preceding line.
    Skip comment lines (* /), debug lines (D).
    Returns list of (start_line_num, merged_content).
    """
    out = []
    for line_num, indicator, content in cobol_lines:
        if indicator in ('*', '/', 'D'):
            continue
        if indicator == '-':
            if out:
                # Strip leading quote character from continuation string literals
                cont = content.lstrip()
                if cont and cont[0] in ('"', "'"):
                    cont = cont[1:]
                out[-1] = (out[-1][0], out[-1][1] + ' ' + cont)
        else:
            out.append((line_num, content))
    return out


# ── PIC byte calculator ────────────────────────────────────────────────────────

def _expand_pic_repeats(pic: str) -> str:
    """Expand X(5) → XXXXX, 9(3) → 999, etc."""
    return re.sub(
        r'([A-Za-z9\+\-\*Zz])\((\d+)\)',
        lambda m: m.group(1) * int(m.group(2)),
        pic
    )


def pic_bytes(pic_str: str, usage: str) -> Optional[int]:
    """Return storage bytes for a PIC clause + USAGE combination."""
    if not pic_str:
        return None

    u = (usage or 'DISPLAY').upper()

    if re.match(r'COMP-?1$|COMPUTATIONAL-?1$', u):
        return 4
    if re.match(r'COMP-?2$|COMPUTATIONAL-?2$', u):
        return 8
    if u == 'INDEX':
        return 4

    expanded = _expand_pic_repeats(pic_str.upper().replace(' ', ''))
    digits   = len(re.findall(r'[9Z\*\+\-]', expanded))
    alphanum = len(re.findall(r'[XA]', expanded))
    total    = digits + alphanum

    if total == 0:
        return None

    if re.match(r'COMP-?3$|COMPUTATIONAL-?3$|PACKED-DECIMAL$', u):
        return (digits + 2) // 2   # ceiling of (n+1)/2

    if re.match(r'COMP-?4?$|COMPUTATIONAL-?4?$|BINARY$', u):
        if digits <= 4:  return 2
        if digits <= 9:  return 4
        return 8

    return total   # DISPLAY default


# ── Regexes ────────────────────────────────────────────────────────────────────

_LEVEL_RE   = re.compile(r'^(\d{1,2})\s+(\S+)(.*)', re.DOTALL)
_PIC_RE     = re.compile(r'\bPIC(?:TURE)?\s+(?:IS\s+)?([^\s,\.]+)', re.IGNORECASE)
_USAGE_RE   = re.compile(
    r'\bUSAGE\s+(?:IS\s+)?(COMP(?:UTATIONAL)?(?:-[1-4])?|BINARY|PACKED-DECIMAL|DISPLAY|INDEX)\b'
    r'|\b(COMP(?:-[1-4])?|PACKED-DECIMAL|BINARY|INDEX)\b',
    re.IGNORECASE
)
_VALUE_RE   = re.compile(r'\bVALUES?\s+(?:ARE\s+|IS\s+)?(.+)', re.IGNORECASE)
_COPY_RE    = re.compile(r'\bCOPY\s+([A-Z0-9][A-Z0-9\-]*)', re.IGNORECASE)
_SELECT_RE  = re.compile(r'\bSELECT\s+(\S+)\s+ASSIGN\s+(?:TO\s+)?(\S+)', re.IGNORECASE)
_FD_RE      = re.compile(r'^FD\s+(\S+)', re.IGNORECASE)
_PERFORM_RE = re.compile(
    r'\bPERFORM\s+([A-Z0-9][A-Z0-9\-]+)(?:\s+THRU\s+([A-Z0-9][A-Z0-9\-]+))?',
    re.IGNORECASE
)
_CALL_RE    = re.compile(r"\bCALL\s+['\"]?([A-Z0-9][A-Z0-9\-]+)['\"]?", re.IGNORECASE)

_SECTION_MAP = {
    'FILE SECTION':             'FILE',
    'WORKING-STORAGE SECTION':  'WORKING-STORAGE',
    'LOCAL-STORAGE SECTION':    'LOCAL-STORAGE',
    'LINKAGE SECTION':          'LINKAGE',
}


# ── Copybook resolver ──────────────────────────────────────────────────────────

def _find_cpy(name: str, source_dir: Path) -> Optional[Path]:
    for stem in (name, name.upper(), name.lower()):
        for ext in ('.cpy', '.CPY', '.cob', '.COB'):
            p = source_dir / (stem + ext)
            if p.exists():
                return p
    return None


def _parse_items_from_lines(llines: list, section: Optional[str], copybook: Optional[str]) -> list:
    """Extract data items from a list of (line_num, content) logical lines."""
    items = []
    for line_num, content in llines:
        m = _LEVEL_RE.match(content.strip())
        if not m:
            continue
        level = int(m.group(1))
        name  = m.group(2).rstrip('.')
        rest  = m.group(3)

        pic_m   = _PIC_RE.search(rest)
        pic     = pic_m.group(1) if pic_m else None

        usage_m = _USAGE_RE.search(rest)
        if usage_m:
            usage = (usage_m.group(1) or usage_m.group(2)).upper()
        else:
            usage = None

        value_m = _VALUE_RE.search(rest)
        value   = value_m.group(1).strip().rstrip('.') if value_m else None

        comp3   = bool(usage and re.match(r'COMP-?3$|PACKED-DECIMAL$', usage, re.I))
        nbytes  = pic_bytes(pic, usage) if pic else None

        items.append({
            'level':     level,
            'name':      name,
            'pic':       pic,
            'usage':     usage,
            'value':     value,
            'bytes':     nbytes,
            'comp3':     comp3,
            'line':      line_num,
            'section':   section,
            'copybook':  copybook,
            'is_filler': name.upper() == 'FILLER',
            'is_88':     level == 88,
        })
    return items


def resolve_copybook(name: str, source_dir: Path, section: Optional[str]) -> list:
    path = _find_cpy(name, source_dir)
    if not path:
        return []
    raw   = read_cobol_lines(path)
    llines = logical_lines(raw)
    return _parse_items_from_lines(llines, section, name)


# ── Data division parser ───────────────────────────────────────────────────────

def parse_data_division(llines: list, source_dir: Path):
    """
    Returns:
      data_items     flat list (own fields + copybook fields)
      copy_stmts     ordered list of copybook names
      copybooks      {name: [items]}
      file_section   [{dd_name, record_names}]
      select_assigns {logical_name: ddname}
    """
    data_items   = []
    copy_stmts   = []
    copybooks    = {}
    file_section = []
    select_assigns = {}

    in_env  = False
    in_data = False
    current_section: Optional[str] = None
    current_fd = None

    for line_num, content in llines:
        upper = content.upper().strip()

        # Division boundaries
        if 'ENVIRONMENT DIVISION' in upper:
            in_env, in_data = True, False
            continue
        if 'DATA DIVISION' in upper:
            in_env, in_data = False, True
            continue
        if 'PROCEDURE DIVISION' in upper:
            in_env = in_data = False
            continue

        # SELECT/ASSIGN (Environment Division)
        if in_env:
            sel = _SELECT_RE.search(content)
            if sel:
                select_assigns[sel.group(1).rstrip('.').upper()] = sel.group(2).rstrip('.').upper()
            continue

        if not in_data:
            continue

        # Section detection
        for key, val in _SECTION_MAP.items():
            if key in upper:
                current_section = val
                current_fd = None
                break

        # FD entry
        fd_m = _FD_RE.match(content.strip())
        if fd_m and current_section == 'FILE':
            current_fd = {'dd_name': fd_m.group(1).rstrip('.').upper(), 'record_names': []}
            file_section.append(current_fd)
            continue

        # COPY statement
        copy_m = _COPY_RE.search(content)
        if copy_m:
            cpy_name = copy_m.group(1).upper()
            if cpy_name not in copy_stmts:
                copy_stmts.append(cpy_name)
            items = resolve_copybook(cpy_name, source_dir, current_section)
            copybooks[cpy_name] = items
            data_items.extend(items)
            # Track FD record names from copybook
            if current_fd:
                for it in items:
                    if it['level'] == 1:
                        current_fd['record_names'].append(it['name'])
            continue

        # Level-number data item
        lm = _LEVEL_RE.match(content.strip())
        if lm:
            level = int(lm.group(1))
            name  = lm.group(2).rstrip('.')
            rest  = lm.group(3)

            pic_m   = _PIC_RE.search(rest)
            pic     = pic_m.group(1) if pic_m else None
            usage_m = _USAGE_RE.search(rest)
            usage   = (usage_m.group(1) or usage_m.group(2)).upper() if usage_m else None
            value_m = _VALUE_RE.search(rest)
            value   = value_m.group(1).strip().rstrip('.') if value_m else None
            comp3   = bool(usage and re.match(r'COMP-?3$|PACKED-DECIMAL$', usage, re.I))
            nbytes  = pic_bytes(pic, usage) if pic else None

            item = {
                'level':     level,
                'name':      name,
                'pic':       pic,
                'usage':     usage,
                'value':     value,
                'bytes':     nbytes,
                'comp3':     comp3,
                'line':      line_num,
                'section':   current_section,
                'copybook':  None,
                'is_filler': name.upper() == 'FILLER',
                'is_88':     level == 88,
            }
            data_items.append(item)

            if current_fd and level == 1:
                current_fd['record_names'].append(name)

    return data_items, copy_stmts, copybooks, file_section, select_assigns


# ── Procedure division parser ──────────────────────────────────────────────────

def parse_procedure_division(raw_lines: list, llines: list, data_items: list):
    """
    Returns:
      paragraphs        [{name, line_start, line_end}]
      perform_graph     {para: [called_paras]}
      call_stmts        [{caller, callee, line}]
      field_refs        {field_name: [paragraph_names]}
      unreferenced      [field_names]
    """
    # ── Find procedure division start line ──
    proc_start = None
    for line_num, indicator, content in raw_lines:
        if indicator not in ('*', '/', 'D') and 'PROCEDURE DIVISION' in content.upper():
            proc_start = line_num
            break
    if proc_start is None:
        return [], {}, [], {}, []

    # ── Paragraph detection from raw lines (need col 8 = position 0 of content) ──
    paragraphs = []
    current_para = None

    for line_num, indicator, content in raw_lines:
        if line_num < proc_start:
            continue
        if indicator in ('*', '/', 'D', '-'):
            continue
        if not content.strip():
            continue
        # Paragraph names sit at Area A (col 8-11) — no leading spaces in our 'content'
        # Statements sit at Area B (col 12+) — 4+ leading spaces
        if content and not content[0].isspace():
            stripped = content.rstrip()
            # Must end with a period and look like a name (letters/digits/hyphens)
            if stripped.endswith('.'):
                candidate = stripped[:-1].strip()
                if re.match(r'^[A-Z0-9][A-Z0-9\-]*$', candidate, re.IGNORECASE):
                    name_upper = candidate.upper()
                    # Exclude division/section markers
                    if 'DIVISION' not in name_upper and 'SECTION' not in name_upper:
                        if current_para:
                            current_para['line_end'] = line_num - 1
                        current_para = {
                            'name':       name_upper,
                            'line_start': line_num,
                            'line_end':   None,
                        }
                        paragraphs.append(current_para)

    if current_para and current_para['line_end'] is None:
        current_para['line_end'] = raw_lines[-1][0] if raw_lines else proc_start

    para_names = {p['name'] for p in paragraphs}

    # ── Field name set (non-filler, non-88) for reference scanning ──
    field_name_set = {
        item['name'].upper()
        for item in data_items
        if not item['is_filler'] and not item['is_88']
        and item['name'].upper() not in RESERVED
    }

    # ── Paragraph lookup by line number ──
    sorted_paras = sorted(paragraphs, key=lambda p: p['line_start'])

    def para_at(ln: int) -> Optional[str]:
        result = None
        for p in sorted_paras:
            if p['line_start'] <= ln:
                result = p['name']
            else:
                break
        return result

    # ── Scan procedure division logical lines ──
    perform_graph: dict = {p['name']: [] for p in paragraphs}
    call_stmts   = []
    field_refs   = {name: [] for name in field_name_set}

    proc_llines = [(ln, c) for ln, c in llines if ln >= proc_start]

    for line_num, content in proc_llines:
        upper = content.upper()
        current = para_at(line_num)

        # PERFORM targets
        for m in _PERFORM_RE.finditer(upper):
            for grp in (m.group(1), m.group(2)):
                if grp and grp.upper() in para_names and current:
                    tgt = grp.upper()
                    if tgt not in perform_graph[current]:
                        perform_graph[current].append(tgt)

        # CALL targets
        cm = _CALL_RE.search(content)
        if cm:
            call_stmts.append({
                'caller': current,
                'callee': cm.group(1).upper(),
                'line':   line_num,
            })

        # Field references — word-boundary scan
        words = set(re.findall(r'\b([A-Z][A-Z0-9\-]{2,})\b', upper))
        for word in words:
            if word in field_name_set and current:
                if current not in field_refs[word]:
                    field_refs[word].append(current)

    unreferenced = sorted(
        name for name in field_name_set
        if not field_refs.get(name)
    )

    return paragraphs, perform_graph, call_stmts, field_refs, unreferenced


# ── 88-level grouping ──────────────────────────────────────────────────────────

def build_88_map(data_items: list) -> dict:
    """Map each parent field to its 88-level values."""
    result   = {}
    last_non = None
    for item in data_items:
        if item['level'] == 88:
            if last_non:
                parent = last_non['name']
                result.setdefault(parent, []).append({
                    'name':  item['name'],
                    'value': item['value'],
                })
        else:
            last_non = item
    return result


# ── Full extraction ────────────────────────────────────────────────────────────

def _find_cbl(prog: str, source_dir: Path) -> Optional[Path]:
    for stem in (prog, prog.upper(), prog.lower()):
        for ext in ('.cbl', '.CBL', '.cob', '.COB'):
            p = source_dir / (stem + ext)
            if p.exists():
                return p
    return None


def extract_ir(prog: str, source_dir: Path = SOURCE_DIR) -> dict:
    cbl = _find_cbl(prog, source_dir)
    if not cbl:
        return {'error': f'Source not found for {prog}', 'program': prog.upper()}

    raw  = read_cobol_lines(cbl)
    ll   = logical_lines(raw)

    data_items, copy_stmts, copybooks, file_sec, sel_assigns = \
        parse_data_division(ll, source_dir)

    # Enrich file_section with SELECT/ASSIGN info
    for fd in file_sec:
        dd = fd['dd_name']
        for logical, ddname in sel_assigns.items():
            if logical == dd or ddname == dd:
                fd['logical_name'] = logical
                fd['ddname']       = ddname
                break
        fd.setdefault('logical_name', dd)
        fd.setdefault('ddname',       dd)

    paragraphs, perf_graph, call_stmts, field_refs, unreferenced = \
        parse_procedure_division(raw, ll, data_items)

    comp3_fields  = [it['name'] for it in data_items if it['comp3'] and not it['is_filler']]
    level_88_map  = build_88_map(data_items)
    field_refs_out = {k: v for k, v in field_refs.items() if v}

    return {
        'program':            prog.upper(),
        'source_file':        str(cbl.relative_to(REPO_ROOT)).replace('\\', '/'),
        'source_lines':       len(raw),
        'select_assigns':     sel_assigns,
        'file_section':       file_sec,
        'data_items':         data_items,
        'copy_statements':    copy_stmts,
        'copybooks':          copybooks,
        'paragraphs':         paragraphs,
        'perform_graph':      {k: v for k, v in perf_graph.items() if v},
        'call_statements':    call_stmts,
        'field_references':   field_refs_out,
        'unreferenced_fields': unreferenced,
        'comp3_fields':       comp3_fields,
        'level_88_values':    level_88_map,
    }


# ── CLI ────────────────────────────────────────────────────────────────────────

def main():
    ap = argparse.ArgumentParser(description='Extract COBOL IR to JSON')
    ap.add_argument('program', nargs='?', help='Program name, e.g. CBACT01C')
    ap.add_argument('--all',    action='store_true', help='Process all .cbl/.CBL files in source/cobol/')
    ap.add_argument('--stdout', action='store_true', help='Print JSON to stdout instead of file')
    ap.add_argument('--source', default=str(SOURCE_DIR), help='COBOL source directory')
    ap.add_argument('--out',    default=str(IR_DIR),     help='Output directory for IR JSON files')
    args = ap.parse_args()

    src_dir = Path(args.source)
    out_dir = Path(args.out)

    programs = []
    if args.all:
        programs = sorted(
            p.stem.upper()
            for p in src_dir.glob('*.[Cc][Bb][Ll]')
        )
    elif args.program:
        programs = [args.program.upper()]
    else:
        ap.print_help()
        sys.exit(1)

    failed = 0
    for prog in programs:
        ir = extract_ir(prog, src_dir)
        if 'error' in ir:
            print(f'ERROR [{prog}]: {ir["error"]}', file=sys.stderr)
            failed += 1
            continue

        out_json = json.dumps(ir, indent=2, default=str)

        if args.stdout:
            print(out_json)
        else:
            out_dir.mkdir(parents=True, exist_ok=True)
            out_file = out_dir / f'{prog}.json'
            out_file.write_text(out_json, encoding='utf-8')
            print(
                f'[{prog}]  {ir["source_lines"]} lines  '
                f'{len(ir["paragraphs"])} paragraphs  '
                f'{len(ir["data_items"])} fields  '
                f'{len(ir["comp3_fields"])} COMP-3  '
                f'{len(ir["unreferenced_fields"])} unreferenced  '
                f'→ {out_file.name}'
            )

    sys.exit(1 if failed else 0)


if __name__ == '__main__':
    main()
