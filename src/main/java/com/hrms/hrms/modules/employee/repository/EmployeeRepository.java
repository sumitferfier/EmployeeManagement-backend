package com.hrms.hrms.modules.employee.repository;

import com.hrms.hrms.modules.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository
        extends JpaRepository<Employee, UUID> {

    // Check whether an employee code already exists
    boolean existsByEmployeeCode(String employeeCode);

    // Find employee using employee code
    Optional<Employee> findByEmployeeCode(String employeeCode);

    // Check whether a User already has an Employee profile
    boolean existsByUserId(UUID userId);

    // Find employee using User ID
    Optional<Employee> findByUserId(UUID userId);

    // Find employee using the email stored in User entity
    Optional<Employee> findByUserEmail(String email);
}