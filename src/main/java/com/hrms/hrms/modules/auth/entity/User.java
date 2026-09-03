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

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //first name
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    //Last Name
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    // LOGIN EMAIL
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // PASSWORD
    @Column(nullable = false, length = 255)
    private String password;

    // ACCOUNT STATUS
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    // LAST LOGIN
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // AUDIT FIELDS
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ROLE / ACCESS
    /*
     * One User has one Role record.
     *
     * users.id
     *     ↓
     * roles.user_id
     */

    @OneToOne(
            mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private Role role;

    // BEFORE INSERT
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }

    // BEFORE UPDATE
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}