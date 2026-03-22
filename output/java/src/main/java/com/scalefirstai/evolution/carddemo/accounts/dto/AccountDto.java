package com.scalefirstai.evolution.carddemo.accounts.dto;

import java.math.BigDecimal;

/**
 * DTO representing account details.
 * <p>
 * Maps to COBOL copybook COACCTDA.CPY fields from ACCTDAT VSAM file.
 * All monetary amounts use BigDecimal per banking standards.
 * </p>
 *
 * @param accountId       the account identifier (maps to ACCT-ID)
 * @param status          the account status (maps to ACCT-ACTIVE-STATUS)
 * @param currentBalance  the current balance (maps to ACCT-CUR-BAL PIC S9(13)V99)
 * @param creditLimit     the credit limit (maps to ACCT-CREDIT-LIMIT PIC S9(13)V99)
 * @param cashCreditLimit the cash advance limit (maps to ACCT-CASH-CREDIT-LIMIT PIC S9(13)V99)
 * @param openDate        the account open date (maps to ACCT-OPEN-DATE)
 * @param expirationDate  the account expiration date (maps to ACCT-EXPIRATN-DATE)
 */
public record AccountDto(
        String accountId,
        String status,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate
) {
}
