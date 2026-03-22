# Skills, MCP Servers, and Enforcement Guide

This guide covers the EvolutionAI project's skill system, MCP server integrations, slash commands, hooks, and rules. It serves as the primary reference for understanding how AI agents are configured and constrained in this modernization project.

## 1. Skill Directory Convention

Every skill follows the same directory structure:

```
.claude/skills/<skill-name>/
├── SKILL.md          # Concise workflow steps (<500 lines)
├── scripts/          # Executable validation and automation scripts
└── references/       # Detailed reference material
```

### Annotated SKILL.md Example

```yaml
---
name: code-transforming
description: COBOL-to-Java translation protocol with validation loops
agent: polecat            # Which Gas Town agent uses this skill
stage: translate          # Which pipeline stage (discover/design/translate/validate/deploy)
---
```

```markdown
# Code Transforming Skill

## Prerequisites
- RE report must exist at `output/docs/RE-{module}.md`
- PIC clause mapping loaded from `references/` directory

## Steps
1. Verify RE report exists (run `scripts/validate-translation.sh`)
2. Load COBOL source from `source/{module}.cbl`
3. Translate IDENTIFICATION DIVISION → Java class declaration with Javadoc
4. Translate DATA DIVISION → Java records and fields
5. Translate PROCEDURE DIVISION → Java methods
6. Validate: no float/double for currency, BigDecimal from String only
7. Generate unit tests for all translated methods
8. Self-review against `references/java-conventions.md`

## Validation
Run `scripts/validate-translation.sh {module}` before marking complete.
```

**Key principles:**
- **Progressive disclosure**: SKILL.md stays concise; detailed tables and rubrics go in `references/`
- **Self-validation**: Every skill includes scripts that the agent runs to verify its own output
- **Metadata-driven**: Frontmatter enables the Mayor agent to route skills to the right agents

## 2. Skills Inventory

All 9 skills in this project, organized by pipeline stage:

| Skill | Stage | Agent | Purpose |
|-------|-------|-------|---------|
| `legacy-discovery` | Discover | Analyst | Inventory COBOL codebase, dependency graphs, complexity scoring |
| `cobol-analysis` | Discover | Analyst | Deep analysis of individual COBOL modules, business rule extraction |
| `api-wrapping` | Design | Human + Polecat | OpenAPI spec generation, strangler-fig pattern design |
| `code-transforming` | Translate | Polecat | COBOL-to-Java translation with validation loops |
| `fineract-integration` | Translate | Polecat | Map translated code to Apache Fineract APIs |
| `testing-legacy` | Validate | Witness | Behavioral equivalence testing, coverage enforcement |
| `monitoring-modernization` | Deploy | Deacon | Post-deploy monitoring, alerts, daily reports |
| `observability-setup` | Infrastructure | Deacon | Langfuse + Prometheus + Grafana stack configuration |
| `mcp-building` | Infrastructure | Any | MCP server creation patterns, security checklist |

## 3. Creating a New Skill

Follow these steps to add a skill to the project:

### Step 1: Create the Directory Structure

```bash
mkdir -p .claude/skills/my-new-skill/{scripts,references}
```

### Step 2: Write the SKILL.md

Start with YAML frontmatter:

```yaml
---
name: my-new-skill
description: One-line description of what this skill does
agent: polecat          # analyst | polecat | witness | deacon | mayor
stage: translate        # discover | design | translate | validate | deploy
---
```

Then write concise, numbered steps. Keep SKILL.md under 500 lines. If you need detailed reference tables, put them in `references/`.

### Step 3: Add Validation Scripts

Create at least one script in `scripts/` that validates the skill's output:

```bash
#!/bin/bash
# scripts/validate-output.sh
# Validates that the skill's output meets project standards

MODULE="${1:?Usage: validate-output.sh <module-name>}"

# Check for specific requirements
if ! grep -q "BigDecimal" "output/java/${MODULE}.java" 2>/dev/null; then
    echo "ERROR: No BigDecimal usage found. All monetary amounts must use BigDecimal."
    exit 1
fi

echo "PASS: Validation complete for ${MODULE}"
exit 0
```

Make scripts executable: `chmod +x .claude/skills/my-new-skill/scripts/*.sh`

### Step 4: Add Reference Material

Place detailed mapping tables, rubrics, and guides in `references/`:

```
references/
├── mapping-table.md      # Detailed type/API mapping tables
├── conventions.md        # Coding conventions specific to this skill
└── examples.md           # Worked examples (optional)
```

### Step 5: Verify Structure

Run the CI skills-validation job or check manually:

