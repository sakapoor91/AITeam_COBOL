# Witness Agent Review: COACTUPC (Account View/Update Module)

## Review Metadata

| Field | Value |
|-------|-------|
| Source Program | COACTUPC.cbl |
| Target Module | com.scalefirstai.evolution.carddemo.accounts |
| RE Report | RE-COACTUPC.md |
| Review Date | 2026-03-22 |
| Witness Agent | @agent-witness |
| Review Iteration | 1 |

---

## 1. Behavioral Equivalence Assessment

### 1.1 COBOL Paragraph Coverage

| COBOL Paragraph | Java Method | Equivalence Status |
|----------------|-------------|-------------------|
| PROCESS-ENTER-KEY | AccountController.getAccount() / updateAccount() | EQUIVALENT |
| READ-ACCTDAT-FILE | AccountRepository.findById() | EQUIVALENT |
| REWRITE-ACCTDAT-FILE | AccountRepository.save() | EQUIVALENT |
| EDIT-ACCT-CHANGES | AccountValidator.validate() | EQUIVALENT |
| VALIDATE-CREDIT-LIMIT | AccountValidator.validateCreditLimit() | EQUIVALENT |
| VALIDATE-CASH-LIMIT | AccountValidator.validateCashCreditLimit() | EQUIVALENT |
| VALIDATE-ACCT-STATUS | AccountStatusStateMachine.validateTransition() | EQUIVALENT |
| COMPARE-OLD-NEW-FIELDS | JPA dirty checking | FUNCTIONALLY EQUIVALENT |
| POPULATE-ACCT-SCREEN | AccountResponse DTO mapping | EQUIVALENT |
| FORMAT-CURR-FIELDS | N/A (client-side formatting) | NOT APPLICABLE |
| PROTECT-FIELDS / UNPROTECT-FIELDS | RBAC on PUT endpoint | FUNCTIONALLY EQUIVALENT |
| PROCESS-PF5-UPDATE | Separate PUT endpoint | FUNCTIONALLY EQUIVALENT |
| CONFIRM-UPDATE | N/A (PUT is explicit intent) | NOT APPLICABLE |

### 1.2 Business Rule Verification

| Business Rule | COBOL Behavior | Java Behavior | Status |
|---------------|---------------|---------------|--------|
| BR-ACCT-001 | Credit limit >= 0, PIC S9(15)V99 bounds | BigDecimal >= 0, max 9999999999999.99 | PASS |
| BR-ACCT-002 | Cash limit <= credit limit | Validated in service layer | PASS |
| BR-ACCT-003 | Status transitions Y/N/C with C terminal | State machine enum with transition matrix | PASS |
| BR-ACCT-004 | PF5 toggle, admin-only edit | RBAC: PUT requires ADMIN role | PASS |
| BR-ACCT-005 | CICS READ UPDATE lock | JPA @Version optimistic lock | PASS (EQUIVALENT) |
| BR-ACCT-006 | FORMAT-CURR-FIELDS for display | Raw BigDecimal in response (formatting is client concern) | PASS |

### 1.3 Status Transition Matrix Verification

| From/To | Y (Active) | N (Inactive) | C (Closed) |
|---------|-----------|-------------|-----------|
| Y | - | ALLOWED | ALLOWED |
| N | ALLOWED | - | ALLOWED |
| C | BLOCKED | BLOCKED | - |

Verified: All transitions match COBOL VALIDATE-ACCT-STATUS paragraph logic. Closed accounts correctly return HTTP 409 when any update is attempted.

### 1.4 Edge Cases Tested

- Negative credit limit: rejected with 400
- Zero credit limit: accepted (valid per COBOL)
- Cash credit limit exceeding credit limit: rejected with 400
- Credit limit with 3+ decimal places: rejected with 400
- Maximum COBOL value (9999999999999.99): accepted
- Overflow beyond PIC S9(15)V99 maximum: rejected with 400
- Update to closed account: rejected with 409
- Account not found: returns 404
- Concurrent update (version conflict): returns 409
- Empty update body: rejected with 400
- Malformed JSON: rejected with 400
- Invalid account ID format: rejected with 400

---

## 2. Financial Accuracy

### 2.1 Monetary Precision Tests

| Test Case | Expected | Actual | Status |
|-----------|----------|--------|--------|
| Credit limit 10000.99 | 10000.99 | 10000.99 | PASS |
| Credit limit 0.01 (penny) | 0.01 | 0.01 | PASS |
| Balance -500.00 (negative) | -500.00 | -500.00 | PASS |
| Credit limit 9999999999999.99 (max) | 9999999999999.99 | 9999999999999.99 | PASS |
| Cash limit == credit limit | Accepted | Accepted | PASS |
| 0.10 + 0.20 balance calc | 0.30 | 0.30 | PASS |

### 2.2 BigDecimal Compliance

- All monetary fields use BigDecimal with scale 2
- No float or double in any monetary path (verified via SpotBugs custom rule)
- RoundingMode.HALF_EVEN used where rounding is needed (matches COBOL ROUNDED)
- Database column types: DECIMAL(17,2) matching PIC S9(15)V99

---

## 3. Security Assessment

| Check | Status | Notes |
|-------|--------|-------|
| Authorization (GET) | PASS | Authenticated users can view their accounts |
| Authorization (PUT) | PASS | Only ADMIN role can update accounts |
| Input validation | PASS | Bean Validation on AccountUpdateRequest |
| SQL injection | PASS | Parameterized queries via JPA |
| Mass assignment | PASS | DTO pattern prevents updating protected fields (accountId, openDate) |
| Optimistic locking | PASS | @Version prevents lost updates |
| Audit logging | PASS | All account changes logged with before/after values |
| PII in logs | PASS | Customer ID and account ID logged, but no names or addresses |

