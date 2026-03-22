# Legacy Banking Modernization — Agentic Architecture Project

## Project Identity
This is a multi-agent COBOL-to-modern-stack banking modernization project.
We are converting AWS CardDemo (COBOL credit card management) into Apache Fineract-compatible Java/Quarkus services using AI agent orchestration.

## Architecture Stack
- **Source codebase**: AWS CardDemo (COBOL, CICS, VSAM)
- **Migration engine**: Azure Legacy-Modernization-Agents (Semantic Kernel)
- **Orchestration**: Gas Town patterns (Mayor/Polecat/Witness/Refinery)
- **Target platform**: Apache Fineract APIs (Java/Spring Boot)
- **Compliance**: FINOS CDM + Open-Source Regulatory Reporting
- **Observability**: Langfuse (self-hosted) + Grafana + Prometheus
- **Agent coordination**: Claude Code Agent Teams + subagents

## Tech Constraints
- Java 17+ for all generated output code
- Quarkus framework preferred; Spring Boot acceptable
- PostgreSQL for persistence (no Oracle dependencies)
- OpenAPI 3.1 specs required for every new service endpoint
- All COBOL business rules must be documented BEFORE translation
- Git worktrees for agent isolation (one branch per agent)
- Every agent action logged to Langfuse via OpenTelemetry

## Coding Standards
- No code generation without a SKILL.md or spec reference
- All generated Java must include Javadoc with COBOL source reference
- Test coverage minimum: 90% line coverage, 80% branch coverage
- Integration tests must validate behavioral equivalence against COBOL
- Use record types for DTOs, sealed interfaces for domain types
- No `var` in public API signatures
- All monetary amounts use `BigDecimal`, never `double` or `float`

## Forbidden Patterns
- NEVER generate code that uses `float` or `double` for currency
- NEVER skip the reverse-engineering step before translation
- NEVER merge agent output without Witness agent approval
- NEVER commit directly to `main` — all work via feature branches
- NEVER use Spring WebFlux (stick to servlet model for banking)
- NEVER auto-approve security-sensitive changes (auth, crypto, PII)

## Agent Roles (Gas Town Mapping)
| Role | Agent | Responsibility |
|------|-------|----------------|
| Mayor | `@agent-mayor` | Orchestrates work, decomposes tasks, manages dependencies |
| Polecat | `@agent-polecat-*` | Executes translation/refactoring tasks in parallel |
| Witness | `@agent-witness` | Reviews all output for quality, compliance, security |
| Refinery | `@agent-refinery` | Manages merge queue, resolves conflicts |
| Deacon | `@agent-deacon` | Health monitoring, stale work detection, cost tracking |
| Analyst | `@agent-analyst` | COBOL analysis, dependency mapping, business rule extraction |

## Workflow: Five-Stage Loop (per module)
1. **Discover** → analyst agent maps COBOL module structure + dependencies
2. **Design** → human architect reviews, makes target architecture decisions
3. **Translate** → polecat agents convert COBOL → Java in parallel
4. **Validate** → witness agent runs equivalence tests + compliance checks
5. **Deploy** → refinery merges, deacon monitors post-deploy metrics

## Observability Requirements
- Every LLM call traced in Langfuse with: model, tokens, latency, cost
- Every agent task tracked as a Langfuse session with parent trace
- Prometheus metrics exported: `agent_task_duration_seconds`, `agent_tokens_total`, `cobol_lines_analyzed_total`, `translation_equivalence_pass_rate`
- Grafana dashboards: Executive, Architecture, Operations, Compliance views
- Alert on: token spend > $50/hour, equivalence test failure, agent idle > 30min

## MCP Servers (configure in settings)
- `github` — PR creation, branch management, code review
- `postgres` — query Fineract schema during translation
- `langfuse` — trace ingestion and query (when MCP server available)

## Key File Locations
- `source/` — COBOL source files from CardDemo
- `output/java/` — Generated Java/Quarkus output
- `output/tests/` — Generated test suites
- `output/docs/` — Reverse-engineering documentation
- `specs/` — OpenAPI specs for new endpoints
- `compliance/` — FINOS CDM validation reports
- `metrics/` — Observability configuration (Langfuse, Grafana, Prometheus)
- `.claude/agents/` — Subagent definitions
- `.claude/skills/` — Project-specific skills
