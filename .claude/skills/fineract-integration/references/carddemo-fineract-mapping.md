# CardDemo → Fineract Mapping

## Module Mapping

| CardDemo Module | CardDemo Function | Fineract Equivalent |
|----------------|-------------------|---------------------|
| COSGN00C | User authentication | Fineract auth (OAuth2/basic) |
| COACTUPC | Account update | `PUT /clients/{id}` or `PUT /savingsaccounts/{id}` |
| COCRDLIC | Credit card listing | `GET /savingsaccounts?clientId={id}` |
| COCRDUPC | Credit card update | `PUT /savingsaccounts/{id}` |
| COTRN00C | Transaction processing | `POST /savingsaccounts/{id}?command=deposit/withdrawal` |
| CBTRN01C | Batch transactions | Fineract batch API: `POST /batches` |
| CBACT01C | Batch account processing | Fineract batch API with account operations |

## Fineract-Compatible Data Models

### Account Entity
```java
public record AccountDto(
    Long clientId,
    Long productId,
    String accountNo,          // from COBOL: ACCT-ID (PIC X(11))
    BigDecimal nominalAnnualInterestRate,
    String locale,             // "en"
    String dateFormat,         // "dd MMMM yyyy"
    String submittedOnDate,
    BigDecimal balance         // from COBOL: ACCT-BAL (PIC S9(7)V99)
) {}
```

### Transaction Entity
```java
public record TransactionDto(
    String transactionDate,    // from COBOL: TRAN-DATE
    BigDecimal transactionAmount, // from COBOL: TRAN-AMT (PIC S9(7)V99)
    String paymentTypeId,      // mapped from COBOL: TRAN-TYPE-CD
    String locale,
    String dateFormat
) {}
```

## Integration Pattern
```java
@ApplicationScoped
public class AccountService {
    @Inject
    @RestClient
    FineractClient fineractClient;

    public AccountDto updateAccount(String cobolAccountId, AccountUpdateRequest request) {
        Long fineractId = accountMapping.resolve(cobolAccountId);
        return fineractClient.updateSavingsAccount(fineractId, request);
    }
}
```