```bash
# Verify required components exist
ls .claude/skills/my-new-skill/SKILL.md
ls .claude/skills/my-new-skill/scripts/
ls .claude/skills/my-new-skill/references/
```

## 4. Slash Commands Reference

Slash commands provide quick access to common workflows. They are defined in `.claude/commands/` and invoked with `/<command-name>`.

| Command | Usage | Purpose |
|---------|-------|---------|
| `/assess` | `/assess COSGN00C` | Quick complexity assessment of a COBOL module |
| `/plan-migration` | `/plan-migration COSGN00C COACTUPC COTRN00C` | Generate dependency-ordered migration plan |
| `/wrap-api` | `/wrap-api COACTUPC` | Generate OpenAPI spec + API wrapper skeleton |
| `/validate` | `/validate COACTUPC` | Run full validation suite on a translated module |

### `/assess`

Analyzes a single COBOL module and produces a structured assessment:
- Line count and structural breakdown
- COPY statement and CALL dependencies
- CICS command inventory
- Complexity score (Simple / Moderate / Complex)
- Migration recommendation

### `/plan-migration`

Takes multiple module names, builds a dependency graph, and outputs a migration plan ordered by dependencies (leaf modules first). Includes estimated complexity and recommended parallelism.

### `/wrap-api`

Generates an OpenAPI 3.1 specification and a Quarkus REST controller skeleton for a module. Requires that the module's RE report exists. The generated spec is placed in `specs/` and the controller in `output/java/`.

### `/validate`

Runs all quality checks for a translated module:
1. RE report existence
2. Forbidden pattern scan (float/double for currency)
3. BigDecimal constructor validation
4. Test coverage thresholds (90% line / 80% branch)
5. Equivalence test results

## 5. MCP Server Configuration

MCP servers are configured in `.claude/settings.json` at the project root.

### Current Server Inventory

**GitHub** (`@modelcontextprotocol/server-github`):
- Enables PR creation, branch management, and code review
- Required for the Refinery agent's merge workflow
- Requires `GITHUB_TOKEN` environment variable

**PostgreSQL** (`@modelcontextprotocol/server-postgres`):
- Read-only access to the Fineract database schema
- Used during translation to verify data model alignment
- Requires `FINERACT_DB_URL` environment variable

**Filesystem** (`@modelcontextprotocol/server-filesystem`):
- Read-only access to `source/` and `output/` directories
- Provides structured file access for agents
- No credentials required

**Langfuse** (TODO):
- Trace ingestion and querying
- Pending availability of an official Langfuse MCP server
- See `mcp-building` skill for guidance on building a custom server

### Adding a New MCP Server

1. Identify the need (an agent workflow that repeatedly accesses an external system)
2. Complete the security checklist in `.claude/skills/mcp-building/references/mcp-security-checklist.md`
3. Add the server configuration to `.claude/settings.json`
4. Test with read-only access first
5. Document the server in this guide

## 6. MCP Security Model

Security constraints for MCP servers in this banking modernization project:

**Credential Management:**
- All credentials passed via environment variables (`${VARIABLE}` syntax)
- Never hardcode tokens, passwords, or connection strings
- Use `.env` files locally (gitignored) and CI secrets in pipelines

**Access Control:**
- Start with read-only access for every new MCP server
- Upgrade to read-write only when the workflow requires it and has been validated
- The filesystem server uses explicit path allowlists, not broad access

**Audit:**
- All MCP server interactions are logged via Langfuse tracing
- The Deacon agent monitors MCP server health and usage patterns
- Unusual access patterns trigger alerts

**Scope Limits:**
- PostgreSQL server connects to the Fineract schema only, not application databases
- GitHub server scoped to the project repository
- Filesystem server restricted to `source/` and `output/` directories

## 7. Hooks and Rules Enforcement

### Hooks

Hooks are shell scripts in `.claude/hooks/` that run automatically at specific lifecycle points. They enforce project constraints programmatically.

| Hook | Trigger | What It Checks |
|------|---------|----------------|
| `check-no-float-currency.sh` | Pre-commit | No `float`/`double` in monetary contexts |
| `check-bigdecimal-constructor.sh` | Pre-commit | No `new BigDecimal(double)` anti-pattern |
| `check-no-main-commit.sh` | Pre-commit | No direct commits to `main` branch |
| `check-re-report-exists.sh` | Pre-translate | RE report exists before translation starts |

Each hook outputs an educational error message explaining:
1. **What** was detected
2. **Why** it is forbidden
3. **How** to fix it

