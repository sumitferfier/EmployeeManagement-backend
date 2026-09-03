package com.hrms.hrms.modules.employee.controller;

import com.hrms.hrms.modules.employee.dto.EmployeeResponse;
import com.hrms.hrms.modules.employee.dto.EmployeeUpdateRequest;
import com.hrms.hrms.modules.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(
            EmployeeService employeeService
    ) {
        this.employeeService = employeeService;
    }

    /*
     * ============================================================
     * GET ALL REGISTERED USERS
     * ============================================================
     *
     * GET /api/v1/employees
     *
     * Returns EVERY registered user.
     *
     * Employee profile information is included when available.
     */
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(
                employeeService.getAllEmployees()
        );
    }

    /*
     * ============================================================
     * GET USER BY EMAIL
     * ============================================================
     *
     * GET /api/v1/employees?email=sumit@ferfier.com
     */
    @GetMapping(params = "email")
    public ResponseEntity<EmployeeResponse> getEmployeeByEmail(
            @RequestParam String email
    ) {

        return ResponseEntity.ok(
                employeeService.getEmployeeByEmail(email)
        );
    }

    /*
     * ============================================================
     * GET LOGGED-IN USER
     * ============================================================
     *
     * GET /api/v1/employees/me
     *
     * Email comes from JWT Authentication.
     */
    @GetMapping("/me")
    public ResponseEntity<EmployeeResponse> getMyProfile(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                employeeService.getMyProfile(email)
        );
    }

    /*
     * ============================================================
     * UPDATE EMPLOYEE
     * ============================================================
     *
     * PATCH /api/v1/employees?email=...
     */
    @PatchMapping
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @RequestParam String email,
            @Valid @RequestBody EmployeeUpdateRequest request
    ) {

        return ResponseEntity.ok(
                employeeService.updateEmployee(
                        email,
                        request
                )
        );
    }
}