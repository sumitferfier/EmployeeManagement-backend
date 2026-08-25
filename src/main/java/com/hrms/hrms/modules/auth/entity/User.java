package com.hrms.hrms.modules.auth.entity;

import com.hrms.hrms.modules.role.entity.Role;
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

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Email is now the unique login identity
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // BCrypt hashed password
    @Column(nullable = false, length = 255)
    private String password;

    // User role: isAdmin or isEmployee
    @Column(nullable = false)
    private boolean isAdmin;

    @Column(nullable = false)
    private boolean isEmployee;

    // Account status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    // Last successful login time
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Record creation timestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Record update timestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Automatically executed before INSERT
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

    // Automatically executed before UPDATE
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}