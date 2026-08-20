package com.hrms.hrms.common.exception;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/*@RestControllerAdvice makes this class a global
 * exception handler for all REST controllers.
 * Instead of writing try/catch in every controller,
 * exceptions are handled here.*/
@RestControllerAdvice
public class GlobalExceptionHandler {

    // RESOURCE NOT FOUND
    /*
     * Handles ResourceNotFoundException.
     * HTTP status: 404 NOT FOUND*/
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())// Current date and time
                        .status(HttpStatus.NOT_FOUND.value())// HTTP 404
                        .message(exception.getMessage())// Exception message
                        .path(request.getRequestURI())// Requested API path
                        .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // DUPLICATE RESOURCE
    /*Handles DuplicateResourceException.
     * HTTP status: 409 CONFLICT*/
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException exception,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }


    // BAD REQUEST
    /*Handles BadRequestException
     * HTTP status: 400 BAD REQUEST*/
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException exception,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // VALIDATION ERRORS
    /*Handles @Valid validation failures.
     * Example:@NotBlank, @Email, @Size
     * If the client sends invalid data,
     * Spring throws MethodArgumentNotValidException.*/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        // Collect all validation error messages.
        String message = exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(message)
                        .path(request.getRequestURI())
                        .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // CONSTRAINT VIOLATION
    /*Handles validation errors that occur on
     * request parameters or path variables.*/
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // GENERAL EXCEPTION
    /*This is the fallback handler.
     * If an unexpected exception occurs that we
     * haven't specifically handled above, this method
     * prevents Spring from returning an inconsistent
     * error response.*/
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception exception,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("An unexpected error occurred")
                        .path(request.getRequestURI())
                        .build();

        // Log the actual exception for developers.
        // We don't expose internal exception details
        // to the client.
        exception.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}