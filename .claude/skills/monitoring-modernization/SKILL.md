---
name: monitoring-modernization
description: Post-deployment monitoring, alert thresholds, and daily progress reports
agent: deacon
stage: deploy
---

# Monitoring Modernization Skill

## When to Use
Stage 5 of the Five-Stage Loop. Use after modules are merged and deployed.

## Prerequisites
- Observability stack running (see `observability-setup` skill)
- Modules deployed to target environment
- Langfuse tracing configured for all agents

## Steps

1. **Configure alerts**: Set thresholds per `references/alert-thresholds.md`:
   - Token spend > $50/hour → alert on-call
   - Equivalence test failure → block deployment
   - Agent idle > 30 minutes → notify Mayor agent
2. **Monitor post-deploy**: Watch for:
   - Error rate changes in the first 24 hours
   - Latency degradation vs baseline
   - Data integrity (financial calculations producing correct results)
3. **Generate daily report**: Run `scripts/generate-daily-report.sh`
   - Modules analyzed, translated, validated, deployed
   - Token spend and budget remaining
   - Test pass rates and coverage metrics
   - Active agent count and utilization
4. **Incident response**: Follow `references/incident-response.md` if alerts fire

## Key Metrics to Watch
- `translation_equivalence_pass_rate` — must stay ≥ 99.7%
- `agent_task_duration_seconds` — watch for outliers
- `agent_tokens_total` — track against budget
- `cobol_lines_analyzed_total` — progress indicator

## References
- `references/alert-thresholds.md` — Alert definitions and thresholds
- `references/incident-response.md` — Incident response procedures
