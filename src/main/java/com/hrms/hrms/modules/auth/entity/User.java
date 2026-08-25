package com.hrms.hrms.modules.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // =========================================================
    // PRIMARY KEY
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // =========================================================
    // LOGIN EMAIL
    // =========================================================

    @Column(nullable = false, unique = true, length = 100)
    private String email;


    // =========================================================
    // PASSWORD
    // =========================================================

    @Column(nullable = false, length = 255)
    private String password;


    // =========================================================
    // ACCESS PERMISSIONS
    // =========================================================

    /*
     * true  -> User can access Admin features
     * false -> User cannot access Admin features
     */
    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;


    /*
     * true  -> User can access Employee features
     * false -> User cannot access Employee features
     */
    @Column(name = "is_employee", nullable = false)
    private boolean isEmployee;


    // =========================================================
    // ACCOUNT STATUS
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;


    // =========================================================
    // LAST LOGIN
    // =========================================================

    @Column(name = "last_login")
    private LocalDateTime lastLogin;


    // =========================================================
    // AUDIT FIELDS
    // =========================================================

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // =========================================================
    // BEFORE INSERT
    // =========================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        // New accounts are active by default
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }


    // =========================================================
    // BEFORE UPDATE
    // =========================================================

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}