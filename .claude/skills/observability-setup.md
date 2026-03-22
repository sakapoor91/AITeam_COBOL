---
name: observability-setup
description: >
  Skill for configuring the open-source observability stack:
  Langfuse + Prometheus + Grafana. Agent tracing, cost tracking,
  and modernization metrics dashboards.
---

# Observability Stack Skill

## Architecture
```
Agents (Claude Code / Codex)
    │
    ├──→ Langfuse (LLM Traces)      ← OpenTelemetry / SDK
    │       ├── Traces, spans, generations
    │       ├── Token usage & cost
    │       ├── Session grouping (per-module)
    │       └── Evaluation scores
    │
    ├──→ Prometheus (Metrics)         ← Custom exporter
    │       ├── agent_task_duration_seconds
    │       ├── agent_tokens_total
    │       ├── cobol_lines_analyzed_total
    │       └── translation_equivalence_pass_rate
    │
    └──→ Grafana (Dashboards)        ← Prometheus + Langfuse data
            ├── Executive View
            ├── Architecture View
            ├── Operations View
            └── Compliance View
```

## Langfuse Setup (Self-Hosted)

### Docker Compose
```yaml
# metrics/docker-compose.observability.yml
version: "3.8"
services:
  langfuse:
    image: langfuse/langfuse:latest
    ports:
      - "3000:3000"
    environment:
      - DATABASE_URL=postgresql://langfuse:langfuse@langfuse-db:5432/langfuse
      - NEXTAUTH_URL=http://localhost:3000
      - NEXTAUTH_SECRET=your-secret-here-change-in-production
      - SALT=your-salt-here-change-in-production
    depends_on:
      - langfuse-db

  langfuse-db:
    image: postgres:16
    environment:
      - POSTGRES_USER=langfuse
      - POSTGRES_PASSWORD=langfuse
      - POSTGRES_DB=langfuse
    volumes:
      - langfuse-data:/var/lib/postgresql/data

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./grafana/datasources:/etc/grafana/provisioning/datasources

  metrics-exporter:
    build: ./metrics-exporter
    ports:
      - "8080:8080"
    environment:
      - LANGFUSE_PUBLIC_KEY=pk-lf-your-key
      - LANGFUSE_SECRET_KEY=sk-lf-your-key
      - LANGFUSE_BASE_URL=http://langfuse:3000

volumes:
  langfuse-data:
  prometheus-data:
  grafana-data:
```

### Prometheus Config
```yaml
# metrics/prometheus.yml
global:
  scrape_interval: 30s

scrape_configs:
  - job_name: 'modernization-metrics'
    static_configs:
      - targets: ['metrics-exporter:8080']
    metrics_path: /metrics
```

## Python Tracing Integration

### Agent Wrapper with Langfuse
```python
# scripts/agent_trace.py
"""
Wrap agent executions with Langfuse tracing.
Use this to instrument any agent task.
"""
import os
import time
from langfuse import Langfuse

langfuse = Langfuse(
    public_key=os.getenv("LANGFUSE_PUBLIC_KEY", "pk-lf-local"),
    secret_key=os.getenv("LANGFUSE_SECRET_KEY", "sk-lf-local"),
    host=os.getenv("LANGFUSE_BASE_URL", "http://localhost:3000"),
)

def trace_agent_task(agent_name: str, task_id: str, module_name: str):
    """Create a Langfuse trace for an agent task."""
    trace = langfuse.trace(
        name=f"{agent_name}/{task_id}",
        metadata={
            "agent": agent_name,
            "task_id": task_id,
            "module": module_name,
            "project": "carddemo-modernization",
        },
        tags=[agent_name, module_name],
    )
    return trace

def trace_llm_call(trace, model: str, prompt: str, completion: str, tokens_in: int, tokens_out: int):
    """Log an LLM generation within a trace."""
    trace.generation(
        name="llm-call",
        model=model,
        input=prompt,
        output=completion,
        usage={
            "input": tokens_in,
            "output": tokens_out,
            "total": tokens_in + tokens_out,
        },
    )

def score_task(trace, name: str, value: float, comment: str = ""):
    """Score a task (e.g., equivalence test pass rate)."""
    trace.score(name=name, value=value, comment=comment)
```