Example output from `check-no-float-currency.sh`:
```
ERROR: float/double detected in monetary context
  File: output/java/AccountService.java:42
  Found: double balance = 0.0;

WHY: IEEE 754 floating-point arithmetic produces rounding errors.
  In banking, 0.1 + 0.2 = 0.30000000000000004, not 0.3.
  This error compounds across millions of transactions.

FIX: Use BigDecimal instead:
  BigDecimal balance = BigDecimal.ZERO;
```

### Rules

Rules are markdown files in `.claude/rules/` that provide standing instructions for all agents. Unlike hooks (which are automated checks), rules are guidance that agents read and follow.

| Rule File | Key Requirements |
|-----------|-----------------|
| `legacy-safety.md` | Never translate without RE report, never skip Witness review |
| `testing-requirements.md` | 90% line / 80% branch coverage, equivalence tests mandatory |
| `documentation-standards.md` | Javadoc with COBOL references, OpenAPI specs required |
| `financial-accuracy.md` | BigDecimal always, HALF_EVEN rounding, String constructor |

Rules are loaded into agent context at startup. They complement hooks: hooks catch violations automatically, while rules guide agents to avoid violations in the first place.

## 8. Integration Map

How all the pieces connect in the Five-Stage modernization loop:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        MAYOR (Orchestrator)                         │
│  Reads: all skills, rules, slash commands                          │
│  Assigns: tasks to agents based on skill.agent metadata            │
└──────────┬──────────┬──────────┬──────────┬──────────┬─────────────┘
           │          │          │          │          │
     ┌─────▼────┐ ┌───▼───┐ ┌───▼────┐ ┌───▼────┐ ┌──▼─────┐
     │ DISCOVER │ │DESIGN │ │TRANSLATE│ │VALIDATE│ │ DEPLOY │
     │          │ │       │ │         │ │        │ │        │
     │ Skills:  │ │Skills:│ │ Skills: │ │Skills: │ │Skills: │
     │ legacy-  │ │api-   │ │ code-   │ │testing-│ │monitor-│
     │ discovery│ │wrapping│ │ trans-  │ │legacy  │ │ing-mod │
     │ cobol-   │ │       │ │ forming │ │        │ │observ- │
     │ analysis │ │       │ │ fineract│ │        │ │ability │
     │          │ │       │ │ -integ  │ │        │ │        │
     │ Agent:   │ │Agent: │ │ Agent:  │ │Agent:  │ │Agent:  │
     │ Analyst  │ │Human  │ │ Polecat │ │Witness │ │Deacon  │
     └──────────┘ └───────┘ └─────────┘ └────────┘ └────────┘
           │          │          │          │          │
           ▼          ▼          ▼          ▼          ▼
     ┌─────────────────────────────────────────────────────────┐
     │                    ENFORCEMENT LAYER                     │
     │  Hooks: automated checks (pre-commit, pre-translate)    │
     │  Rules: agent guidance (loaded at startup)              │
     │  CI:    pipeline validation (coverage, security, lint)  │
     └─────────────────────────────────────────────────────────┘
           │          │          │
     ┌─────▼────┐ ┌───▼────┐ ┌──▼──────┐
     │  GitHub  │ │Postgres│ │Filesystem│
     │  MCP     │ │  MCP   │ │  MCP     │
     │ (PRs,    │ │(schema │ │(source,  │
     │  review) │ │ query) │ │ output)  │
     └──────────┘ └────────┘ └──────────┘
```

### Data Flow

1. **Discover**: Analyst agent uses `legacy-discovery` + `cobol-analysis` skills to produce RE reports in `output/docs/`
2. **Design**: Human architect reviews RE reports, uses `/wrap-api` to generate OpenAPI specs in `specs/`
3. **Translate**: Polecat agents use `code-transforming` + `fineract-integration` to produce Java in `output/java/`. Hooks run pre-commit checks automatically.
4. **Validate**: Witness agent uses `testing-legacy` to run equivalence tests. `/validate` command provides a quick check.
5. **Deploy**: Refinery agent merges via GitHub MCP. Deacon agent uses `monitoring-modernization` to track post-deploy metrics via Langfuse and Prometheus.

### Slash Command Integration

Slash commands are entry points that invoke skills:
- `/assess` → triggers `cobol-analysis` skill
- `/plan-migration` → triggers `legacy-discovery` skill
- `/wrap-api` → triggers `api-wrapping` skill
- `/validate` → triggers `testing-legacy` skill

### MCP Server Usage by Agent

| Agent | MCP Servers Used |
|-------|-----------------|
| Analyst | filesystem (read COBOL source) |
| Polecat | filesystem (read source, write output), postgres (schema queries) |
| Witness | filesystem (read output for review) |
| Refinery | github (create PRs, manage branches) |
| Deacon | postgres (query Langfuse data), github (check CI status) |
