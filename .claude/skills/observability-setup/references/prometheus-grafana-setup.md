# Prometheus & Grafana Setup Reference

## Prometheus Configuration

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

## Docker Compose (Prometheus + Grafana)

```yaml
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
```

## Grafana Datasource Provisioning

```yaml
# metrics/grafana/datasources/datasource.yml
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    url: http://prometheus:9090
    access: proxy
    isDefault: true
```

## Dashboard Views
- **Executive**: Cost, progress, timeline
- **Architecture**: Translation quality, pattern compliance
- **Operations**: Agent health, task duration, queue depth
- **Compliance**: CDM conformance, test pass rates
