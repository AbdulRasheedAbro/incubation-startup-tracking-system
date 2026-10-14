package com.time.incubation.exception;

/**
 * Thrown when user input fails validation — empty required fields,
 * malformed email or phone, scores outside 1..10, end date before
 * start date, and similar cases.
 */
public class InvalidInputException extends Exception {

    public InvalidInputException(String message) {
        super(message);
    }
}