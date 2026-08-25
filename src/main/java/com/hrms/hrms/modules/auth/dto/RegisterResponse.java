package com.hrms.hrms.modules.auth.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

// REGISTER RESPONSE DTO
@Getter
@Builder
public class RegisterResponse {
    private UUID userId;
    private String email;
    private boolean isAdmin;
    private boolean isEmployee;
    private String status;
    private String message;
}