package com.scalefirstai.evolution.carddemo.transactions.dto;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new transaction.
 * <p>
 * Maps to input fields from COBOL program COTRN01C.CBL
 * transaction entry screen and COTRN02C.CBL processing.
 * </p>
 *
 * @param cardNumber      the card number for the transaction
 * @param transactionType the transaction type code
 * @param amount          the transaction amount as BigDecimal
 * @param merchantName    the merchant name
 * @param description     the transaction description
 */
public record TransactionRequest(
        String cardNumber,
        String transactionType,
        BigDecimal amount,
        String merchantName,
        String description
) {
}
