package com.hrms.hrms.modules.employee.repository;

import com.hrms.hrms.modules.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository; // JPA auto insert tables in pg admin

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByUserId(Long userId);

    boolean existsByEmployeeCode(String employeeCode);
}