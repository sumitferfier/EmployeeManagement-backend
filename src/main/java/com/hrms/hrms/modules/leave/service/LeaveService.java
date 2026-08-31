package com.hrms.hrms.modules.leave.service;

import com.hrms.hrms.common.exception.BadRequestException;
import com.hrms.hrms.common.exception.ResourceNotFoundException;

import com.hrms.hrms.modules.employee.entity.Employee;
import com.hrms.hrms.modules.employee.repository.EmployeeRepository;

import com.hrms.hrms.modules.leave.dto.LeaveRequest;
import com.hrms.hrms.modules.leave.dto.LeaveResponse;
import com.hrms.hrms.modules.leave.entity.Leave;
import com.hrms.hrms.modules.leave.entity.LeaveStatus;
import com.hrms.hrms.modules.leave.repository.LeaveRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
public class LeaveService {


    // =====================================================
    // DEPENDENCIES
    // =====================================================

    private final LeaveRepository leaveRepository;

    private final EmployeeRepository employeeRepository;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public LeaveService(
            LeaveRepository leaveRepository,
            EmployeeRepository employeeRepository
    ) {

        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
    }


    // =====================================================
    // APPLY FOR LEAVE
    // =====================================================

    /*
     * Employee applies for leave.
     *
     * Email comes from JWT.
     *
     * requestedTo is automatically taken from
     * employee.reportingManagerEmail.
     *
     * Employee does NOT send requestedTo
     * in the request body.
     */

    @Transactional
    public LeaveResponse applyLeave(
            String email,
            LeaveRequest request
    ) {


        // =================================================
        // STEP 1: FIND EMPLOYEE
        // =================================================

        Employee employee =
                findEmployeeByEmail(email);


        // =================================================
        // STEP 2: GET REPORTING MANAGER EMAIL
        // =================================================

        String reportingManagerEmail =
                employee.getReportingManagerEmail();


        // =================================================
        // STEP 3: CHECK REPORTING MANAGER
        // =================================================

        if (
                reportingManagerEmail == null
                        ||
                        reportingManagerEmail.isBlank()
        ) {

            throw new BadRequestException(
                    "No reporting manager is assigned to this employee"
            );
        }


        reportingManagerEmail =
                reportingManagerEmail
                        .trim()
                        .toLowerCase();


        // =================================================
        // STEP 4: VERIFY REPORTING MANAGER EXISTS
        // =================================================

        findEmployeeByEmail(
                reportingManagerEmail
        );


        // =================================================
        // STEP 5: VALIDATE DATES
        // =================================================

        LocalDate fromDate =
                request.getFrom();

        LocalDate toDate =
                request.getTo();


        if (fromDate.isAfter(toDate)) {

            throw new BadRequestException(
                    "From date cannot be after To date"
            );
        }


        // =================================================
        // STEP 6: CHECK OVERLAPPING LEAVE
        // =================================================

        long overlappingLeaves =
                leaveRepository.countOverlappingLeaves(
                        email,
                        fromDate,
                        toDate,
                        List.of(
                                LeaveStatus.PENDING,
                                LeaveStatus.APPROVED
                        )
                );


        if (overlappingLeaves > 0) {

            throw new BadRequestException(
                    "You already have a pending or approved leave "
                            + "for the selected dates"
            );
        }


        // =================================================
        // STEP 7: CREATE LEAVE
        // =================================================

        Leave leave =
                Leave.builder()

                        .employee(
                                employee
                        )

                        .leaveType(
                                request.getLeaveType()
                        )

                        .fromDate(
                                fromDate
                        )

                        .toDate(
                                toDate
                        )

                        .reason(
                                request.getReason()
                                        .trim()
                        )

                        /*
                         * IMPORTANT:
                         *
                         * Store reporting manager email
                         * inside leaves.requested_to.
                         */
                        .requestedTo(
                                reportingManagerEmail
                        )

                        .status(
                                LeaveStatus.PENDING
                        )

                        .build();


        // =================================================
        // STEP 8: SAVE
        // =================================================

        Leave savedLeave =
                leaveRepository.save(leave);


        // =================================================
        // STEP 9: RETURN RESPONSE
        // =================================================

        return mapToResponse(
                savedLeave
        );
    }


    // =====================================================
    // GET MY LEAVES
    // =====================================================

