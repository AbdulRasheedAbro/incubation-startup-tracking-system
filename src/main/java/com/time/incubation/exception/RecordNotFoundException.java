package com.time.incubation.exception;

/**
 * Thrown when a lookup by ID finds no matching row.
 */
public class RecordNotFoundException extends Exception {

    public RecordNotFoundException(String message) {
        super(message);
    }
}