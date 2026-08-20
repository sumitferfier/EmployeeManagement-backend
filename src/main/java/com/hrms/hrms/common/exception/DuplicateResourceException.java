package com.hrms.hrms.common.exception;

/*
 * This exception is used when we try to create
 * a resource that already exists.
 * Examples:
 * Username already exists
 * Email already exists
 * Role already exists
 * Department already exists
 */
public class DuplicateResourceException
        extends RuntimeException {

    // Constructor receives the error message
    public DuplicateResourceException(String message) {
        super(message);
    }
}