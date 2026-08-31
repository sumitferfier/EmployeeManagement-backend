package com.hrms.hrms.modules.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


// =========================================================
// EMPLOYEE UPDATE REQUEST
// =========================================================

public class EmployeeUpdateRequest {


    // =====================================================
    // DEPARTMENT
    // =====================================================

    /*
     * Admin provides department name.
     *
     * Example:
     * IT
     */

    private String departmentName;


    // =====================================================
    // DESIGNATION
    // =====================================================

    private String designation;


    // =====================================================
    // DATE OF JOINING
    // =====================================================

    private LocalDate dateOfJoining;


    // =====================================================
    // REPORTING MANAGER EMAIL
    // =====================================================

    /*
     * Example:
     *
     * manager@gmail.com
     */

    private String reportingManagerEmail;


    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }


    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }


    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }


    public String getReportingManagerEmail() {
        return reportingManagerEmail;
    }

    public void setReportingManagerEmail(
            String reportingManagerEmail
    ) {
        this.reportingManagerEmail = reportingManagerEmail;
    }
}