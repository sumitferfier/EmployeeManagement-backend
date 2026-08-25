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


@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;


    public JwtAuthFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsServiceImpl userDetailsService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }


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


        String token = null;
        String email = null;


        // =====================================================
        // STEP 2: EXTRACT BEARER TOKEN
        // =====================================================

        if (
                authorizationHeader != null
                        && authorizationHeader.startsWith("Bearer ")
        ) {

            token = authorizationHeader.substring(7);


            // =================================================
            // STEP 3: VALIDATE TOKEN BEFORE EXTRACTING EMAIL
            // =================================================

            if (jwtTokenProvider.validateToken(token)) {

                try {

                    email = jwtTokenProvider.extractEmail(token);

                } catch (Exception e) {

                    System.out.println(
                            "Unable to extract email from JWT"
                    );
                }
            }
        }


        // =====================================================
        // STEP 4: AUTHENTICATE USER
        // =====================================================

        if (
                email != null
                        && SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null
        ) {

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);


            // Validate token again before authentication
            if (jwtTokenProvider.validateToken(token)) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(

                                userDetails,

                                null,

                                userDetails.getAuthorities()
                        );


                authentication.setDetails(

                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        }


        // =====================================================
        // STEP 5: CONTINUE REQUEST
        // =====================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}