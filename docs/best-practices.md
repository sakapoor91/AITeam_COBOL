# Best Practices for AI-Driven COBOL Modernization

## 1. Always Reverse-Engineer Before Translating

The single most common cause of modernization failure is translating code without fully understanding it. COBOL programs often contain decades of accumulated business logic embedded in paragraph names, condition checks, and copybook structures that are not self-documenting.

**Rule**: Every module must have a complete Reverse-Engineering (RE) Report before any translation begins.

The RE Report must document:
- All business rules (validation logic, branching conditions, calculations)
- Data structures with their COBOL PIC definitions and semantic meaning
- External dependencies (CICS calls, VSAM file access, inter-program calls)
- Error handling paths and edge cases

Skipping this step saves a few hours upfront and costs weeks in rework when translated code silently diverges from the original behavior.

## 2. Human-in-the-Loop for Design Decisions

AI agents are effective at analyzing existing code and generating translations according to specifications. They should not make architectural decisions autonomously.

**Decisions that require human review:**
- API shape and endpoint design
- Database schema and data model choices
- Security architecture (authentication, authorization, encryption)
- Integration patterns between modernized and legacy modules
- Any change to established business rules

**Decisions agents can make autonomously:**
- Code formatting and style
- Test case generation from documented business rules
- Mapping COBOL types to Java types according to established tables
- Generating OpenAPI specifications from finalized API contracts

The boundary is clear: agents execute within defined parameters; humans define the parameters.

## 3. Use BigDecimal for All Currency and Financial Calculations

This is a non-negotiable rule for financial software. IEEE 754 floating-point arithmetic produces rounding errors that are unacceptable in banking.

**The problem:**

```java
// WRONG: float/double for currency
double a = 0.1;
double b = 0.2;
System.out.println(a + b);  // Output: 0.30000000000000004

// This is not a bug. This is how IEEE 754 works.
// In banking, this error compounds across millions of transactions.
```

**The solution:**

```java
// CORRECT: BigDecimal for currency
BigDecimal a = new BigDecimal("0.1");
BigDecimal b = new BigDecimal("0.2");
System.out.println(a.add(b));  // Output: 0.3

// Always use String constructor, never double constructor
// new BigDecimal(0.1) still carries the floating-point error
```

**Rules for BigDecimal usage:**
- Always construct from String literals, never from `double` values
- Specify `RoundingMode` explicitly on every division operation
- Use `compareTo()` for equality checks, never `equals()` (which also compares scale)
- Store as `NUMERIC` or `DECIMAL` in PostgreSQL, never as `FLOAT` or `DOUBLE PRECISION`

COBOL's packed decimal arithmetic (`COMP-3`) is inherently exact. The Java replacement must preserve this property.

## 4. Behavioral Equivalence Testing

Modernization success is measured by one criterion: does the new code produce the same outputs as the old code for the same inputs?

**Behavioral equivalence testing requires:**

1. **Input corpus**: Collect representative inputs from the original COBOL system, including edge cases and error conditions
2. **Expected outputs**: Record what the COBOL system produces for each input
3. **Automated comparison**: Run the same inputs through the Java system and compare outputs field by field
4. **Regression suite**: Maintain these tests permanently as a regression safety net

**What to compare:**
- Output values (amounts, dates, status codes, messages)
- Side effects (database writes, file updates, audit log entries)
- Error behavior (which inputs produce errors, what error codes/messages are returned)

**What not to compare:**
- Performance characteristics (the Java system may be faster or slower)
- Internal data structures (implementation details may differ)
- Log formatting (operational concerns, not business logic)

A 99.7% pass rate means that 997 out of 1000 test scenarios produce identical results. The remaining 0.3% must be individually reviewed and justified (typically intentional improvements like better error messages or stricter input validation).

## 5. Token Budget Management

AI agent operations consume tokens, and token consumption maps directly to cost. Without active management, costs can escalate rapidly during complex migrations.

**Budget controls:**
- Set per-module token budgets based on COBOL line count (guideline: 10-20 tokens per COBOL line for the full pipeline)
- Monitor token consumption in real-time via the Deacon agent and Langfuse dashboard
- Alert when any single agent task exceeds 2x the expected token count
- Alert when hourly spend exceeds $50

**Optimization strategies:**
- Use the Analyst agent's RE Report to front-load understanding, reducing back-and-forth during translation
- Break large COBOL programs into smaller translation units (paragraph-level rather than program-level)
- Cache common patterns (copybook translations, standard CICS mappings) to avoid re-analyzing identical structures
- Use smaller, faster models for routine tasks (formatting, boilerplate generation) and reserve larger models for complex business logic translation

## 6. Agent Isolation via Git Worktrees

Every agent operates in its own Git worktree, which is a separate working directory linked to the same repository but on an independent branch. This prevents agents from interfering with each other's work.

**Why worktrees matter:**
- Multiple Polecat agents can translate different modules in parallel without merge conflicts during active work
- The Witness agent reviews code on a separate branch from where it was produced, ensuring independent evaluation
- If an agent's work is rejected, its worktree can be discarded without affecting other agents

**Worktree rules:**
- One branch per agent task (naming convention: `agent/<role>/<module-name>`)
- No direct commits to `main` from any agent
- Worktrees are cleaned up after successful merge or rejection
- The Refinery agent is the only agent that merges branches

## 7. FINOS CDM Compliance from Day One

The FINOS Common Domain Model (CDM) provides standardized data structures and processes for financial services. Retrofitting compliance after migration is far more expensive than building it in from the start.

