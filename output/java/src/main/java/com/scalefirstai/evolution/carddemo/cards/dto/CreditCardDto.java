package com.scalefirstai.evolution.carddemo.cards.dto;

import java.math.BigDecimal;

/**
 * DTO representing credit card details.
 * <p>
 * Maps to COBOL copybook COCRDDA.CPY fields from CARDDAT VSAM file.
 * All monetary amounts use BigDecimal per banking standards.
 * </p>
 *
 * @param cardNumber     the card number (maps to CARD-NUM)
 * @param accountId      the associated account ID (maps to CARD-ACCT-ID)
 * @param cardholderName the cardholder name (maps to CARD-CVV-CD)
 * @param expiryDate     the card expiry date (maps to CARD-EXPIRATN-DATE)
 * @param status         the card status (maps to CARD-ACTIVE-STATUS)
 * @param creditLimit    the credit limit amount (maps to CARD-CREDIT-LIMIT)
 * @param currentBalance the current balance amount (maps to CARD-CUR-BAL)
 */
public record CreditCardDto(
        String cardNumber,
        String accountId,
        String cardholderName,
        String expiryDate,
        String status,
        BigDecimal creditLimit,
        BigDecimal currentBalance
) {
}
