package com.time.incubation.exception;

/**
 * Thrown when attempting to insert or update a record whose email
 * is already used by another record in the same table.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String message) {
        super(message);
    }
}