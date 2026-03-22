package com.scalefirstai.evolution.carddemo.cards.dto;

/**
 * Request DTO for credit card updates.
 * <p>
 * Maps to editable fields from COBOL copybook COCRDDA.CPY
 * used in program COCRDUPC.CBL card update screen.
 * </p>
 *
 * @param cardholderName the updated cardholder name
 * @param expiryDate     the updated expiry date
 * @param status         the updated card status
 */
public record CardUpdateRequest(
        String cardholderName,
        String expiryDate,
        String status
) {
}
