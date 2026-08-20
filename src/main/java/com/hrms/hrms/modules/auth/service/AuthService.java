package com.hrms.hrms.modules.auth.service;


// =========================================================
// COMMON EXCEPTIONS
// =========================================================

import com.hrms.hrms.common.exception.DuplicateResourceException;
import com.hrms.hrms.common.exception.ResourceNotFoundException;
import com.hrms.hrms.common.exception.BadRequestException;


// =========================================================
// AUTH DTO
// =========================================================

import com.hrms.hrms.modules.auth.dto.LoginRequest;
import com.hrms.hrms.modules.auth.dto.LoginResponse;
import com.hrms.hrms.modules.auth.dto.RegisterRequest;
import com.hrms.hrms.modules.auth.dto.RegisterResponse;


// =========================================================
// AUTH ENTITY
// =========================================================

import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.entity.UserStatus;


// =========================================================
// AUTH REPOSITORY
// =========================================================

import com.hrms.hrms.modules.auth.repository.UserRepository;


// =========================================================
// ROLE
// =========================================================

import com.hrms.hrms.modules.role.entity.Role;
import com.hrms.hrms.modules.role.repository.RoleRepository;


// =========================================================
// EMPLOYEE
// =========================================================

import com.hrms.hrms.modules.employee.entity.Employee;
import com.hrms.hrms.modules.employee.entity.EmployeeStatus;
import com.hrms.hrms.modules.employee.repository.EmployeeRepository;


// =========================================================
// SECURITY
// =========================================================

