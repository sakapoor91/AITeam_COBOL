# ADR-005: Three-Tier Memory Architecture for Agents

## Status

Accepted

## Context

AI agents in the modernization pipeline need access to context from previous tasks, architectural decisions, and accumulated knowledge about the COBOL codebase. Without structured memory, each agent invocation starts from zero, leading to redundant analysis, inconsistent decisions, and wasted tokens.

However, giving every agent access to all historical context creates two problems: excessive token consumption (context window limits) and information overload (agents perform worse when given irrelevant context).

A structured memory architecture is needed that provides the right context at the right time.

## Decision

We implement a **three-tier memory architecture**:

### Tier 1: Working Memory (per-task)

- Scope: Single agent task execution
- Contents: Current COBOL module source, RE report, design decisions, work-in-progress code
- Lifetime: Created at task start, discarded at task completion
- Storage: Agent's Git worktree + in-context prompt
- Size: Fits within a single LLM context window

### Tier 2: Project Memory (per-project)

- Scope: Shared across all agents within a project
- Contents: Completed RE reports, architecture decisions (ADRs), translation pattern library, copybook mappings, coding standards
- Lifetime: Persists for the duration of the project
- Storage: Git repository (files in `output/docs/`, `specs/`, `.claude/` directories)
- Access: Agents retrieve relevant documents based on the module they are working on

### Tier 3: Institutional Memory (cross-project)

- Scope: Shared across all modernization projects within the organization
- Contents: COBOL-to-Java mapping reference, common error patterns, lessons learned, performance benchmarks
- Lifetime: Persists indefinitely, curated by human architects
- Storage: Shared documentation repository or knowledge base
- Access: Read-only for agents; human-curated and maintained

### Memory Retrieval Rules

- Agents receive Tier 1 memory automatically (it is the task context)
- Agents request Tier 2 memory by referencing specific modules or decisions (e.g., "retrieve RE report for ACCTPROC")
- Tier 3 memory is injected into agent prompts as reference material (e.g., the COBOL-to-Java mapping guide)
- No agent has write access to Tier 3; only human architects can update institutional memory

## Consequences

### Positive

- Agents receive relevant context without token waste from irrelevant history
- Consistent architectural decisions across modules via shared Tier 2 ADRs
- Accumulated learning persists across projects via Tier 3 institutional memory
- Clear separation of concerns: working context, project knowledge, organizational knowledge

### Negative

- Requires active curation of Tier 2 and Tier 3 memory by human team members
- Memory retrieval adds latency to agent task startup
- Risk of stale information in Tier 2/3 if not regularly reviewed and updated

### Neutral

- The three-tier model mirrors standard caching architectures (L1/L2/L3), making it intuitive for engineering teams to reason about
