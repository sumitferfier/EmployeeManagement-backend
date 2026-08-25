package com.hrms.hrms.modules.employee.service;

import com.hrms.hrms.common.exception.BadRequestException;
import com.hrms.hrms.common.exception.DuplicateResourceException;
import com.hrms.hrms.common.exception.ResourceNotFoundException;

import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.repository.UserRepository;

import com.hrms.hrms.modules.department.entity.Department;
import com.hrms.hrms.modules.department.repository.DepartmentRepository;

import com.hrms.hrms.modules.employee.dto.EmployeeRequest;
import com.hrms.hrms.modules.employee.dto.EmployeeResponse;

import com.hrms.hrms.modules.employee.entity.Employee;
import com.hrms.hrms.modules.employee.entity.EmployeeStatus;

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
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository
    ) {

        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }


    // =========================================================
    // CREATE EMPLOYEE
    // =========================================================

    @Transactional
    public EmployeeResponse createEmployee(
            EmployeeRequest request
    ) {

        // Check whether employee code already exists.
        if (employeeRepository.existsByEmployeeCode(
                request.getEmployeeCode()
        )) {

            throw new DuplicateResourceException(
                    "Employee code already exists: "
                            + request.getEmployeeCode()
            );
        }


        // Find the User account.
        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: "
                                        + request.getUserId()
                        )
                );


        // One User can have only one Employee profile.
        if (employeeRepository.existsByUserId(
                request.getUserId()
        )) {

            throw new DuplicateResourceException(
                    "Employee already exists for user ID: "
                            + request.getUserId()
            );
        }


        // Find Department.
        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with ID: "
                                        + request.getDepartmentId()
                        )
                );


        // Find reporting manager if provided.
        Employee reportingManager = null;

        if (request.getReportingManagerId() != null) {

            reportingManager = employeeRepository
                    .findById(request.getReportingManagerId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Reporting manager not found with ID: "
                                            + request.getReportingManagerId()
                            )
                    );
        }


        // Convert request status to EmployeeStatus enum.
        EmployeeStatus status = parseEmployeeStatus(
                request.getStatus()
        );


        // Create Employee entity.
        Employee employee = Employee.builder()

                .user(user)

                .employeeCode(
                        request.getEmployeeCode().trim()
                )

                .firstName(
                        request.getFirstName().trim()
                )

                .lastName(
                        request.getLastName().trim()
                )

                .phone(
                        request.getPhone()
                )

                .department(department)

                .designation(
                        request.getDesignation().trim()
                )

                .dateOfJoining(
                        request.getDateOfJoining()
                )

                .reportingManager(
                        reportingManager
                )

                .status(status)

                .build();


        // Save Employee.
        Employee savedEmployee =
                employeeRepository.save(employee);


        // Return response.
        return mapToResponse(savedEmployee);
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
    // GET EMPLOYEE BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(
            UUID id
    ) {

        Employee employee = employeeRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with ID: " + id
                        )
                );

        return mapToResponse(employee);
    }


    // =========================================================
    // GET LOGGED-IN EMPLOYEE PROFILE
    // =========================================================

