package com.hrms.hrms.modules.role.dto;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private UUID id;    // Unique role ID
    private String roleName;    // Role name
    private String description;    // Role description
}