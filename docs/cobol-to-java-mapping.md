# COBOL to Java Mapping Reference

This document provides the complete mapping reference used by EvolutionAI's Polecat agents when translating COBOL to Java/Quarkus. Every mapping has been validated against behavioral equivalence tests.

---

## COBOL Divisions to Java Structure

| COBOL Division | Java Equivalent | Notes |
|---------------|----------------|-------|
| IDENTIFICATION DIVISION | Class declaration + Javadoc | Program-ID becomes the class name |
| ENVIRONMENT DIVISION | Application configuration | `application.properties` or `@ConfigProperty` |
| DATA DIVISION - Working Storage | Instance fields / local variables | Scope determines field vs. local |
| DATA DIVISION - File Section | JPA Entity definitions | One entity per logical file |
| DATA DIVISION - Linkage Section | Method parameters / DTOs | Input/output record types |
| PROCEDURE DIVISION | Methods | Each paragraph becomes a method |

### Example

```cobol
       IDENTIFICATION DIVISION.
       PROGRAM-ID. ACCTPROC.
      * Account Processing Module
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 WS-ACCOUNT-BAL    PIC S9(9)V99 COMP-3.
       01 WS-STATUS-CODE    PIC X(2).
       PROCEDURE DIVISION.
           PERFORM VALIDATE-ACCOUNT
           PERFORM PROCESS-TRANSACTION
           STOP RUN.
```

```java
/**
 * Account Processing Module.
 * Translated from COBOL source: ACCTPROC.cbl
 */
@ApplicationScoped
public class AccountProcessingService {

    private BigDecimal accountBalance;
    private String statusCode;

    public void execute() {
        validateAccount();
        processTransaction();
    }
}
```

---

## PIC Clauses to Java Types

### Numeric Types

| COBOL PIC | Example Value | Java Type | Notes |
|-----------|--------------|-----------|-------|
| `PIC 9(n)` | `12345` | `int` or `long` | Use `long` when n > 9 |
| `PIC S9(n)` | `-12345` | `int` or `long` | Signed integer |
| `PIC 9(n)V9(m)` | `12345.67` | `BigDecimal` | Fixed-point decimal |
| `PIC S9(n)V9(m)` | `-12345.67` | `BigDecimal` | Signed fixed-point decimal |
| `PIC S9(n)V9(m) COMP-3` | `-12345.67` | `BigDecimal` | Packed decimal -- always BigDecimal |
| `PIC S9(n) COMP` | `-12345` | `int` or `long` | Binary integer |
| `PIC 9(n) COMP` | `12345` | `int` or `long` | Unsigned binary integer |

### Alphanumeric Types

| COBOL PIC | Example Value | Java Type | Notes |
|-----------|--------------|-----------|-------|
| `PIC X(n)` | `"HELLO   "` | `String` | Right-padded with spaces in COBOL |
| `PIC A(n)` | `"HELLO   "` | `String` | Alphabetic only |
| `PIC X` | `"Y"` | `String` | Single character, still use String |
| `PIC 9(n) (display)` | `"00123"` | `String` | When used as display/identifier, not arithmetic |

### Date and Time

| COBOL Pattern | Example | Java Type | Notes |
|--------------|---------|-----------|-------|
| `PIC 9(8)` (YYYYMMDD) | `20260322` | `LocalDate` | Parse with `DateTimeFormatter.BASIC_ISO_DATE` |
| `PIC 9(6)` (HHMMSS) | `143022` | `LocalTime` | Parse with pattern `HHmmss` |
| `PIC 9(14)` (YYYYMMDDHHMMSS) | `20260322143022` | `LocalDateTime` | Combined date-time |
| `PIC 9(7)` (Julian YYYYDDD) | `2026081` | `LocalDate` | Convert from day-of-year format |

### Boolean / Flag

| COBOL Pattern | Java Type | Notes |
|--------------|-----------|-------|
| `PIC X` with 88-level conditions | `boolean` or `enum` | Use enum when more than 2 states |
| `88 IS-ACTIVE VALUE 'Y'` | `boolean isActive` | Direct mapping |
| `88 STATUS-OPEN VALUE 'O'` / `88 STATUS-CLOSED VALUE 'C'` | `enum Status { OPEN, CLOSED }` | Multiple 88-levels on same field |

---

## CICS to REST Mapping

