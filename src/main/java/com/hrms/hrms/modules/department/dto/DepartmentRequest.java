package com.hrms.hrms.modules.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentRequest {

    // DEPARTMENT NAME
    @NotBlank(message = "Department name is required")
    @Size(max = 50, message = "Department name cannot exceed 50 characters")
    private String departmentName;

    // DESCRIPTION
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}