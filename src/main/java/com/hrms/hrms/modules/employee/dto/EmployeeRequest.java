package com.hrms.hrms.modules.employee.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

/*Request DTO used when creating or updating an employee.
 * We don't directly accept the Employee entity from the frontend.*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {

    // USER ID
    /*Existing User account that will be linked
     * with this employee.*/
    @NotNull(message = "User ID is required")
    private UUID userId;

    // EMPLOYEE CODE
    @NotBlank(message = "Employee code is required")
    @Size(max = 20, message = "Employee code cannot exceed 20 characters")
    private String employeeCode;

    // FIRST NAME
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    // LAST NAME
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    // PHONE
    @Size(max = 15, message = "Phone cannot exceed 15 characters")
    private String phone;

    // DEPARTMENT ID
    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    // DESIGNATION
    @NotBlank(message = "Designation is required")
    @Size(max = 50, message = "Designation cannot exceed 50 characters")
    private String designation;

    // DATE OF JOINING
    @NotNull(message = "Date of joining is required")
    private LocalDate dateOfJoining;

    // REPORTING MANAGER
    /*Optional.
     * An employee may not have a reporting manager,
     * for example, a senior manager.*/
    private UUID reportingManagerId;

    // STATUS
    /*Optional.
     * If not provided, service will use ACTIVE.*/
    private String status;
}