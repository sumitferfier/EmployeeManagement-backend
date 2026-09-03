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
import com.hrms.hrms.modules.employee.repository.EmployeeRepository;
import com.hrms.hrms.modules.role.entity.Role;
import com.hrms.hrms.modules.role.repository.RoleRepository;
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

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            BlacklistedTokenRepository blacklistedTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // USER REGISTRATION
    // =========================================================

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // STEP 1: NORMALIZE EMAIL

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        // STEP 2: CHECK DUPLICATE EMAIL

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "Email already exists: " + email
            );
        }

        // STEP 3: CREATE USER

        /*
         * New user gets:
         *
         * isAdmin    = false
         * isEmployee = false
         *
         * Therefore the user initially has VIEW-ONLY access.
         */

        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .status(UserStatus.ACTIVE)
                .build();

        // STEP 4: SAVE USER

        User savedUser = userRepository.save(user);

        // STEP 5: CREATE ROLE / ACCESS RECORD

        Role role = Role.builder()
                .user(savedUser)
                .email(savedUser.getEmail())
                .isAdmin(false)
                .isEmployee(false)
                .build();

        // STEP 6: SAVE ROLE

        Role savedRole = roleRepository.save(role);

        // Connect both sides of relationship

        savedUser.setRole(savedRole);

        // STEP 7: RETURN RESPONSE

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .isAdmin(savedRole.isAdmin())
                .isEmployee(savedRole.isEmployee())
                .status(savedUser.getStatus().name())
                .message(
                        "User registered successfully. "
                                + "Access will be assigned by an administrator."
                )
                .build();
    }

    // =========================================================
    // USER LOGIN
    // =========================================================

    @Transactional
    public LoginResponse login(LoginRequest request) {

        // STEP 1: NORMALIZE EMAIL

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        // STEP 2: AUTHENTICATE
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        // STEP 3: FIND USER

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        // STEP 4: CHECK ACCOUNT STATUS
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException(
                    "Your account is inactive. "
                            + "Please contact the administrator."
            );
        }

        // STEP 5: FIND ACCESS RECORD
        Role role = roleRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Access record not found for user: "
                                        + user.getEmail()
                        )
                );

        // STEP 6: FIND EMPLOYEE PROFILE

        /*
         * View-only user:
         * Employee = null
         *
         * Employee user:
         * Employee = existing employee profile
         */

        Employee employee = employeeRepository
                .findByUser_Email(user.getEmail())
                .orElse(null);

        // STEP 7: UPDATE LAST LOGIN
        user.setLastLogin(LocalDateTime.now());

        userRepository.save(user);

        // STEP 8: GENERATE JWT
        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                role.isAdmin(),
                role.isEmployee()
        );

        // STEP 9: RETURN RESPONSE
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .isAdmin(role.isAdmin())
                .isEmployee(role.isEmployee())

                /*
                 * Names come from Employee if employee
                 * profile exists.
                 *
                 */
                .firstName(
                        employee != null
                                ? employee.getFirstName()
                                : user.getFirstName()
                )

                .lastName(
                        employee != null
                                ? employee.getLastName()
                                : user.getLastName()
                )

                .build();
    }

    // USER LOGOUT
    @Transactional
    public void logout(String token) {

        if (blacklistedTokenRepository.existsByToken(token)) {
            return;
        }

        Date expirationDate =
                jwtTokenProvider.extractExpiration(token);

        LocalDateTime expiresAt =
                expirationDate
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        BlacklistedToken blacklistedToken =
                BlacklistedToken.builder()
                        .token(token)
                        .expiresAt(expiresAt)
                        .build();

        blacklistedTokenRepository.save(
                blacklistedToken
        );
    }
}