//    @Transactional(readOnly = true)
//    public EmployeeResponse getMyProfile(String email
//    ) {
//        Employee employee = employeeRepository
//                .findByUserEmail(email)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "Employee profile not found for email: "
//                                        + email
//                        )
//                );
//
//        return mapToResponse(employee);
//    }


    // =========================================================
    // UPDATE EMPLOYEE
    // =========================================================

    @Transactional
    public EmployeeResponse updateEmployee(
            UUID id,
            EmployeeRequest request
    ) {

        // Find existing employee.
        Employee employee = employeeRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with ID: " + id
                        )
                );


        // Check employee code duplication.
        employeeRepository
                .findByEmployeeCode(
                        request.getEmployeeCode()
                )
                .ifPresent(existingEmployee -> {

                    if (!existingEmployee.getId().equals(id)) {

                        throw new DuplicateResourceException(
                                "Employee code already exists: "
                                        + request.getEmployeeCode()
                        );
                    }
                });


        // Find User.
        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: "
                                        + request.getUserId()
                        )
                );


        // Prevent User from being linked to another Employee.
        employeeRepository
                .findByUserId(request.getUserId())
                .ifPresent(existingEmployee -> {

                    if (!existingEmployee.getId().equals(id)) {

                        throw new DuplicateResourceException(
                                "User is already assigned to another employee"
                        );
                    }
                });


        // Find Department.
        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with ID: "
                                        + request.getDepartmentId()
                        )
                );


        // Find reporting manager.
        Employee reportingManager = null;

        if (request.getReportingManagerId() != null) {

            // Employee cannot be their own manager.
            if (request.getReportingManagerId().equals(id)) {

                throw new BadRequestException(
                        "Employee cannot be their own reporting manager"
                );
            }


            reportingManager = employeeRepository
                    .findById(
                            request.getReportingManagerId()
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Reporting manager not found with ID: "
                                            + request.getReportingManagerId()
                            )
                    );
        }


        // Update Employee fields.
        employee.setUser(user);

        employee.setEmployeeCode(
                request.getEmployeeCode().trim()
        );

        employee.setFirstName(
                request.getFirstName().trim()
        );

        employee.setLastName(
                request.getLastName().trim()
        );

        employee.setPhone(
                request.getPhone()
        );

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


        // Update status.
        employee.setStatus(
                parseEmployeeStatus(
                        request.getStatus()
                )
        );


        // Save updated Employee.
        Employee updatedEmployee =
                employeeRepository.save(employee);

        return mapToResponse(updatedEmployee);
    }


    // =========================================================
    // DELETE EMPLOYEE
    // =========================================================

    @Transactional
    public void deleteEmployee(
            UUID id
    ) {

        Employee employee = employeeRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with ID: " + id
                        )
                );


        // Delete Employee profile.
        employeeRepository.delete(employee);
    }


    // =========================================================
    // PARSE EMPLOYEE STATUS
    // =========================================================

    private EmployeeStatus parseEmployeeStatus(
            String status
    ) {

        // Default status.
        if (status == null || status.isBlank()) {

            return EmployeeStatus.ACTIVE;
        }

        try {

            return EmployeeStatus.valueOf(
                    status.trim().toUpperCase()
            );

        } catch (IllegalArgumentException exception) {

            throw new BadRequestException(
                    "Invalid employee status. Allowed values are: "
                            + "ACTIVE, INACTIVE"
            );
        }
    }


    // =========================================================
    // ENTITY → RESPONSE DTO
    // =========================================================

    private EmployeeResponse mapToResponse(
            Employee employee
    ) {

        String reportingManagerName = null;
        UUID reportingManagerId = null;

        UUID departmentId = null;
        String departmentName = null;


        // =====================================================
        // REPORTING MANAGER DETAILS
        // =====================================================

        if (employee.getReportingManager() != null) {

            reportingManagerId =
                    employee.getReportingManager().getId();

            reportingManagerName =
                    employee.getReportingManager().getFirstName()
                            + " "
                            + employee.getReportingManager().getLastName();
        }


        // =====================================================
        // DEPARTMENT DETAILS
        // =====================================================

        /*
         * Department can be null for a newly registered employee
         * before an Admin assigns them to a department.
         */

        if (employee.getDepartment() != null) {

            departmentId =
                    employee.getDepartment().getId();

            departmentName =
                    employee.getDepartment()
                            .getDepartmentName();
        }


        // =====================================================
        // RETURN RESPONSE
        // =====================================================

        return EmployeeResponse.builder()

                .id(
                        employee.getId()
                )

                .userId(
                        employee.getUser().getId()
                )

                .email(
                        employee.getUser().getEmail()
                )

                .employeeCode(
                        employee.getEmployeeCode()
                )

                .firstName(
                        employee.getFirstName()
                )

                .lastName(
                        employee.getLastName()
                )

                .phone(
                        employee.getPhone()
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