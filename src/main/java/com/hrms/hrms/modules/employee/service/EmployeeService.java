package com.hrms.hrms.modules.employee.service;

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
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository
    ) {

        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    // CREATE EMPLOYEE
    @Transactional
    public EmployeeResponse createEmployee(
            EmployeeRequest request
    ) {
        // Check employee code
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new DuplicateResourceException("Employee code already exists: " + request.getEmployeeCode());
        }

        // Find User
        User user = userRepository.findById(request.getUserId()).orElseThrow(() ->
                new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // Check whether User already has Employee
        if (employeeRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateResourceException("Employee already exists for user ID: " + request.getUserId());
        }

        // Find Department
        Department department = departmentRepository.findById(request.getDepartmentId()).orElseThrow(() ->
                        new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));


        // Find Reporting Manager
        Employee reportingManager = null;
        if (request.getReportingManagerId() != null) {
            reportingManager = employeeRepository.findById(request.getReportingManagerId()).orElseThrow(() ->
                            new ResourceNotFoundException("Reporting manager not found with ID: " + request.getReportingManagerId()));
        }

        // Determine Employee Status
        EmployeeStatus status = EmployeeStatus.ACTIVE;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            status = EmployeeStatus.valueOf(request.getStatus().trim().toUpperCase());
        }

        // Create Employee Entity
        Employee employee = Employee.builder()
                .user(user)
                .employeeCode(request.getEmployeeCode().trim())
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phone(request.getPhone())
                .department(department)
                .designation(request.getDesignation().trim())
                .dateOfJoining(request.getDateOfJoining())
                .reportingManager(reportingManager)
                .status(status)
                .build();

        // Save Employee
        Employee savedEmployee = employeeRepository.save(employee);

        // Convert Entity → Response DTO
        return mapToResponse(savedEmployee);
    }

    // GET ALL EMPLOYEES
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET EMPLOYEE BY ID
    public EmployeeResponse getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() ->
                                new ResourceNotFoundException("Employee not found with ID: " + id));
        return mapToResponse(employee);
    }

    // UPDATE EMPLOYEE
    @Transactional
    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request) {

        // Find existing employee
        Employee employee = employeeRepository.findById(id).orElseThrow(() ->
                                new ResourceNotFoundException("Employee not found with ID: " + id));

        // Check employee code
        employeeRepository.findByEmployeeCode(request.getEmployeeCode()).ifPresent(existingEmployee -> {
            if (!existingEmployee.getId().equals(id)) {
                throw new DuplicateResourceException("Employee code already exists: " + request.getEmployeeCode());
            }
        });

        // Find User
        User user = userRepository.findById(request.getUserId()).orElseThrow(() ->
                        new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // Find Department
        Department department = departmentRepository.findById(request.getDepartmentId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        // Find Reporting Manager
        Employee reportingManager = null;
        if (request.getReportingManagerId() != null) {

            // Prevent employee from reporting to itself
            if (request.getReportingManagerId().equals(id)) {
                throw new IllegalArgumentException("Employee cannot be their own reporting manager");
            }
            reportingManager = employeeRepository.findById(request.getReportingManagerId()).orElseThrow(() ->
                            new ResourceNotFoundException("Reporting manager not found with ID: " + request.getReportingManagerId()));
        }

        // Update fields
        employee.setUser(user);
        employee.setEmployeeCode(request.getEmployeeCode().trim());
        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setPhone(request.getPhone());
        employee.setDepartment(department);
        employee.setDesignation(request.getDesignation().trim());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setReportingManager(reportingManager);

        // Update status
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            employee.setStatus(EmployeeStatus.valueOf(request.getStatus().trim().toUpperCase()));
        }

        // Save updated employee
        Employee updatedEmployee = employeeRepository.save(employee);
        return mapToResponse(updatedEmployee);
    }

    // DELETE EMPLOYEE
    @Transactional
    public void deleteEmployee(UUID id) {

        // Find employee first
        Employee employee = employeeRepository.findById(id).orElseThrow(() ->
                                new ResourceNotFoundException("Employee not found with ID: " + id));

        // Delete employee
        employeeRepository.delete(employee);
    }

    // ENTITY → RESPONSE DTO
    private EmployeeResponse mapToResponse(Employee employee) {
        String reportingManagerName = null;
        UUID reportingManagerId = null;

        // Check whether reporting manager exists
        if (employee.getReportingManager() != null) {
            reportingManagerId = employee.getReportingManager().getId();
            reportingManagerName = employee.getReportingManager()
                            .getFirstName()
                            + " "
                            + employee.getReportingManager()
                            .getLastName();
        }
        return EmployeeResponse.builder()
                .id(employee.getId())
                .userId(employee.getUser().getId())
                .email(employee.getUser().getEmail())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .phone(employee.getPhone())
                .departmentId(employee.getDepartment().getId())
                .departmentName(employee.getDepartment().getDepartmentName())
                .designation(employee.getDesignation())
                .dateOfJoining(employee.getDateOfJoining())
                .reportingManagerId(reportingManagerId)
                .reportingManagerName(reportingManagerName)
                .status(employee.getStatus())
                .build();
    }
}