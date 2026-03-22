# Fineract Core API Endpoints

## Client Management
```
POST   /fineract-provider/api/v1/clients          → Create client
GET    /fineract-provider/api/v1/clients/{id}      → Get client
PUT    /fineract-provider/api/v1/clients/{id}      → Update client
DELETE /fineract-provider/api/v1/clients/{id}      → Delete client
GET    /fineract-provider/api/v1/clients?sqlSearch= → Search clients
```

## Savings Accounts
```
POST   /api/v1/savingsaccounts                     → Create account
GET    /api/v1/savingsaccounts/{id}                → Get account
PUT    /api/v1/savingsaccounts/{id}                → Update account
POST   /api/v1/savingsaccounts/{id}?command=deposit    → Deposit
POST   /api/v1/savingsaccounts/{id}?command=withdrawal → Withdraw
POST   /api/v1/savingsaccounts/{id}?command=activate   → Activate
POST   /api/v1/savingsaccounts/{id}?command=close      → Close
```

## Loan Products
```
POST   /api/v1/loans                               → Create loan
GET    /api/v1/loans/{id}                          → Get loan
PUT    /api/v1/loans/{id}                          → Update loan
POST   /api/v1/loans/{id}?command=approve          → Approve
POST   /api/v1/loans/{id}?command=disburse         → Disburse
POST   /api/v1/loans/{id}/transactions?command=repayment → Repayment
```

## Charges & Fees
```
POST   /api/v1/charges                             → Create charge
GET    /api/v1/charges/{id}                        → Get charge
PUT    /api/v1/charges/{id}                        → Update charge
DELETE /api/v1/charges/{id}                        → Delete charge
```

## Batch API
```
POST   /api/v1/batches                             → Execute batch operations
```

## Authentication
```
Authorization: Basic base64(username:password)
Content-Type: application/json
Fineract-Platform-TenantId: default
```
Default credentials: `mifos` / `password`

## Testing
```bash
docker run -p 8443:8443 apache/fineract:latest
# Base URL: https://localhost:8443/fineract-provider/api/v1/
```
