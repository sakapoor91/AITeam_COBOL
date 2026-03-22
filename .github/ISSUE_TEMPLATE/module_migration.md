---
name: Module Migration
about: Track the migration of a specific COBOL module
title: '[MODULE] '
labels: migration
assignees: ''
---

## COBOL Module
- **Program**: (e.g., COSGN00C)
- **Type**: Online (CICS) / Batch
- **Complexity**: Simple / Moderate / Complex

## Business Function
Describe what this COBOL program does in business terms.

## Target API
- **Endpoint**: (e.g., `/api/v1/auth/login`)
- **HTTP Method**:
- **Fineract Alignment**: (which Fineract API it maps to)

## Dependencies
- **Copybooks**: List referenced `.cpy` files
- **Called Programs**: List any CALLed subprograms
- **VSAM Files**: List file references

## Migration Checklist
- [ ] Reverse engineering report (`RE-*.md`) created
- [ ] Architecture decision documented
- [ ] Java translation complete
- [ ] Unit tests written (90%+ coverage)
- [ ] Integration/equivalence tests written
- [ ] OpenAPI spec generated
- [ ] Witness review: APPROVED
- [ ] FINOS CDM compliance check passed
- [ ] Merged to main
