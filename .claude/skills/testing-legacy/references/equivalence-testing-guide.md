# Equivalence Testing Guide

## Test Structure

Each equivalence test follows this pattern:

```java
@Test
@DisplayName("COACTUPC: Update account balance — matches COBOL output")
void testUpdateAccountBalance() {
    // GIVEN: Same input as COBOL test case #42
    String accountId = "12345678901";
    BigDecimal updateAmount = new BigDecimal("150.75");

    // WHEN: Execute the translated Java logic
    AccountResult result = accountService.updateAccount(accountId, updateAmount);

    // THEN: Compare with documented COBOL output
    assertThat(result.newBalance()).isEqualByComparingTo(new BigDecimal("1250.75"));
    assertThat(result.statusCode()).isEqualTo("00");
    assertThat(result.message()).isEqualTo("ACCOUNT UPDATED SUCCESSFULLY");
}
```

## Test Categories

### 1. Happy Path Tests
- Normal operations with valid inputs
- Standard business flows (create, read, update, delete)
- Each PERFORM paragraph should have at least one happy path test

### 2. Edge Case Tests
- Boundary values (max PIC size, zero, negative)
- Empty strings, null equivalents
- Date boundaries (leap years, month-end)
- Maximum transaction amounts

### 3. Error Path Tests
- Invalid input formats
- Not-found conditions (NOTFND response)
- Duplicate record conditions (DUPREC)
- Authorization failures

### 4. Financial Precision Tests
- Rounding behavior must match COBOL exactly
- Interest calculations across multiple periods
- Running balance calculations
- Currency conversion with specific decimal handling

## Comparison Rules

### Numeric Values
```java
// Use isEqualByComparingTo (ignores scale differences)
assertThat(actual).isEqualByComparingTo(expected);

// NOT equals() which also compares scale
// new BigDecimal("1.0").equals(new BigDecimal("1.00")) → false!
```

### String Values
```java
// COBOL pads strings with spaces — trim before comparing
assertThat(actual.trim()).isEqualTo(expected.trim());
```

### Date Values
```java
// COBOL dates may be in YYYYMMDD or MMDDYYYY format
// Convert both to LocalDate before comparing
assertThat(parseCobolDate(actual)).isEqualTo(parseCobolDate(expected));
```

## Coverage Thresholds

| Metric | Minimum | Rationale |
|--------|---------|-----------|
| Line coverage | 90% | Ensures most code paths are exercised |
| Branch coverage | 80% | Catches untested conditional logic |
| Equivalence pass rate | 99.7% | Banking-grade accuracy requirement |
