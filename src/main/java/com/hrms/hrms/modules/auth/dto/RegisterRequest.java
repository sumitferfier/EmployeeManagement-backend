package com.hrms.hrms.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;


// =========================================================
// REGISTER REQUEST DTO
// =========================================================

@Getter
@Setter
public class RegisterRequest {

    // Employee first name
    @NotBlank(message = "First name is required")
    private String firstName;


    // Employee last name
    @NotBlank(message = "Last name is required")
    private String lastName;


    // Email is used as login identity
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;


    // Password
    @NotBlank(message = "Password is required")
    @Size(
            min = 6,
            message = "Password must contain at least 6 characters"
    )
    private String password;
}