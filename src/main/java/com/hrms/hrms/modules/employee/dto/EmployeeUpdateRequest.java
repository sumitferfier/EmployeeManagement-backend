package com.hrms.hrms.modules.employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUpdateRequest {

    // Department selected by Admin
    @NotBlank(message = "Department name is required")
    private String departmentName;

    // Job designation assigned by Admin
    @NotBlank(message = "Designation is required")
    private String designation;

    // Date employee officially joined
    private LocalDate dateOfJoining;

    /*
     * Reporting Manager is identified using email.
     *
     * Email is safer than manager name because
     * multiple employees can have the same name.
     */
    private String reportingManagerEmail;
}