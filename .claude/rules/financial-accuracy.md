# Financial Accuracy Rules

These rules preserve the exactness of COBOL packed decimal arithmetic in Java.

## Rules

1. **All monetary amounts must use `BigDecimal`, never `float` or `double`.** This applies to variables, method parameters, return types, DTO fields, and database columns.

2. **`BigDecimal` must be constructed from String literals.** Use `new BigDecimal("0.01")`. The expression `new BigDecimal(0.01)` creates a value of `0.01000000000000000020816681711721685228163541` due to IEEE 754 representation.

3. **Division operations must specify `RoundingMode.HALF_EVEN`.** This is banker's rounding, matching COBOL's default behavior. Always provide scale and rounding mode: `amount.divide(divisor, 2, RoundingMode.HALF_EVEN)`.

4. **Equality comparisons must use `compareTo()`, never `equals()`.** `BigDecimal.equals()` also compares scale, so `new BigDecimal("1.0").equals(new BigDecimal("1.00"))` returns `false`. Use `a.compareTo(b) == 0` instead.

5. **PostgreSQL columns for monetary values must use `NUMERIC` or `DECIMAL` types.** Never use `FLOAT`, `DOUBLE PRECISION`, or `REAL` for any column that stores financial data.

## Rationale
COBOL's packed decimal (`COMP-3`) arithmetic is inherently exact. The Java replacement must preserve this property. A single rounding error compounding across millions of daily transactions creates financial discrepancies that are expensive to detect and costly to remediate.
