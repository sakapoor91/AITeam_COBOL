# Translation Protocol: Division-by-Division

## IDENTIFICATION DIVISION → Class Declaration

```cobol
IDENTIFICATION DIVISION.
PROGRAM-ID. COACTUPC.
AUTHOR. CARDDEMO TEAM.
```

```java
/**
 * Account Update Service.
 * Translated from COBOL module COACTUPC.
 *
 * @see source/COACTUPC.cbl
 */
@ApplicationScoped
public class AccountUpdateService {
```

## DATA DIVISION → Java Types

### FILE SECTION → JPA Entities
```cobol
FD ACCTDAT.
01 ACCT-RECORD.
   05 ACCT-ID       PIC X(11).
   05 ACCT-BAL      PIC S9(7)V99 COMP-3.
```

```java
@Entity
@Table(name = "accounts")
public record AccountEntity(
    @Id @Column(length = 11)
    String accountId,
    @Column(precision = 9, scale = 2)
    BigDecimal balance
) {}
```

### WORKING-STORAGE → Private Fields
```cobol
01 WS-ERROR-FLAG   PIC X VALUE 'N'.
01 WS-TOTAL-AMT    PIC S9(9)V99 COMP-3.
```

```java
private boolean errorFlag = false;
private BigDecimal totalAmount = BigDecimal.ZERO;
```

### LINKAGE SECTION → Method Parameters
```cobol
LINKAGE SECTION.
01 LK-ACCT-ID     PIC X(11).
01 LK-UPDATE-AMT  PIC S9(7)V99 COMP-3.
```

```java
public void updateAccount(String accountId, BigDecimal updateAmount) {
```

## PROCEDURE DIVISION → Methods

### PERFORM → Method Call
```cobol
PERFORM 1000-VALIDATE-INPUT.
PERFORM 2000-UPDATE-ACCOUNT.
```

```java
validateInput();
updateAccount();
```

### EVALUATE → Switch Expression
```cobol
EVALUATE WS-ACTION
    WHEN 'A' PERFORM 1000-ADD
    WHEN 'U' PERFORM 2000-UPDATE
    WHEN 'D' PERFORM 3000-DELETE
    WHEN OTHER PERFORM 9000-ERROR
END-EVALUATE.
```

```java
switch (action) {
    case "A" -> addRecord();
    case "U" -> updateRecord();
    case "D" -> deleteRecord();
    default -> handleError();
};
```

### Arithmetic → BigDecimal Operations
```cobol
COMPUTE WS-TOTAL = WS-AMOUNT * WS-RATE / 100.
```

```java
BigDecimal total = amount.multiply(rate)
    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_EVEN);
```
