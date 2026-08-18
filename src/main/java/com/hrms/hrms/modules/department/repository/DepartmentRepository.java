package com.hrms.hrms.modules.department.repository;

import com.hrms.hrms.modules.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

}