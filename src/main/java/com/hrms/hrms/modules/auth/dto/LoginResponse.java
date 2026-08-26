package com.hrms.hrms.modules.auth.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private UUID userId;
    private String email;
    private boolean isAdmin;
    private boolean isEmployee;
    private String firstName;
    private String lastName;
}