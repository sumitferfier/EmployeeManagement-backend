package com.hrms.hrms.common.exception;

/*
 * This exception is used when the client sends
 * invalid or logically incorrect data.
 * Examples:
 * End date is before start date
 * Invalid leave status
 * Invalid payroll month
 */
public class BadRequestException
        extends RuntimeException {

    // Constructor receives the error message
    public BadRequestException(String message) {
        super(message);
    }
}