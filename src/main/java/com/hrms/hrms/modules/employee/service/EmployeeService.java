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

        // =====================================================
        // STEP 1: CHECK EMPLOYEE CODE
        // =====================================================

        if (employeeRepository.existsByEmployeeCode(
                request.getEmployeeCode()
        )) {

            throw new DuplicateResourceException(
                    "Employee code already exists: "
                            + request.getEmployeeCode()
            );
        }


        // =====================================================
        // STEP 2: FIND USER
        // =====================================================

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: "
                                        + request.getUserId()
                        )
                );


        // =====================================================
        // STEP 3: CHECK WHETHER USER ALREADY HAS EMPLOYEE
        // =====================================================

        if (employeeRepository.existsByUserId(
                request.getUserId()
        )) {

            throw new DuplicateResourceException(
                    "Employee already exists for user ID: "
                            + request.getUserId()
            );
        }


        // =====================================================
        // STEP 4: FIND DEPARTMENT
        // =====================================================

        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with ID: "
                                        + request.getDepartmentId()
                        )
                );


        // =====================================================
        // STEP 5: FIND REPORTING MANAGER
        // =====================================================

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


        // =====================================================
        // STEP 6: DETERMINE EMPLOYEE STATUS
        // =====================================================

        EmployeeStatus status = parseEmployeeStatus(
                request.getStatus()
        );


        // =====================================================
        // STEP 7: CREATE EMPLOYEE ENTITY
        // =====================================================

        Employee employee = Employee.builder()

                // Linked User account
                .user(user)

                // Employee information
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

                // Department
                .department(department)

                // Job information
                .designation(
                        request.getDesignation().trim()
                )

                .dateOfJoining(
                        request.getDateOfJoining()
                )

                // Reporting manager
                .reportingManager(
                        reportingManager
                )

                // Employment status
                .status(
                        status
                )

                .build();


        // =====================================================
        // STEP 8: SAVE EMPLOYEE
        // =====================================================

        Employee savedEmployee =
                employeeRepository.save(employee);


        // =====================================================
        // STEP 9: RETURN RESPONSE
        // =====================================================

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
    // UPDATE EMPLOYEE
    // =========================================================

    @Transactional
    public EmployeeResponse updateEmployee(
            UUID id,
            EmployeeRequest request
    ) {

        // =====================================================
        // STEP 1: FIND EXISTING EMPLOYEE
        // =====================================================

        Employee employee = employeeRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with ID: " + id
                        )
                );


        // =====================================================
        // STEP 2: CHECK EMPLOYEE CODE
        // =====================================================

        employeeRepository
                .findByEmployeeCode(
                        request.getEmployeeCode()
                )
                .ifPresent(existingEmployee -> {

                    // Allow the same employee to keep their code.
                    if (!existingEmployee.getId().equals(id)) {

                        throw new DuplicateResourceException(
                                "Employee code already exists: "
                                        + request.getEmployeeCode()
                        );
                    }
                });


        // =====================================================
        // STEP 3: FIND USER
        // =====================================================

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: "
                                        + request.getUserId()
                        )
                );


        // =====================================================
        // STEP 4: PREVENT USER FROM BELONGING TO
        // ANOTHER EMPLOYEE
        // =====================================================

        employeeRepository
                .findByUserId(request.getUserId())
                .ifPresent(existingEmployee -> {

                    // If the User belongs to a different Employee,
                    // the update is not allowed.
                    if (!existingEmployee.getId().equals(id)) {

                        throw new DuplicateResourceException(
                                "User is already assigned to another employee"
                        );
                    }
                });


        // =====================================================
        // STEP 5: FIND DEPARTMENT
        // =====================================================

        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with ID: "
                                        + request.getDepartmentId()
                        )
                );


        // =====================================================
        // STEP 6: FIND REPORTING MANAGER
        // =====================================================

        Employee reportingManager = null;

        if (request.getReportingManagerId() != null) {

            // Employee cannot report to themselves.
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


        // =====================================================
        // STEP 7: UPDATE EMPLOYEE INFORMATION
        // =====================================================

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


        // =====================================================
        // STEP 8: UPDATE STATUS
        // =====================================================

        employee.setStatus(
                parseEmployeeStatus(
                        request.getStatus()
                )
        );


        // =====================================================
        // STEP 9: SAVE UPDATED EMPLOYEE
        // =====================================================

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

        // Find employee first.
        Employee employee = employeeRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with ID: " + id
                        )
                );


        // Delete employee profile.
        employeeRepository.delete(employee);
    }


    // =========================================================
    // PARSE EMPLOYEE STATUS
    // =========================================================

    /*
     * Converts String status from the request
     * into EmployeeStatus enum.
     */

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
        // RETURN RESPONSE
        // =====================================================

        return EmployeeResponse.builder()

                // Employee
                .id(
                        employee.getId()
                )


                // User account
                .userId(
                        employee.getUser().getId()
                )

                .email(
                        employee.getUser().getEmail()
                )


                // Employee details
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


                // Department
                .departmentId(
                        employee.getDepartment().getId()
                )

                .departmentName(
                        employee.getDepartment()
                                .getDepartmentName()
                )


                // Job information
                .designation(
                        employee.getDesignation()
                )

                .dateOfJoining(
                        employee.getDateOfJoining()
                )


                // Reporting manager
                .reportingManagerId(
                        reportingManagerId
                )

                .reportingManagerName(
                        reportingManagerName
                )


                // Employee status
                .status(
                        employee.getStatus()
                )

                .build();
    }
}