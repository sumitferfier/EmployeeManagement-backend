package com.hrms.hrms.modules.auth.dto;

import lombok.*;

// This will get the loginRequest Response like
//token, tokentype, userID, Username, role
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private String role;
}