# ADR-006: Langfuse for LLM Observability

## Status

Accepted

## Context

The modernization pipeline makes hundreds of LLM calls per module translation. Without observability into these calls, the team cannot:

- Track cost (token consumption per agent, per task, per module)
- Debug quality issues (which agent produced a defective translation, and what prompt led to it)
- Optimize performance (identify slow or redundant LLM calls)
- Satisfy audit requirements (prove that the AI-driven process is controlled and traceable)

Options considered:

- **Custom logging**: Build our own trace ingestion. Maximum flexibility but high development and maintenance cost.
- **LangSmith**: LangChain's observability platform. Tightly coupled to the LangChain ecosystem, which we do not use.
- **Langfuse**: Open-source LLM observability platform. Self-hostable, vendor-neutral, supports OpenTelemetry integration.
- **Prometheus/Grafana only**: Metrics but no trace-level detail for individual LLM calls.

## Decision

We will use **Langfuse** (self-hosted) as the primary LLM observability platform, complemented by Prometheus for metrics aggregation and Grafana for dashboards.

Architecture:

- Langfuse self-hosted via Docker Compose alongside the development stack
- Every LLM call instrumented via OpenTelemetry, reporting to Langfuse
- Each agent task tracked as a Langfuse **session**, with individual LLM calls as **traces** within the session
- Prometheus scrapes aggregated metrics from Langfuse and application endpoints
- Grafana dashboards visualize both real-time metrics (Prometheus) and drill-down traces (Langfuse links)

Required trace data per LLM call:

- Model identifier and version
- Input token count and output token count
- Latency (request duration)
- Estimated cost (based on model pricing)
- Agent role and task identifier
- Quality score (assigned by Witness agent during review)

Required Prometheus metrics:

- `agent_task_duration_seconds` (histogram)
- `agent_tokens_total` (counter, labeled by agent role and model)
- `cobol_lines_analyzed_total` (counter)
- `translation_equivalence_pass_rate` (gauge)

Alerting rules:

- Token spend exceeds $50/hour
- Equivalence test pass rate drops below 99%
- Any agent idle for more than 30 minutes
- Langfuse ingestion failures

## Consequences

### Positive

- Full traceability of every LLM call satisfies audit and compliance requirements
- Cost visibility enables proactive budget management and optimization
- Quality debugging: trace a defective output back to the exact prompt and model call that produced it
- Self-hosted deployment keeps all trace data within the organization's infrastructure
- Open-source eliminates vendor lock-in for observability tooling

### Negative

- Self-hosting Langfuse requires operational maintenance (updates, backups, storage management)
- Trace storage grows with pipeline volume; requires periodic archival or retention policies
- OpenTelemetry instrumentation adds a small amount of latency to each LLM call

### Neutral

- Langfuse is a relatively young project compared to established APM tools; the team should monitor its development roadmap and community health
