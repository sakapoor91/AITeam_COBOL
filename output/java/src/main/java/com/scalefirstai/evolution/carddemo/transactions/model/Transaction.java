package com.scalefirstai.evolution.carddemo.transactions.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a credit card transaction.
 * <p>
 * Modernized from COBOL VSAM file TRANSACT with record layout
 * defined in copybook COTRNDA.CPY. All monetary fields use
 * BigDecimal (original COBOL PIC S9(13)V99 COMP-3).
 * </p>
 */
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @Column(name = "transaction_id", length = 36, nullable = false)
    private String transactionId;

    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "merchant_name", length = 100)
    private String merchantName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    /** Default constructor required by JPA. */
    public Transaction() {
    }

    /**
     * Returns the transaction identifier.
     * @return the transaction ID (maps to TRAN-ID)
     */
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Returns the card number.
     * @return the card number (maps to TRAN-CARD-NUM PIC X(16))
     */
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * Returns the transaction type.
     * @return the transaction type (maps to TRAN-TYPE-CD)
     */
    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * Returns the transaction amount.
     * @return the amount as BigDecimal (maps to TRAN-AMT PIC S9(13)V99)
     */
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Returns the merchant name.
     * @return the merchant name (maps to TRAN-MERCHANT-NAME)
     */
    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    /**
     * Returns the transaction description.
     * @return the description (maps to TRAN-DESC)
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the transaction date and time.
     * @return the transaction timestamp (maps to TRAN-ORIG-TS)
     */
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}
