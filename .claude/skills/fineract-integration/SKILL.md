---
name: fineract-integration
description: Map translated COBOL modules to Apache Fineract APIs for integration
agent: polecat
stage: translate
---

# Fineract Integration Skill

## When to Use
After COBOL-to-Java translation, use this skill to map generated services to Fineract's API surface.

## Prerequisites
- Module has been translated to Java (exists in `output/java/`)
- RE report exists in `output/docs/`
- Fineract instance accessible for schema verification

## Steps

1. **Identify Fineract mappings**: Match CardDemo functions to Fineract API endpoints using `references/carddemo-fineract-mapping.md`
2. **Align data models**: Map translated Java DTOs to Fineract-compatible records using `references/fineract-api-endpoints.md`
3. **Generate REST client**: Create a `@RestClient` interface for each Fineract API used
4. **Implement service layer**: Wrap Fineract calls in an `@ApplicationScoped` service with proper error handling
5. **Configure authentication**: Set up HTTP Basic Auth headers (Fineract default)
6. **Validate**: Run `scripts/validate-fineract-mapping.sh` to check API pattern compliance
7. **Test**: Use Fineract Docker instance for integration testing

## Key Rules
- All monetary fields use `BigDecimal` with String constructor
- Use Quarkus `@RestClient` for Fineract API calls
- Include `Fineract-Platform-TenantId: default` header
- Date format: `dd MMMM yyyy`, locale: `en`

## References
- `references/fineract-api-endpoints.md` — Full API endpoint reference
- `references/carddemo-fineract-mapping.md` — CardDemo → Fineract mapping + data models