| CICS Command | REST Equivalent | Java/Quarkus Implementation |
|-------------|----------------|---------------------------|
| `EXEC CICS RECEIVE MAP` | Request body parsing | `@POST` with `@RequestBody` DTO |
| `EXEC CICS SEND MAP` | Response body | Return DTO, serialized to JSON |
| `EXEC CICS READ FILE` | Database read | `@Inject EntityManager` or JPA Repository `findById()` |
| `EXEC CICS WRITE FILE` | Database insert | `entityManager.persist()` or Repository `save()` |
| `EXEC CICS REWRITE FILE` | Database update | `entityManager.merge()` or Repository `save()` |
| `EXEC CICS DELETE FILE` | Database delete | `entityManager.remove()` or Repository `deleteById()` |
| `EXEC CICS STARTBR FILE` | Database query | JPA query with cursor / `Stream<Entity>` |
| `EXEC CICS READNEXT FILE` | Iterate results | Stream processing or pagination |
| `EXEC CICS ENDBR FILE` | Close result set | Stream auto-close or explicit close |
| `EXEC CICS LINK PROGRAM` | Service call | `@Inject` dependent service, call method |
| `EXEC CICS XCTL PROGRAM` | Redirect / forward | Return response with redirect or call next service |
| `EXEC CICS RETURN` | Return response | `return Response.ok(dto).build()` |
| `EXEC CICS SYNCPOINT` | Transaction commit | `@Transactional` annotation |
| `EXEC CICS SYNCPOINT ROLLBACK` | Transaction rollback | Throw exception within `@Transactional` |
| `EXEC CICS ASKTIME` | Get current time | `LocalDateTime.now()` |
| `EXEC CICS FORMATTIME` | Format date/time | `DateTimeFormatter.ofPattern(...)` |
| `EXEC CICS HANDLE CONDITION` | Exception handling | `try-catch` blocks or `@ExceptionHandler` |
| `EXEC CICS HANDLE ABEND` | Global error handler | `@Provider` JAX-RS ExceptionMapper |

### Example: CICS READ to JPA

```cobol
       EXEC CICS READ
           FILE('ACCTFILE')
           INTO(WS-ACCOUNT-REC)
           RIDFLD(WS-ACCT-ID)
           RESP(WS-RESP-CODE)
       END-EXEC.
       IF WS-RESP-CODE NOT = DFHRESP(NORMAL)
           PERFORM HANDLE-READ-ERROR
       END-IF.
```

```java
/**
 * Reads account record by ID.
 * Source: ACCTPROC.cbl, paragraph READ-ACCOUNT
 * CICS: READ FILE('ACCTFILE') RIDFLD(WS-ACCT-ID)
 */
public Optional<AccountEntity> readAccount(String accountId) {
    try {
        AccountEntity account = entityManager.find(AccountEntity.class, accountId);
        return Optional.ofNullable(account);
    } catch (PersistenceException e) {
        handleReadError(accountId, e);
        return Optional.empty();
    }
}
```

---

## VSAM to JPA Mapping

| VSAM Concept | JPA Equivalent | Notes |
|-------------|----------------|-------|
| VSAM KSDS (Key-Sequenced) | `@Entity` with `@Id` | Primary key access |
| VSAM RRDS (Relative Record) | `@Entity` with `@Id Long` | Numeric record number as key |
| VSAM ESDS (Entry-Sequenced) | `@Entity` with `@GeneratedValue` | Auto-generated sequential ID |
| VSAM Alternate Index | `@Index` + query method | Secondary lookup path |
| VSAM Record Layout | Entity fields | One field per COBOL data item |
| VSAM File | PostgreSQL table | One table per VSAM file |
| VSAM Cluster | Schema grouping | Logical grouping via schema or naming |

### Example: VSAM Record to JPA Entity

```cobol
       01 ACCT-RECORD.
           05 ACCT-ID            PIC X(11).
           05 ACCT-NAME          PIC X(30).
           05 ACCT-BALANCE       PIC S9(9)V99 COMP-3.
           05 ACCT-STATUS        PIC X.
              88 ACCT-ACTIVE     VALUE 'A'.
              88 ACCT-CLOSED     VALUE 'C'.
              88 ACCT-SUSPENDED  VALUE 'S'.
           05 ACCT-OPEN-DATE     PIC 9(8).
```

```java
/**
 * Account entity mapped from VSAM KSDS file ACCTFILE.
 * Source: ACCTDATA.cpy
 */
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "acct_id", length = 11, nullable = false)
    private String accountId;

    @Column(name = "acct_name", length = 30, nullable = false)
    private String accountName;

    @Column(name = "acct_balance", precision = 11, scale = 2, nullable = false)
    private BigDecimal accountBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "acct_status", length = 1, nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "acct_open_date", nullable = false)
    private LocalDate accountOpenDate;

    // Getters, setters, equals, hashCode omitted for brevity
}

public enum AccountStatus {
    ACTIVE("A"),
    CLOSED("C"),
    SUSPENDED("S");

    private final String cobolValue;

    AccountStatus(String cobolValue) {
        this.cobolValue = cobolValue;
    }

    public String getCobolValue() {
        return cobolValue;
    }

    public static AccountStatus fromCobolValue(String value) {
        for (AccountStatus status : values()) {
            if (status.cobolValue.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown COBOL status value: " + value);
    }
}
```

---

## PERFORM to Method Calls

| COBOL PERFORM | Java Equivalent | Notes |
|--------------|----------------|-------|
| `PERFORM paragraph-name` | `methodName()` | Simple method call |
| `PERFORM paragraph-name THRU exit-para` | `methodName()` | Combine into single method |
| `PERFORM paragraph-name N TIMES` | `for` loop with method call | `for (int i = 0; i < n; i++) { method(); }` |
| `PERFORM paragraph-name UNTIL condition` | `while` loop | `while (!condition) { method(); }` |
| `PERFORM paragraph-name VARYING i` | `for` loop | Standard counted loop |
| `PERFORM paragraph-name WITH TEST BEFORE` | `while` loop | Condition checked before each iteration |
| `PERFORM paragraph-name WITH TEST AFTER` | `do-while` loop | Condition checked after each iteration |

