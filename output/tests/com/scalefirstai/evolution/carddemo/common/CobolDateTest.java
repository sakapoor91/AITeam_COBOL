package com.scalefirstai.evolution.carddemo.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CobolDate utility.
 * Validates date parsing/formatting between COBOL date formats and Java LocalDate.
 *
 * COBOL date fields translated:
 * - ACCT-OPEN-DATE          PIC X(8)  (YYYYMMDD format)
 * - ACCT-EXPIRATION-DATE    PIC X(8)  (YYYYMMDD format)
 * - ACCT-REISSUE-DATE       PIC X(8)  (YYYYMMDD format)
 * - CARD-EXPIRY-DATE        PIC X(8)  (YYYYMMDD format)
 * - TRAN-DATE               PIC X(8)  (YYYYMMDD format)
 * - TRAN-TIME               PIC X(6)  (HHMMSS format)
 *
 * The COBOL CardDemo system uses YYYYMMDD as a standard date format
 * across all programs (COSGN00C, COACTUPC, COCRDUPC, COTRN00C).
 */
class CobolDateTest {

    // --- COBOL string to LocalDate ---

    @Test
    void testParseValidCobolDate() {
        LocalDate result = CobolDate.parseCobolDate("20260322");
        assertEquals(LocalDate.of(2026, 3, 22), result);
    }

    @Test
    void testParseCobolDateJanuary() {
        LocalDate result = CobolDate.parseCobolDate("20260101");
        assertEquals(LocalDate.of(2026, 1, 1), result);
    }

    @Test
    void testParseCobolDateDecember() {
        LocalDate result = CobolDate.parseCobolDate("20261231");
        assertEquals(LocalDate.of(2026, 12, 31), result);
    }

    @Test
    void testParseCobolDateLeapYear() {
        LocalDate result = CobolDate.parseCobolDate("20240229");
        assertEquals(LocalDate.of(2024, 2, 29), result);
    }

    @Test
    void testParseCobolDateNonLeapYear() {
        assertThrows(DateTimeParseException.class, () -> CobolDate.parseCobolDate("20250229"),
            "Feb 29 in non-leap year must be rejected");
    }

    @Test
    void testParseCobolDateMinDate() {
        LocalDate result = CobolDate.parseCobolDate("19000101");
        assertEquals(LocalDate.of(1900, 1, 1), result);
    }

    @Test
    void testParseCobolDateY2KDate() {
        LocalDate result = CobolDate.parseCobolDate("20000101");
        assertEquals(LocalDate.of(2000, 1, 1), result);
    }

    // --- LocalDate to COBOL string ---

    @Test
    void testFormatToCobolDate() {
        String result = CobolDate.formatToCobol(LocalDate.of(2026, 3, 22));
        assertEquals("20260322", result);
    }

    @Test
    void testFormatToCobolDateJanuary() {
        String result = CobolDate.formatToCobol(LocalDate.of(2026, 1, 1));
        assertEquals("20260101", result);
    }

    @Test
    void testFormatToCobolDateLeadingZeros() {
        String result = CobolDate.formatToCobol(LocalDate.of(2026, 3, 5));
        assertEquals("20260305", result);
    }

    @Test
    void testFormatToCobolDateDecember() {
        String result = CobolDate.formatToCobol(LocalDate.of(2026, 12, 31));
        assertEquals("20261231", result);
    }

    // --- Roundtrip ---

    @Test
    void testRoundtripParseFormat() {
        String original = "20260322";
        LocalDate parsed = CobolDate.parseCobolDate(original);
        String formatted = CobolDate.formatToCobol(parsed);
        assertEquals(original, formatted);
    }

    @Test
    void testRoundtripFormatParse() {
        LocalDate original = LocalDate.of(2026, 7, 15);
        String formatted = CobolDate.formatToCobol(original);
        LocalDate parsed = CobolDate.parseCobolDate(formatted);
        assertEquals(original, parsed);
    }

    // --- Invalid input handling ---

    @Test
    void testParseNullDate() {
        assertThrows(NullPointerException.class, () -> CobolDate.parseCobolDate(null));
    }

    @Test
    void testParseEmptyDate() {
        assertThrows(IllegalArgumentException.class, () -> CobolDate.parseCobolDate(""));
    }

    @Test
    void testParseShortDate() {
        assertThrows(IllegalArgumentException.class, () -> CobolDate.parseCobolDate("202603"),
            "Dates shorter than 8 characters must be rejected");
    }

    @Test
    void testParseLongDate() {
        assertThrows(IllegalArgumentException.class, () -> CobolDate.parseCobolDate("202603221"),
            "Dates longer than 8 characters must be rejected");
    }

    @Test
    void testParseNonNumericDate() {
        assertThrows(IllegalArgumentException.class, () -> CobolDate.parseCobolDate("2026ABCD"),
            "Non-numeric dates must be rejected");
    }

    @Test
    void testParseInvalidMonth() {
        assertThrows(DateTimeParseException.class, () -> CobolDate.parseCobolDate("20261301"),
            "Month 13 must be rejected");
    }

    @Test
    void testParseInvalidDay() {
        assertThrows(DateTimeParseException.class, () -> CobolDate.parseCobolDate("20260332"),
            "Day 32 must be rejected");
    }

    @Test
    void testParseZeroMonth() {
        assertThrows(DateTimeParseException.class, () -> CobolDate.parseCobolDate("20260022"),
            "Month 00 must be rejected");
    }

    @Test
    void testParseZeroDay() {
        assertThrows(DateTimeParseException.class, () -> CobolDate.parseCobolDate("20260300"),
            "Day 00 must be rejected");
    }

    @ParameterizedTest
    @ValueSource(strings = {"SPACES  ", "00000000", "        "})
    void testParseCobolSpecialValues(String cobolValue) {
        assertThrows(Exception.class, () -> CobolDate.parseCobolDate(cobolValue),
            "COBOL special values (SPACES, zeros, blanks) must be rejected");
    }

    @Test
    void testFormatNullDate() {
        assertThrows(NullPointerException.class, () -> CobolDate.formatToCobol(null));
    }

    // --- COBOL time parsing ---

    @Test
    void testParseCobolTime() {
        var result = CobolDate.parseCobolTime("143022");
        assertEquals(14, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(22, result.getSecond());
    }

    @Test
    void testParseCobolTimeMidnight() {
        var result = CobolDate.parseCobolTime("000000");
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    void testParseCobolTimeEndOfDay() {
        var result = CobolDate.parseCobolTime("235959");
        assertEquals(23, result.getHour());
        assertEquals(59, result.getMinute());
        assertEquals(59, result.getSecond());
    }

    @Test
    void testParseInvalidCobolTime() {
        assertThrows(Exception.class, () -> CobolDate.parseCobolTime("250000"),
            "Hour 25 must be rejected");
    }

    @Test
    void testParseNullCobolTime() {
        assertThrows(NullPointerException.class, () -> CobolDate.parseCobolTime(null));
    }

    @Test
    void testResultStringLength() {
        String result = CobolDate.formatToCobol(LocalDate.of(2026, 3, 22));
        assertEquals(8, result.length(), "COBOL date must be exactly 8 characters");
    }
}
