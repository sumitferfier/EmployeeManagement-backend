package com.hrms.hrms.modules.leave.dto;

import com.hrms.hrms.modules.leave.entity.LeaveType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;

import java.time.LocalDate;


// =========================================================
// LEAVE REQUEST DTO
// =========================================================

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {


    // =====================================================
    // LEAVE TYPE
    // =====================================================

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;


    // =====================================================
    // FROM DATE
    // =====================================================

    @NotNull(message = "From date is required")
    private LocalDate from;


    // =====================================================
    // TO DATE
    // =====================================================

    @NotNull(message = "To date is required")
    private LocalDate to;


    // =====================================================
    // REASON
    // =====================================================

    @NotBlank(message = "Reason is required")
    private String reason;
}