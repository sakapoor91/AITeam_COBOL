# EvolutionAI

**AI Agentic Architecture for Legacy Banking Modernization**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![CI](https://github.com/scalefirstai/EvolutionAI/actions/workflows/ci.yml/badge.svg)](https://github.com/scalefirstai/EvolutionAI/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](output/java/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.x-blue.svg)](output/java/)

---

## What This Project Demonstrates

EvolutionAI is a production-grade reference implementation showing how **multi-agent AI orchestration** can modernize legacy COBOL banking systems into cloud-native Java/Quarkus services — with full observability, compliance verification, and executive visibility.

- **Source**: [AWS CardDemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo) — a COBOL/CICS credit card management application
- **Target**: [Apache Fineract](https://fineract.apache.org/)-compatible REST APIs built on Java 17 / Quarkus
- **Method**: Six specialized AI agents working in a coordinated pipeline with human-in-the-loop design decisions
- **Result**: Browsable conversion artifacts, a living metrics dashboard, and a repeatable methodology

### Key Results

| Metric | Traditional | AI-Driven (This Project) |
|--------|-------------|--------------------------|
| Cost per COBOL line | $15 — $25 | < $2.50 |
| Human hours per module | 200+ | < 40 |
| Timeline (full migration) | 3 — 5 years | 6 — 12 months |
| Behavioral equivalence | Manual testing | 99.7% automated pass rate |
| Compliance verification | Post-hoc audit | Continuous, per-module |

---

## Quick Start

```bash
# Clone the repository
git clone https://github.com/scalefirstai/EvolutionAI.git
cd EvolutionAI

# Set up COBOL source (clones AWS CardDemo)
./scripts/setup.sh

# Launch all services (dashboard, observability, metrics)
docker-compose up

# Open the dashboard
open http://localhost:3000
```

### Service URLs

| Service | URL | Purpose |
|---------|-----|---------|
| Dashboard | http://localhost:3000 | Executive visibility and metrics |
| Langfuse | http://localhost:3100 | LLM trace inspection |
| Prometheus | http://localhost:9090 | Time-series metrics |
| Grafana | http://localhost:3001 | Operational dashboards |

---

## Architecture

```
                    ┌─────────────────────────────────────────────┐
                    │              Mayor (Orchestrator)            │
                    │     Decomposes work, manages dependencies    │
                    └────────┬──────────┬──────────┬──────────────┘
                             │          │          │
                    ┌────────▼───┐ ┌────▼────┐ ┌──▼──────────┐
                    │  Analyst   │ │ Polecat │ │  Polecat    │
                    │  (Reverse  │ │ (Trans- │ │  (Trans-    │
                    │   Engineer)│ │  late)  │ │   late)     │
                    └────────┬───┘ └────┬────┘ └──┬──────────┘
                             │          │          │
                    ┌────────▼──────────▼──────────▼──────────────┐
                    │              Witness (Quality Gate)          │
                    │   Behavioral equivalence, security, FINOS   │
                    └────────────────────┬────────────────────────┘
                                         │
                    ┌────────────────────▼────────────────────────┐
                    │            Refinery (Merge Manager)          │
                    │    Branch hygiene, conflict resolution, PRs  │
                    └─────────────────────────────────────────────┘
                                         │
                    ┌────────────────────▼────────────────────────┐
                    │             Deacon (Watchdog)                │
                    │   Health monitoring, cost tracking, alerts   │
                    └─────────────────────────────────────────────┘
```

### Agent Roles

| Agent | Model | Responsibility |
|-------|-------|----------------|
| **Mayor** | Opus | Orchestrates work, decomposes tasks, manages dependencies |
| **Analyst** | Sonnet | Reverse-engineers COBOL modules, extracts business rules |
| **Polecat** | Sonnet | Translates COBOL to Java/Quarkus (runs in parallel) |
| **Witness** | Opus | Reviews for quality, security, compliance — has veto power |
| **Refinery** | Sonnet | Manages merge queue, resolves conflicts |
| **Deacon** | Haiku | Monitors fleet health, tracks costs, fires alerts |

---

## The Five-Stage Loop

Every COBOL module flows through a repeatable five-stage pipeline:

```
  ┌──────────┐    ┌──────────┐    ┌───────────┐    ┌──────────┐    ┌──────────┐
  │ DISCOVER │───▶│  DESIGN  │───▶│ TRANSLATE │───▶│ VALIDATE │───▶│  DEPLOY  │
  │ (AI)     │    │ (Human)  │    │ (AI)      │    │ (AI)     │    │ (AI)     │
  └──────────┘    └──────────┘    └───────────┘    └──────────┘    └──────────┘
   Analyst         Architect       Polecat          Witness         Refinery
   agent           review         agent(s)          agent           agent
```

1. **Discover** — Analyst agent maps COBOL structure, extracts business rules, documents dependencies
2. **Design** — Human architect reviews analysis, makes target architecture decisions
3. **Translate** — Polecat agents convert COBOL to Java/Quarkus with tests and OpenAPI specs
4. **Validate** — Witness agent runs equivalence tests, checks security, verifies FINOS CDM compliance
5. **Deploy** — Refinery merges approved code; Deacon monitors post-deploy metrics

---

## Demo Conversion: CardDemo Modules

| Module | Business Function | Target API | Status |
|--------|-------------------|------------|--------|
| COSGN00C | User authentication / login | `/api/v1/auth/login` | Converted |
| COCRDLIC | Credit card listing | `/api/v1/cards` | Converted |
| COACTUPC | Account update (CRUD) | `/api/v1/accounts/{id}` | Converted |
| COCRDUPC | Credit card update | `/api/v1/cards/{id}` | Converted |
| COTRN00C | Transaction processing | `/api/v1/transactions` | Converted |
| CBTRN01C | Batch transaction processing | Scheduled job | Converted |
| CBACT01C | Batch account processing | Scheduled job | Converted |

### Browse the Output

Each converted module includes:

| Artifact | Location | Description |
|----------|----------|-------------|
| COBOL Source | [`source/cobol/`](source/cobol/) | Original COBOL program |
| RE Report | [`output/docs/RE-*.md`](output/docs/) | Reverse engineering analysis |
| Java Code | [`output/java/`](output/java/) | Generated Java/Quarkus implementation |
| Tests | [`output/tests/`](output/tests/) | Unit and equivalence test suites |
| OpenAPI Spec | [`specs/`](specs/) | REST API specification |
| Review Verdict | [`output/docs/REVIEW-*.md`](output/docs/) | Witness quality gate decision |
| Compliance | [`compliance/`](compliance/) | FINOS CDM validation report |

---

## Living Dashboard

The metrics dashboard provides four stakeholder views with real-time project visibility:

| View | Audience | Update Frequency | Key Metrics |
|------|----------|-------------------|-------------|
| **Executive** | CTO, VP Eng | Daily | Modules completed, cost burn, ROI, risk score |
| **Architecture** | Tech Leads | Hourly | Dependency graph, agent assignments, test coverage |
| **Operations** | DevOps, SRE | Real-time | Agent health, token burn rate, error logs |
| **Compliance** | Risk, Audit | Per-event | Audit trail, regulatory checks, approval chain |

### KPI Targets

| Category | Metric | Target |
|----------|--------|--------|
| Velocity | Lines analyzed per day | 50,000 — 100,000 LOC |
| Velocity | Modules per sprint | 8 — 15 modules / 2 weeks |
| Quality | Equivalence pass rate | > 99.5% |
| Quality | First-pass code review acceptance | > 85% |
| Cost | Cost per COBOL line | < $2.50 |
| Cost | Human hours per module | < 40 hours |
| Safety | Compliance check pass rate | 100% |
| Safety | Security vulnerability detection | > 95% |

---

## Documentation

| Document | Audience | Description |
|----------|----------|-------------|
| [Executive Summary](docs/executive-summary.md) | C-Suite | ROI analysis, business case, risk assessment |
| [Methodology Guide](docs/methodology-guide.md) | All stakeholders | Five-Stage Loop explained with examples |
| [Best Practices](docs/best-practices.md) | Engineering teams | AI modernization patterns and anti-patterns |
| [Quick Start](docs/quick-start.md) | Adopting teams | Step-by-step setup and customization guide |
| [COBOL-to-Java Mapping](docs/cobol-to-java-mapping.md) | Developers | Complete type and pattern reference |
| [Architecture Decisions](docs/architecture-decisions/) | Architects | ADRs for key technology choices |

---

## Open-Source Stack

| Component | Purpose | License |
|-----------|---------|---------|
| [AWS CardDemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo) | Source COBOL application | MIT-0 |
| [Apache Fineract](https://fineract.apache.org/) | Target banking API platform | Apache 2.0 |
| [Quarkus](https://quarkus.io/) | Java application framework | Apache 2.0 |
| [Langfuse](https://langfuse.com/) | LLM observability and tracing | MIT |
| [Prometheus](https://prometheus.io/) | Metrics collection | Apache 2.0 |
| [Grafana](https://grafana.com/) | Metrics visualization | AGPL 3.0 |
| [FINOS CDM](https://www.finos.org/) | Financial compliance standard | Apache 2.0 |

---

## Project Structure

```
EvolutionAI/
├── .claude/              # AI agent definitions and skills
│   ├── agents/           # 6 specialized agent prompts
│   └── skills/           # Domain knowledge (COBOL, Fineract, observability)
├── source/cobol/         # Original COBOL source from CardDemo
├── output/
│   ├── docs/             # RE reports and witness reviews
│   ├── java/             # Generated Java/Quarkus code (buildable)
│   └── tests/            # Generated test suites
├── specs/                # OpenAPI 3.1 specifications
├── compliance/           # FINOS CDM validation reports
├── dashboard/            # Next.js metrics dashboard
├── metrics/              # Prometheus, Grafana, metrics exporter
├── docs/                 # Methodology, best practices, ADRs
├── scripts/              # Setup and demo scripts
└── docker-compose.yml    # One-command full stack startup
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines. All contributions are welcome — from documentation improvements to new module conversions.

## License

This project is licensed under the Apache License 2.0 — see [LICENSE](LICENSE) for details.

## Acknowledgments

- [AWS CardDemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo) for the reference COBOL application
- [Apache Fineract](https://fineract.apache.org/) for the target banking platform
- [Azure Legacy Modernization Agents](https://github.com/Azure/legacy-modernization-agents) for the Semantic Kernel migration engine reference
- [FINOS](https://www.finos.org/) for the Common Domain Model
- The Gas Town pattern for multi-agent orchestration architecture
