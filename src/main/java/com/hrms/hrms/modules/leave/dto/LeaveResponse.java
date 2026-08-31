package com.hrms.hrms.modules.leave.dto;

import com.hrms.hrms.modules.leave.entity.LeaveStatus;
import com.hrms.hrms.modules.leave.entity.LeaveType;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;


// =========================================================
// LEAVE RESPONSE DTO
// =========================================================

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveResponse {

    // LEAVE ID
    private UUID id;


    // =====================================================
    // EMPLOYEE DETAILS
    // =====================================================

    private String name;

    private String email;


    // =====================================================
    // REQUESTED TO
    // =====================================================

    /*
     * Reporting manager email.
     */

    private String requestedTo;


    // =====================================================
    // LEAVE DETAILS
    // =====================================================

    private LeaveType leaveType;

    private LocalDate from;

    private LocalDate to;

    private String reason;

    private LeaveStatus status;
}