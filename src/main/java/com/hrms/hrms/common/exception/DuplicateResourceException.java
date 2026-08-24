package com.hrms.hrms.common.exception;

//This exception is used when we try to create

public class DuplicateResourceException
        extends RuntimeException {

    // Constructor receives the error message
    public DuplicateResourceException(String message) {
        super(message);
    }
}