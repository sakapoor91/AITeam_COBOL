# EvolutionAI Quick-Start Guide

## Prerequisites

Before starting, ensure you have the following installed:

| Tool | Minimum Version | Purpose |
|------|----------------|---------|
| Docker | 24.0+ | Runs Langfuse, PostgreSQL, Grafana, Prometheus |
| Docker Compose | 2.20+ | Orchestrates the observability stack |
| Git | 2.40+ | Version control with worktree support |
| Java | 17+ | Compiles and runs generated Quarkus services |
| Maven | 3.9+ | Builds Java projects |
| Claude Code | Latest | AI agent runtime |

## Clone and Setup

```bash
# 1. Clone the repository
git clone https://github.com/your-org/evolution-ai.git
cd evolution-ai

# 2. Start the infrastructure stack (Langfuse, PostgreSQL, Grafana, Prometheus)
docker compose up -d

# 3. Verify all services are running
docker compose ps
```

Expected output: all containers in `running` state with health checks passing.

### Service Endpoints After Setup

| Service | URL | Default Credentials |
|---------|-----|-------------------|
| Langfuse Dashboard | http://localhost:3000 | admin / admin |
| Grafana | http://localhost:3001 | admin / admin |
| Prometheus | http://localhost:9090 | N/A |
| PostgreSQL | localhost:5432 | postgres / postgres |

## Running the Demo

The demo migrates the AWS CardDemo COSGN00C login module through all five stages.

```bash
# Run the full pipeline for the login module
./scripts/run-demo.sh --module COSGN00C

# Or run individual stages
./scripts/run-stage.sh discover COSGN00C
./scripts/run-stage.sh design COSGN00C    # Opens review in your editor
./scripts/run-stage.sh translate COSGN00C
./scripts/run-stage.sh validate COSGN00C
./scripts/run-stage.sh deploy COSGN00C
```

The demo takes approximately 10-15 minutes to complete all five stages. Progress is visible in real-time on the Grafana dashboard.

## Understanding Output Artifacts

After a successful run, the following artifacts are produced:

```
output/
  docs/
    COSGN00C-re-report.md        # Reverse-engineering report (Stage 1)
    COSGN00C-design-decisions.md  # Architecture decisions (Stage 2)
  java/
    src/main/java/
      com/evolution/auth/
        LoginResource.java        # REST endpoint
        LoginService.java         # Business logic
        UserRepository.java       # Data access
        dto/
          LoginRequest.java       # Request DTO (Java record)
          LoginResponse.java      # Response DTO (Java record)
  tests/
    src/test/java/
      com/evolution/auth/
        LoginResourceTest.java    # Unit tests
        LoginIntegrationTest.java # Behavioral equivalence tests
  specs/
    auth-login.yaml               # OpenAPI 3.1 specification
  compliance/
    COSGN00C-cdm-report.json      # FINOS CDM validation report
```

### Key Files to Review

- **RE Report**: Read this first to understand what the COBOL module does and what business rules were extracted
- **Design Decisions**: Shows the architectural choices made during Stage 2
- **Integration Tests**: These verify behavioral equivalence -- the most important quality signal
- **CDM Report**: Confirms FINOS compliance status

## Customizing for Your COBOL Codebase

### Step 1: Add Your COBOL Source

Place your COBOL source files in the `source/` directory:

```bash
cp /path/to/your/cobol/*.cbl source/
cp /path/to/your/cobol/*.cpy source/copybooks/
```

### Step 2: Configure Module Manifest

Create or edit `config/modules.yaml` to define your modules:

```yaml
modules:
  - name: ACCTPROC
    source: source/ACCTPROC.cbl
    copybooks:
      - source/copybooks/ACCTDATA.cpy
      - source/copybooks/ERRHDLR.cpy
    priority: high
    dependencies:
      - USRSEC
    notes: "Account processing - core business logic"
```

### Step 3: Configure Target Architecture

Edit `config/target-arch.yaml` to set your preferences:

```yaml
target:
  framework: quarkus          # quarkus or spring-boot
  java-version: 17
  database: postgresql
  package-base: com.yourorg.banking
  api-prefix: /api/v1
  auth-mechanism: jwt
```

### Step 4: Run the Pipeline

```bash
# Analyze all configured modules
./scripts/run-stage.sh discover --all

# Review RE Reports, then proceed module by module
./scripts/run-stage.sh design ACCTPROC
./scripts/run-stage.sh translate ACCTPROC
./scripts/run-stage.sh validate ACCTPROC
./scripts/run-stage.sh deploy ACCTPROC
```

## Monitoring via Dashboard

### Grafana Dashboards

Access Grafana at http://localhost:3001. Four pre-configured dashboards are available:

**Executive Dashboard**
- Total COBOL lines analyzed vs. translated
- Cumulative cost (token spend in dollars)
- Module completion progress
- Equivalence test pass rate trend

**Architecture Dashboard**
- Translation quality metrics per module
- Business rule coverage (rules extracted vs. rules tested)
- Code coverage (line and branch)
- OpenAPI spec validation status

**Operations Dashboard**
- Active agent count and status
- Agent task duration distribution
- Token consumption rate (tokens/minute)
- Queue depth and throughput

**Compliance Dashboard**
- FINOS CDM conformance per module
- Security review status
- Audit trail completeness
- Pending human approvals

### Langfuse Traces

Access Langfuse at http://localhost:3000 for detailed AI agent traces:

- Every LLM call with prompt, response, token count, latency, and cost
- Agent session traces linking individual calls to their parent task
- Quality scores assigned by the Witness agent
- Error traces for failed translation attempts

## Troubleshooting

### Docker services fail to start

```bash
# Check for port conflicts
lsof -i :3000 -i :3001 -i :5432 -i :9090

# Reset and restart
docker compose down -v
docker compose up -d
```

### Stage 1 (Discover) produces incomplete RE Report

- Verify all copybooks referenced by the COBOL source are present in `source/copybooks/`
- Check that the COBOL file uses standard formatting (columns 7-72)
- Review the Langfuse trace for the Analyst agent to identify parsing issues

### Stage 3 (Translate) generates code that does not compile

- Ensure Java 17+ is configured: `java --version`
- Check that the RE Report and design decisions are complete and consistent
- Review the Polecat agent trace in Langfuse for error details
- Run `mvn compile` manually in the output directory for detailed compiler errors

### Stage 4 (Validate) fails equivalence tests

- Review the specific test failures in the Witness agent report
- Compare the failing test's expected output (from COBOL behavior) against actual output (from Java code)
- Check whether the failure is a genuine logic error or a test data issue
- The Witness report includes suggested remediation steps

### High token consumption

- Check the Operations dashboard for agents consuming disproportionate tokens
- Large COBOL programs (>5000 lines) should be split into smaller translation units
- Verify that agents are not stuck in retry loops (Deacon agent should alert on this)
- Review Langfuse traces for unusually long agent conversations

### Agent appears stuck or idle

- The Deacon agent alerts after 30 minutes of inactivity
- Check the agent's Git worktree for uncommitted work: `git worktree list`
- Review the agent's last Langfuse trace for errors
- Restart the agent task: `./scripts/restart-agent.sh <agent-name> <task-id>`
