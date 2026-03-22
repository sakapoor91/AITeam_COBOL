# CICS Command → REST/Java Mapping

## Screen I/O → HTTP Request/Response

| CICS Command | REST Equivalent | Java Implementation |
|-------------|----------------|---------------------|
| `EXEC CICS SEND MAP` | HTTP Response (render view) | Return DTO from `@GET`/`@POST` endpoint |
| `EXEC CICS RECEIVE MAP` | HTTP Request (parse form input) | `@POST` with `@RequestBody` DTO parameter |
| `EXEC CICS SEND TEXT` | HTTP Response (plain text) | Return `Response.ok(text).build()` |

## Data Access → Repository Operations

| CICS Command | REST Equivalent | Java Implementation |
|-------------|----------------|---------------------|
| `EXEC CICS READ` | `GET /resource/{id}` | `repository.findById(id)` |
| `EXEC CICS WRITE` | `POST /resource` | `repository.save(entity)` |
| `EXEC CICS REWRITE` | `PUT /resource/{id}` | `repository.save(entity)` (update) |
| `EXEC CICS DELETE` | `DELETE /resource/{id}` | `repository.deleteById(id)` |
| `EXEC CICS STARTBR` | Begin pagination | `repository.findAll(Pageable)` |
| `EXEC CICS READNEXT` | Next page | Iterator / cursor-based pagination |
| `EXEC CICS ENDBR` | End pagination | Close cursor / end iteration |
| `EXEC CICS RESETBR` | Reset pagination | Reset cursor position |

## Program Control → Service Calls

| CICS Command | REST Equivalent | Java Implementation |
|-------------|----------------|---------------------|
| `EXEC CICS LINK` | Internal service call | `@Inject` service method call |
| `EXEC CICS XCTL` | Controller redirect | `Response.seeOther(uri).build()` |
| `EXEC CICS RETURN` | Return HTTP response | Method return statement |
| `EXEC CICS RETURN TRANSID` | Schedule next request | Redirect with state parameter |

## Queue / Async → Messaging

| CICS Command | REST Equivalent | Java Implementation |
|-------------|----------------|---------------------|
| `EXEC CICS WRITEQ TS` | Write to temp queue | `@Inject` message queue producer |
| `EXEC CICS READQ TS` | Read from temp queue | Message queue consumer |
| `EXEC CICS DELETEQ TS` | Delete queue | Queue cleanup |
| `EXEC CICS WRITEQ TD` | Write to transient data | Event/log destination |

## Error Handling

| CICS Condition | Java Equivalent |
|---------------|----------------|
| `RESP(NORMAL)` | Successful return (no exception) |
| `RESP(NOTFND)` | `throw new NotFoundException()` or return `Optional.empty()` |
| `RESP(DUPREC)` | `throw new DuplicateKeyException()` |
| `RESP(LENGERR)` | `throw new ValidationException("field length exceeded")` |
| `RESP(INVREQ)` | `throw new BadRequestException()` |
| `HANDLE CONDITION` | `try/catch` block |
| `HANDLE ABEND` | Global exception handler (`@ExceptionMapper`) |
