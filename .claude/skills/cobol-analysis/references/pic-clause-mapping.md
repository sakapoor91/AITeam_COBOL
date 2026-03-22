# PIC Clause → Java Type Mapping

## Numeric Types

| PIC Clause | Java Type | Notes |
|-----------|-----------|-------|
| `PIC 9(n)` where n ≤ 4 | `short` | Unsigned integer, max 9999 |
| `PIC 9(n)` where 5 ≤ n ≤ 9 | `int` | Unsigned integer |
| `PIC 9(n)` where n ≥ 10 | `long` | Unsigned integer |
| `PIC 9(n)V9(m)` | `BigDecimal`, scale m | Fixed-point decimal |
| `PIC S9(n)` | `int` or `long` (signed) | Sign in leading or trailing position |
| `PIC S9(n)V9(m)` | `BigDecimal`, scale m | Signed fixed-point |
| `PIC S9(n) COMP` | `short`/`int`/`long` | Binary storage, signed |
| `PIC S9(n) COMP-3` | `BigDecimal` | Packed decimal — ALWAYS use BigDecimal |
| `PIC S9(n)V9(m) COMP-3` | `BigDecimal`, scale m | Packed decimal with fraction |

## String Types

| PIC Clause | Java Type | Notes |
|-----------|-----------|-------|
| `PIC X(n)` | `String`, max length n | Alphanumeric |
| `PIC A(n)` | `String`, alphabetic only | Add validation annotation |
| `PIC 9(n)` (used as ID) | `String` | When used as identifiers, not arithmetic |

## Boolean / Enum Types

| PIC Clause | Java Type | Notes |
|-----------|-----------|-------|
| `88 level` | `boolean` or enum constant | Condition name |
| `88 level` (multiple) | `enum` | Multiple condition names on same field |

## COBOL USAGE Clauses

| USAGE | Storage | Java Mapping |
|-------|---------|-------------|
| `DISPLAY` (default) | Character | Parse as String, convert to target type |
| `COMP` / `BINARY` | Binary | `int` / `long` (fixed width) |
| `COMP-3` / `PACKED-DECIMAL` | Packed BCD | `BigDecimal` (always) |
| `COMP-1` | Single-precision float | `BigDecimal` (NEVER use `float`) |
| `COMP-2` | Double-precision float | `BigDecimal` (NEVER use `double`) |

## Critical Rule
**All monetary/financial PIC clauses MUST map to `BigDecimal`.** This includes:
- Any field with `V` (implied decimal point)
- Any `COMP-3` field
- Any field used in financial calculations regardless of PIC clause
- Use `new BigDecimal("value")` — never `new BigDecimal(double)`
