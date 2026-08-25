package com.hrms.hrms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JwtTokenProvider {

    // =========================================================
    // SECRET KEY
    // =========================================================

    private final SecretKey secretKey;


    // =========================================================
    // TOKEN EXPIRATION
    // =========================================================

    private final long expiration;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration
    ) {

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expiration = expiration;
    }


    // =========================================================
    // GENERATE JWT TOKEN
    // =========================================================

    public String generateToken(
            String email,
            boolean isAdmin,
            boolean isEmployee
    ) {

        Date now = new Date();

        Date expiryDate = new Date(
                now.getTime() + expiration
        );

        return Jwts.builder()

                // JWT subject = user email
                .subject(email)

                // Access permissions
                .claim("isAdmin", isAdmin)
                .claim("isEmployee", isEmployee)

                // Token timestamps
                .issuedAt(now)
                .expiration(expiryDate)

                // Sign token
                .signWith(secretKey)

                .compact();
    }


    // =========================================================
    // EXTRACT EMAIL
    // =========================================================

    public String extractEmail(String token) {

        return getClaims(token).getSubject();
    }


    // =========================================================
    // EXTRACT ADMIN ACCESS
    // =========================================================

    public boolean isAdminFromToken(String token) {

        Boolean isAdmin = getClaims(token)
                .get("isAdmin", Boolean.class);

        return Boolean.TRUE.equals(isAdmin);
    }


    // =========================================================
    // EXTRACT EMPLOYEE ACCESS
    // =========================================================

    public boolean isEmployeeFromToken(String token) {

        Boolean isEmployee = getClaims(token)
                .get("isEmployee", Boolean.class);

        return Boolean.TRUE.equals(isEmployee);
    }


    // =========================================================
    // VALIDATE JWT TOKEN
    // =========================================================

    public boolean validateToken(String token) {

        try {

            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException exception) {

            return false;
        }
    }


    // =========================================================
    // EXTRACT CLAIMS
    // =========================================================

    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}