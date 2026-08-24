package com.hrms.hrms.common.exception;

//This exception is used when a requested resource
public class ResourceNotFoundException
        extends RuntimeException {

    // Constructor receives the error message
    public ResourceNotFoundException(String message) {
        super(message);
    }
}