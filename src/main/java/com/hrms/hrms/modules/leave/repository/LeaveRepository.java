package com.hrms.hrms.modules.leave.repository;

import com.hrms.hrms.modules.leave.entity.Leave;
import com.hrms.hrms.modules.leave.entity.LeaveStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


public interface LeaveRepository
        extends JpaRepository<Leave, UUID> {


    // =====================================================
    // EMPLOYEE LEAVE HISTORY
    // =====================================================

    @Query("""
            SELECT l
            FROM Leave l
            JOIN l.employee e
            JOIN e.user u
            WHERE LOWER(u.email) = LOWER(:email)
            ORDER BY l.fromDate DESC
            """)
    List<Leave> findByEmployeeEmail(
            @Param("email") String email
    );


    // =====================================================
    // MANAGER TEAM LEAVES
    // =====================================================

    /*
     * Employee now stores reporting manager
     * as an email instead of Employee relationship.
     *
     * employees.reporting_manager_email
     *              ↓
     * manager's user email
     */

    @Query("""
            SELECT l
            FROM Leave l
            JOIN l.employee e
            WHERE LOWER(e.reportingManagerEmail)
                  = LOWER(:managerEmail)
            ORDER BY l.fromDate DESC
            """)
    List<Leave> findTeamLeaves(
            @Param("managerEmail") String managerEmail
    );


    // =====================================================
    // PENDING TEAM LEAVES
    // =====================================================

    @Query("""
            SELECT l
            FROM Leave l
            JOIN l.employee e
            WHERE LOWER(e.reportingManagerEmail)
                  = LOWER(:managerEmail)
            AND l.status = :status
            ORDER BY l.fromDate ASC
            """)
    List<Leave> findTeamLeavesByStatus(
            @Param("managerEmail") String managerEmail,
            @Param("status") LeaveStatus status
    );


    // =====================================================
    // CHECK OVERLAPPING LEAVE
    // =====================================================

    @Query("""
            SELECT COUNT(l)
            FROM Leave l
            JOIN l.employee e
            JOIN e.user u
            WHERE LOWER(u.email) = LOWER(:email)
            AND l.status IN :statuses
            AND l.fromDate <= :toDate
            AND l.toDate >= :fromDate
            """)
    long countOverlappingLeaves(
            @Param("email") String email,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("statuses") List<LeaveStatus> statuses
    );
}