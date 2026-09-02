package com.hrms.hrms.modules.leave.repository;

import com.hrms.hrms.modules.leave.entity.Leave;
import com.hrms.hrms.modules.leave.entity.LeaveStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


public interface LeaveRepository extends JpaRepository<Leave, UUID> {

    @Query("""
            SELECT l
            FROM Leave l
            JOIN l.employee e
            JOIN e.user u
            WHERE LOWER(u.email) = LOWER(:email)
            ORDER BY l.fromDate DESC
            """)
    List<Leave> findByEmployeeEmail(@Param("email") String email);


    @Query("""
            SELECT l
            FROM Leave l
            JOIN l.employee e
            WHERE LOWER(e.reportingManagerEmail) = LOWER(:managerEmail)
            ORDER BY l.fromDate DESC
            """)
    List<Leave> findTeamLeaves(
            @Param("managerEmail") String managerEmail
    );


    @Query("""
            SELECT l
            FROM Leave l
            JOIN l.employee e
            WHERE LOWER(e.reportingManagerEmail) = LOWER(:managerEmail)
            AND l.status = :status
            ORDER BY l.fromDate ASC
            """)
    List<Leave> findTeamLeavesByStatus(
            @Param("managerEmail") String managerEmail,
            @Param("status") LeaveStatus status
    );


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


    // =====================================================
    // ADMIN - ALL LEAVES
    // =====================================================

    @Query("""
            SELECT l
            FROM Leave l
            JOIN FETCH l.employee e
            JOIN FETCH e.user u
            LEFT JOIN FETCH e.department d
            ORDER BY l.fromDate DESC
            """)
    List<Leave> findAllLeavesForAdmin();
}