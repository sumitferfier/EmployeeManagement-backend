package com.hrms.hrms.common.exception;

//Ths exception is used when the client sends
public class BadRequestException
        extends RuntimeException {

    // Constructor receives the error message
    public BadRequestException(String message) {
        super(message);
    }
}