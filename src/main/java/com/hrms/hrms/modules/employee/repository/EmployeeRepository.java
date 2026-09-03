package com.hrms.hrms.modules.employee.repository;

import com.hrms.hrms.modules.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository
        extends JpaRepository<Employee, UUID> {

    // Find employee using User email
    Optional<Employee> findByUser_Email(String email);

    // Find team using reporting manager email
    List<Employee> findByReportingManagerEmail(String reportingManagerEmail);
}