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


    // =====================================================
    // EMPLOYEE DETAILS
    // =====================================================

    /*
     * Employee code is assigned later by Admin.
     *
     * During signup it can be NULL.
     */

    @Column(
            name = "employee_code",
            unique = true
    )
    private String employeeCode;


    /*
     * First name comes from signup.
     */

    @Column(
            name = "first_name",
            nullable = false
    )
    private String firstName;


    /*
     * Last name comes from signup.
     */

    @Column(
            name = "last_name",
            nullable = false
    )
    private String lastName;


    /*
     * Phone can be updated later.
     */

    @Column(name = "phone")
    private String phone;


    // =====================================================
    // DEPARTMENT
    // =====================================================

    /*
     * Department is assigned later by Admin.
     *
     * During signup it can be NULL.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;


    // =====================================================
    // JOB INFORMATION
    // =====================================================

    /*
     * Designation is assigned later by Admin.
     *
     * During signup it can be NULL.
     */

    @Column(name = "designation")
    private String designation;


    /*
     * Date of joining is assigned later by Admin.
     *
     * During signup it can be NULL.
     */

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;


    // =====================================================
    // REPORTING MANAGER
    // =====================================================

    /*
     * Self-referencing relationship.
     *
     * Employee
     *    ↓
     * Reporting Manager
     *
     * During signup it can be NULL.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id")
    private Employee reportingManager;


    // =====================================================
    // EMPLOYMENT STATUS
    // =====================================================

    /*
     * New employees are ACTIVE by default.
     */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;
}