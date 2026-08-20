package com.hrms.hrms.modules.employee.controller;

import com.hrms.hrms.modules.employee.dto.EmployeeRequest;
import com.hrms.hrms.modules.employee.dto.EmployeeResponse;
import com.hrms.hrms.modules.employee.service.EmployeeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


/*REST Controller for Employee Management.
 * Base URL: /api/v1/employees
 * All endpoints in this controller are currently
 * protected as ADMIN-only in SecurityConfig.*/
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // CREATE EMPLOYEE
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET ALL EMPLOYEES
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>>
    getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // GET EMPLOYEE BY UUID
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse>
    getEmployeeById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // UPDATE EMPLOYEE
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse>
    updateEmployee(@PathVariable UUID id, @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    // DELETE EMPLOYEE
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }
}