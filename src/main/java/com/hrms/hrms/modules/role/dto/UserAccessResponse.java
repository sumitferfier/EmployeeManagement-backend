package com.hrms.hrms.modules.role.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccessResponse {
    private UUID userId;
    private String email;
    private boolean isAdmin;
    private boolean isEmployee;
    private String status;
    private String message;
}