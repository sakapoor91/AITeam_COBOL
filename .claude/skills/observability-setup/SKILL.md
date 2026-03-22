---
name: observability-setup
description: Configure Langfuse + Prometheus + Grafana observability stack for agent tracing and metrics
agent: deacon
stage: deploy
---

# Observability Setup Skill

## Architecture
```
Agents (Claude Code)
    ├──→ Langfuse (LLM Traces)      ← OpenTelemetry / SDK
    ├──→ Prometheus (Metrics)         ← Custom exporter
    └──→ Grafana (Dashboards)        ← Prometheus data source
```

## Prerequisites
- Docker and Docker Compose installed
- Network access for pulling container images
- Port availability: 3000 (Langfuse), 9090 (Prometheus), 3001 (Grafana), 8080 (exporter)

## Steps

1. **Deploy the stack**: Run Docker Compose from `metrics/` directory
   ```bash
   cd metrics/ && docker compose -f docker-compose.observability.yml up -d
   ```
2. **Configure Langfuse**: Access http://localhost:3000, create project, obtain API keys
3. **Set environment variables**: Export `LANGFUSE_PUBLIC_KEY` and `LANGFUSE_SECRET_KEY`
4. **Verify Prometheus**: Access http://localhost:9090, confirm `modernization-metrics` target is UP
5. **Configure Grafana**: Access http://localhost:3001 (admin/admin), verify Prometheus data source
6. **Validate**: Run `scripts/check-observability-stack.sh` to verify all services

## Key Metrics
- `agent_task_duration_seconds` — Task completion time by agent and type
- `agent_tokens_total` — Token consumption by agent and model
- `cobol_lines_analyzed_total` — Progress tracking
- `translation_equivalence_pass_rate` — Quality metric

## Alert Thresholds
- Token spend > $50/hour
- Equivalence test failure
- Agent idle > 30 minutes

## References
- `references/langfuse-setup.md` — Docker Compose config, Langfuse details
- `references/prometheus-grafana-setup.md` — Prometheus and Grafana configuration
- `references/tracing-integration.md` — Python tracing code, metrics exporter
