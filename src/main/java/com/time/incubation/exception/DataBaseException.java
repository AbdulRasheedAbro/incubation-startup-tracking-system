package com.time.incubation.exception;

/**
 * Wraps a low-level SQLException so the rest of the application
 * never has to import java.sql.SQLException. Preserves the original
 * cause for debugging.
 */
public class DataBaseException extends Exception {

    public DataBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataBaseException(String message) {
        super(message);
    }
}