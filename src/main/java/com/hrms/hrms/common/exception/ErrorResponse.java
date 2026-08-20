package com.hrms.hrms.common.exception;

import lombok.*;

import java.time.LocalDateTime;

/*Standard error response returned by our APIs.
 * Every API error will follow this structure.*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;    // Time when the error occurred
    private int status;    // HTTP status code
    private String message;    // Human-readable error message
    private String path;    // API endpoint where the error occurred

}