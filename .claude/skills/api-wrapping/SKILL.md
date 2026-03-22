---
name: api-wrapping
description: OpenAPI spec generation and strangler-fig pattern for API wrappers
agent: polecat
stage: design
---

# API Wrapping Skill

## When to Use
Stage 2 of the Five-Stage Loop. Use to design REST API wrappers around COBOL module interfaces.

## Prerequisites
- RE report exists at `output/docs/RE-{MODULE}.md`
- Human architect has approved the API shape

## Steps

1. **Analyze COBOL interface**: From the RE report, identify:
   - CICS SEND/RECEIVE MAP pairs → request/response DTOs
   - LINKAGE SECTION fields → API parameters
   - CALL parameters → service method signatures
2. **Design REST contract**:
   - Map CICS transactions to HTTP methods (READ→GET, WRITE→POST, REWRITE→PUT, DELETE→DELETE)
   - Define request/response schemas using JSON
   - Include error responses for all CICS RESP conditions
3. **Generate OpenAPI 3.1 spec**: Write to `specs/{MODULE}.yml`
   - All monetary fields as `type: string, format: decimal` (maps to BigDecimal)
   - Include examples from RE report I/O pairs
   - Add `x-cobol-source` extension pointing to source module
4. **Validate spec**: Run `scripts/validate-openapi-spec.sh {MODULE}`
5. **Generate controller skeleton**: Create Quarkus `@Path` controller in `output/java/`
6. **Apply strangler-fig pattern**: See `references/strangler-fig-pattern.md`

## OpenAPI Conventions
- Version: 3.1.0
- All monetary amounts: `type: string, format: decimal`
- Date format: ISO 8601
- Error responses: 400, 404, 409, 500 with problem detail body
- Security: Bearer token (mapped from COBOL USRSEC)

## References
- `references/strangler-fig-pattern.md` — Gradual migration via API gateway
- `references/openapi-conventions.md` — OpenAPI spec conventions for this project