**What this means in practice:**
- Use CDM-aligned data types for financial instruments, parties, and events
- Generate compliance reports as part of the translation pipeline, not as a separate post-processing step
- The Witness agent validates CDM conformance as part of its Stage 4 review
- Non-conformance is a blocking finding that must be resolved before deployment

## 8. Observability from Day One

Every AI agent action, every LLM call, and every quality gate decision must be traceable. This is not optional.

**Required observability:**
- **Langfuse**: Traces every LLM call with model, token count, latency, cost, and result quality
- **Prometheus**: Exports metrics for agent task duration, token consumption, COBOL lines analyzed, and equivalence test pass rate
- **Grafana**: Dashboards for executive (cost and progress), architecture (translation quality), operations (agent health), and compliance (CDM conformance) views
- **Alerting**: Automated alerts for token budget overruns, equivalence test failures, agent idle time, and build failures

Observability serves three purposes: cost control, quality assurance, and audit compliance. Financial regulators expect to see evidence that modernization processes are controlled and traceable.

## 9. When to Use Overlay vs Full Migration

Not every COBOL module needs full translation to Java. Some modules are better served by an **overlay** approach, where a modern API layer sits in front of the existing COBOL system.

**Use full migration when:**
- The module contains business logic that needs to evolve rapidly
- The COBOL runtime environment is being decommissioned
- The module has no external dependencies that would remain on the legacy platform
- Regulatory requirements demand modern audit trails that the legacy system cannot provide

**Use overlay when:**
- The module is stable and rarely changed
- Full migration risk outweighs the benefit for this specific module
- The module will be retired within 2-3 years regardless
- Complex inter-system dependencies make extraction impractical in the current phase

The overlay approach uses API gateways and data transformation layers to present a modern REST interface while the underlying COBOL continues to execute. This is a pragmatic interim strategy, not a permanent solution.

## 10. Structuring Skills for AI Agent Workflows

Skills are the building blocks of agent behavior. A well-structured skill tells an agent exactly what to do, how to validate its work, and where to find detailed reference material when needed.

**The SKILL.md Directory Convention:**

Each skill lives in its own directory under `.claude/skills/` with a standard structure:

```
.claude/skills/<skill-name>/
├── SKILL.md          # Workflow steps with YAML frontmatter (<500 lines)
├── scripts/          # Executable validation and automation scripts
└── references/       # Detailed reference material (mapping tables, rubrics, guides)
```

This structure implements **progressive disclosure**: the SKILL.md contains concise, actionable steps that an agent follows. When the agent needs deeper detail (type mapping tables, API endpoint lists, scoring rubrics), it reads from `references/`. When it needs to validate its work, it runs scripts from `scripts/`.

**SKILL.md Frontmatter:**

Every SKILL.md starts with YAML frontmatter that declares metadata:

```yaml
---
name: code-transforming
description: COBOL-to-Java translation protocol with validation loops
agent: polecat
stage: translate
---
```

The `agent` field maps the skill to a Gas Town agent role. The `stage` field links it to the Five-Stage Loop (discover, design, translate, validate, deploy). This metadata enables the Mayor agent to assign skills to the right agents at the right pipeline stage.

**Validation Loops:**

Every skill should include a self-validation step. The agent runs a script from `scripts/` to check its own output before declaring the task complete. This catches common errors (missing Javadoc, float usage, incomplete RE reports) before the Witness agent review, reducing review cycles.

**Agent-Skill Mapping:**

| Agent | Primary Skills | Pipeline Stage |
|-------|---------------|----------------|
| Analyst | legacy-discovery, cobol-analysis | Discover |
| Architect (human) | api-wrapping | Design |
| Polecat | code-transforming, fineract-integration | Translate |
| Witness | testing-legacy | Validate |
| Deacon | monitoring-modernization, observability-setup | Deploy |
| Mayor | All (orchestration) | All |

Skills are not exclusive to one agent. The Mayor agent may invoke any skill during orchestration. The mapping above represents primary usage.

## 11. MCP Server Integration for Legacy Modernization

Model Context Protocol (MCP) servers extend agent capabilities by providing structured access to external systems. In a banking modernization context, security is the primary design constraint.

**Security-First Approach:**

MCP servers grant agents access to systems that contain production data, source code, and infrastructure controls. Every MCP integration must follow these principles:

1. **Read-only first**: Start with read-only access. Only grant write access after validating the agent workflow in a controlled environment.
2. **Least privilege**: Each MCP server should expose the minimum set of operations needed for its purpose.
3. **Credential isolation**: MCP server credentials are passed via environment variables, never hardcoded. Use `${VARIABLE}` references in configuration.
4. **Scope limits**: The filesystem MCP server is configured with explicit path allowlists (`./source`, `./output`), not broad access.

**Server Inventory for This Project:**

| Server | Purpose | Access Level |
|--------|---------|-------------|
| `github` | PR creation, branch management, code review | Read-write (required for agent workflow) |
| `postgres` | Query Fineract schema during translation | Read-only (schema inspection only) |
| `filesystem` | Access COBOL source and generated output | Read-only (source is never modified) |
| `langfuse` | Trace ingestion and query | Read-write (TODO: pending MCP server availability) |

**Configuration:**

MCP servers are configured in `.claude/settings.json`. The configuration specifies the command to launch each server, its arguments, and environment variables:

```json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "${GITHUB_TOKEN}"
      }
    }
  }
}
```

**When to Add a New MCP Server:**

Add an MCP server when agents repeatedly need structured access to an external system. The `mcp-building` skill provides step-by-step guidance for creating custom MCP servers, including a security checklist that must be completed before deployment. Do not add MCP servers speculatively — wait until an agent workflow demonstrates the need.
