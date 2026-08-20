package com.hrms.hrms.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    // Email used for authentication
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Password used for authentication
    @NotBlank(message = "Password is required")
    private String password;
}