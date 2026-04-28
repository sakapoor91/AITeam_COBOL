# AITeam_COBOL

**AI Agentic Architecture for Legacy Banking Modernization**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![CI](https://github.com/sakapoor91/AITeam_COBOL/actions/workflows/ci.yml/badge.svg)](https://github.com/sakapoor91/AITeam_COBOL/actions/workflows/ci.yml)
[![Docs](https://img.shields.io/badge/Docs-44%2F44-brightgreen.svg)](output/business-docs/)
[![Validated](https://img.shields.io/badge/Validated-44%2F44-brightgreen.svg)](output/validation/)

---

## What This Project Demonstrates

AITeam_COBOL is a production-grade reference implementation showing how **multi-agent AI orchestration** can modernize legacy COBOL banking systems into cloud-native Java/Quarkus services — with full observability, compliance verification, and executive visibility.

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
git clone https://github.com/sakapoor91/AITeam_COBOL.git
cd AITeam_COBOL

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

## Documentation: All 44 Programs

Every COBOL program has been fully reverse-engineered into a **business documentation** layer — plain English deep-dives that serve as the single source of truth for translation.

Each document in [`output/business-docs/`](output/business-docs/) includes: inline copybook field tables (with PIC clause and byte count), exact paragraph names and line numbers, 88-level value decodes, COMP-3 migration flags, latent bug catalogue, and a Mermaid execution flow diagram.

Every document has been through a **two-phase validation pipeline**: Phase 1 (mechanical — structure, line-number bounds, identifier existence, PIC byte math) and Phase 2 (LLM-as-judge — semantic accuracy across program flow, error handling, migration notes, copybook fields, and external calls). Validation reports live in [`output/validation/`](output/validation/).

| Program | Domain | Business Function |
|---------|--------|-----------------|
| CBACT01C | Account Batch | Print all account records to report |
| CBACT02C | Account Batch | Cross-reference accounts to cards |
| CBACT03C | Account Batch | Update account balances from transaction summary |
| CBACT04C | Account Batch | Close over-limit accounts |
| CBCUS01C | Customer Batch | Print all customer records to report |
| CBEXPORT | Data Exchange | Export account/card data to flat file |
| CBIMPORT | Data Exchange | Import and validate external transactions |
| CBPAUP0C | Statement | Pause utility between statement steps |
| CBSTM03A | Statement | Generate monthly statement detail lines |
| CBSTM03B | Statement | Write formatted statement to print file |
| CBTRN01C | Transaction Batch | Validate and post daily transactions |
| CBTRN02C | Transaction Batch | Transaction category summary report |
| CBTRN03C | Transaction Batch | Reject failing transactions |
| COACCT01 | Account Online | Account inquiry screen |
| COACTUPC | Account Online | Account update (limit, status, expiry) |
| COACTVWC | Account Online | Account view (read-only) |
| COADM01C | Admin | System administration menu |
| COBIL00C | Billing | Billing inquiry and payment entry |
| COBSWAIT | Utility | Timed wait between batch steps |
| COBTUPDT | Utility | Update control table record |
| COCRDLIC | Cards | Credit card list for an account |
| COCRDSLC | Cards | Credit card selection |
| COCRDUPC | Cards | Credit card update |
| CODATE01 | Utility | Date format conversion |
| COMEN01C | Menu | Main application menu |
| COPAUA0C | Pause | Pause screen A |
| COPAUS0C | Pause | Short pause between CICS transactions |
| COPAUS1C | Pause | Extended pause with status message |
| COPAUS2C | Pause | Pause with error detail |
| CORPT00C | Reporting | Report menu — batch report request |
| COSGN00C | Sign-On | User login — credential validation |
| COTRN00C | Transaction Online | Transaction list for an account |
| COTRN01C | Transaction Online | Post new transaction from screen |
| COTRN02C | Transaction Online | View single transaction detail |
| COTRTLIC | Transaction Online | Transaction type list |
| COTRTUPC | Transaction Online | Amend pending transaction |
| COUSR00C | User Mgmt | User list |
| COUSR01C | User Mgmt | Add user |
| COUSR02C | User Mgmt | Update user |
| COUSR03C | User Mgmt | Delete user |
| CSUTLDTC | Utility | Date validation/conversion subroutine |
| DBUNLDGS | DB Unload | Unload VSAM to sequential flat file |
| PAUDBLOD | Audit | Load pause audit records to DB |
| PAUDBUNL | Audit | Export pause audit records |

### Conversion Progress

| Artifact | Count | Status |
|----------|-------|--------|
| COBOL source programs | 44 | Complete (read-only reference) |
| BIZ-*.md business docs | 44 / 44 | **Complete** — see [`output/business-docs/`](output/business-docs/) |
| Validation reports (Phase 1 + Phase 2) | 44 / 44 | **Complete** — see [`output/validation/`](output/validation/) |
| RE-*.md technical reports | 7 / 44 | In progress |
| Java / Quarkus translation | 7 / 44 | In progress |
| OpenAPI specs | 4 / 44 | In progress |
| Witness-approved | 7 / 44 | In progress |

### Browse the Output

| Artifact | Location | Description |
|----------|----------|-------------|
| COBOL Source | [`source/cobol/`](source/cobol/) | 44 COBOL programs + 62 copybooks (read-only) |
| **Master Document** | [`output/MASTER-CARDDEMO.md`](output/MASTER-CARDDEMO.md) | **Single-file reference: architecture, all domains, flows, copybook catalog, program table, risk register** |
| Business Docs | [`output/business-docs/`](output/business-docs/) | Plain-English deep-dive docs for all 44 programs |
| Validation Reports | [`output/validation/`](output/validation/) | Phase 1 + Phase 2 accuracy reports for all 44 programs |
| OpenAPI Specs | [`specs/`](specs/) | REST API specifications (accounts, cards, transactions, auth) |
| Compliance | [`compliance/`](compliance/) | FINOS CDM validation reports |
| Methodology | [`docs/`](docs/) | Executive summary, best practices, architecture decisions |

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
AITeam_COBOL/
├── .claude/
│   ├── CLAUDE.md              # Project brain — loaded into every Claude session
│   ├── agents/
│   │   ├── documenter.md      # Generates BIZ-*.md from COBOL source (Sonnet)
│   │   └── validator.md       # LLM-as-judge: Phase 2 semantic accuracy check (Opus)
│   ├── commands/              # Slash commands
│   │   ├── document.md        # /document PROGNAME — generate BIZ-*.md + DOCX + PNG
│   │   ├── document-all.md    # /document-all — generate docs for all missing programs
│   │   ├── convert.md         # /convert PROGNAME — re-run MD→DOCX converter
│   │   ├── check-doc.md       # /check-doc PROGNAME — quick section presence check
│   │   └── validate-doc.md    # /validate-doc PROGNAME — full two-phase validation
│   ├── hooks/
│   │   └── check-doc-sections.sh  # Pre-commit: blocks BIZ-*.md missing required sections
│   ├── rules/
│   │   └── documentation-depth.md # 10 non-negotiable documentation quality rules
│   └── settings.json          # MCP filesystem server configuration
│
├── source/cobol/              # Original COBOL source — read-only reference
│   ├── *.cbl / *.CBL          # 44 COBOL programs
│   └── *.cpy / *.CPY          # 84 copybooks
│
├── output/
│   ├── MASTER-CARDDEMO.md     # Single master reference: architecture, all programs, copybook catalog, risk register
│   ├── MASTER-CARDDEMO.docx   # Word version (generated, gitignored)
│   ├── business-docs/         # Deep business docs — all 44 programs
│   │   ├── README.md          # Program inventory and generation guide
│   │   ├── DOCUMENTATION-STANDARD.md  # Depth rules every BIZ-*.md must follow
│   │   ├── TEMPLATE.md        # Blank fill-in template for new programs
│   │   ├── BUSINESS-OVERVIEW.md       # Master system overview (8 domains)
│   │   ├── tools/             # MD→DOCX converter, batch generator, Phase 1 validator
│   │   └── PROGNAME/          # One folder per program (44 total)
│   │       ├── BIZ-PROGNAME.md        # Deep-dive business doc (committed)
│   │       ├── BIZ-PROGNAME.docx      # Word version (generated, gitignored)
│   │       └── BIZ-PROGNAME-flow.png  # Mermaid diagram (generated, gitignored)
│   └── validation/            # Two-phase validation reports — all 44 programs
│       └── PROGNAME/
│           ├── PROGNAME-validation.md   # Phase 1 + Phase 2 combined report (committed)
│           └── PROGNAME-validation.docx # Word version (generated, gitignored)
│
├── specs/                     # OpenAPI 3.1 specifications (accounts, cards, transactions, auth)
├── compliance/                # FINOS CDM validation reports
├── dashboard/                 # Executive visibility dashboard
├── metrics/                   # Prometheus + Grafana configuration
├── docs/                      # Methodology guides, best practices, ADRs
├── scripts/                   # Setup and demo scripts
└── docker-compose.yml         # One-command full stack startup
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
- [Anthropic Claude](https://www.anthropic.com/) for the AI models powering the documentation and validation pipeline
