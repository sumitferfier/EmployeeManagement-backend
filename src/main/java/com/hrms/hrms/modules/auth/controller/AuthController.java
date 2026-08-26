package com.hrms.hrms.modules.auth.controller;

import com.hrms.hrms.modules.auth.dto.LoginRequest;
import com.hrms.hrms.modules.auth.dto.LoginResponse;
import com.hrms.hrms.modules.auth.dto.RegisterRequest;
import com.hrms.hrms.modules.auth.dto.RegisterResponse;
import com.hrms.hrms.modules.auth.service.AuthService;
import org.springframework.http.HttpHeaders;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    // REGISTER
    // Public endpoint.
    // Creates an EMPLOYEE account.
    // No JWT is required for registration.
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // LOGIN
    // Public endpoint.
    // Valid username/password returns JWT.
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // USER LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {

        // Check Authorization header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid Authorization header");
        }

        // Remove "Bearer " from token
        String token = authorizationHeader.substring(7);

        // Logout user
        authService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }
}