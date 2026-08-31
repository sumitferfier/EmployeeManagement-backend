package com.hrms.hrms.modules.employee.repository;
import com.hrms.hrms.modules.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// EMPLOYEE REPOSITORY
public interface EmployeeRepository
        extends JpaRepository<Employee, UUID> {

    // FIND EMPLOYEE USING USER EMAIL
    Optional<Employee> findByUserEmail(
            String reportingManagerEmail
    );

    // FIND TEAM USING MANAGER EMAIL
    List<Employee> findByReportingManagerEmail(
            String reportingManagerEmail
    );
}