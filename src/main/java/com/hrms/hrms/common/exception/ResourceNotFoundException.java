package com.hrms.hrms.common.exception;

/*
 * This exception is used when a requested resource
 * does not exist in the database.
 * Examples:
 * Employee ID 100 does not exist
 * Role ID 50 does not exist
 * Department ID 20 does not exist
 */
public class ResourceNotFoundException
        extends RuntimeException {

    // Constructor receives the error message
    public ResourceNotFoundException(String message) {
        super(message);
    }
}