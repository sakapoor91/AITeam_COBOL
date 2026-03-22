# Complexity Scoring Rubric

## Scoring Dimensions

| Dimension | Weight | Simple (1) | Moderate (2) | Complex (3) |
|-----------|--------|-----------|-------------|-------------|
| Lines of Code | 30% | <200 LOC | 200-500 LOC | >500 LOC |
| CICS Commands | 25% | None | 1-5 commands | >5 commands |
| Inter-program Calls | 20% | None | 1-2 CALL/LINK | >2 CALL/LINK |
| Copybook Dependencies | 15% | 0-1 COPY | 2-4 COPY | >4 COPY |
| Data Complexity | 10% | Simple PIC types | COMP-3, REDEFINES | Nested structures, OCCURS DEPENDING ON |

## Overall Score Calculation

```
Score = (LOC_score × 0.30) + (CICS_score × 0.25) + (CALL_score × 0.20) + (COPY_score × 0.15) + (DATA_score × 0.10)
```

| Weighted Score | Classification | Migration Estimate | Recommended Agents |
|---------------|---------------|-------------------|-------------------|
| 1.0 - 1.5 | Simple | 1-2 days | 1 Polecat |
| 1.6 - 2.2 | Moderate | 3-5 days | 1-2 Polecats |
| 2.3 - 3.0 | Complex | 1-2 weeks | 2-3 Polecats + dedicated Witness |

## Risk Factors (add +0.5 to score)
- Contains security/authentication logic
- Handles financial calculations with complex rounding
- Has circular dependencies with other modules
- Uses non-standard CICS extensions
- Contains embedded SQL (EXEC SQL)

## Migration Order Rules
1. Shared copybooks first (no dependencies)
2. Leaf modules next (depend on copybooks only)
3. Mid-tier modules (depend on leaf modules)
4. Core modules last (most dependencies)
5. Within a tier, prioritize by business value
