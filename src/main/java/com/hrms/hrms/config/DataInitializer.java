package com.hrms.hrms.config;

import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.entity.UserStatus;
import com.hrms.hrms.modules.auth.repository.UserRepository;
import com.hrms.hrms.modules.role.entity.Role;
import com.hrms.hrms.modules.role.repository.RoleRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

// This will create the temporary data (username, password, role)
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {Role adminRole = roleRepository.findByRoleName("ADMIN").orElseGet(() -> roleRepository.save(Role.builder().roleName("ADMIN").description("System Administrator").build()));

            Role employeeRole = roleRepository.findByRoleName("EMPLOYEE").orElseGet(() ->
                                    roleRepository.save(Role.builder()
                                            .roleName("EMPLOYEE")
                                            .description("Company Employee")
                                            .build()));

            if (!userRepository.existsByUsername("admin")) {

                User admin = User.builder()
                        .username("admin")
                        .email("admin@hrms.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(adminRole)
                        .status(UserStatus.ACTIVE)
                        .build();
                userRepository.save(admin);
            }

            if (!userRepository.existsByUsername("employee")) {

                User employee = User.builder()
                        .username("employee")
                        .email("employee@hrms.com")
                        .password(passwordEncoder.encode("employee123"))
                        .role(employeeRole)
                        .status(UserStatus.ACTIVE)
                        .build();
                userRepository.save(employee);
            }
        };
    }
}