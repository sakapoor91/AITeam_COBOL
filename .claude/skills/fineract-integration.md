---
name: fineract-integration
description: >
  Skill for mapping translated COBOL modules to Apache Fineract APIs.
  Covers account management, transactions, and loan operations.
---

# Apache Fineract Integration Skill

## When to Use
After COBOL translation to Java, use this skill to map generated services
to Fineract's API surface for integration.

## Fineract Core API Endpoints

### Client Management
```
POST   /fineract-provider/api/v1/clients          → Create client
GET    /fineract-provider/api/v1/clients/{id}      → Get client
PUT    /fineract-provider/api/v1/clients/{id}      → Update client
DELETE /fineract-provider/api/v1/clients/{id}      → Delete client
```
**CardDemo mapping**: COACTUPC (account update) → Fineract client update

### Savings Accounts
```
POST   /api/v1/savingsaccounts                     → Create account
GET    /api/v1/savingsaccounts/{id}                → Get account
POST   /api/v1/savingsaccounts/{id}?command=deposit    → Deposit
POST   /api/v1/savingsaccounts/{id}?command=withdrawal → Withdraw
```

### Loan Products
```
POST   /api/v1/loans                               → Create loan
GET    /api/v1/loans/{id}                          → Get loan
POST   /api/v1/loans/{id}?command=approve          → Approve
POST   /api/v1/loans/{id}?command=disburse         → Disburse
POST   /api/v1/loans/{id}/transactions?command=repayment → Repayment
```

### Charges & Fees
```
POST   /api/v1/charges                             → Create charge
GET    /api/v1/charges/{id}                        → Get charge
```

## CardDemo → Fineract Mapping Table

| CardDemo Module | CardDemo Function | Fineract Equivalent |
|----------------|-------------------|---------------------|
| COSGN00C | User authentication | Fineract auth (OAuth2/basic) |
| COACTUPC | Account update | PUT /clients/{id} or PUT /savingsaccounts/{id} |
| COCRDLIC | Credit card listing | GET /savingsaccounts?clientId={id} |
| COCRDUPC | Credit card update | PUT /savingsaccounts/{id} |
| COTRN00C | Transaction processing | POST /savingsaccounts/{id}?command=deposit/withdrawal |
| CBTRN01C | Batch transactions | Fineract batch API: POST /batches |
| CBACT01C | Batch account processing | Fineract batch API with account operations |

## Fineract Data Model Alignment

### Account Entity
```java
// Fineract-compatible account record
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
// Fineract-compatible transaction record
public record TransactionDto(
    String transactionDate,    // from COBOL: TRAN-DATE
    BigDecimal transactionAmount, // from COBOL: TRAN-AMT (PIC S9(7)V99)
    String paymentTypeId,      // mapped from COBOL: TRAN-TYPE-CD
    String locale,
    String dateFormat
) {}
```

## Authentication
Fineract uses HTTP Basic Auth by default:
```
Authorization: Basic base64(username:password)
Content-Type: application/json
Fineract-Platform-TenantId: default
```

## Integration Pattern
Translated CardDemo services should wrap Fineract REST calls:
```java
@ApplicationScoped
public class AccountService {
    @Inject
    @RestClient
    FineractClient fineractClient;

    public AccountDto updateAccount(String cobolAccountId, AccountUpdateRequest request) {
        // Map COBOL account ID format to Fineract
        Long fineractId = accountMapping.resolve(cobolAccountId);
        return fineractClient.updateSavingsAccount(fineractId, request);
    }
}
```

## Testing Against Fineract
```bash
# Run Fineract locally for integration testing
docker run -p 8443:8443 apache/fineract:latest

# Default credentials: mifos / password
# Tenant: default
# Base URL: https://localhost:8443/fineract-provider/api/v1/
```
