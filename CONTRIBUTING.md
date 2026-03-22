# Contributing to EvolutionAI

Thank you for your interest in contributing to EvolutionAI! This project demonstrates AI-driven legacy modernization using multi-agent orchestration, and we welcome contributions that improve the methodology, tooling, and documentation.

## How to Contribute

### Reporting Issues

- Use the appropriate issue template (bug report, feature request, or module migration)
- Include reproduction steps for bugs
- For module migration requests, specify the COBOL program name and business function

### Submitting Changes

1. **Fork** the repository
2. **Create a feature branch** from `main`: `git checkout -b feature/your-feature`
3. **Make your changes** following the coding standards below
4. **Test your changes** — all existing tests must pass
5. **Submit a Pull Request** using the PR template

### Branch Naming Convention

- `feature/` — New functionality
- `fix/` — Bug fixes
- `docs/` — Documentation updates
- `module/` — COBOL module conversion work (e.g., `module/COSGN00C-auth`)

## Coding Standards

### Java (Generated Output)

- Java 17+ syntax required
- Quarkus framework preferred
- All monetary amounts use `BigDecimal`, never `double` or `float`
- Javadoc required on all public methods, referencing original COBOL source
- Record types for DTOs, sealed interfaces for domain types
- No `var` in public API signatures
- Minimum 90% line coverage, 80% branch coverage

### Documentation

- Every COBOL module must have a reverse-engineering report (`RE-*.md`) before translation
- Every translation must have a witness review (`REVIEW-*.md`) before merge
- Architecture decisions documented as ADRs in `docs/architecture-decisions/`

### Dashboard (TypeScript/React)

- TypeScript strict mode
- Functional components with hooks
- Tailwind CSS for styling
- Components in appropriate subdirectory under `src/components/`

## Development Setup

```bash
# Clone and set up
git clone https://github.com/scalefirstai/EvolutionAI.git
cd EvolutionAI

# Start all services
docker-compose up

# Dashboard development
cd dashboard && npm install && npm run dev

# Java build
cd output/java && ./mvnw verify
```

## Code Review Process

All PRs require:
1. At least one reviewer approval
2. All CI checks passing
3. For module translations: Witness agent review verdict of APPROVED

## Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