### 3.1 Authorization Matrix

| Operation | ADMIN | USER | ANONYMOUS |
|-----------|-------|------|-----------|
| GET /accounts/{id} | ALLOWED | ALLOWED (own account) | DENIED |
| PUT /accounts/{id} | ALLOWED | DENIED | DENIED |

---

## 4. Data Integrity

### 4.1 Data Mapping Validation

| COBOL Field | DB Column | Type Mapping | Constraint | Status |
|-------------|-----------|-------------|-----------|--------|
| ACCT-ID (X(11)) | account_id (VARCHAR(11)) | Direct | PK, NOT NULL | PASS |
| ACCT-ACTIVE-STATUS (X(1)) | account_status (VARCHAR(1)) | Direct | CHECK IN (Y,N,C) | PASS |
| ACCT-CREDIT-LIMIT (S9(15)V99) | credit_limit (DECIMAL(17,2)) | Direct | >= 0 | PASS |
| ACCT-CURR-BAL (S9(15)V99) | current_balance (DECIMAL(17,2)) | Direct | No constraint | PASS |
| ACCT-CASH-CREDIT-LIMIT (S9(15)V99) | cash_credit_limit (DECIMAL(17,2)) | Direct | >= 0, <= credit_limit | PASS |
| ACCT-OPEN-DATE (X(8)) | open_date (DATE) | CobolDate parse | NOT NULL | PASS |
| ACCT-EXPIRATN-DATE (X(8)) | expiration_date (DATE) | CobolDate parse | NOT NULL | PASS |
| ACCT-REISSUE-DATE (X(8)) | reissue_date (DATE) | CobolDate parse | NULLABLE | PASS |
| ACCT-CURR-CYC-CREDIT (S9(15)V99) | current_cycle_credit (DECIMAL(17,2)) | Direct | >= 0 | PASS |
| ACCT-CURR-CYC-DEBIT (S9(15)V99) | current_cycle_debit (DECIMAL(17,2)) | Direct | >= 0 | PASS |
| ACCT-GROUP-ID (X(10)) | group_id (VARCHAR(10)) | Direct | NULLABLE | PASS |
| ACCT-CUST-ID (9(09)) | customer_id (VARCHAR(9)) | Direct | FK to customer | PASS |

### 4.2 Concurrency Verification

- JPA @Version column (INTEGER) ensures optimistic locking
- Stale version on PUT results in OptimisticLockException mapped to HTTP 409
- Verified with concurrent update test: two simultaneous PUTs, one succeeds, one gets 409

---

## 5. FINOS CDM Compliance

| CDM Requirement | Status | Notes |
|----------------|--------|-------|
| Account representation | COMPLIANT | Follows CDM account product model |
| Credit facility modeling | COMPLIANT | Credit limit modeled as facility |
| Account lifecycle | COMPLIANT | Status transitions follow CDM lifecycle |
| Monetary amounts | COMPLIANT | BigDecimal with ISO 4217 currency support |
| Audit trail | COMPLIANT | All changes audited with full before/after state |

---

## 6. Code Quality Metrics

| Metric | Value | Threshold | Status |
|--------|-------|-----------|--------|
| Total Tests | 32 | >= 25 | PASS |
| Line Coverage | 91.4% | >= 90% | PASS |
| Branch Coverage | 84.7% | >= 80% | PASS |
| Mutation Score | 80.2% | >= 75% | PASS |
| Cyclomatic Complexity (max) | 7 | <= 10 | PASS |
| Javadoc Coverage | 100% | 100% | PASS |
| Checkstyle Violations | 0 | 0 | PASS |
| SpotBugs Issues | 0 | 0 | PASS |
| Dependency Vulnerabilities | 0 (critical) | 0 | PASS |

### 6.1 Test Breakdown

| Test Category | Count | Pass | Fail |
|--------------|-------|------|------|
| Account retrieval | 5 | 5 | 0 |
| Credit limit validation | 7 | 7 | 0 |
| Cash credit limit validation | 3 | 3 | 0 |
| Status transition | 5 | 5 | 0 |
| Monetary precision | 4 | 4 | 0 |
| Concurrency/locking | 2 | 2 | 0 |
| Error handling | 4 | 4 | 0 |
| Authorization | 2 | 2 | 0 |

---

## 7. Recommendations

### 7.1 Required Actions (before merge)
None. All checks pass.

### 7.2 Advisory Actions (post-merge)
1. **Account history API**: Consider adding GET /accounts/{id}/history for audit trail access (not in COBOL scope).
2. **Credit limit change approval workflow**: In production banking, credit limit changes typically require approval. Consider adding an approval step for changes above a configurable threshold.
3. **Multi-currency support**: COBOL assumed single currency. The CDM model supports multi-currency; consider adding a currency field to the account entity.

### 7.3 Technical Debt
- Customer cross-reference (CVACT02Y) is loaded but the customer table relationship is not yet fully modeled in JPA.
- Group ID field semantics are unclear from COBOL source; needs business clarification.

---

## 8. Final Verdict

| Category | Result |
|----------|--------|
| Behavioral Equivalence | PASS |
| Financial Accuracy | PASS |
| Security | PASS |
| Data Integrity | PASS |
| FINOS CDM Compliance | PASS |
| Code Quality | PASS |

### **APPROVED**

This module is approved for merge. Account view and update functionality preserves all COBOL business rules including credit limit validation, status transition enforcement, and concurrent update protection. Financial accuracy verified with BigDecimal precision tests.

---

*Reviewed by @agent-witness | Trace ID: witness-coactupc-20260322-001*
