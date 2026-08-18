package com.hrms.hrms.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    // Username used for login
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50,
            message = "Username must be between 3 and 50 characters")
    private String username;

    // Email address of the employee
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100,
            message = "Email cannot exceed 100 characters")
    private String email;

    // Plain password received from the client.
    // It will be encrypted using BCrypt before saving.
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100,
            message = "Password must be at least 6 characters")
    private String password;
}