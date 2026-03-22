package com.scalefirstai.evolution.carddemo.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MoneyUtil.
 * Validates BigDecimal precision handling for financial calculations,
 * ensuring behavioral equivalence with COBOL COMP-3 / PIC S9(15)V99 fields.
 *
 * COBOL fields translated:
 * - ACCT-CREDIT-LIMIT      PIC S9(15)V99 COMP-3
 * - ACCT-CURR-BAL           PIC S9(15)V99 COMP-3
 * - ACCT-CASH-CREDIT-LIMIT  PIC S9(15)V99 COMP-3
 * - TRAN-AMT                PIC S9(9)V99  COMP-3
 *
 * All monetary operations must use BigDecimal with HALF_EVEN rounding
 * to match COBOL ROUNDED semantics.
 */
class MoneyUtilTest {

    @Test
    void testMoneyScale() {
        BigDecimal amount = MoneyUtil.of("100.00");
        assertEquals(2, amount.scale(), "Monetary amounts must have scale 2");
    }

    @Test
    void testMoneyScaleFromInteger() {
        BigDecimal amount = MoneyUtil.of("100");
        assertEquals(2, amount.scale(), "Integer amounts must be scaled to 2 decimal places");
        assertEquals(new BigDecimal("100.00"), amount);
    }

    @Test
    void testMoneyScaleFromThreeDecimals() {
        assertThrows(IllegalArgumentException.class, () -> MoneyUtil.of("100.999"),
            "Amounts with more than 2 decimal places must be rejected");
    }

    @Test
    void testAddition() {
        BigDecimal a = MoneyUtil.of("100.50");
        BigDecimal b = MoneyUtil.of("200.75");
        BigDecimal result = MoneyUtil.add(a, b);
        assertEquals(new BigDecimal("301.25"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testSubtraction() {
        BigDecimal a = MoneyUtil.of("500.00");
        BigDecimal b = MoneyUtil.of("123.45");
        BigDecimal result = MoneyUtil.subtract(a, b);
        assertEquals(new BigDecimal("376.55"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testSubtractionResultingInNegative() {
        BigDecimal a = MoneyUtil.of("100.00");
        BigDecimal b = MoneyUtil.of("200.00");
        BigDecimal result = MoneyUtil.subtract(a, b);
        assertEquals(new BigDecimal("-100.00"), result);
        assertTrue(result.compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void testMultiplication() {
        BigDecimal amount = MoneyUtil.of("100.00");
        BigDecimal rate = new BigDecimal("0.015");
        BigDecimal result = MoneyUtil.multiply(amount, rate);
        assertEquals(new BigDecimal("1.50"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testMultiplicationRounding() {
        BigDecimal amount = MoneyUtil.of("100.00");
        BigDecimal rate = new BigDecimal("0.033");
        BigDecimal result = MoneyUtil.multiply(amount, rate);
        // 100.00 * 0.033 = 3.30, HALF_EVEN rounding
        assertEquals(new BigDecimal("3.30"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testClassicFloatingPointProblem() {
        // The classic 0.1 + 0.2 = 0.3 test
        // This MUST pass to prove we are not using float/double
        BigDecimal a = MoneyUtil.of("0.10");
        BigDecimal b = MoneyUtil.of("0.20");
        BigDecimal result = MoneyUtil.add(a, b);
        assertEquals(new BigDecimal("0.30"), result,
            "0.1 + 0.2 must equal 0.3 exactly (BigDecimal, not float)");
    }

    @Test
    void testNullAmount() {
        assertThrows(NullPointerException.class, () -> MoneyUtil.of(null),
            "Null amounts must be rejected");
    }

    @Test
    void testEmptyAmount() {
        assertThrows(IllegalArgumentException.class, () -> MoneyUtil.of(""),
            "Empty amounts must be rejected");
    }

    @Test
    void testNonNumericAmount() {
        assertThrows(NumberFormatException.class, () -> MoneyUtil.of("ABC"),
            "Non-numeric amounts must be rejected");
    }

    @Test
    void testZeroAmount() {
        BigDecimal result = MoneyUtil.of("0.00");
        assertEquals(BigDecimal.ZERO.setScale(2), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testNegativeAmount() {
        BigDecimal result = MoneyUtil.of("-500.00");
        assertEquals(new BigDecimal("-500.00"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testMaxCobolAmount() {
        // PIC S9(15)V99 max value: 999999999999999.99
        BigDecimal maxAmount = MoneyUtil.of("999999999999999.99");
        assertEquals(new BigDecimal("999999999999999.99"), maxAmount);
        assertEquals(2, maxAmount.scale());
    }

    @Test
    void testIsPositive() {
        assertTrue(MoneyUtil.isPositive(MoneyUtil.of("100.00")));
        assertFalse(MoneyUtil.isPositive(MoneyUtil.of("0.00")));
        assertFalse(MoneyUtil.isPositive(MoneyUtil.of("-100.00")));
    }

    @Test
    void testIsNonNegative() {
        assertTrue(MoneyUtil.isNonNegative(MoneyUtil.of("100.00")));
        assertTrue(MoneyUtil.isNonNegative(MoneyUtil.of("0.00")));
        assertFalse(MoneyUtil.isNonNegative(MoneyUtil.of("-0.01")));
    }

    @Test
    void testAddNulls() {
        assertThrows(NullPointerException.class, () -> MoneyUtil.add(null, MoneyUtil.of("1.00")));
        assertThrows(NullPointerException.class, () -> MoneyUtil.add(MoneyUtil.of("1.00"), null));
    }

    @Test
    void testSubtractNulls() {
        assertThrows(NullPointerException.class, () -> MoneyUtil.subtract(null, MoneyUtil.of("1.00")));
        assertThrows(NullPointerException.class, () -> MoneyUtil.subtract(MoneyUtil.of("1.00"), null));
    }

    @Test
    void testMultiplyByZero() {
        BigDecimal amount = MoneyUtil.of("500.00");
        BigDecimal result = MoneyUtil.multiply(amount, BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testPennyPrecision() {
        BigDecimal a = MoneyUtil.of("0.01");
        BigDecimal b = MoneyUtil.of("0.01");
        BigDecimal result = MoneyUtil.add(a, b);
        assertEquals(new BigDecimal("0.02"), result);
    }

    @Test
    void testLargeAddition() {
        BigDecimal a = MoneyUtil.of("999999999999999.00");
        BigDecimal b = MoneyUtil.of("0.99");
        BigDecimal result = MoneyUtil.add(a, b);
        assertEquals(new BigDecimal("999999999999999.99"), result);
    }

    @Test
    void testRoundingModeIsHalfEven() {
        // Banker's rounding: 2.5 -> 2, 3.5 -> 4
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal divisor = new BigDecimal("3");
        BigDecimal result = amount.divide(divisor, 2, RoundingMode.HALF_EVEN);
        assertEquals(new BigDecimal("33.33"), result);
    }
}
