package com.hrms.hrms.modules.role.repository;

import com.hrms.hrms.modules.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository; // implement JPA repository for auto insert tables in pgadmin

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);
}