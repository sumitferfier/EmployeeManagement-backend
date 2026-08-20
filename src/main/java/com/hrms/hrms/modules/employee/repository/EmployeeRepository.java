package com.hrms.hrms.modules.employee.repository;

import com.hrms.hrms.modules.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository
        extends JpaRepository<Employee, UUID> {

    // Check employee code.
    boolean existsByEmployeeCode(String employeeCode);

    // Find employee by employee code.
    Optional<Employee> findByEmployeeCode(
            String employeeCode
    );

    // Check whether User already has an Employee profile.
    boolean existsByUserId(
            UUID userId
    );

    // Find Employee profile using User ID.
    Optional<Employee> findByUserId(
            UUID userId
    );
}