# Documentation Standards

These rules ensure translated code is independently verifiable and maintainable.

## Rules

1. **All generated Java classes must include Javadoc with `@see` referencing the source COBOL program.** Example: `@see source/COACTUPC.cbl`. This creates a traceable link from translated code back to the original.

2. **All new service endpoints must have an OpenAPI 3.1 specification in `specs/`.** The spec must include request/response schemas, error responses, and the `x-cobol-source` extension.

3. **RE reports must document: business rules, data structures, dependencies, and error paths.** These four sections are mandatory. The report lives at `output/docs/RE-{MODULE}.md`.

4. **Architecture decisions must follow the ADR template in `docs/architecture-decisions/`.** Number sequentially, include context, decision, and consequences sections.

5. **Translated code must not reference "magic numbers" without explanation.** Any constant from the COBOL source must be named and documented with its COBOL origin.

## Rationale
Documentation enables independent verification by auditors, regulators, and future maintainers who may not have access to the original COBOL system or the AI agents that performed the translation.
