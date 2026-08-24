package com.hrms.hrms.modules.auth.service;

import com.hrms.hrms.common.exception.DuplicateResourceException;
import com.hrms.hrms.common.exception.ResourceNotFoundException;
import com.hrms.hrms.common.exception.BadRequestException;
import com.hrms.hrms.modules.auth.dto.LoginRequest;
import com.hrms.hrms.modules.auth.dto.LoginResponse;
import com.hrms.hrms.modules.auth.dto.RegisterRequest;
import com.hrms.hrms.modules.auth.dto.RegisterResponse;
import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.entity.UserStatus;
import com.hrms.hrms.modules.auth.repository.UserRepository;
import com.hrms.hrms.modules.role.entity.Role;
import com.hrms.hrms.modules.role.repository.RoleRepository;
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

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmployeeRepository employeeRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // STEP 1: CHECK EMAIL
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // STEP 2: GET DEFAULT EMPLOYEE ROLE
        Role employeeRole = roleRepository
                .findByRoleName("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("Default EMPLOYEE role not found"));

        // STEP 3: CREATE USER ACCOUNT
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(employeeRole)
                .status(UserStatus.ACTIVE)
                .build();


        // STEP 4: SAVE USER
        User savedUser = userRepository.save(user);

        // STEP 5: CREATE EMPLOYEE PROFILE
        Employee employee = Employee.builder()
                .user(savedUser)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .employeeCode(null)
                .department(null)
                .designation(null)
                .dateOfJoining(null)
                .reportingManager(null)
                .status(EmployeeStatus.ACTIVE)
                .build();

        // STEP 6: SAVE EMPLOYEE
        Employee savedEmployee = employeeRepository.save(employee);
        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .employeeId(savedEmployee.getId())
                .firstName(savedEmployee.getFirstName())
                .lastName(savedEmployee.getLastName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().getRoleName())
                .status(savedUser.getStatus().name())
                .message("Employee registered successfully")
                .build();
    }

    // USER LOGIN
    @Transactional
    public LoginResponse login(LoginRequest request) {

        // STEP 1: AUTHENTICATE EMAIL AND PASSWORD
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // STEP 2: GET USER FROM DATABASE
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // STEP 3: CHECK ACCOUNT STATUS
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("Your account is inactive. " + "Please contact the administrator.");
        }

        // STEP 4: UPDATE LAST LOGIN
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // STEP 5: GENERATE JWT
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().getRoleName());

        // STEP 6: RETURN LOGIN RESPONSE
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole()
                        .getRoleName())
                .build();
    }
}