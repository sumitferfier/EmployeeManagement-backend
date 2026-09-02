package com.hrms.hrms.modules.admin.dto;
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
    private Boolean isAdmin;
    private Boolean isEmployee;
    private String status;
    private String message;
}
