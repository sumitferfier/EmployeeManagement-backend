package com.hrms.hrms.modules.leave.entity;

import com.hrms.hrms.modules.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;


// =========================================================
// LEAVE ENTITY
// =========================================================

@Entity
@Table(name = "leaves")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leave {

    // =====================================================
    // PRIMARY KEY
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // =====================================================
    // EMPLOYEE
    // =====================================================

    /*
     * Leave belongs to one Employee.
     *
     * leaves.employee_id
     *        ↓
     * employees.id
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_id",
            nullable = false
    )
    private Employee employee;


    // =====================================================
    // REQUESTED TO
    // =====================================================

    /*
     * Email of the reporting manager
     * to whom this leave request is submitted.
     *
     * Example:
     *
     * requested_to = manager@gmail.com
     */

    @Column(
            name = "requested_to",
            nullable = false
    )
    private String requestedTo;


    // =====================================================
    // LEAVE TYPE
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(
            name = "leave_type",
            nullable = false
    )
    private LeaveType leaveType;


    // =====================================================
    // FROM DATE
    // =====================================================

    @Column(
            name = "from_date",
            nullable = false
    )
    private LocalDate fromDate;


    // =====================================================
    // TO DATE
    // =====================================================

    @Column(
            name = "to_date",
            nullable = false
    )
    private LocalDate toDate;


    // =====================================================
    // REASON
    // =====================================================

    @Column(
            name = "reason",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String reason;


    // =====================================================
    // STATUS
    // =====================================================

    /*
     * PENDING
     * APPROVED
     * REJECTED
     */

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false
    )
    private LeaveStatus status;
}