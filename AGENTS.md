# Agent Quick Reference

## Running Agents

```bash
# Orchestrator — decomposes and assigns work
claude --agent mayor "Analyze and plan migration for COSGN00C login module"

# Reverse Engineering — analyze COBOL before translation
claude --agent analyst "Analyze source/cobol/COSGN00C.cbl"

# Translation — convert COBOL to Java (run multiple in parallel)
claude --agent polecat "Translate COSGN00C based on output/docs/RE-COSGN00C.md"

# Quality Gate — review translation output
claude --agent witness "Review output/java/ for COSGN00C translation"

# Merge Management — integrate approved work
claude --agent refinery "Merge approved COSGN00C translation to main"

# Health Monitoring — check system status
claude --agent deacon "Generate daily health report"
```

## Agent Hierarchy

```
Mayor (Opus) ─── orchestrates everything
  ├── Analyst (Sonnet) ─── reverse-engineers COBOL
  ├── Polecat-1 (Sonnet) ─── translates module A
  ├── Polecat-2 (Sonnet) ─── translates module B (parallel)
  ├── Witness (Opus) ─── reviews all output (veto power)
  ├── Refinery (Sonnet) ─── manages merges
  └── Deacon (Haiku) ─── monitors health + cost
```

## Agent Output Locations

| Agent | Produces | Location |
|-------|----------|----------|
| Analyst | Reverse engineering reports | `output/docs/RE-<MODULE>.md` |
| Polecat | Java source code | `output/java/com/carddemo/<module>/` |
| Polecat | Test suites | `output/tests/com/carddemo/<module>/` |
| Polecat | OpenAPI specs | `specs/<module>-api.yml` |
| Witness | Review verdicts | `output/docs/REVIEW-<MODULE>.md` |
| Refinery | Pull requests | GitHub PRs |
| Deacon | Health reports | `metrics/daily-report.md` |
| Mayor | Status updates | `STATUS.md` |

## Rules

1. **Never skip the Analyst step** — reverse-engineering must happen before translation
2. **Never merge without Witness approval** — the Witness has veto power
3. **All monetary amounts use BigDecimal** — never `double` or `float` for currency
4. **One branch per agent** — Git worktrees provide isolation
5. **Every action is traced** — all LLM calls logged to Langfuse
