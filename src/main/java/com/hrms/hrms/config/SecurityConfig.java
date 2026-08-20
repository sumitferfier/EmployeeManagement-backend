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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;


@Configuration
public class SecurityConfig {

    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SecurityConfig(
            UserDetailsServiceImpl userDetailsService,
            JwtAuthFilter jwtAuthFilter
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }




    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }


    // =========================================================
    // CORS CONFIGURATION
    // =========================================================

    /*
     * Allows Angular frontend to access this backend.
     *
     * Angular:
     * http://localhost:4200
     *
     * Spring Boot:
     * http://localhost:8080
     * or
     * http://localhost:8081
     */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();


        // -----------------------------------------------------
        // ALLOWED FRONTEND URLS
        // -----------------------------------------------------

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:4200"
                )
        );


        // -----------------------------------------------------
        // ALLOWED HTTP METHODS
        // -----------------------------------------------------

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );


        // -----------------------------------------------------
        // ALLOWED REQUEST HEADERS
        // -----------------------------------------------------

        /*
         * Authorization is required for JWT:
         *
         * Authorization: Bearer <token>
         */

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );


        // -----------------------------------------------------
        // OPTIONAL: ALLOW RESPONSE HEADERS
        // -----------------------------------------------------

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );


        // Register CORS rules for all APIs
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // -------------------------------------------------
                // DISABLE CSRF
                // -------------------------------------------------

                /*
                 * REST APIs using JWT normally do not use
                 * browser session-based CSRF protection.
                 */

                .csrf(csrf -> csrf.disable())


                // -------------------------------------------------
                // ENABLE CORS
                // -------------------------------------------------

                /*
                 * Allows Angular frontend to call
                 * Spring Boot APIs.
                 */

                .cors(Customizer.withDefaults())


                // -------------------------------------------------
                // AUTHORIZATION RULES
                // -------------------------------------------------

                .authorizeHttpRequests(auth -> auth


                        // =========================================
                        // PUBLIC AUTHENTICATION APIs
                        // =========================================

                        /*
                         * No JWT token required.
                         *
                         * Examples:
                         *
                         * POST /api/v1/auth/register
                         * POST /api/v1/auth/login
                         */

                        .requestMatchers(
                                "/api/v1/auth/**"
                        ).permitAll()


                        // =========================================
                        // ROLE MANAGEMENT
                        // =========================================

                        /*
                         * Only ADMIN can:
                         *
                         * Create roles
                         * View roles
                         * Update roles
                         * Delete roles
                         */

                        .requestMatchers(
                                "/api/v1/roles/**"
                        ).hasRole("ADMIN")


                        // =========================================
                        // DEPARTMENT MANAGEMENT
                        // =========================================

                        /*
                         * Only ADMIN can manage departments.
                         */

                        .requestMatchers(
                                "/api/v1/departments/**"
                        ).hasRole("ADMIN")


                        // =========================================
                        // EMPLOYEE MANAGEMENT
                        // =========================================

                        /*
                         * Currently only ADMIN can:
                         *
                         * Create employees
                         * View employees
                         * Update employees
                         * Delete employees
                         */

                        .requestMatchers(
                                "/api/v1/employees/**"
                        ).hasRole("ADMIN")


                        // =========================================
                        // ADMIN TEST API
                        // =========================================

                        .requestMatchers(
                                "/api/v1/admin-test"
                        ).hasRole("ADMIN")


                        // =========================================
                        // EMPLOYEE TEST API
                        // =========================================

                        .requestMatchers(
                                "/api/v1/employee-test"
                        ).hasRole("EMPLOYEE")


                        // =========================================
                        // EVERYTHING ELSE
                        // =========================================

                        /*
                         * Any other API requires
                         * a valid JWT token.
                         */

                        .anyRequest()
                        .authenticated()
                )


                // -------------------------------------------------
                // JWT FILTER
                // -------------------------------------------------

                /*
                 * JWT filter runs before Spring Security's
                 * UsernamePasswordAuthenticationFilter.
                 *
                 * Flow:
                 *
                 * Angular Request
                 *        ↓
                 * Authorization: Bearer JWT_TOKEN
                 *        ↓
                 * JwtAuthFilter
                 *        ↓
                 * Validate JWT
                 *        ↓
                 * Load User
                 *        ↓
                 * Check Role
                 *        ↓
                 * Allow / Deny Request
                 */

                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}