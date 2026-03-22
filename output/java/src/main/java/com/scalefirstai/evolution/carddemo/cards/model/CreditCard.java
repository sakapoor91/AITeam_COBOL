package com.scalefirstai.evolution.carddemo.cards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * JPA entity representing a credit card.
 * <p>
 * Modernized from COBOL VSAM file CARDDAT with record layout
 * defined in copybook COCRDDA.CPY. All monetary fields use
 * BigDecimal to preserve precision (original COBOL PIC S9(n)V99).
 * </p>
 */
@Entity
@Table(name = "credit_card")
public class CreditCard {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "account_id", length = 11, nullable = false)
    private String accountId;

    @Column(name = "cardholder_name", length = 50)
    private String cardholderName;

    @Column(name = "expiry_date", length = 10)
    private String expiryDate;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "current_balance", precision = 15, scale = 2)
    private BigDecimal currentBalance;

    /** Default constructor required by JPA. */
    public CreditCard() {
    }

    /**
     * Returns the card number.
     * @return the card number (maps to CARD-NUM PIC X(16))
     */
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * Returns the associated account ID.
     * @return the account ID (maps to CARD-ACCT-ID PIC X(11))
     */
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * Returns the cardholder name.
     * @return the cardholder name
     */
    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    /**
     * Returns the card expiry date.
     * @return the expiry date (maps to CARD-EXPIRATN-DATE)
     */
    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Returns the card active status.
     * @return the status (maps to CARD-ACTIVE-STATUS PIC X(1))
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the credit limit.
     * @return the credit limit as BigDecimal (maps to CARD-CREDIT-LIMIT PIC S9(13)V99)
     */
    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    /**
     * Returns the current balance.
     * @return the current balance as BigDecimal (maps to CARD-CUR-BAL PIC S9(13)V99)
     */
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
}
