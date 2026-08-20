package com.hrms.hrms.config;

import com.hrms.hrms.security.JwtAuthFilter;
import com.hrms.hrms.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtAuthFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // PASSWORD ENCODER
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AUTHENTICATION PROVIDER
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // AUTHENTICATION MANAGER
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {return configuration.getAuthenticationManager();
    }

    // SECURITY FILTER CHAIN
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {http

                // REST APIs using JWT do not normally use
                // browser session-based CSRF protection.
                .csrf(csrf -> csrf.disable())

             // AUTHORIZATION RULES
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/roles/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/departments/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/employees/**")
                        .hasRole("ADMIN")


                        // -----------------------------------------
                        // ADMIN TEST API
                        // -----------------------------------------
                        .requestMatchers(
                                "/api/v1/admin-test"
                        ).hasRole("ADMIN")


                        // -----------------------------------------
                        // EMPLOYEE TEST API
                        // -----------------------------------------
                        .requestMatchers(
                                "/api/v1/employee-test"
                        ).hasRole("EMPLOYEE")


                        // -----------------------------------------
                        // EVERYTHING ELSE
                        // -----------------------------------------
                        .anyRequest().authenticated()
                )


                // =================================================
                // JWT FILTER
                // =================================================
                // Our JWT filter runs before Spring Security's
                // UsernamePasswordAuthenticationFilter.
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}