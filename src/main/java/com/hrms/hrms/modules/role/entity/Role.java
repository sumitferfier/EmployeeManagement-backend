package com.hrms.hrms.modules.role.entity;

import com.hrms.hrms.modules.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    // =========================================================
    // PRIMARY KEY
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // =========================================================
    // USER ID
    // =========================================================

    /*
     * Links this role/access record to the user.
     *
     * roles.user_id
     *       ↓
     * users.id
     */

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;


    // =========================================================
    // USER EMAIL
    // =========================================================

    /*
     * Email is stored here as well so that the roles table
     * is easy to understand and Admin can identify the user
     * directly by email.
     */

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String email;


    // =========================================================
    // ADMIN ACCESS
    // =========================================================

    /*
     * true  -> Admin access
     * false -> No Admin access
     */

    @Column(
            name = "is_admin",
            nullable = false
    )
    private boolean isAdmin;


    // =========================================================
    // EMPLOYEE ACCESS
    // =========================================================

    /*
     * true  -> Employee access
     * false -> No Employee access
     */

    @Column(
            name = "is_employee",
            nullable = false
    )
    private boolean isEmployee;
}