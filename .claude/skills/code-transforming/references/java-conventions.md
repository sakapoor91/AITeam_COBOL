# Java Conventions for Translated Code

## General Rules
- Java 17+ features required
- Quarkus framework preferred; Spring Boot acceptable
- No `var` in public API signatures
- All public classes and methods must have Javadoc with `@see` COBOL source reference

## Type Rules

### DTOs
Use Java records:
```java
public record AccountDto(
    String accountId,
    BigDecimal balance,
    String status
) {}
```

### Domain Types
Use sealed interfaces:
```java
public sealed interface TransactionResult
    permits TransactionResult.Success, TransactionResult.Failure {
    record Success(String transactionId, BigDecimal newBalance) implements TransactionResult {}
    record Failure(String errorCode, String message) implements TransactionResult {}
}
```

### Financial Amounts
Always `BigDecimal`:
```java
// CORRECT
BigDecimal amount = new BigDecimal("100.50");
BigDecimal result = amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
if (amount.compareTo(BigDecimal.ZERO) > 0) { ... }

// FORBIDDEN
double amount = 100.50;               // NEVER for currency
new BigDecimal(100.50);               // NEVER double constructor
amount.equals(other);                 // NEVER equals() for BigDecimal comparison
```

## Database
- PostgreSQL only (no Oracle dependencies)
- Money columns: `NUMERIC` or `DECIMAL`, never `FLOAT` or `DOUBLE PRECISION`
- Use JPA with Hibernate for persistence

## REST Endpoints
- Quarkus RESTEasy (servlet model, NOT reactive)
- OpenAPI 3.1 spec required for every endpoint
- Return proper HTTP status codes
- Use `@ExceptionMapper` for error handling

## Testing
- JUnit 5 for all tests
- 90% line coverage, 80% branch coverage
- Behavioral equivalence tests comparing COBOL I/O with Java I/O
