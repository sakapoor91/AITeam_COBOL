Generate an OpenAPI spec and API wrapper for the COBOL module: $ARGUMENTS

Steps:
1. Read the RE report from `output/docs/RE-{MODULE}.md` — if it doesn't exist, stop and tell the user to run the cobol-analysis skill first
2. From the RE report, identify:
   - CICS SEND/RECEIVE MAP pairs → request/response DTOs
   - LINKAGE SECTION fields → API parameters
   - Business operations → REST endpoints
3. Map CICS operations to HTTP methods:
   - READ → GET
   - WRITE → POST
   - REWRITE → PUT
   - DELETE → DELETE
   - STARTBR/READNEXT → GET with pagination
4. Generate an OpenAPI 3.1.0 specification:
   - Save to `specs/{MODULE}.yml`
   - All monetary fields as `type: string, format: decimal`
   - Include `x-cobol-source: source/{MODULE}.cbl` extension
   - Define error responses (400, 404, 409, 500)
   - Include example values from the RE report
5. Generate a Quarkus REST controller skeleton:
   - Save to `output/java/` with appropriate package structure
   - Use `@Path`, `@GET`, `@POST`, `@PUT`, `@DELETE` annotations
   - All monetary parameters as `BigDecimal`
   - Include Javadoc with `@see` reference to COBOL source
   - Include `@Inject` service references

Use the api-wrapping skill references for OpenAPI conventions and strangler-fig patterns.
