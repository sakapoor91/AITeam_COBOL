Generate a dependency-ordered migration plan for these COBOL modules: $ARGUMENTS

Steps:
1. For each module listed, find its source file in `source/` directory
2. Parse each module to identify:
   - COPY statements (copybook dependencies)
   - CALL statements (inter-program dependencies)
   - EXEC CICS LINK/XCTL (CICS program transfers)
3. Build a dependency graph showing which modules depend on which
4. Identify shared copybooks used by multiple modules
5. Order modules for migration following these rules:
   - Shared copybooks first (they have no dependencies)
   - Leaf modules next (depend only on copybooks)
   - Mid-tier modules (depend on leaf modules)
   - Core modules last (most dependencies)
   - Within a tier, prioritize by business value and complexity
6. Score each module's complexity (Simple/Moderate/Complex)

Output a migration plan in this format:

```
## Migration Plan

### Dependency Graph
{text-based dependency graph}

### Migration Order

| Order | Module | Type | Complexity | Dependencies | Est. Effort | Agents |
|-------|--------|------|-----------|-------------|-------------|--------|
| 1 | {module} | Copybook/Online/Batch | Simple/Mod/Complex | {deps} | {days} | {count} |

### Migration Waves
- **Wave 1**: {copybooks and leaf modules — can be parallelized}
- **Wave 2**: {mid-tier modules — depends on Wave 1}
- **Wave 3**: {core modules — depends on Wave 2}

### Notes
{any circular dependencies, risk factors, or special considerations}
```

Use the legacy-discovery skill for complexity scoring rubric.
