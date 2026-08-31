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


    // =========================================================
    // GET ALL EMPLOYEES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>>
    getAllEmployees() {

        return ResponseEntity.ok(
                employeeService.getAllEmployees()
        );
    }


    // =========================================================
    // GET EMPLOYEE BY EMAIL
    // =========================================================

    @GetMapping(params = "email")
    public ResponseEntity<EmployeeResponse>
    getEmployeeByEmail(

            @RequestParam String email
    ) {

        return ResponseEntity.ok(
                employeeService.getEmployeeByEmail(email)
        );
    }


    // =========================================================
    // GET LOGGED-IN USER PROFILE
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<EmployeeResponse>
    getMyProfile(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                employeeService.getMyProfile(
                        authentication.getName()
                )
        );
    }


    // =========================================================
    // UPDATE EMPLOYEE MANAGEMENT DETAILS
    // =========================================================

    @PatchMapping
    public ResponseEntity<EmployeeResponse>
    updateEmployee(

            @RequestParam String email,

            @Valid
            @RequestBody
            EmployeeUpdateRequest request
    ) {

        return ResponseEntity.ok(
                employeeService.updateEmployee(
                        email,
                        request
                )
        );
    }


    // =========================================================
    // GET MANAGER TEAM
    // =========================================================

    @GetMapping("/team")
    public ResponseEntity<List<EmployeeResponse>>
    getEmployeeTeam(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                employeeService.getEmployeeTeam(
                        authentication.getName()
                )
        );
    }
}