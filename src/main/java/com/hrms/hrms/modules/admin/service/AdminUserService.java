package com.hrms.hrms.modules.admin.service;

import com.hrms.hrms.common.exception.BadRequestException;
import com.hrms.hrms.common.exception.ResourceNotFoundException;
import com.hrms.hrms.modules.admin.dto.UserAccessRequest;
import com.hrms.hrms.modules.admin.dto.UserAccessResponse;
import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.repository.UserRepository;
import com.hrms.hrms.modules.employee.entity.Employee;
import com.hrms.hrms.modules.employee.entity.EmployeeStatus;
import com.hrms.hrms.modules.employee.repository.EmployeeRepository;
import com.hrms.hrms.modules.role.entity.Role;
import com.hrms.hrms.modules.role.repository.RoleRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;

    public AdminUserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmployeeRepository employeeRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
    }

    // =========================================================
    // GET ALL REGISTERED USERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<UserAccessResponse> getAllUsers() {

        return userRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    @Transactional(readOnly = true)
    public UserAccessResponse getUserByEmail(String email) {

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

        return mapToResponse(user);
    }

    // =========================================================
    // UPDATE USER ACCESS BY EMAIL
    // =========================================================

    @Transactional
    public UserAccessResponse updateUserAccess(
            String email,
            UserAccessRequest request
    ) {

        // =====================================================
        // STEP 1: GET LOGGED-IN ADMIN
        // =====================================================

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String loggedInAdminEmail =
                authentication.getName();

        // =====================================================
        // STEP 2: PREVENT ADMIN FROM CHANGING OWN ACCESS
        // =====================================================

        if (loggedInAdminEmail.equalsIgnoreCase(email)) {

            throw new BadRequestException(
                    "You cannot change your own access permissions."
            );
        }

        // =====================================================
        // STEP 3: FIND TARGET USER
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
        // STEP 4: FIND USER'S ROLE
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
        // STEP 5: SAVE OLD EMPLOYEE ACCESS
        // =====================================================

        boolean oldEmployeeAccess = role.isEmployee();

        // =====================================================
        // STEP 6: UPDATE ROLE ACCESS
        // =====================================================

        role.setAdmin(request.getIsAdmin());
        role.setEmployee(request.getIsEmployee());

        // Keep role email synchronized with User email
        role.setEmail(user.getEmail());

        Role updatedRole =
                roleRepository.save(role);

        // =====================================================
        // STEP 7: CREATE EMPLOYEE IF ACCESS IS GRANTED
        // =====================================================

        if (!oldEmployeeAccess && updatedRole.isEmployee()) {

            createEmployeeProfileIfNotExists(user);
        }

        // =====================================================
        // STEP 8: RETURN RESPONSE
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

        Employee employee = Employee.builder()
                .user(user)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .status(EmployeeStatus.ACTIVE)
                .build();

        employeeRepository.save(employee);
    }

    // =========================================================
    // ENTITY → RESPONSE DTO
    // =========================================================

    private UserAccessResponse mapToResponse(User user) {

        Role role = roleRepository
                .findByUserId(user.getId())
                .orElse(null);

        return UserAccessResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .isAdmin(role != null && role.isAdmin())
                .isEmployee(role != null && role.isEmployee())
                .status(user.getStatus().name())
                .build();
    }
}