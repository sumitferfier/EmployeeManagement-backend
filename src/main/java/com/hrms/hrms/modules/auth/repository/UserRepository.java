package com.hrms.hrms.modules.auth.repository;

import com.hrms.hrms.modules.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository
        extends JpaRepository<User, UUID> {

    // Find user by email.
    Optional<User> findByEmail(String email);

    // Check whether email is already registered.
    boolean existsByEmail(String email);

    // Check whether any user is assigned to a particular role.
    // This will be used before deleting a role.
    boolean existsByRoleId(UUID roleId);
}