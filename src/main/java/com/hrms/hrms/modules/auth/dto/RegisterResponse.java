package com.hrms.hrms.modules.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;


// =========================================================
// REGISTER RESPONSE DTO
// =========================================================

@Getter
@Builder
public class RegisterResponse {

    // User account UUID
    private UUID userId;


    // Employee profile UUID
    private UUID employeeId;


    // Employee details
    private String firstName;
    private String lastName;


    // Login email
    private String email;


    // Default assigned role
    private String role;


    // Account status
    private String status;


    // Success message
    private String message;
}