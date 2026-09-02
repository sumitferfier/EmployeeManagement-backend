package com.hrms.hrms.security;

import com.hrms.hrms.modules.auth.token.repository.BlacklistedTokenRepository;
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

    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public JwtAuthFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsServiceImpl userDetailsService,
            BlacklistedTokenRepository blacklistedTokenRepository
    ) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }


    // =========================================================
    // JWT FILTER
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
            // STEP 3: CHECK IF TOKEN IS BLACKLISTED
            // =================================================

            if (
                    blacklistedTokenRepository
                            .existsByToken(token)
            ) {

                // Token was invalidated during logout.
                // Continue request without authentication.
                filterChain.doFilter(request, response);

                return;
            }


            // =================================================
            // STEP 4: VALIDATE TOKEN
            // =================================================

            if (
                    jwtTokenProvider.validateToken(token)
            ) {

                try {

                    // Extract email from JWT subject.
                    email = jwtTokenProvider.extractEmail(token);

                } catch (Exception exception) {

                    System.out.println(
                            "Unable to extract email from JWT"
                    );
                }
            }
        }


        // =====================================================
        // STEP 5: AUTHENTICATE USER
        // =====================================================

        if (

                email != null

                        && SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null

        ) {

            // Load latest user permissions from database.
            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(email);


            // =================================================
            // VALIDATE TOKEN AGAIN
            // =================================================

            if (
                    jwtTokenProvider.validateToken(token)
            ) {

                UsernamePasswordAuthenticationToken authentication =

                        new UsernamePasswordAuthenticationToken(

                                userDetails,

                                null,

                                userDetails.getAuthorities()
                        );


                // Attach request details.
                authentication.setDetails(

                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );


                // Store authenticated user in Security Context.
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        }


        // =====================================================
        // STEP 6: CONTINUE REQUEST
        // =====================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}