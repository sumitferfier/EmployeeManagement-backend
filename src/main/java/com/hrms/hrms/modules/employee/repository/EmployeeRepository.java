package com.hrms.hrms.modules.employee.repository;

import com.hrms.hrms.modules.employee.entity.Employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface EmployeeRepository
        extends JpaRepository<Employee, UUID> {

    // Check whether employee code already exists
    boolean existsByEmployeeCode(String employeeCode);

}