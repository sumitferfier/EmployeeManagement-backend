package com.hrms.hrms.modules.role.service;

import com.hrms.hrms.common.exception.ResourceNotFoundException;
import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.repository.UserRepository;
import com.hrms.hrms.modules.employee.entity.Employee;
import com.hrms.hrms.modules.employee.entity.EmployeeStatus;
import com.hrms.hrms.modules.employee.repository.EmployeeRepository;
import com.hrms.hrms.modules.role.dto.RoleResponse;
import com.hrms.hrms.modules.role.dto.UserAccessRequest;
import com.hrms.hrms.modules.role.dto.UserAccessResponse;
import com.hrms.hrms.modules.role.entity.Role;
import com.hrms.hrms.modules.role.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;

    public RoleService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmployeeRepository employeeRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
    }

    // =========================================================
    // UPDATE USER ACCESS
    // =========================================================

    @Transactional
    public UserAccessResponse updateUserAccess(
            String email,
            UserAccessRequest request
    ) {

        // =====================================================
        // STEP 1: FIND USER
        // =====================================================

        String normalizedEmail = email
                .trim()
                .toLowerCase();

        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        // =====================================================
        // STEP 2: FIND OR CREATE ROLE
        // =====================================================

        Role role = roleRepository
                .findByUserId(user.getId())
                .orElseGet(() -> {

                    Role newRole = Role.builder()
                            .user(user)
                            .email(user.getEmail())
                            .isAdmin(false)
                            .isEmployee(false)
                            .build();

                    return roleRepository.save(newRole);
                });

        // =====================================================
        // STEP 3: UPDATE ACCESS
        // =====================================================

        boolean oldEmployeeAccess = role.isEmployee();

        role.setAdmin(request.getIsAdmin());
        role.setEmployee(request.getIsEmployee());

        // Keep role email synchronized with user email
        role.setEmail(user.getEmail());

        Role updatedRole = roleRepository.save(role);

        // =====================================================
        // STEP 4: CREATE EMPLOYEE PROFILE
        // =====================================================

        /*
         * If the user is being given Employee access for
         * the first time, create an Employee record.
         */
        if (!oldEmployeeAccess && updatedRole.isEmployee()) {

            createEmployeeProfileIfNotExists(user);
        }

        // =====================================================
        // STEP 5: RETURN RESPONSE
        // =====================================================

        return UserAccessResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .isAdmin(updatedRole.isAdmin())
                .isEmployee(updatedRole.isEmployee())
                .status(user.getStatus().name())
                .message("User access updated successfully")
                .build();
    }

    // =========================================================
    // CREATE EMPLOYEE PROFILE
    // =========================================================

    private void createEmployeeProfileIfNotExists(User user) {

        boolean employeeExists =
                employeeRepository
                        .findByUser_Email(user.getEmail())
                        .isPresent();

        if (employeeExists) {
            return;
        }

        /*
         * Copy basic information from User to Employee.
         *
         * User:
         * firstName
         * lastName
         *
         * Employee:
         * firstName
         * lastName
         */

        Employee employee = Employee.builder()
                .user(user)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .status(EmployeeStatus.ACTIVE)
                .build();

        employeeRepository.save(employee);
    }

    // GET ALL ROLES / ACCESS RECORDS
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET ROLE BY ID
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with ID: " + id
                        )
                );

        return mapToResponse(role);
    }

    // =========================================================
    // MAP ROLE ENTITY → ROLE RESPONSE
    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .userId(role.getUser().getId())
                .email(role.getEmail())
                .isAdmin(role.isAdmin())
                .isEmployee(role.isEmployee())
                .build();
    }
}