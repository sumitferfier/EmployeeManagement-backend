package com.hrms.hrms.modules.auth.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

// REGISTER RESPONSE DTO
@Getter
@Builder
public class RegisterResponse {
    private UUID userId;
    private UUID employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String status;
    private String message;
}