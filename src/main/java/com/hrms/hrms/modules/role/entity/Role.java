package com.hrms.hrms.modules.role.entity;

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

    // PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ROLE NAME
    @Column(
            name = "role_name", nullable = false, unique = true, length = 30)
    private String roleName;

    // DESCRIPTION
    @Column(length = 255)
    private String description;
}