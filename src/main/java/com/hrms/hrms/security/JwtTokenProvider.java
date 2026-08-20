package com.hrms.hrms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Secret key used to sign and verify JWT tokens
    private final SecretKey secretKey;

    // JWT expiration time in milliseconds
    private final long expiration;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration}") long expiration) {

        // Convert the secret string into a secure HMAC key
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    // GENERATE JWT TOKE
    public String generateToken(
            String email,
            String role
    ) {

        // Current date and time
        Date now = new Date();

        // Calculate token expiry time
        Date expiryDate = new Date(
                now.getTime() + expiration
        );

        // Build and sign JWT token
        return Jwts.builder()

                // Email is the JWT subject
                .subject(email)

                // Store user role as a claim
                .claim("role", role)

                // Token creation time
                .issuedAt(now)

                // Token expiration time
                .expiration(expiryDate)

                // Sign token using secret key
                .signWith(secretKey)

                .compact();
    }


    // =========================================================
    // EXTRACT EMAIL FROM TOKEN
    // =========================================================

    /*
     * The JWT subject contains the user's email.
     */

    public String extractEmail(String token) {

        return getClaims(token)
                .getSubject();
    }


    // =========================================================
    // EXTRACT ROLE FROM TOKEN
    // =========================================================

    public String getRoleFromToken(String token) {

        return getClaims(token)
                .get("role", String.class);
    }


    // =========================================================
    // VALIDATE JWT TOKEN
    // =========================================================

    /*
     * Checks:
     *
     * 1. Token signature
     * 2. Token format
     * 3. Token expiration
     */

    public boolean validateToken(String token) {

        try {

            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }


    // =========================================================
    // EXTRACT ALL CLAIMS
    // =========================================================

    /*
     * Reads the JWT payload and returns all claims.
     */

    private Claims getClaims(String token) {

        return Jwts.parser()

                // Verify token signature
                .verifyWith(secretKey)

                .build()

                // Parse JWT token
                .parseSignedClaims(token)

                // Return token payload
                .getPayload();
    }
}