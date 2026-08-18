package com.hrms.hrms.modules.auth.service;
import com.hrms.hrms.modules.auth.dto.LoginRequest;
import com.hrms.hrms.modules.auth.dto.LoginResponse;
import com.hrms.hrms.modules.auth.dto.RegisterRequest;
import com.hrms.hrms.modules.auth.dto.RegisterResponse;
import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.entity.UserStatus;
import com.hrms.hrms.modules.auth.repository.UserRepository;
import com.hrms.hrms.modules.role.entity.Role;
import com.hrms.hrms.modules.role.repository.RoleRepository;
import com.hrms.hrms.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    // USER REGISTRATION
        // Creates a new Employee account.
        // Public registration is only for EMPLOYEE role.
    // Admin accounts should not be created through this API.

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // Step 1: Check whether username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Step 2: Check whether email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Step 3: Find EMPLOYEE role
        Role employeeRole = roleRepository.findByRoleName("EMPLOYEE")
                        .orElseThrow(() -> new RuntimeException("EMPLOYEE role not found"));

        // Step 4: Create User entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())

                // NEVER save plain password.
                // BCrypt converts it into a secure hash.
                .password(passwordEncoder.encode(request.getPassword()))

                // Every public registration becomes EMPLOYEE
                .role(employeeRole)

                // New account starts as ACTIVE
                .status(UserStatus.ACTIVE)
                .build();

        // Step 5: Save user into PostgreSQL
        User savedUser = userRepository.save(user);

        // Step 6: Return registration response
        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().getRoleName())
                .status(savedUser.getStatus().name())
                .message("Employee registered successfully")
                .build();
    }


    // USER LOGIN
    public LoginResponse login(LoginRequest request) {

        // Authenticate username and password.
        // Spring Security checks the BCrypt password.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Find the authenticated user from database
        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow();

        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT containing username and role
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().getRoleName());

        // Return JWT and user information
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().getRoleName())
                .build();
    }
}