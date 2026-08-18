package com.hrms.hrms.modules.role.entity;

import jakarta.persistence.*;
import lombok.*;

// Table name for database insertion
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = 30)
    private String roleName;

    @Column(length = 255)
    private String description;
}