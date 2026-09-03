package com.hrms.hrms.modules.employee.dto;

import com.hrms.hrms.modules.employee.entity.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private UUID id;
    private UUID userId;
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