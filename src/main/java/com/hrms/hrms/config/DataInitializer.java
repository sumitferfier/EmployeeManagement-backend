//package com.hrms.hrms.config;
//
//import com.hrms.hrms.modules.auth.entity.User;
//import com.hrms.hrms.modules.auth.entity.UserStatus;
//import com.hrms.hrms.modules.auth.repository.UserRepository;
//import com.hrms.hrms.modules.role.entity.Role;
//import com.hrms.hrms.modules.role.repository.RoleRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//@Configuration
//public class DataInitializer {
//    @Bean
//    public CommandLineRunner initializeData(
//            RoleRepository roleRepository,
//            UserRepository userRepository,
//            PasswordEncoder passwordEncoder
//    ) {
//        return args -> {
//
//            // CREATE ADMIN ROLE
//            Role adminRole = roleRepository.findByRoleName("ADMIN").orElseGet(() -> roleRepository.save(Role.builder()
//                                            .roleName("ADMIN")
//                                            .description("System Administrator")
//                                            .build()));
//
//            // CREATE EMPLOYEE ROLE
//            roleRepository
//                    .findByRoleName("EMPLOYEE")
//                    .orElseGet(() -> roleRepository.save(Role.builder()
//                                            .roleName("EMPLOYEE")
//                                            .description("Standard Employee")
//                                            .build()));
//
//            // CREATE INITIAL ADMIN
//            String adminEmail = "admin@hrms.com";
//            if (!userRepository.existsByEmail(adminEmail)) {
//                User adminUser = User.builder()
//                        .email(adminEmail)
//                        .password(passwordEncoder.encode("Admin@123"))
//                        .role(adminRole)
//                        .status(UserStatus.ACTIVE)
//                        .build();
//
//                userRepository.save(adminUser);
//                System.out.println("Initial ADMIN created successfully");
//            } else {
//                System.out.println("Initial ADMIN already exists");
//            }
//        };
//    }
//}