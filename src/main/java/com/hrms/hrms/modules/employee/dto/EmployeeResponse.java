package com.hrms.hrms.modules.employee.dto;

import com.hrms.hrms.modules.employee.entity.EmployeeStatus;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;


// =========================================================
// EMPLOYEE RESPONSE
// =========================================================

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UUID departmentId;
    private String departmentName;
    private String designation;
    private LocalDate dateOfJoining;
    private String reportingManagerEmail;
    private EmployeeStatus status;
}