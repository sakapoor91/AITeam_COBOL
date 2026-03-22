package com.scalefirstai.evolution.carddemo.transactions.model;

/**
 * Enumeration of transaction types.
 * <p>
 * Modernized from COBOL TRAN-TYPE-CD field in copybook COTRNDA.CPY.
 * Original values mapped from two-character codes (SA, CR, etc.)
 * to descriptive enum constants.
 * </p>
 */
public enum TransactionType {

    /** Standard purchase transaction. Maps to COBOL type code 'SA'. */
    PURCHASE,

    /** Payment/credit to account. Maps to COBOL type code 'CR'. */
    PAYMENT,

    /** Cash advance withdrawal. Maps to COBOL type code 'CA'. */
    CASH_ADVANCE,

    /** Refund/return transaction. Maps to COBOL type code 'RF'. */
    REFUND,

    /** Account fee charge. Maps to COBOL type code 'FE'. */
    FEE,

    /** Interest charge. Maps to COBOL type code 'IN'. */
    INTEREST
}