    @Transactional(readOnly = true)
    public List<LeaveResponse> getMyLeaves(
            String email
    ) {

        return leaveRepository
                .findByEmployeeEmail(email)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =====================================================
    // GET MANAGER TEAM LEAVES
    // =====================================================

    @Transactional(readOnly = true)
    public List<LeaveResponse> getTeamLeaves(
            String managerEmail
    ) {

        // Verify manager exists.
        findEmployeeByEmail(managerEmail);


        return leaveRepository
                .findTeamLeaves(managerEmail)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =====================================================
    // GET PENDING TEAM LEAVES
    // =====================================================

    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingTeamLeaves(
            String managerEmail
    ) {

        // Verify manager exists.
        findEmployeeByEmail(managerEmail);


        return leaveRepository
                .findTeamLeavesByStatus(
                        managerEmail,
                        LeaveStatus.PENDING
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =====================================================
    // APPROVE LEAVE
    // =====================================================

    @Transactional
    public LeaveResponse approveLeave(
            UUID leaveId,
            String managerEmail
    ) {


        // =================================================
        // STEP 1: FIND LEAVE
        // =================================================

        Leave leave =
                findLeaveById(leaveId);


        // =================================================
        // STEP 2: VERIFY MANAGER
        // =================================================

        verifyReportingManager(
                leave,
                managerEmail
        );


        // =================================================
        // STEP 3: CHECK STATUS
        // =================================================

        if (
                leave.getStatus()
                        != LeaveStatus.PENDING
        ) {

            throw new BadRequestException(
                    "Only pending leave requests can be approved"
            );
        }


        // =================================================
        // STEP 4: APPROVE
        // =================================================

        leave.setStatus(
                LeaveStatus.APPROVED
        );


        Leave updatedLeave =
                leaveRepository.save(leave);


        return mapToResponse(
                updatedLeave
        );
    }


    // =====================================================
    // REJECT LEAVE
    // =====================================================

    @Transactional
    public LeaveResponse rejectLeave(
            UUID leaveId,
            String managerEmail
    ) {


        // =================================================
        // STEP 1: FIND LEAVE
        // =================================================

        Leave leave =
                findLeaveById(leaveId);


        // =================================================
        // STEP 2: VERIFY MANAGER
        // =================================================

        verifyReportingManager(
                leave,
                managerEmail
        );


        // =================================================
        // STEP 3: CHECK STATUS
        // =================================================

        if (
                leave.getStatus()
                        != LeaveStatus.PENDING
        ) {

            throw new BadRequestException(
                    "Only pending leave requests can be rejected"
            );
        }


        // =================================================
        // STEP 4: REJECT
        // =================================================

        leave.setStatus(
                LeaveStatus.REJECTED
        );


        Leave updatedLeave =
                leaveRepository.save(leave);


        return mapToResponse(
                updatedLeave
        );
    }


    // =====================================================
    // FIND EMPLOYEE BY EMAIL
    // =====================================================

    private Employee findEmployeeByEmail(
            String email
    ) {

        return employeeRepository
                .findByUserEmail(
                        email.trim()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with email: "
                                        + email
                        )
                );
    }


    // =====================================================
    // FIND LEAVE BY ID
    // =====================================================

    private Leave findLeaveById(
            UUID leaveId
    ) {

        return leaveRepository
                .findById(leaveId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave not found with ID: "
                                        + leaveId
                        )
                );
    }


    // =====================================================
    // VERIFY REPORTING MANAGER
    // =====================================================

    private void verifyReportingManager(
            Leave leave,
            String managerEmail
    ) {


        // =================================================
        // STEP 1: GET EMPLOYEE
        // =================================================

        Employee employee =
                leave.getEmployee();


        // =================================================
        // STEP 2: GET REPORTING MANAGER EMAIL
        // =================================================

        String reportingManagerEmail =
                employee.getReportingManagerEmail();


        // =================================================
        // STEP 3: CHECK MANAGER ASSIGNED
        // =================================================

        if (
                reportingManagerEmail == null
                        ||
                        reportingManagerEmail.isBlank()
        ) {

            throw new BadRequestException(
                    "Employee does not have a reporting manager"
            );
        }


        // =================================================
        // STEP 4: VERIFY MANAGER EMAIL
        // =================================================

        if (
                !reportingManagerEmail
                        .trim()
                        .equalsIgnoreCase(
                                managerEmail.trim()
                        )
        ) {

            throw new BadRequestException(
                    "You are not the reporting manager "
                            + "for this employee"
            );
        }


        // =================================================
        // STEP 5: VERIFY MANAGER EXISTS
        // =================================================

        findEmployeeByEmail(
                reportingManagerEmail
        );
    }


    // =====================================================
    // ENTITY → RESPONSE
    // =====================================================

    private LeaveResponse mapToResponse(
            Leave leave
    ) {


        Employee employee =
                leave.getEmployee();


        String name =
                employee.getFirstName()
                        + " "
                        + employee.getLastName();


        String email =
                employee.getUser()
                        .getEmail();


        return LeaveResponse.builder()

                .id(
                        leave.getId()
                )

                .name(
                        name
                )

                .email(
                        email
                )

                .leaveType(
                        leave.getLeaveType()
                )

                .from(
                        leave.getFromDate()
                )

                .to(
                        leave.getToDate()
                )

                .reason(
                        leave.getReason()
                )

                /*
                 * Requested To comes from the
                 * Leave table, not Employee table.
                 */
                .requestedTo(
                        leave.getRequestedTo()
                )

                .status(
                        leave.getStatus()
                )

                .build();
    }
}