package com.time.incubation.util;

import java.time.LocalDate;

/**
 * Static validation helpers used by the service layer.
 */
public final class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[0-9+\\-() ]{7,20}$");
    }

    public static boolean isValidScore(int score) {
        return score >= 1 && score <= 10;
    }

    public static boolean isValidDateRange(LocalDate start, LocalDate end) {
        return start != null && end != null && !end.isBefore(start);
    }

    /**
     * Parses a YYYY-MM-DD string. Returns null if the input is empty
     * or not a valid date.
     */
    public static LocalDate parseDateOrNull(String s) {
        if (isNullOrEmpty(s)) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}