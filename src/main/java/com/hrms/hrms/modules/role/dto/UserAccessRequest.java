package com.hrms.hrms.modules.role.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccessRequest {

    @NotNull(message = "isAdmin is required")
    private Boolean isAdmin;

    @NotNull(message = "isEmployee is required")
    private Boolean isEmployee;
}