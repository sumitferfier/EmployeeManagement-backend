package com.hrms.hrms.modules.role.service;

import com.hrms.hrms.common.exception.BadRequestException;
import com.hrms.hrms.common.exception.DuplicateResourceException;
import com.hrms.hrms.common.exception.ResourceNotFoundException;
import com.hrms.hrms.modules.auth.repository.UserRepository;
import com.hrms.hrms.modules.role.dto.RoleRequest;
import com.hrms.hrms.modules.role.dto.RoleResponse;
import com.hrms.hrms.modules.role.entity.Role;
import com.hrms.hrms.modules.role.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;


    // CONSTRUCTOR
    public RoleService(
            RoleRepository roleRepository,
            UserRepository userRepository
    ) {

        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }


    // CREATE ROLE
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        // Convert role name to uppercase.
        // This prevents situations like:admin, Admin, ADMIN
        // being treated as different roles.
        String roleName = request.getRoleName().trim().toUpperCase();

        // Check whether role already exists.
        if (roleRepository.existsByRoleName(roleName)) {
            throw new DuplicateResourceException("Role already exists: " + roleName);
        }

        // Create Role entity.
        Role role = Role.builder().roleName(roleName).description(request.getDescription()).build();

        // Save role to PostgreSQL.
        Role savedRole = roleRepository.save(role);

        // Convert entity to response DTO.
        return mapToResponse(savedRole);
    }


    // GET ALL ROLES
    public List<RoleResponse> getAllRoles() {

        // Get all roles from PostgreSQL.
        List<Role> roles = roleRepository.findAll();

        // Convert each Role entity to RoleResponse.
        return roles.stream().map(this::mapToResponse).toList();
    }


    // GET ROLE BY ID
    public RoleResponse getRoleById(UUID id) {

        // Find role by primary key.
        Role role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));

        // Return DTO instead of directly returning entity.
        return mapToResponse(role);
    }

    // UPDATE ROLE
    @Transactional
    public RoleResponse updateRole(
            UUID id,
            RoleRequest request
    ) {
        // Find the role by ID.
        // If the role doesn't exist,
        // ResourceNotFoundException will be thrown.
        Role role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));

        // PROTECT SYSTEM ROLES
        // ADMIN and EMPLOYEE are mandatory system roles.
        // They should not be renamed or modified.
        String existingRoleName = role.getRoleName();

        if (existingRoleName.equalsIgnoreCase("ADMIN")
                || existingRoleName.equalsIgnoreCase("EMPLOYEE")) {

            throw new BadRequestException("System role cannot be modified: " + existingRoleName);
        }

        // PREPARE NEW ROLE NAME
        String roleName = request.getRoleName().trim().toUpperCase();

        // CHECK DUPLICATE ROLE NAME
        roleRepository.findByRoleName(roleName)
                .ifPresent(existingRole -> {

                    // If another role already has this name,
                    // don't allow the update.
                    if (!existingRole.getId().equals(id)) {
                        throw new DuplicateResourceException("Role already exists: " + roleName);
                    }
                });

        // UPDATE ROLE
        role.setRoleName(roleName);
        role.setDescription(request.getDescription());

        // Save changes to PostgreSQL.
        Role updatedRole = roleRepository.save(role);

        // Convert entity to DTO.
        return mapToResponse(updatedRole);
    }

    // DELETE ROLE
    @Transactional
    public void deleteRole(UUID id) {

        // Find role.
        Role role = roleRepository.findById(id).orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));

        // PROTECT SYSTEM ROLES
        String roleName = role.getRoleName();

        if (roleName.equalsIgnoreCase("ADMIN")
                || roleName.equalsIgnoreCase("EMPLOYEE")) {

            throw new BadRequestException("System role cannot be deleted: " + roleName);
        }

        // CHECK WHETHER ROLE IS CURRENTLY IN USE
        boolean roleInUse = userRepository.existsByRoleId(id);

        if (roleInUse) {
            throw new BadRequestException("Role cannot be deleted because it is assigned to users");
        }

        // Delete role.
        roleRepository.delete(role);
    }
    // ENTITY → DTO MAPPER
    // Converts Role entity into RoleResponse.
    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .build();
    }
}