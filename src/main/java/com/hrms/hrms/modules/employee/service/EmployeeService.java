package com.hrms.hrms.modules.employee.service;

import com.hrms.hrms.common.exception.BadRequestException;
import com.hrms.hrms.common.exception.ResourceNotFoundException;
import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.repository.UserRepository;
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
    private final UserRepository userRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    /*
     * ============================================================
     * GET ALL USERS
     * ============================================================
     *
     * This method returns EVERY registered user.
     *
     * It does NOT depend on the employees table.
     *
     * If a user has an Employee record:
     *      employee-specific information is returned.
     *
     * If a user does NOT have an Employee record:
     *      user information is still returned.
     *      employee-specific fields will be null.
     */
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {

        return userRepository.findAll()
                .stream()
                .map(this::mapUserToResponse)
                .toList();
    }

    /*
     * ============================================================
     * GET USER / EMPLOYEE BY EMAIL
     * ============================================================
     *
     * This also starts from the users table.
     *
     * Therefore even a registered user without an Employee
     * profile can be returned.
     */
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByEmail(String email) {

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return mapUserToResponse(user);
    }

    /*
     * ============================================================
     * GET LOGGED-IN USER PROFILE
     * ============================================================
     */
    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile(String email) {

        return getEmployeeByEmail(email);
    }

    /*
     * ============================================================
     * UPDATE EMPLOYEE
     * ============================================================
     *
     * This operation is only possible when an Employee record
     * already exists.
     *
     * A normal registered user without an Employee profile
     * cannot have employee-specific information updated through
     * this method.
     */
    @Transactional
    public EmployeeResponse updateEmployee(
            String email,
            EmployeeUpdateRequest request
    ) {

        Employee employee = findEmployeeByEmail(email);

        /*
         * --------------------------------------------------------
         * UPDATE DEPARTMENT
         * --------------------------------------------------------
         */
        if (request.getDepartmentName() != null
                && !request.getDepartmentName().isBlank()) {

            String departmentName =
                    request.getDepartmentName().trim();

            Department department =
                    departmentRepository
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

            employee.setDepartment(department);
        }

        /*
         * --------------------------------------------------------
         * UPDATE DESIGNATION
         * --------------------------------------------------------
         */
        if (request.getDesignation() != null
                && !request.getDesignation().isBlank()) {

            employee.setDesignation(
                    request.getDesignation().trim()
            );
        }

        /*
         * --------------------------------------------------------
         * UPDATE DATE OF JOINING
         * --------------------------------------------------------
         */
        if (request.getDateOfJoining() != null) {

            employee.setDateOfJoining(
                    request.getDateOfJoining()
            );
        }

        /*
         * --------------------------------------------------------
         * UPDATE REPORTING MANAGER
         * --------------------------------------------------------
         */
        if (request.getReportingManagerEmail() != null) {

            String managerEmail =
                    request.getReportingManagerEmail()
                            .trim()
                            .toLowerCase();

            /*
             * Empty value means remove reporting manager.
             */
            if (managerEmail.isBlank()) {

                employee.setReportingManagerEmail(null);

            } else {

                /*
                 * Employee cannot report to themselves.
                 */
                if (managerEmail.equalsIgnoreCase(
                        email.trim()
                )) {

                    throw new BadRequestException(
                            "Employee cannot be their own reporting manager"
                    );
                }

                /*
                 * Reporting manager must have an Employee profile.
                 */
                Employee manager =
                        employeeRepository
                                .findByUser_Email(managerEmail)
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Reporting manager not found with email: "
                                                        + managerEmail
                                        )
                                );

                String actualManagerEmail =
                        manager.getUser()
                                .getEmail()
                                .trim()
                                .toLowerCase();

                employee.setReportingManagerEmail(
                        actualManagerEmail
                );
            }
        }

        /*
         * Save updated employee.
         */
        Employee updatedEmployee =
                employeeRepository.save(employee);

        return mapEmployeeToResponse(updatedEmployee);
    }

    /*
     * ============================================================
     * GET EMPLOYEE TEAM
     * ============================================================
     */
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeeTeam(
            String managerEmail
    ) {

        return employeeRepository
                .findByReportingManagerEmail(
                        managerEmail.trim().toLowerCase()
                )
                .stream()
                .map(this::mapEmployeeToResponse)
                .toList();
    }

    /*
     * ============================================================
     * FIND EMPLOYEE BY EMAIL
     * ============================================================
     *
     * Used for operations that specifically require an
     * Employee record.
     */
    private Employee findEmployeeByEmail(String email) {

        return employeeRepository
                .findByUser_Email(
                        email.trim().toLowerCase()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with email: "
                                        + email
                        )
                );
    }

    /*
     * ============================================================
     * MAP USER TO RESPONSE
     * ============================================================
     *
     * This is the most important new method.
     *
     * We ALWAYS have a User.
     *
     * Employee may or may not exist.
     */
    private EmployeeResponse mapUserToResponse(User user) {

        /*
         * Try to find the Employee profile.
         */
        Employee employee =
                employeeRepository
                        .findByUser_Email(user.getEmail())
                        .orElse(null);

        /*
         * If Employee profile does NOT exist,
         * return the User information only.
         */
        if (employee == null) {

            return EmployeeResponse.builder()
                    .id(null)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .departmentId(null)
                    .departmentName(null)
                    .designation(null)
                    .dateOfJoining(null)
                    .reportingManagerEmail(null)
                    .status(null)
                    .build();
        }

        /*
         * Employee profile exists.
         * Return both User and Employee information.
         */
        return mapEmployeeToResponse(employee);
    }

    /*
     * ============================================================
     * MAP EMPLOYEE TO RESPONSE
     * ============================================================
     */
    private EmployeeResponse mapEmployeeToResponse(
            Employee employee
    ) {

        UUID departmentId = null;
        String departmentName = null;

        /*
         * Get department information if available.
         */
        if (employee.getDepartment() != null) {

            departmentId =
                    employee.getDepartment().getId();

            departmentName =
                    employee.getDepartment()
                            .getDepartmentName();
        }

        return EmployeeResponse.builder()
                .id(employee.getId())
                .userId(employee.getUser().getId())
                .email(employee.getUser().getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .departmentId(departmentId)
                .departmentName(departmentName)
                .designation(employee.getDesignation())
                .dateOfJoining(employee.getDateOfJoining())
                .reportingManagerEmail(
                        employee.getReportingManagerEmail()
                )
                .status(employee.getStatus())
                .build();
    }
}