package com.hrms.hrms.modules.department.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "departments", uniqueConstraints = {@UniqueConstraint(name = "uk_department_name", columnNames = "department_name")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    // PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // DEPARTMENT NAME
    @Column(name = "department_name",
            nullable = false,
            unique = true,
            length = 50
    )
    private String departmentName;

    // DESCRIPTION
    @Column(length = 255)
    private String description;
}