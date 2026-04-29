# Skills Guide

This document maps the knowledge areas needed to work with each layer of the AITeam_COBOL pipeline.

---

## COBOL Source Reading

Required to review or extend documentation quality. You do not need to write COBOL — only read it.

| Topic | Why it matters |
|-------|---------------|
| IDENTIFICATION / ENVIRONMENT / DATA / PROCEDURE DIVISION structure | Orientation — knowing where to look in any program |
| `COPY` and copybooks | Every copybook must be resolved inline in BIZ-*.md |
| `PIC` clauses and byte-width math | PIC byte counts are mechanically validated — errors fail Phase 1 |
| `COMP-3` / `USAGE PACKED-DECIMAL` | Flagging these prevents data-loss in Java migration |
| `88`-level condition names | Must be decoded (literal → meaning) in every doc |
| `PERFORM` / `PERFORM THRU` call trees | Section 2 (Program Flow) traces these exactly |
| `FILE STATUS` codes | Latent bugs from unhandled statuses go in Section 4 |
| CICS `EXEC CICS` verbs (online programs only) | Relevant for the CICS online programs (CO*.cbl) |

**Resources**: IBM Enterprise COBOL Language Reference, the `source/cobol/` files themselves.

---

## Java Migration Targets

Required for writing Section 4 (Migration Notes) accurately.

| Topic | Why it matters |
|-------|---------------|
| `BigDecimal` for monetary arithmetic | Every `COMP-3` currency field must flag this |
| JPA / Spring Data for VSAM equivalents | VSAM file access maps to repository patterns |
| JDBC / DB2 for embedded SQL programs | `EXEC SQL` programs need SQL layer notes |
| Date/time APIs (`LocalDate`, `DateTimeFormatter`) | Date conversion subroutines (e.g. CODATE01) |
| Character encoding (EBCDIC → UTF-8) | Flat file import/export programs |
| Security — no plaintext passwords | `COSGN00C` stores credentials in Working Storage |

---

## Claude Code and AI Agents

Required to run or modify the pipeline.

| Topic | Why it matters |
|-------|---------------|
| Claude Code CLI (`claude`) | All slash commands run inside Claude Code |
| Slash command authoring (`.claude/commands/`) | Adding or modifying `/document`, `/validate-doc`, etc. |
| Agent definitions (`.claude/agents/`) | `documenter` and `validator` agent prompts live here |
| `CLAUDE.md` conventions | Project brain — changes here affect every session |
| Prompt engineering for structured output | Documentation quality depends on well-scoped prompts |
| Multi-agent orchestration | `/document-all` fans out one agent per missing program |

**Resources**: [Claude Code docs](https://docs.anthropic.com/en/docs/claude-code), `.claude/CLAUDE.md` in this repo.

---

## Python Tooling

Required to run or extend the converter and Phase 1 validator.

| Topic | Why it matters |
|-------|---------------|
| `python-docx` | `_md_to_docx.py` converts BIZ-*.md to Word |
| `argparse` | `validate_doc.py` is invoked as a CLI tool |
| Regex / string matching | Phase 1 checks identifiers, line numbers, PIC math |
| File I/O | Batch generator iterates all 44 program folders |

Install dependencies: `pip install -r output/business-docs/tools/requirements.txt`

---

## Mermaid Diagrams

Required to read or generate the `-flow.png` execution diagrams in each BIZ-*.md.

- Syntax: `flowchart TD`, node labels, `-->`, `classDef`, `:::style`
- Renderer: `mmdc` (Mermaid CLI) — install with `npm install -g @mermaid-js/mermaid-cli`
- Phase 1 validator checks that every diagram has at least one `classDef` style block

---

## Git and CI

| Topic | Why it matters |
|-------|---------------|
| Pre-commit hook (`.claude/hooks/check-doc-sections.sh`) | Blocks commits of BIZ-*.md files missing required sections |
| `.gitignore` | `.docx` and `-flow.png` are generated artifacts — not committed |
| PR template (`.github/PULL_REQUEST_TEMPLATE.md`) | Migration PRs require a validation verdict before merge |

---

## Skill Map by Role

| Role | Must have | Nice to have |
|------|-----------|--------------|
| Documentation reviewer | COBOL reading, validation pipeline | Java migration targets |
| Pipeline engineer | Claude Code, Python tooling | Mermaid, git hooks |
| Java migration developer | Java migration targets, BIZ-*.md reading | COBOL reading |
| New contributor | Git, COBOL basics, Claude Code CLI | Everything above |
