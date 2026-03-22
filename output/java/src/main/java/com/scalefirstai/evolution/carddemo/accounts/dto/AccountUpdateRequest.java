package com.scalefirstai.evolution.carddemo.accounts.dto;

import java.math.BigDecimal;

/**
 * Request DTO for account updates.
 * <p>
 * Maps to editable fields from COBOL copybook COACCTDA.CPY
 * used in program COACTUPC.CBL account update screen.
 * </p>
 *
 * @param status          the updated account status
 * @param creditLimit     the updated credit limit
 * @param cashCreditLimit the updated cash advance credit limit
 */
public record AccountUpdateRequest(
        String status,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit
) {
}
