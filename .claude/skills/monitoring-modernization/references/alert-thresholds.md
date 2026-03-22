# Alert Thresholds

## Cost Alerts

| Alert | Threshold | Severity | Action |
|-------|----------|----------|--------|
| Hourly token spend | > $50/hour | Critical | Pause non-essential agents, notify team |
| Daily token spend | > $500/day | Warning | Review agent efficiency, check for loops |
| Single task tokens | > 2x expected | Warning | Investigate task, check for infinite loops |

## Quality Alerts

| Alert | Threshold | Severity | Action |
|-------|----------|----------|--------|
| Equivalence test failure | Any failure | Critical | Block merge, review with Witness agent |
| Line coverage drop | < 90% | Warning | Add missing tests before proceeding |
| Branch coverage drop | < 80% | Warning | Add branch coverage tests |
| Build failure | Any | Critical | Fix immediately, block further translations |

## Operational Alerts

| Alert | Threshold | Severity | Action |
|-------|----------|----------|--------|
| Agent idle | > 30 minutes | Warning | Check for stuck tasks, reassign or restart |
| Merge queue depth | > 10 items | Warning | Prioritize Refinery agent, reduce parallelism |
| Worktree count | > 20 | Warning | Clean up completed/abandoned worktrees |
| Disk usage | > 80% | Critical | Clean build artifacts, old worktrees |

## Post-Deploy Alerts

| Alert | Threshold | Severity | Action |
|-------|----------|----------|--------|
| Error rate increase | > 1% over baseline | Critical | Rollback, investigate |
| Latency increase | > 2x baseline | Warning | Profile, optimize |
| Financial calc divergence | Any | Critical | Immediate rollback, audit |

## Prometheus Alert Rules

```yaml
groups:
  - name: modernization-alerts
    rules:
      - alert: HighTokenSpend
        expr: rate(agent_tokens_total[1h]) * 0.00001 > 50
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Token spend exceeds $50/hour"

      - alert: AgentIdle
        expr: agent_idle_minutes > 30
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Agent {{ $labels.agent }} idle for >30 minutes"
```
