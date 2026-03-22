package com.scalefirstai.evolution.carddemo.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for monetary amount operations.
 * <p>
 * Provides consistent BigDecimal handling to match COBOL
 * PIC S9(13)V99 COMP-3 precision. All monetary values are
 * scaled to 2 decimal places with HALF_UP rounding.
 * </p>
 */
public final class MoneyUtil {

    /** Standard scale for monetary amounts (2 decimal places). */
    public static final int MONEY_SCALE = 2;

    /** Standard rounding mode for monetary calculations. */
    public static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private MoneyUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Rounds a BigDecimal to standard monetary precision.
     * <p>
     * Equivalent to COBOL COMPUTE with implicit V99 scaling.
     * </p>
     *
     * @param amount the amount to round
     * @return the amount scaled to 2 decimal places with HALF_UP rounding
     */
    public static BigDecimal round(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUNDING);
        }
        return amount.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    /**
     * Adds two monetary amounts with proper rounding.
     *
     * @param a the first amount
     * @param b the second amount
     * @return the sum, rounded to 2 decimal places
     */
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        BigDecimal safeA = (a != null) ? a : BigDecimal.ZERO;
        BigDecimal safeB = (b != null) ? b : BigDecimal.ZERO;
        return safeA.add(safeB).setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    /**
     * Subtracts one monetary amount from another with proper rounding.
     *
     * @param a the amount to subtract from
     * @param b the amount to subtract
     * @return the difference, rounded to 2 decimal places
     */
    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        BigDecimal safeA = (a != null) ? a : BigDecimal.ZERO;
        BigDecimal safeB = (b != null) ? b : BigDecimal.ZERO;
        return safeA.subtract(safeB).setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    /**
     * Checks if a monetary amount is positive (greater than zero).
     *
     * @param amount the amount to check
     * @return true if the amount is greater than zero
     */
    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if a monetary amount is negative (less than zero).
     *
     * @param amount the amount to check
     * @return true if the amount is less than zero
     */
    public static boolean isNegative(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Creates a zero-valued monetary amount with proper scale.
     *
     * @return BigDecimal zero with scale 2
     */
    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
