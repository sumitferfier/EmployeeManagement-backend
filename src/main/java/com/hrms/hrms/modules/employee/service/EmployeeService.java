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
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    // GET ALL EMPLOYEES
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET EMPLOYEE BY EMAIL
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByEmail(
            String email
    ) {

        Employee employee = findEmployeeByEmail(email);
        return mapToResponse(employee);
    }

    // GET LOGGED-IN EMPLOYEE PROFILE
    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile(
            String email
    ) {

        return getEmployeeByEmail(email);
    }

    // UPDATE EMPLOYEE MANAGEMENT DETAILS
    @Transactional
    public EmployeeResponse updateEmployee(

            String email,

            EmployeeUpdateRequest request
    ) {

        // =====================================================
        // STEP 1: FIND EMPLOYEE USING EMAIL
        // =====================================================

        Employee employee = findEmployeeByEmail(email);


        // =====================================================
        // STEP 2: FIND OR CREATE DEPARTMENT
        // =====================================================

        String departmentName =
                request.getDepartmentName().trim();

        Department department = departmentRepository
                .findByDepartmentName(departmentName)
                .orElseGet(() -> {

                    Department newDepartment =
                            Department.builder()
                                    .departmentName(departmentName)
                                    .build();

                    return departmentRepository.save(
                            newDepartment
                    );
                });


        // =====================================================
        // STEP 3: FIND REPORTING MANAGER USING EMAIL
        // =====================================================

        Employee reportingManager = null;

        if (
                request.getReportingManagerEmail() != null
                        &&
                        !request.getReportingManagerEmail().isBlank()
        ) {

            String managerEmail =
                    request.getReportingManagerEmail().trim();


            // Employee cannot be their own manager
            if (managerEmail.equalsIgnoreCase(email)) {

                throw new BadRequestException(
                        "Employee cannot be their own reporting manager"
                );
            }


            reportingManager = employeeRepository
                    .findByUserEmail(managerEmail)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Reporting manager not found with email: "
                                            + managerEmail
                            )
                    );
        }


        // =====================================================
        // STEP 4: UPDATE EMPLOYEE MANAGEMENT DETAILS
        // =====================================================

        employee.setDepartment(
                department
        );

        employee.setDesignation(
                request.getDesignation().trim()
        );

        employee.setDateOfJoining(
                request.getDateOfJoining()
        );

        employee.setReportingManager(
                reportingManager
        );


        // =====================================================
        // STEP 5: SAVE UPDATED EMPLOYEE
        // =====================================================

        Employee updatedEmployee =
                employeeRepository.save(employee);


        // =====================================================
        // STEP 6: RETURN RESPONSE
        // =====================================================

        return mapToResponse(updatedEmployee);
    }


    // =========================================================
    // GET EMPLOYEE TEAM
    // =========================================================

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeeTeam(
            String managerEmail
    ) {

        Employee manager = findEmployeeByEmail(managerEmail);

        return employeeRepository
                .findByReportingManagerId(
                        manager.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // FIND EMPLOYEE HELPER
    // =========================================================

    private Employee findEmployeeByEmail(
            String email
    ) {

        return employeeRepository
                .findByUserEmail(email.trim())
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

        UUID reportingManagerId = null;
        String reportingManagerName = null;


        // Department details
        if (employee.getDepartment() != null) {

            departmentId =
                    employee.getDepartment().getId();

            departmentName =
                    employee.getDepartment()
                            .getDepartmentName();
        }


        // Reporting Manager details
        if (employee.getReportingManager() != null) {

            reportingManagerId =
                    employee.getReportingManager()
                            .getId();

            reportingManagerName =
                    employee.getReportingManager()
                            .getFirstName()
                            + " "
                            + employee.getReportingManager()
                            .getLastName();
        }


        // Return response
        return EmployeeResponse.builder()

                .id(
                        employee.getId()
                )

                .email(
                        employee.getUser().getEmail()
                )

                .firstName(
                        employee.getFirstName()
                )

                .lastName(
                        employee.getLastName()
                )

                .departmentId(
                        departmentId
                )

                .departmentName(
                        departmentName
                )

                .designation(
                        employee.getDesignation()
                )

                .dateOfJoining(
                        employee.getDateOfJoining()
                )

                .reportingManagerId(
                        reportingManagerId
                )

                .reportingManagerName(
                        reportingManagerName
                )

                .status(
                        employee.getStatus()
                )

                .build();
    }
}