import com.hrms.hrms.security.JwtTokenProvider;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class AuthService {


    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final EmployeeRepository employeeRepository;

    private final JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AuthService(

            AuthenticationManager authenticationManager,

            UserRepository userRepository,

            RoleRepository roleRepository,

            EmployeeRepository employeeRepository,

            JwtTokenProvider jwtTokenProvider,

            PasswordEncoder passwordEncoder
    ) {

        this.authenticationManager =
                authenticationManager;

        this.userRepository =
                userRepository;

        this.roleRepository =
                roleRepository;

        this.employeeRepository =
                employeeRepository;

        this.jwtTokenProvider =
                jwtTokenProvider;

        this.passwordEncoder =
                passwordEncoder;
    }


    // =========================================================
    // USER REGISTRATION
    // =========================================================

    /*
     * PUBLIC SIGNUP FLOW
     *
     * Frontend sends:
     *
     * First Name
     * Last Name
     * Email
     * Password
     *
     *
     * Backend performs:
     *
     * 1. Check email
     * 2. Get EMPLOYEE role
     * 3. Create User
     * 4. Encrypt password
     * 5. Save User
     * 6. Create Employee profile
     * 7. Link Employee → User
     * 8. Save Employee
     */

    @Transactional
    public RegisterResponse register(
            RegisterRequest request
    ) {


        // =====================================================
        // STEP 1: CHECK EMAIL
        // =====================================================

        if (userRepository.existsByEmail(
                request.getEmail()
        )) {

            throw new DuplicateResourceException(

                    "Email already exists: "
                            + request.getEmail()
            );
        }


        // =====================================================
        // STEP 2: GET DEFAULT EMPLOYEE ROLE
        // =====================================================

        /*
         * Public registration can only create EMPLOYEE users.
         *
         * ADMIN users cannot be created through
         * the public register API.
         */

        Role employeeRole = roleRepository

                .findByRoleName("EMPLOYEE")

                .orElseThrow(() ->

                        new ResourceNotFoundException(

                                "Default EMPLOYEE role not found"
                        )
                );


        // =====================================================
        // STEP 3: CREATE USER ACCOUNT
        // =====================================================

        User user = User.builder()

                // Email is login identity
                .email(
                        request.getEmail()
                )

                // Encrypt password before saving
                .password(

                        passwordEncoder.encode(

                                request.getPassword()
                        )
                )

                // Automatically assign EMPLOYEE role
                .role(
                        employeeRole
                )

                // Account starts as ACTIVE
                .status(
                        UserStatus.ACTIVE
                )

                .build();


        // =====================================================
        // STEP 4: SAVE USER
        // =====================================================

        /*
         * Save User first.
         *
         * We need User ID because Employee
         * will be linked with this User.
         */

        User savedUser =
                userRepository.save(user);


        // =====================================================
        // STEP 5: CREATE EMPLOYEE PROFILE
        // =====================================================

        Employee employee = Employee.builder()


                // Link employee with user account
                .user(
                        savedUser
                )


                // Name comes from signup form
                .firstName(
                        request.getFirstName()
                )

                .lastName(
                        request.getLastName()
                )


                /*
                 * Employee information below
                 * will be assigned later by Admin.
                 */

                .employeeCode(
                        null
                )

                .department(
                        null
                )

                .designation(
                        null
                )

                .dateOfJoining(
                        null
                )

                .reportingManager(
                        null
                )


                // Employee starts active
                .status(
                        EmployeeStatus.ACTIVE
                )

                .build();


        // =====================================================
        // STEP 6: SAVE EMPLOYEE
        // =====================================================

        Employee savedEmployee =
                employeeRepository.save(employee);


        // =====================================================
        // STEP 7: RETURN RESPONSE
        // =====================================================

        return RegisterResponse.builder()

                // User account UUID
                .userId(
                        savedUser.getId()
                )


                // Employee profile UUID
                .employeeId(
                        savedEmployee.getId()
                )


                // Employee information
                .firstName(
                        savedEmployee.getFirstName()
                )

                .lastName(
                        savedEmployee.getLastName()
                )


                // Login email
                .email(
                        savedUser.getEmail()
                )


                // Assigned role
                .role(

                        savedUser.getRole()
                                .getRoleName()
                )


                // Account status
                .status(

                        savedUser.getStatus()
                                .name()
                )


                // Success message
                .message(

                        "Employee registered successfully"
                )

                .build();
    }


    // =========================================================
    // USER LOGIN
    // =========================================================

    @Transactional
    public LoginResponse login(
            LoginRequest request
    ) {


        // =====================================================
        // STEP 1: AUTHENTICATE EMAIL AND PASSWORD
        // =====================================================

        authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(

                        request.getEmail(),

                        request.getPassword()
                )
        );


        // =====================================================
        // STEP 2: GET USER FROM DATABASE
        // =====================================================

        User user = userRepository

                .findByEmail(
                        request.getEmail()
                )

                .orElseThrow(() ->

                        new ResourceNotFoundException(

                                "User not found"
                        )
                );


        // =====================================================
        // STEP 3: CHECK ACCOUNT STATUS
        // =====================================================

        /*
         * Inactive users cannot login.
         */

        if (user.getStatus()
                == UserStatus.INACTIVE) {

            throw new BadRequestException(

                    "Your account is inactive. "
                            + "Please contact the administrator."
            );
        }


        // =====================================================
        // STEP 4: UPDATE LAST LOGIN
        // =====================================================

        user.setLastLogin(
                LocalDateTime.now()
        );

        userRepository.save(user);


        // =====================================================
        // STEP 5: GENERATE JWT
        // =====================================================

        /*
         * JWT contains:
         *
         * Subject → Email
         * Claim   → Role
         */

        String token =
                jwtTokenProvider.generateToken(

                        user.getEmail(),

                        user.getRole()
                                .getRoleName()
                );


        // =====================================================
        // STEP 6: RETURN LOGIN RESPONSE
        // =====================================================

        return LoginResponse.builder()

                .token(
                        token
                )

                .tokenType(
                        "Bearer"
                )

                .userId(
                        user.getId()
                )

                .email(
                        user.getEmail()
                )

                .role(

                        user.getRole()
                                .getRoleName()
                )

                .build();
    }
}