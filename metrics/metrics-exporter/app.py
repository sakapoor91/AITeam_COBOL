"""EvolutionAI Metrics Exporter - Exposes Prometheus metrics on /metrics at port 8080."""

import os
import random
import threading
import time

from flask import Flask
from prometheus_client import (
    Counter,
    Gauge,
    Histogram,
    generate_latest,
    CONTENT_TYPE_LATEST,
)

app = Flask(__name__)

# --- Prometheus metrics ---

cobol_modules_total = Gauge(
    "cobol_modules_total",
    "Number of COBOL modules by processing status",
    ["status"],
)

agent_task_duration_seconds = Histogram(
    "agent_task_duration_seconds",
    "Duration of agent tasks in seconds",
    buckets=[1, 5, 10, 30, 60, 120, 300, 600, 1800, 3600],
)

agent_tokens_total = Counter(
    "agent_tokens_total",
    "Total tokens consumed by agents",
)

translation_equivalence_pass_rate = Gauge(
    "translation_equivalence_pass_rate",
    "Ratio of translation equivalence tests passing (0.0 - 1.0)",
)


# --- Demo data generator ---

def generate_demo_data():
    """Populate metrics with realistic sample values for demonstration."""
    cobol_modules_total.labels(status="discovered").set(42)
    cobol_modules_total.labels(status="analyzing").set(8)
    cobol_modules_total.labels(status="translating").set(12)
    cobol_modules_total.labels(status="validated").set(18)
    cobol_modules_total.labels(status="failed").set(4)

    agent_tokens_total.inc(285000)
    translation_equivalence_pass_rate.set(0.87)

    for _ in range(50):
        agent_task_duration_seconds.observe(random.uniform(5, 600))


def update_demo_metrics():
    """Periodically update demo metrics to simulate live activity."""
    while True:
        time.sleep(30)
        # Simulate progress
        discovered = max(0, cobol_modules_total.labels(status="discovered")._value.get() - random.randint(0, 2))
        cobol_modules_total.labels(status="discovered").set(discovered)
        cobol_modules_total.labels(status="translating").inc(random.randint(0, 1))
        cobol_modules_total.labels(status="validated").inc(random.randint(0, 2))

        agent_tokens_total.inc(random.randint(500, 5000))
        agent_task_duration_seconds.observe(random.uniform(5, 600))

        pass_rate = min(1.0, translation_equivalence_pass_rate._value.get() + random.uniform(-0.02, 0.03))
        translation_equivalence_pass_rate.set(round(max(0.0, pass_rate), 4))


# --- Routes ---

@app.route("/metrics")
def metrics():
    return generate_latest(), 200, {"Content-Type": CONTENT_TYPE_LATEST}


@app.route("/health")
def health():
    return {"status": "ok"}, 200


# --- Startup ---

if __name__ == "__main__":
    data_mode = os.environ.get("DATA_MODE", "sample")
    if data_mode == "sample":
        generate_demo_data()
        updater = threading.Thread(target=update_demo_metrics, daemon=True)
        updater.start()

    app.run(host="0.0.0.0", port=8080)
