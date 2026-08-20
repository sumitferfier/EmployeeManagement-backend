package com.hrms.hrms.modules.role.repository;

import com.hrms.hrms.modules.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository
        extends JpaRepository<Role, UUID> {

    // Find a role using its name.
    Optional<Role> findByRoleName(String roleName);

    // Check whether a role name already exists.
    boolean existsByRoleName(String roleName);
}