### Shell Wrapper for Claude Code
```bash
#!/bin/bash
# scripts/traced-agent.sh
# Run a Claude Code agent with Langfuse trace context

AGENT_NAME="${1:-polecat}"
TASK_ID="${2:-TASK-000}"
MODULE="${3:-unknown}"

export LANGFUSE_PUBLIC_KEY="${LANGFUSE_PUBLIC_KEY:-pk-lf-local}"
export LANGFUSE_SECRET_KEY="${LANGFUSE_SECRET_KEY:-sk-lf-local}"
export LANGFUSE_BASE_URL="${LANGFUSE_BASE_URL:-http://localhost:3000}"

# Log task start
python3 -c "
from scripts.agent_trace import trace_agent_task
trace = trace_agent_task('$AGENT_NAME', '$TASK_ID', '$MODULE')
print(f'TRACE_ID={trace.id}')
" > /tmp/trace_${TASK_ID}.env

echo "[$(date)] Starting agent=$AGENT_NAME task=$TASK_ID module=$MODULE"

# Run Claude Code with the agent
claude --agent "$AGENT_NAME" --print "Execute task $TASK_ID for module $MODULE"

echo "[$(date)] Completed agent=$AGENT_NAME task=$TASK_ID"
```

## Metrics Exporter (Python)

```python
# metrics/metrics-exporter/app.py
"""
Custom Prometheus exporter that reads project state
and exposes modernization metrics.
"""
from prometheus_client import start_http_server, Gauge, Counter, Histogram
import os, glob, time

# Gauges
modules_total = Gauge('cobol_modules_total', 'COBOL modules by status', ['status'])
merge_queue_depth = Gauge('merge_queue_depth', 'Items pending in merge queue')
agent_idle_minutes = Gauge('agent_idle_minutes', 'Minutes since last agent activity', ['agent'])

# Counters
lines_analyzed = Counter('cobol_lines_analyzed_total', 'Total COBOL lines analyzed')
tokens_consumed = Counter('agent_tokens_total', 'Tokens consumed', ['agent', 'model'])

# Histograms
task_duration = Histogram('agent_task_duration_seconds', 'Task completion time',
                          ['agent', 'task_type'],
                          buckets=[60, 300, 600, 1800, 3600, 7200])

def collect_metrics():
    """Scan project directories to compute metrics."""
    source_count = len(glob.glob('source/*.cbl')) + len(glob.glob('source/*.cob'))
    analyzed = len(glob.glob('output/docs/RE-*.md'))
    translated = len(glob.glob('output/java/**/*.java', recursive=True)) > 0
    approved = len(glob.glob('output/docs/REVIEW-*.md'))

    modules_total.labels(status='total').set(source_count)
    modules_total.labels(status='analyzed').set(analyzed)
    modules_total.labels(status='approved').set(approved)

if __name__ == '__main__':
    start_http_server(8080)
    while True:
        collect_metrics()
        time.sleep(30)
```

## Grafana Dashboard Provisioning

```json
// metrics/grafana/datasources/datasource.yml
{
  "apiVersion": 1,
  "datasources": [
    {
      "name": "Prometheus",
      "type": "prometheus",
      "url": "http://prometheus:9090",
      "access": "proxy",
      "isDefault": true
    }
  ]
}
```

## Quick Start
```bash
cd metrics/
docker compose -f docker-compose.observability.yml up -d

# Langfuse UI: http://localhost:3000
# Prometheus: http://localhost:9090
# Grafana: http://localhost:3001 (admin/admin)
```