### Example

```cobol
       PERFORM PROCESS-RECORD
           UNTIL WS-EOF = 'Y'
           OR WS-ERROR-FLAG = 'Y'.
```

```java
while (!eof && !errorFlag) {
    processRecord();
}
```

---

## Copybooks to Shared DTOs

COBOL copybooks (`COPY` statements) are shared data structure definitions included in multiple programs. They map to shared Java record types or classes in a common module.

| COBOL | Java | Notes |
|-------|------|-------|
| Copybook file (`.cpy`) | Shared DTO class or record | Placed in a `common` or `shared` package |
| `COPY ACCTDATA` | `import com.evolution.common.dto.AccountData` | Standard Java import |
| Nested `01` level in copybook | Nested record or separate class | Depends on complexity |

### Example

```cobol
      * ACCTDATA.cpy - shared account data layout
       01 ACCT-DATA.
           05 ACCT-ID            PIC X(11).
           05 ACCT-NAME          PIC X(30).
           05 ACCT-TYPE          PIC X(2).
```

```java
/**
 * Shared account data DTO.
 * Source: copybook ACCTDATA.cpy
 * Used by: ACCTPROC, ACCTVIEW, ACCTUPDT
 */
public record AccountData(
    String accountId,
    String accountName,
    String accountType
) {
    public AccountData {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(accountName, "accountName must not be null");
        Objects.requireNonNull(accountType, "accountType must not be null");
    }
}
```

---

## Error Handling Patterns

### CICS HANDLE CONDITION to Exception Handling

```cobol
       EXEC CICS HANDLE CONDITION
           NOTFND(HANDLE-NOT-FOUND)
           DUPKEY(HANDLE-DUP-KEY)
           ERROR(HANDLE-GENERAL-ERROR)
       END-EXEC.
```

```java
try {
    // CICS operation equivalent
    AccountEntity account = repository.findByIdOrThrow(accountId);
} catch (EntityNotFoundException e) {
    handleNotFound(accountId);
} catch (DuplicateKeyException e) {
    handleDuplicateKey(accountId);
} catch (PersistenceException e) {
    handleGeneralError(accountId, e);
}
```

### COBOL Status Codes to HTTP Status Codes

| COBOL Pattern | HTTP Status | Response Body |
|--------------|------------|---------------|
| Normal completion | 200 OK | Success DTO |
| Record not found (NOTFND) | 404 Not Found | Error DTO with message |
| Duplicate key (DUPKEY) | 409 Conflict | Error DTO with message |
| Validation failure | 400 Bad Request | Error DTO with field-level errors |
| Authorization failure | 403 Forbidden | Error DTO with message |
| General error | 500 Internal Server Error | Error DTO (no internal details exposed) |

### Standard Error Response DTO

```java
/**
 * Standard error response aligned with RFC 7807 (Problem Details).
 */
public record ErrorResponse(
    String type,
    String title,
    int status,
    String detail,
    String instance
) {}
```

---

## Batch Processing Patterns

COBOL batch programs (JCL-driven) map to scheduled tasks or batch processing frameworks in Java.

| COBOL Batch Pattern | Java Equivalent | Notes |
|--------------------|----------------|-------|
| JCL job step | Quarkus `@Scheduled` or Quarkus CLI command | Depends on trigger mechanism |
| Sequential file read | `BufferedReader` or batch framework `ItemReader` | Stream processing preferred |
| Sequential file write | `BufferedWriter` or batch framework `ItemWriter` | Buffered output |
| SORT utility | `List.sort()` or database `ORDER BY` | In-memory or database sort |
| Control break logic | `Collectors.groupingBy()` + stream processing | Group and aggregate |
| Checkpoint/restart | Transaction boundaries + progress tracking | Save progress to resume on failure |

### Example: Batch File Processing

```cobol
       PERFORM UNTIL WS-EOF = 'Y'
           READ INPUT-FILE INTO WS-RECORD
               AT END SET WS-EOF TO TRUE
               NOT AT END
                   ADD 1 TO WS-REC-COUNT
                   PERFORM PROCESS-RECORD
                   WRITE OUTPUT-RECORD FROM WS-OUTPUT
           END-READ
       END-PERFORM.
```

```java
/**
 * Batch file processor.
 * Source: BATCHPROC.cbl, main processing loop
 */
public BatchResult processFile(Path inputFile, Path outputFile) {
    long recordCount = 0;

    try (BufferedReader reader = Files.newBufferedReader(inputFile);
         BufferedWriter writer = Files.newBufferedWriter(outputFile)) {

        String line;
        while ((line = reader.readLine()) != null) {
            recordCount++;
            InputRecord record = InputRecord.parse(line);
            OutputRecord result = processRecord(record);
            writer.write(result.format());
            writer.newLine();
        }
    }

    return new BatchResult(recordCount, BatchStatus.SUCCESS);
}
```
