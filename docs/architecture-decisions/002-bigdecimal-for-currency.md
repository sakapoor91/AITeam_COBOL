# ADR-002: BigDecimal for All Currency and Financial Calculations

## Status

Accepted

## Context

COBOL natively uses packed decimal arithmetic (COMP-3) for financial calculations, which provides exact decimal representation without floating-point rounding errors. Java offers multiple numeric types: `float`, `double`, `BigDecimal`, and `long` (for minor currency units).

In banking, even sub-cent rounding errors are unacceptable. They compound across millions of transactions, produce reconciliation failures, and can violate regulatory requirements for exact financial reporting.

IEEE 754 floating-point (`float` and `double`) cannot exactly represent many common decimal values. For example, `0.1 + 0.2` evaluates to `0.30000000000000004` in double-precision arithmetic.

## Decision

All currency amounts, interest rates, exchange rates, and financial calculations in generated Java code will use `java.math.BigDecimal`. The use of `float` or `double` for any monetary or financial value is explicitly forbidden.

Specific rules:

- Construct BigDecimal from String literals (`new BigDecimal("0.10")`), never from double values
- Specify `RoundingMode` explicitly on every division operation
- Use `compareTo()` for equality checks, not `equals()` (which also compares scale)
- Store as `NUMERIC` or `DECIMAL` column types in PostgreSQL
- Define precision and scale on all JPA `@Column` annotations for monetary fields

## Consequences

### Positive

- Exact decimal arithmetic preserves behavioral equivalence with COBOL COMP-3 calculations
- Eliminates an entire class of rounding-related defects in financial computations
- Meets regulatory requirements for precise financial reporting
- BigDecimal operations are auditable and deterministic

### Negative

- BigDecimal is more verbose than primitive arithmetic; generated code is longer
- BigDecimal operations are slower than primitive arithmetic (approximately 10-100x), though this is negligible for banking transaction volumes
- Developers unfamiliar with BigDecimal may introduce bugs if they modify generated code without understanding the rules (e.g., using the double constructor)

### Neutral

- The `long`-in-minor-units approach (storing cents as integers) was considered and rejected because it requires consistent unit conversion and does not naturally handle multi-currency scenarios with varying decimal places
