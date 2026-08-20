package com.hrms.hrms.modules.department.repository;

import com.hrms.hrms.modules.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository
        extends JpaRepository<Department, UUID> {

    // Find department by name.
    Optional<Department> findByDepartmentName(
            String departmentName
    );

    // Check whether department already exists.
    boolean existsByDepartmentName(
            String departmentName
    );
}