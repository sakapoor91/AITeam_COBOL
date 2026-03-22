package com.scalefirstai.evolution.carddemo.transactions.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing transaction details.
 * <p>
 * Maps to COBOL copybook COTRNDA.CPY fields from TRANSACT VSAM file.
 * All monetary amounts use BigDecimal per banking standards.
 * </p>
 *
 * @param transactionId   the unique transaction identifier (maps to TRAN-ID)
 * @param cardNumber      the card number (maps to TRAN-CARD-NUM)
 * @param transactionType the transaction type (maps to TRAN-TYPE-CD)
 * @param amount          the transaction amount as BigDecimal (maps to TRAN-AMT PIC S9(13)V99)
 * @param merchantName    the merchant name (maps to TRAN-MERCHANT-NAME)
 * @param description     the transaction description (maps to TRAN-DESC)
 * @param transactionDate the transaction date and time (maps to TRAN-ORIG-TS)
 */
public record TransactionDto(
        String transactionId,
        String cardNumber,
        String transactionType,
        BigDecimal amount,
        String merchantName,
        String description,
        LocalDateTime transactionDate
) {
}
