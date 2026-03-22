package com.scalefirstai.evolution.carddemo.accounts.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * JPA entity representing a credit card account.
 * <p>
 * Modernized from COBOL VSAM file ACCTDAT with record layout
 * defined in copybook COACCTDA.CPY. All monetary fields use
 * BigDecimal (original COBOL PIC S9(13)V99 COMP-3).
 * </p>
 */
@Entity
@Table(name = "account")
public class Account {

    @Id
    @Column(name = "account_id", length = 11, nullable = false)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private AccountStatus status;

    @Column(name = "current_balance", precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "cash_credit_limit", precision = 15, scale = 2)
    private BigDecimal cashCreditLimit;

    @Column(name = "open_date", length = 10)
    private String openDate;

    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    /** Default constructor required by JPA. */
    public Account() {
    }

    /**
     * Returns the account identifier.
     * @return the account ID (maps to ACCT-ID PIC X(11))
     */
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * Returns the account status.
     * @return the status (maps to ACCT-ACTIVE-STATUS)
     */
    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    /**
     * Returns the current balance.
     * @return the current balance as BigDecimal (maps to ACCT-CUR-BAL PIC S9(13)V99)
     */
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    /**
     * Returns the credit limit.
     * @return the credit limit as BigDecimal (maps to ACCT-CREDIT-LIMIT PIC S9(13)V99)
     */
    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    /**
     * Returns the cash advance credit limit.
     * @return the cash credit limit as BigDecimal (maps to ACCT-CASH-CREDIT-LIMIT PIC S9(13)V99)
     */
    public BigDecimal getCashCreditLimit() {
        return cashCreditLimit;
    }

    public void setCashCreditLimit(BigDecimal cashCreditLimit) {
        this.cashCreditLimit = cashCreditLimit;
    }

    /**
     * Returns the account open date.
     * @return the open date (maps to ACCT-OPEN-DATE)
     */
    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    /**
     * Returns the account expiration date.
     * @return the expiration date (maps to ACCT-EXPIRATN-DATE)
     */
    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
