package com.time.incubation.exception;

/**
 * Thrown when attempting to insert a record whose primary key
 * (startup ID, mentor ID, founder ID, etc.) already exists.
 */
public class DuplicateIdException extends Exception {

    public DuplicateIdException(String message) {
        super(message);
    }
}