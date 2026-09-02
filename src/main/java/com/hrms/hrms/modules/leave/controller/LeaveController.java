package com.hrms.hrms.modules.leave.controller;

import com.hrms.hrms.modules.leave.dto.LeaveRequest;
import com.hrms.hrms.modules.leave.dto.LeaveResponse;
import com.hrms.hrms.modules.leave.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


// =========================================================
// LEAVE CONTROLLER
// =========================================================

@RestController
@RequestMapping("/api/v1/leaves")
public class LeaveController {

    // DEPENDENCY
    private final LeaveService leaveService;

    // CONSTRUCTOR
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }


    // =========================================================
    // APPLY FOR LEAVE
    // =========================================================

    /*
     * POST
     * /api/v1/leaves
     *
     * Employee email comes from JWT.
     */

    @PostMapping
    public ResponseEntity<LeaveResponse> applyLeave(Authentication authentication,
            @Valid
            @RequestBody
            LeaveRequest request) {
        String email = authentication.getName();
        LeaveResponse response = leaveService.applyLeave(email, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================================
    // GET MY LEAVES
    // =========================================================

    /*
     * GET
     * /api/v1/leaves/my
     *
     * Employee sees only their own leaves.
     */

    @GetMapping("/my")
    public ResponseEntity<List<LeaveResponse>>
    getMyLeaves(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(leaveService.getMyLeaves(email));
    }

    // =========================================================
    // GET MANAGER TEAM LEAVES
    // =========================================================

    /*
     * GET
     * /api/v1/leaves/team
     *
     * Manager sees leaves of employees
     * who report directly to them.
     */


    // Admin Can See All the leaves(PENDING, APPROVED, REJECTED)
    @GetMapping("/admin")
    public ResponseEntity<List<LeaveResponse>> getAllLeavesForAdmin() {

        return ResponseEntity.ok(
                leaveService.getAllLeavesForAdmin()
        );
    }

    @GetMapping("/team")
    public ResponseEntity<List<LeaveResponse>>
    getTeamLeaves(
            Authentication authentication
    ) {

        String managerEmail =
                authentication.getName();


        return ResponseEntity.ok(
                leaveService.getTeamLeaves(
                        managerEmail
                )
        );
    }


    // =========================================================
    // GET PENDING TEAM LEAVES
    // =========================================================

    /*
     * GET
     * /api/v1/leaves/team/pending
     *
     * Manager sees only PENDING requests.
     */

    @GetMapping("/team/pending")
    public ResponseEntity<List<LeaveResponse>>
    getPendingTeamLeaves(
            Authentication authentication
    ) {

        String managerEmail =
                authentication.getName();


        return ResponseEntity.ok(
                leaveService.getPendingTeamLeaves(
                        managerEmail
                )
        );
    }


    // =========================================================
    // APPROVE LEAVE
    // =========================================================

    /*
     * PATCH
     * /api/v1/leaves/{leaveId}/approve
     *
     * Only the employee's reporting manager
     * can approve the leave.
     */

    @PatchMapping("/{leaveId}/approve")
    public ResponseEntity<LeaveResponse>
    approveLeave(

            @PathVariable UUID leaveId,

            Authentication authentication
    ) {

        String managerEmail =
                authentication.getName();


        return ResponseEntity.ok(
                leaveService.approveLeave(
                        leaveId,
                        managerEmail
                )
        );
    }


    // =========================================================
    // REJECT LEAVE
    // =========================================================

    /*
     * PATCH
     * /api/v1/leaves/{leaveId}/reject
     *
     * Only the employee's reporting manager
     * can reject the leave.
     */

    @PatchMapping("/{leaveId}/reject")
    public ResponseEntity<LeaveResponse>
    rejectLeave(

            @PathVariable UUID leaveId,

            Authentication authentication
    ) {

        String managerEmail =
                authentication.getName();


        return ResponseEntity.ok(
                leaveService.rejectLeave(
                        leaveId,
                        managerEmail
                )
        );
    }
}