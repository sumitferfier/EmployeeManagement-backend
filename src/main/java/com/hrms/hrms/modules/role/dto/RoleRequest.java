package com.hrms.hrms.modules.role.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleRequest {

    // ROLE NAME
    // Example: ADMIN, EMPLOYEE, MANAGER
    @NotBlank(message = "Role name is required")
    @Size(max = 30, message = "Role name cannot exceed 30 characters")
    private String roleName;

    // ROLE DESCRIPTION
    @Size(max = 20, message = "Description cannot exceed 20 characters")
    private String description;
}