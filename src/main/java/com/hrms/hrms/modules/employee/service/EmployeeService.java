package com.hrms.hrms.modules.employee.service;

import com.hrms.hrms.common.exception.BadRequestException;
import com.hrms.hrms.common.exception.ResourceNotFoundException;

import com.hrms.hrms.modules.department.entity.Department;
import com.hrms.hrms.modules.department.repository.DepartmentRepository;

import com.hrms.hrms.modules.employee.dto.EmployeeResponse;
import com.hrms.hrms.modules.employee.dto.EmployeeUpdateRequest;
import com.hrms.hrms.modules.employee.entity.Employee;
import com.hrms.hrms.modules.employee.repository.EmployeeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class EmployeeService {

    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }


    // =========================================================
    // GET ALL EMPLOYEES
    // =========================================================

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET EMPLOYEE BY EMAIL
    // =========================================================

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByEmail(String email) {

        Employee employee =
                findEmployeeByEmail(email);

        return mapToResponse(employee);
    }


    // =========================================================
    // GET MY PROFILE
    // =========================================================

    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile(String email) {

        return getEmployeeByEmail(email);
    }


    // =========================================================
    // UPDATE EMPLOYEE
    // =========================================================

    /*
     * PATCH
     *
     * /api/v1/employees?email=user@gmail.com
     *
     * Admin manages:
     *
     * - Department
     * - Designation
     * - Date Of Joining
     * - Reporting Manager Email
     *
     * These are NOT changed here:
     *
     * - First Name
     * - Last Name
     * - Email
     * - Status
     *
     * Those come from User Access Management.
     */

    @Transactional
    public EmployeeResponse updateEmployee(
            String email,
            EmployeeUpdateRequest request
    ) {

        // =====================================================
        // STEP 1: FIND EMPLOYEE
        // =====================================================

        Employee employee =
                findEmployeeByEmail(email);


        // =====================================================
        // STEP 2: DEPARTMENT
        // =====================================================

        /*
         * If department name is provided:
         *
         * 1. Search existing department.
         * 2. If found -> use it.
         * 3. If not found -> create it.
         *
         * This handles both:
         *
         * First assignment
         * AND
         * Department change.
         */

        if (
                request.getDepartmentName() != null
                        &&
                        !request.getDepartmentName().isBlank()
        ) {

            String departmentName =
                    request.getDepartmentName()
                            .trim();


            Department department =
                    departmentRepository
                            .findByDepartmentName(
                                    departmentName
                            )
                            .orElseGet(() -> {

                                Department newDepartment =
                                        Department.builder()
                                                .departmentName(
                                                        departmentName
                                                )
                                                .build();

                                return departmentRepository
                                        .save(newDepartment);
                            });


            employee.setDepartment(department);
        }


        // =====================================================
        // STEP 3: DESIGNATION
        // =====================================================

        if (
                request.getDesignation() != null
                        &&
                        !request.getDesignation().isBlank()
        ) {

            employee.setDesignation(
                    request.getDesignation().trim()
            );
        }


        // =====================================================
        // STEP 4: DATE OF JOINING
        // =====================================================

        if (request.getDateOfJoining() != null) {

            employee.setDateOfJoining(
                    request.getDateOfJoining()
            );
        }


        // =====================================================
        // STEP 5: REPORTING MANAGER
        // =====================================================

        /*
         * Reporting manager is stored as EMAIL.
         *
         * Example:
         *
         * reporting_manager_email
         *          =
         * manager@gmail.com
         */

        if (
                request.getReportingManagerEmail() != null
        ) {

            String managerEmail =
                    request.getReportingManagerEmail()
                            .trim()
                            .toLowerCase();


            // -------------------------------------------------
            // REMOVE REPORTING MANAGER
            // -------------------------------------------------

            /*
             * If frontend sends empty string,
             * remove the current manager.
             */

            if (managerEmail.isBlank()) {

                employee.setReportingManagerEmail(null);

            } else {

                // -------------------------------------------------
                // EMPLOYEE CANNOT BE THEIR OWN MANAGER
                // -------------------------------------------------

                if (
                        managerEmail.equalsIgnoreCase(
                                email.trim()
                        )
                ) {

                    throw new BadRequestException(
                            "Employee cannot be their own reporting manager"
                    );
                }


                // -------------------------------------------------
                // FIND MANAGER
                // -------------------------------------------------

                Employee manager =
                        employeeRepository
                                .findByUserEmail(
                                        managerEmail
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Reporting manager not found with email: "
                                                        + managerEmail
                                        )
                                );


                // -------------------------------------------------
                // GET ACTUAL USER EMAIL
                // -------------------------------------------------

                String actualManagerEmail =
                        manager.getUser()
                                .getEmail()
                                .trim()
                                .toLowerCase();


                // -------------------------------------------------
                // SAVE MANAGER EMAIL
                // -------------------------------------------------

                employee.setReportingManagerEmail(
                        actualManagerEmail
                );
            }
        }


        // =====================================================
        // STEP 6: SAVE EMPLOYEE
        // =====================================================

        Employee updatedEmployee =
                employeeRepository.save(employee);


        // =====================================================
        // STEP 7: RETURN RESPONSE
        // =====================================================

        return mapToResponse(
                updatedEmployee
        );
    }


    // =========================================================
    // GET MANAGER TEAM
    // =========================================================

    /*
     * Finds all employees whose
     * reporting_manager_email matches
     * the manager's email.
     */

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeeTeam(
            String managerEmail
    ) {

        return employeeRepository
                .findByReportingManagerEmail(
                        managerEmail
                                .trim()
                                .toLowerCase()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // FIND EMPLOYEE BY EMAIL
    // =========================================================

    private Employee findEmployeeByEmail(
            String email
    ) {

        return employeeRepository
                .findByUserEmail(
                        email.trim()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with email: "
                                        + email
                        )
                );
    }


    // =========================================================
    // ENTITY → RESPONSE DTO
    // =========================================================

    private EmployeeResponse mapToResponse(
            Employee employee
    ) {

        UUID departmentId = null;
        String departmentName = null;


        // =====================================================
        // DEPARTMENT DETAILS
        // =====================================================

        if (employee.getDepartment() != null) {

            departmentId =
                    employee.getDepartment()
                            .getId();

            departmentName =
                    employee.getDepartment()
                            .getDepartmentName();
        }


        // =====================================================
        // RESPONSE
        // =====================================================

        return EmployeeResponse.builder()

                // Employee ID
                .id(
                        employee.getId()
                )

                // Email
                .email(
                        employee.getUser()
                                .getEmail()
                )

                // Name
                .firstName(
                        employee.getFirstName()
                )

                .lastName(
                        employee.getLastName()
                )

                // Department
                .departmentId(
                        departmentId
                )

                .departmentName(
                        departmentName
                )

                // Designation
                .designation(
                        employee.getDesignation()
                )

                // Joining Date
                .dateOfJoining(
                        employee.getDateOfJoining()
                )

                // Reporting Manager Email
                .reportingManagerEmail(
                        employee.getReportingManagerEmail()
                )

                // Status
                .status(
                        employee.getStatus()
                )

                .build();
    }
}
