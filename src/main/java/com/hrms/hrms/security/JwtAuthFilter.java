package com.hrms.hrms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


//This filter runs once for every HTTP request.

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // Used for extracting and validating JWT
    private final JwtTokenProvider jwtTokenProvider;

    // Used for loading user from database
    private final UserDetailsServiceImpl userDetailsService;
    public JwtAuthFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsServiceImpl userDetailsService
    ) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }


    // =========================================================
    // JWT AUTHENTICATION FILTER
    // =========================================================

    @Override
    protected void doFilterInternal(

            HttpServletRequest request,

            HttpServletResponse response,

            FilterChain filterChain

    ) throws ServletException, IOException {


        // =====================================================
        // STEP 1: GET AUTHORIZATION HEADER
        // =====================================================

        String authorizationHeader =
                request.getHeader("Authorization");


        // JWT token variable
        String token = null;

        // Email extracted from JWT
        String email = null;


        // =====================================================
        // STEP 2: CHECK FOR BEARER TOKEN
        // =====================================================

        /*
         * Expected header:
         *
         * Authorization: Bearer <JWT_TOKEN>
         */

        if (authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")) {

            // Remove "Bearer " from the header
            token = authorizationHeader.substring(7);


            // =================================================
            // STEP 3: VALIDATE TOKEN AND EXTRACT EMAIL
            // =================================================

            try {

                // Validate JWT signature and expiration
                if (jwtTokenProvider.validateToken(token)) {

                    // JWT subject contains user email
                    email = jwtTokenProvider.extractEmail(token);
                }

            } catch (Exception e) {

                // Invalid JWT token
                System.out.println(
                        "Unable to validate or extract email from JWT"
                );
            }
        }


        // =====================================================
        // STEP 4: AUTHENTICATE USER
        // =====================================================

        /*
         * Only authenticate if:
         *
         * 1. Email was successfully extracted.
         * 2. User is not already authenticated.
         */

        if (email != null
                && SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {


            // Load user from database using email
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);


            // =================================================
            // STEP 5: SET AUTHENTICATION
            // =================================================

            /*
             * UserDetails contains:
             *
             * Email
             * Password
             * Authorities:
             * ROLE_ADMIN / ROLE_EMPLOYEE
             */

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(

                            userDetails,

                            null,

                            userDetails.getAuthorities()
                    );


            // Attach request information
            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );


            // Store authenticated user in Spring Security
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }


        // =====================================================
        // STEP 6: CONTINUE REQUEST
        // =====================================================

        filterChain.doFilter(request, response);
    }
}