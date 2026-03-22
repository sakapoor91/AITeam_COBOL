# ADR-003: Gas Town Orchestration Pattern for Agent Coordination

## Status

Accepted

## Context

The modernization pipeline requires multiple AI agents working in parallel on different COBOL modules while maintaining quality control, conflict-free merges, and full traceability. A coordination model is needed that defines clear roles, communication boundaries, and accountability.

Options considered:

- **Flat peer-to-peer**: All agents communicate directly with each other. Simple but creates coordination chaos at scale.
- **Single orchestrator**: One agent manages everything. Creates a bottleneck and single point of failure.
- **Hierarchical with specialization**: A central coordinator delegates to specialized agents, each with a defined role and accountability boundary. More complex to set up but scales reliably.

## Decision

We adopt the **Gas Town orchestration pattern**, a hierarchical agent coordination model with six specialized roles:

| Role | Agent | Responsibility |
|------|-------|----------------|
| **Mayor** | Orchestrator | Decomposes work, manages dependencies, assigns tasks to agents |
| **Analyst** | Reverse Engineer | Analyzes COBOL source, extracts business rules, produces RE reports |
| **Polecat** | Translator | Converts COBOL to Java (multiple instances for parallel translation) |
| **Witness** | Reviewer | Independent quality review, security audit, compliance validation |
| **Refinery** | Integrator | Manages merge queue, resolves conflicts, maintains branch integrity |
| **Deacon** | Monitor | Health monitoring, cost tracking, stale work detection, alerting |

Key design principles:

- **Single responsibility**: Each agent has exactly one role
- **Isolation**: Each agent works in its own Git worktree
- **Explicit handoffs**: Work moves between agents only through defined quality gates
- **Independence of review**: The Witness agent is structurally independent from the Polecat agents whose work it reviews
- **Human escalation**: The Mayor escalates to human architects for any decision outside defined parameters

## Consequences

### Positive

- Clear accountability: every defect can be traced to the responsible agent and stage
- Parallel execution: multiple Polecat agents translate independent modules simultaneously
- Independent review: Witness agent has no incentive to approve its own work, reducing quality gate bypass
- Scalable: adding more Polecat instances increases throughput without changing the coordination model
- Observable: each agent's actions are independently traceable in Langfuse

### Negative

- Higher setup complexity than a single-agent approach
- Inter-agent communication overhead adds latency to the pipeline
- Requires careful prompt engineering for each agent role to maintain boundaries
- The Mayor agent is still a coordination bottleneck for task assignment, though not for execution

### Neutral

- The pattern name ("Gas Town") is a project-internal convention; the underlying principles are standard distributed systems coordination patterns (supervisor hierarchy, work queues, independent auditors)
