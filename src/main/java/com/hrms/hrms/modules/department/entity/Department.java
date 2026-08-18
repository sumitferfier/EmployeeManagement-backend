package com.hrms.hrms.modules.department.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "department_name",
            nullable = false,
            unique = true,
            length = 50
    )
    private String departmentName;

    @Column(length = 255)
    private String description;
}