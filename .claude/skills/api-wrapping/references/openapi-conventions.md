# OpenAPI Conventions for EvolutionAI

## Spec Template

```yaml
openapi: 3.1.0
info:
  title: "{Module} API"
  description: "REST API translated from COBOL module {MODULE}"
  version: "1.0.0"
  x-cobol-source: "source/{MODULE}.cbl"

servers:
  - url: http://localhost:8080/api/v1
    description: Local development

paths:
  /resource:
    get:
      summary: "List resources"
      operationId: listResources
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            default: 20
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ResourceList'

components:
  schemas:
    MonetaryAmount:
      type: string
      format: decimal
      description: "Monetary value as decimal string (maps to BigDecimal)"
      example: "1250.75"
```

## Rules

1. **Monetary amounts**: Always `type: string, format: decimal` — never `type: number`
2. **Dates**: ISO 8601 format (`yyyy-MM-dd`)
3. **IDs**: `type: string` for COBOL-originated IDs (preserves leading zeros)
4. **Error responses**: RFC 7807 Problem Detail format
5. **Versioning**: URL path versioning (`/api/v1/`)
6. **Pagination**: `page` and `size` query parameters
7. **Custom extension**: `x-cobol-source` on info and operations pointing to source module
