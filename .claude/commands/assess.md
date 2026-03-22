Perform a quick assessment of the COBOL module: $ARGUMENTS

Steps:
1. Find the source file in the `source/` directory (try .cbl and .cob extensions)
2. Count the total lines of code
3. Identify all COPY statements and list the copybook dependencies
4. Identify all CALL statements and list inter-program dependencies
5. Count EXEC CICS commands and categorize them (READ, WRITE, SEND, RECEIVE, LINK, XCTL)
6. Score complexity using this rubric:
   - **Simple**: <200 LOC, no CICS commands, no inter-program CALL statements
   - **Moderate**: 200-500 LOC, or uses CICS, or has 1-2 CALL dependencies
   - **Complex**: >500 LOC with CICS commands and >2 inter-program dependencies

Output a structured assessment in this format:

```
## Module Assessment: {MODULE_NAME}

| Metric | Value |
|--------|-------|
| Source file | source/{file} |
| Lines of code | {count} |
| Type | Online (CICS) / Batch |
| Complexity | Simple / Moderate / Complex |

### Dependencies
- COPY: {list of copybooks}
- CALL: {list of called programs}

### CICS Commands
{categorized list with counts}

### Migration Recommendation
{recommendation based on complexity: suggested approach, estimated effort, recommended agent count}
```

Use the cobol-analysis skill references for PIC clause and CICS command mappings.
