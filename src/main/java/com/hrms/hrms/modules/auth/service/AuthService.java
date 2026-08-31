package com.hrms.hrms.modules.auth.service;

import com.hrms.hrms.common.exception.BadRequestException;
import com.hrms.hrms.common.exception.DuplicateResourceException;
import com.hrms.hrms.common.exception.ResourceNotFoundException;
import com.hrms.hrms.modules.auth.dto.LoginRequest;
import com.hrms.hrms.modules.auth.dto.LoginResponse;
import com.hrms.hrms.modules.auth.dto.RegisterRequest;
import com.hrms.hrms.modules.auth.dto.RegisterResponse;
import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.entity.UserStatus;
import com.hrms.hrms.modules.auth.repository.UserRepository;
import com.hrms.hrms.modules.auth.token.entity.BlacklistedToken;
import com.hrms.hrms.modules.auth.token.repository.BlacklistedTokenRepository;
import com.hrms.hrms.modules.employee.entity.Employee;
import com.hrms.hrms.modules.employee.entity.EmployeeStatus;
import com.hrms.hrms.modules.employee.repository.EmployeeRepository;
import com.hrms.hrms.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class AuthService {

    // DEPENDENCIES
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    // CONSTRUCTOR
    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            BlacklistedTokenRepository blacklistedTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder
    ) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    // USER REGISTRATION
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // STEP 1: CHECK EMAIL
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }


        // STEP 2: CREATE USER ACCOUNT
        /*
         * Default permissions:
         *
         * isAdmin    = false
         * isEmployee = true
         */

        User user = User.builder().email(request.getEmail().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAdmin(false)
                .isEmployee(true)
                .status(UserStatus.ACTIVE)
                .build();

        // STEP 3: SAVE USER
        User savedUser = userRepository.save(user);

        // STEP 4: CREATE EMPLOYEE PROFILE
        Employee employee = Employee.builder()
                .user(savedUser)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .department(null)
                .designation(null)
                .dateOfJoining(null)
                .reportingManager(null)
                .status(EmployeeStatus.ACTIVE)
                .build();

        // STEP 5: SAVE EMPLOYEE PROFILE
        employeeRepository.save(employee);

        // STEP 6: RETURN RESPONSE
        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .isAdmin(savedUser.isAdmin())
                .isEmployee(savedUser.isEmployee())
                .status(savedUser.getStatus().name())
                .message("User registered successfully")
                .build();
    }

    // USER LOGIN
    @Transactional
    public LoginResponse login(LoginRequest request) {

        // STEP 1: AUTHENTICATE EMAIL AND PASSWORD
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // STEP 2: GET USER USING EMAIL
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // STEP 3: CHECK ACCOUNT STATUS
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("Your account is inactive. " + "Please contact the administrator.");
        }

        // STEP 4: CHECK USER ACCESS
        if (!user.isAdmin() && !user.isEmployee()) {
            throw new BadRequestException("You do not have permission " + "to access the application.");
        }

        // STEP 5: GET EMPLOYEE PROFILE USING EMAIL
        Employee employee = employeeRepository
                .findByUserEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for email: " + user.getEmail()));

        // STEP 6: UPDATE LAST LOGIN
        user.setLastLogin(
                LocalDateTime.now()
        );

        userRepository.save(user);

        // STEP 7: GENERATE JWT TOKEN
        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.isAdmin(),
                user.isEmployee()
        );

        // STEP 8: RETURN LOGIN RESPONSE
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .isAdmin(user.isAdmin())
                .isEmployee(user.isEmployee())
                // Employee details fetched using email
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .build();
    }

    // USER LOGOUT
    @Transactional
    public void logout(String token) {

        // Check if token is already blacklisted
        if (blacklistedTokenRepository.existsByToken(token)) {
            return;
        }

        // GET JWT EXPIRATION TIME
        Date expirationDate = jwtTokenProvider.extractExpiration(token);

        // CONVERT DATE TO LOCALDATETIME
        LocalDateTime expiresAt =
                expirationDate
                        .toInstant()
                        .atZone(
                                ZoneId.systemDefault()
                        )
                        .toLocalDateTime();

        // SAVE TOKEN IN BLACKLIST
        BlacklistedToken blacklistedToken =
                BlacklistedToken.builder()
                        .token(token)
                        .expiresAt(expiresAt)
                        .build();
        blacklistedTokenRepository.save(blacklistedToken);
    }
}
