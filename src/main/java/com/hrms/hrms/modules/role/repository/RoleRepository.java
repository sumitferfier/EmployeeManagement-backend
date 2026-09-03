package com.hrms.hrms.modules.role.repository;

import com.hrms.hrms.modules.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByUserId(UUID userId);

    Optional<Role> findByUser_Email(String email);
}