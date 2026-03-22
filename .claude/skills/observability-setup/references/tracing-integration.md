# Tracing Integration Reference

## Python Agent Wrapper with Langfuse

```python
# scripts/agent_trace.py
"""Wrap agent executions with Langfuse tracing."""
import os
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

def trace_llm_call(trace, model, prompt, completion, tokens_in, tokens_out):
    """Log an LLM generation within a trace."""
    trace.generation(
        name="llm-call",
        model=model,
        input=prompt,
        output=completion,
        usage={"input": tokens_in, "output": tokens_out, "total": tokens_in + tokens_out},
    )

def score_task(trace, name: str, value: float, comment: str = ""):
    """Score a task (e.g., equivalence test pass rate)."""
    trace.score(name=name, value=value, comment=comment)
```

## Shell Wrapper for Claude Code

```bash
#!/bin/bash
# scripts/traced-agent.sh — Run a Claude Code agent with Langfuse trace context

AGENT_NAME="${1:-polecat}"
TASK_ID="${2:-TASK-000}"
MODULE="${3:-unknown}"

export LANGFUSE_PUBLIC_KEY="${LANGFUSE_PUBLIC_KEY:-pk-lf-local}"
export LANGFUSE_SECRET_KEY="${LANGFUSE_SECRET_KEY:-sk-lf-local}"
export LANGFUSE_BASE_URL="${LANGFUSE_BASE_URL:-http://localhost:3000}"

echo "[$(date)] Starting agent=$AGENT_NAME task=$TASK_ID module=$MODULE"
claude --agent "$AGENT_NAME" --print "Execute task $TASK_ID for module $MODULE"
echo "[$(date)] Completed agent=$AGENT_NAME task=$TASK_ID"
```

## Custom Prometheus Metrics Exporter

```python
# metrics/metrics-exporter/app.py
"""Prometheus exporter for modernization metrics."""
from prometheus_client import start_http_server, Gauge, Counter, Histogram
import glob, time

modules_total = Gauge('cobol_modules_total', 'COBOL modules by status', ['status'])
agent_idle_minutes = Gauge('agent_idle_minutes', 'Minutes since last agent activity', ['agent'])
lines_analyzed = Counter('cobol_lines_analyzed_total', 'Total COBOL lines analyzed')
tokens_consumed = Counter('agent_tokens_total', 'Tokens consumed', ['agent', 'model'])
task_duration = Histogram('agent_task_duration_seconds', 'Task completion time',
                          ['agent', 'task_type'],
                          buckets=[60, 300, 600, 1800, 3600, 7200])

def collect_metrics():
    source_count = len(glob.glob('source/*.cbl')) + len(glob.glob('source/*.cob'))
    analyzed = len(glob.glob('output/docs/RE-*.md'))
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
