---
name: cobol-analysis
description: >
  Skill for analyzing COBOL source files. Extracts structure, business rules,
  data layouts, and dependencies. Used by the analyst agent.
---

# COBOL Analysis Skill

## When to Use
Invoke this skill when analyzing any `.cbl`, `.cob`, or `.cpy` file from the CardDemo source.

## COBOL Division Quick Reference

### IDENTIFICATION DIVISION
- PROGRAM-ID: the module name (maps to Java class name)
- AUTHOR, DATE-WRITTEN: metadata for documentation

### ENVIRONMENT DIVISION
- FILE-CONTROL: maps VSAM files → Java repository interfaces
- SELECT/ASSIGN: file-to-dataset mapping

### DATA DIVISION
- FILE SECTION (FD): record layouts for VSAM files
- WORKING-STORAGE SECTION: local variables (→ Java fields)
- LINKAGE SECTION: parameters passed via CALL (→ method parameters)
- COPY statements: shared data structures (→ shared Java DTOs)

### PROCEDURE DIVISION
- PERFORM paragraphs: subroutines (→ Java methods)
- EXEC CICS commands: online transaction handling (→ REST endpoints)
- EXEC SQL: database access (→ JPA/JDBC repository methods)
- EVALUATE/WHEN: switch logic (→ Java switch expressions or pattern matching)
- IF/ELSE: conditional logic

## PIC Clause Parsing
```
PIC 9(5)         → int (max 99999)
PIC 9(7)V99      → BigDecimal, scale 2 (max 9999999.99)
PIC S9(9) COMP-3 → BigDecimal (packed decimal, signed)
PIC S9(4) COMP   → short (binary, signed)
PIC X(30)        → String, max length 30
PIC A(20)        → String, alphabetic only
88 VALID-STATUS  → boolean or enum constant
```

## CICS Command Mapping
```
EXEC CICS SEND MAP      → HTTP Response (render view)
EXEC CICS RECEIVE MAP   → HTTP Request (parse form input)
EXEC CICS READ          → Repository.findById()
EXEC CICS WRITE         → Repository.save()
EXEC CICS REWRITE       → Repository.save() (update)
EXEC CICS DELETE         → Repository.deleteById()
EXEC CICS STARTBR/READNEXT → Repository.findAll() with pagination
EXEC CICS LINK          → Service method call
EXEC CICS XCTL          → Controller redirect
EXEC CICS RETURN        → Return HTTP response
```

## CardDemo Specific Modules
| Module | Purpose | Type |
|--------|---------|------|
| COSGN00C | User login/authentication | Online (CICS) |
| COACTUPC | Account update | Online (CICS) |
| COCRDUPC | Credit card update | Online (CICS) |
| COCRDLIC | Credit card list | Online (CICS) |
| COTRN00C | Transaction processing | Online (CICS) |
| CBTRN01C | Transaction batch processing | Batch |
| CBACT01C | Account batch processing | Batch |

## Output Checklist
After analysis, verify you have documented:
- [ ] Every PERFORM paragraph with business rule description
- [ ] Every data field with PIC clause → Java type mapping
- [ ] Every COPY reference with the copybook content
- [ ] Every CICS command with REST endpoint equivalent
- [ ] Every CALL with target program and parameter mapping
- [ ] Complexity rating and migration recommendation
