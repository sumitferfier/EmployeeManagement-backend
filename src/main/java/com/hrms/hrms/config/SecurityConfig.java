package com.hrms.hrms.config;
import com.hrms.hrms.security.JwtAuthFilter;
import com.hrms.hrms.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.List;

@Configuration
public class SecurityConfig {

    // DEPENDENCIES
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    // CONSTRUCTOR
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
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // AUTHENTICATION MANAGER
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // CORS CONFIGURATION
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Angular frontend URL
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));

        // Allowed HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allowed request headers
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Expose Authorization header
        configuration.setExposedHeaders(List.of("Authorization"));

        // Allow rules for all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // SECURITY FILTER CHAIN
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {http

                // DISABLE CSRF
                .csrf(csrf -> csrf.disable())

                // ENABLE CORS
                .cors(Customizer.withDefaults())
             // JWT IS STATELESS
                .sessionManagement(session -> session
                                .sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )

             // AUTHENTICATION PROVIDER
                .authenticationProvider(authenticationProvider())

             // AUTHORIZATION RULES
            .authorizeHttpRequests(auth -> auth

                    // =====================================================
                    // PUBLIC APIs
                    // =====================================================

                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()


                    // =====================================================
                    // ADMIN APIs
                    // =====================================================

                    // Admin User Access Management
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")

                    // Role Management
                    .requestMatchers("/api/v1/roles/**")
                    .hasRole("ADMIN")

                    // Department Management
                    .requestMatchers("/api/v1/departments/**")
                    .hasRole("ADMIN")


                    // =====================================================
                    // EMPLOYEE APIs
                    // =====================================================

                    // IMPORTANT:
                    // /me MUST COME BEFORE /employees/**
                    //
                    // Employee can access own profile
                    // Admin can also access employee profile
                    .requestMatchers("/api/v1/employees/me")
                    .hasAnyRole("EMPLOYEE", "ADMIN")

                    // All other employee-management APIs are ADMIN only
                    .requestMatchers("/api/v1/employees/**")
                    .hasRole("ADMIN")

                    // LEAVE APIs
                    .requestMatchers("/api/v1/leaves/admin")
                    .hasRole("ADMIN")

                    .requestMatchers("/api/v1/leaves/**")
                    .hasRole("EMPLOYEE")


                    // =====================================================
                    // EVERYTHING ELSE
                    // =====================================================

                    .anyRequest()
                    .authenticated()
            )


                // =============================================
                // JWT FILTER
                // =============================================

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}