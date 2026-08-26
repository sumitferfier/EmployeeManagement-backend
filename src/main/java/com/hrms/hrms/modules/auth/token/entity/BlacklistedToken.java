package com.hrms.hrms.modules.auth.token.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blacklisted_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistedToken {

    // =========================================================
    // PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // =========================================================
    // JWT TOKEN
    // =========================================================

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;


    // =========================================================
    // TOKEN EXPIRATION TIME
    // =========================================================

    @Column(nullable = false)
    private LocalDateTime expiresAt;


    // =========================================================
    // LOGOUT TIME
    // =========================================================

    @Column(nullable = false)
    private LocalDateTime createdAt;


    // =========================================================
    // AUTO SET LOGOUT TIME
    // =========================================================

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}