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

    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

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

    /*
     * Returns all users who have registered.
     *
     * This gets data from the users table.
     *
     * It also includes their current access:
     *
     * - isAdmin
     * - isEmployee
     */

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
    public UserAccessResponse getUserByEmail(
            String email
    ) {

        // -----------------------------------------------------
        // NORMALIZE EMAIL
        // -----------------------------------------------------

        String normalizedEmail =
                email
                        .trim()
                        .toLowerCase();


        // -----------------------------------------------------
        // FIND USER
        // -----------------------------------------------------

        User user =
                userRepository
                        .findByEmail(normalizedEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with email: "
                                                + email
                                )
                        );


        // -----------------------------------------------------
        // RETURN USER + ACCESS
        // -----------------------------------------------------

        return mapToResponse(user);
    }


    // =========================================================
    // UPDATE USER ACCESS BY EMAIL
    // =========================================================

    /*
     * Admin uses this API to give or remove access.
     *
     * Example:
     *
     * PATCH
     * /api/v1/admin/users/access?email=kartik@gmail.com
     *
     * Request:
     *
     * {
     *     "isAdmin": false,
     *     "isEmployee": true
     * }
     *
     *
     * Possible access combinations:
     *
     * 1. false + false
     *    → Registered user / View only
     *
     * 2. true + false
     *    → Admin only
     *
     * 3. false + true
     *    → Employee only
     *
     * 4. true + true
     *    → Admin + Employee
     */

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

        /*
         * Admin should not be able to accidentally remove
         * their own ADMIN access.
         */

        if (
                loggedInAdminEmail
                        .equalsIgnoreCase(email)
        ) {

            throw new BadRequestException(
                    "You cannot change your own access permissions."
            );
        }


        // =====================================================
        // STEP 3: NORMALIZE TARGET EMAIL
        // =====================================================

        String normalizedEmail =
                email
                        .trim()
                        .toLowerCase();


        // =====================================================
        // STEP 4: FIND TARGET USER
        // =====================================================

        User user =
                userRepository
                        .findByEmail(normalizedEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with email: "
                                                + email
                                )
                        );


        // =====================================================
        // STEP 5: FIND USER ROLE
        // =====================================================

        /*
         * Every registered user should normally already have
         * a Role record.
         *
         * But if the Role record is missing for some reason,
         * create it here.
         */

        Role role =
                roleRepository
                        .findByUserId(user.getId())
                        .orElseGet(() -> {

                            Role newRole =
                                    Role.builder()
                                            .user(user)
                                            .email(user.getEmail())
                                            .isAdmin(false)
                                            .isEmployee(false)
                                            .build();

                            return roleRepository.save(newRole);
                        });


        // =====================================================
        // STEP 6: UPDATE ROLE ACCESS
        // =====================================================

        /*
         * Update ADMIN access.
         */

        role.setAdmin(
                request.getIsAdmin()
        );


        /*
         * Update EMPLOYEE access.
         */

        role.setEmployee(
                request.getIsEmployee()
        );


        /*
         * Keep role email synchronized with User email.
         */

        role.setEmail(
                user.getEmail()
        );


        // =====================================================
        // STEP 7: SAVE ROLE
        // =====================================================

        Role updatedRole =
                roleRepository.save(role);


        // =====================================================
        // STEP 8: CREATE EMPLOYEE PROFILE
        // =====================================================

        /*
         * IMPORTANT:
         *
         * If user has EMPLOYEE access,
         * an Employee record must exist.
         *
         * We check this EVERY TIME isEmployee is true.
         *
         * This also repairs old/inconsistent data where:
         *
         * roles.is_employee = true
         *
         * but
         *
         * employees record is missing.
         */

        if (updatedRole.isEmployee()) {

            createEmployeeProfileIfNotExists(
                    user
            );
        }


        // =====================================================
        // STEP 9: RETURN RESPONSE
        // =====================================================

        return UserAccessResponse.builder()

                .userId(
                        user.getId()
                )

                .email(
                        user.getEmail()
                )

                .isAdmin(
                        updatedRole.isAdmin()
                )

                .isEmployee(
                        updatedRole.isEmployee()
                )

                .status(
                        user.getStatus().name()
                )

                .message(
                        "User access updated successfully"
                )

                .build();
    }


    // =========================================================
    // CREATE EMPLOYEE PROFILE IF NOT EXISTS
    // =========================================================

    /*
     * Creates an Employee record when a user receives
     * EMPLOYEE access.
     *
     * First Name and Last Name are copied from users.
     *
     * Admin can later update:
     *
     * - Phone
     * - Department
     * - Designation
     * - Date Of Joining
     * - Reporting Manager
     */

    private void createEmployeeProfileIfNotExists(
            User user
    ) {

        // =====================================================
        // STEP 1: CHECK WHETHER EMPLOYEE ALREADY EXISTS
        // =====================================================

        boolean employeeExists =
                employeeRepository
                        .findByUser_Email(
                                user.getEmail()
                        )
                        .isPresent();


        // =====================================================
        // STEP 2: IF EXISTS → DO NOTHING
        // =====================================================

        if (employeeExists) {
            return;
        }


        // =====================================================
        // STEP 3: CREATE EMPLOYEE
        // =====================================================

        Employee employee =
                Employee.builder()

                        // Link Employee with User
                        .user(user)

                        // Copy name from User
                        .firstName(
                                user.getFirstName()
                        )

                        .lastName(
                                user.getLastName()
                        )

                        // Default employee status
                        .status(
                                EmployeeStatus.ACTIVE
                        )

                        .build();


        // =====================================================
        // STEP 4: SAVE EMPLOYEE
        // =====================================================

        employeeRepository.save(
                employee
        );
    }


    // =========================================================
    // ENTITY → RESPONSE DTO
    // =========================================================

    /*
     * Converts User entity into UserAccessResponse.
     *
     * User information comes from users table.
     *
     * Access information comes from roles table.
     */

    private UserAccessResponse mapToResponse(
            User user
    ) {

        // -----------------------------------------------------
        // FIND ROLE
        // -----------------------------------------------------

        Role role =
                roleRepository
                        .findByUserId(
                                user.getId()
                        )
                        .orElse(null);


        // -----------------------------------------------------
        // BUILD RESPONSE
        // -----------------------------------------------------

        return UserAccessResponse.builder()

                .userId(
                        user.getId()
                )

                .email(
                        user.getEmail()
                )

                .isAdmin(
                        role != null
                                && role.isAdmin()
                )

                .isEmployee(
                        role != null
                                && role.isEmployee()
                )

                .status(
                        user.getStatus().name()
                )

                .build();
    }
}