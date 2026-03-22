package com.scalefirstai.evolution.carddemo.accounts.model;

/**
 * Enumeration of account statuses.
 * <p>
 * Modernized from COBOL ACCT-ACTIVE-STATUS field in copybook
 * COACCTDA.CPY. Original values: 'Y' (active), 'N' (inactive).
 * Expanded to include CLOSED for complete lifecycle tracking.
 * </p>
 */
public enum AccountStatus {

    /** Account is active and in good standing. Maps to COBOL 'Y'. */
    ACTIVE,

    /** Account is temporarily inactive. Maps to COBOL 'N'. */
    INACTIVE,

    /** Account has been permanently closed. Modernization addition. */
    CLOSED
}
