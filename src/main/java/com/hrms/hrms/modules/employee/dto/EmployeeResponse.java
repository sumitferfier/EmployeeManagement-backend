package com.hrms.hrms.modules.employee.dto;

import com.hrms.hrms.modules.employee.entity.EmployeeStatus;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

/*Response DTO returned to the frontend.
 * We return useful IDs and names instead of exposing
 * the complete JPA entity structure.*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private UUID id;
    private UUID userId;    // Linked User UUID
    private String email;

    // Employee information
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String phone;

    // Department information
    private UUID departmentId;
    private String departmentName;

    // Job information
    private String designation;
    private LocalDate dateOfJoining;

    // Reporting manager information
    private UUID reportingManagerId;
    private String reportingManagerName;

    // Employee status
    private EmployeeStatus status;
}