package com.scalefirstai.evolution.carddemo.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for COBOL date format conversions.
 * <p>
 * Handles conversion between COBOL date formats (YYYYMMDD, YYYY-MM-DD)
 * and Java LocalDate. COBOL programs typically store dates as
 * PIC 9(8) in YYYYMMDD format or PIC X(10) in YYYY-MM-DD format.
 * </p>
 */
public final class CobolDate {

    /** COBOL packed date format: YYYYMMDD (PIC 9(8)). */
    public static final DateTimeFormatter COBOL_PACKED_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /** COBOL display date format: YYYY-MM-DD (PIC X(10)). */
    public static final DateTimeFormatter COBOL_DISPLAY_FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE;

    private CobolDate() {
        // Utility class - prevent instantiation
    }

    /**
     * Parses a COBOL packed date string (YYYYMMDD) to LocalDate.
     * <p>
     * COBOL source: Common date handling in CardDemo programs.
     * Converts PIC 9(8) date fields to Java LocalDate.
     * </p>
     *
     * @param cobolDate the date string in YYYYMMDD format
     * @return the parsed LocalDate, or null if the input is null or invalid
     */
    public static LocalDate fromPackedDate(String cobolDate) {
        if (cobolDate == null || cobolDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(cobolDate.trim(), COBOL_PACKED_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Formats a LocalDate to COBOL packed date string (YYYYMMDD).
     *
     * @param date the date to format
     * @return the date as YYYYMMDD string, or empty string if null
     */
    public static String toPackedDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(COBOL_PACKED_FORMAT);
    }

    /**
     * Parses a COBOL display date string (YYYY-MM-DD) to LocalDate.
     *
     * @param displayDate the date string in YYYY-MM-DD format
     * @return the parsed LocalDate, or null if the input is null or invalid
     */
    public static LocalDate fromDisplayDate(String displayDate) {
        if (displayDate == null || displayDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(displayDate.trim(), COBOL_DISPLAY_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Formats a LocalDate to COBOL display date string (YYYY-MM-DD).
     *
     * @param date the date to format
     * @return the date as YYYY-MM-DD string, or empty string if null
     */
    public static String toDisplayDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(COBOL_DISPLAY_FORMAT);
    }

    /**
     * Converts a COBOL packed date (YYYYMMDD) to display format (YYYY-MM-DD).
     *
     * @param packedDate the packed date string
     * @return the display format date string, or empty string if invalid
     */
    public static String packedToDisplay(String packedDate) {
        LocalDate date = fromPackedDate(packedDate);
        return toDisplayDate(date);
    }
}
