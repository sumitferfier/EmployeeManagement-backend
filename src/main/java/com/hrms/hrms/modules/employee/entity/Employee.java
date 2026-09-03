package com.hrms.hrms.modules.employee.entity;

import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.department.entity.Department;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;


// =========================================================
// EMPLOYEE ENTITY
// =========================================================

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {


    // =====================================================
    // PRIMARY KEY
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // =====================================================
    // USER ACCOUNT
    // =====================================================

    /*
     * One Employee has one User account.
     *
     * employees.user_id
     *        ↓
     * users.id
     */

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone")
    private String phone;


    // =====================================================
    // DEPARTMENT
    // =====================================================

    /*
     * Department is assigned by Admin.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;


    // =====================================================
    // JOB INFORMATION
    // =====================================================

    @Column(name = "designation")
    private String designation;


    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;


    // =====================================================
    // REPORTING MANAGER
    // =====================================================

    /*
     * Reporting manager is identified using EMAIL.
     *
     * Example:
     *
     * reporting_manager_email =
     * manager@gmail.com
     *
     * This makes the database easier to understand.
     */

    @Column(name = "reporting_manager_email")
    private String reportingManagerEmail;


    // =====================================================
    // EMPLOYMENT STATUS
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;
}