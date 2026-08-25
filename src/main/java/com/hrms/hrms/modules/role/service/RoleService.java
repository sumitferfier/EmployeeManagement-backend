package com.hrms.hrms.modules.role.service;

import com.hrms.hrms.common.exception.DuplicateResourceException;
import com.hrms.hrms.common.exception.ResourceNotFoundException;

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

    // =========================================================
    // DEPENDENCY
    // =========================================================

    private final RoleRepository roleRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public RoleService(
            RoleRepository roleRepository
    ) {
        this.roleRepository = roleRepository;
    }


    // =========================================================
    // CREATE ROLE
    // =========================================================

    @Transactional
    public RoleResponse createRole(
            RoleRequest request
    ) {

        // Convert role name to uppercase.
        String roleName = request
                .getRoleName()
                .trim()
                .toUpperCase();


        // Check whether role already exists.
        if (roleRepository.existsByRoleName(roleName)) {

            throw new DuplicateResourceException(
                    "Role already exists: " + roleName
            );
        }


        // Create Role entity.
        Role role = Role.builder()

                .roleName(roleName)

                .description(
                        request.getDescription()
                )

                .build();


        // Save role.
        Role savedRole =
                roleRepository.save(role);


        // Return response.
        return mapToResponse(savedRole);
    }


    // =========================================================
    // GET ALL ROLES
    // =========================================================

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {

        return roleRepository

                .findAll()

                .stream()

                .map(this::mapToResponse)

                .toList();
    }


    // =========================================================
    // GET ROLE BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(
            UUID id
    ) {

        Role role = roleRepository

                .findById(id)

                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with ID: " + id
                        )
                );


        return mapToResponse(role);
    }


    // =========================================================
    // UPDATE ROLE
    // =========================================================

    @Transactional
    public RoleResponse updateRole(

            UUID id,

            RoleRequest request
    ) {

        // Find existing role.
        Role role = roleRepository

                .findById(id)

                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with ID: " + id
                        )
                );


        // Prepare new role name.
        String roleName = request

                .getRoleName()

                .trim()

                .toUpperCase();


        // Check duplicate role name.
        roleRepository

                .findByRoleName(roleName)

                .ifPresent(existingRole -> {

                    // Another role cannot have the same name.
                    if (!existingRole.getId().equals(id)) {

                        throw new DuplicateResourceException(
                                "Role already exists: " + roleName
                        );
                    }
                });


        // Update role.
        role.setRoleName(roleName);

        role.setDescription(
                request.getDescription()
        );


        // Save updated role.
        Role updatedRole =
                roleRepository.save(role);


        return mapToResponse(updatedRole);
    }


    // =========================================================
    // DELETE ROLE
    // =========================================================

    @Transactional
    public void deleteRole(
            UUID id
    ) {

        // Find role.
        Role role = roleRepository

                .findById(id)

                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with ID: " + id
                        )
                );


        // No user-role relationship exists anymore.
        // Therefore, the role can be deleted directly.

        roleRepository.delete(role);
    }


    // =========================================================
    // ENTITY → DTO MAPPER
    // =========================================================

    private RoleResponse mapToResponse(
            Role role
    ) {

        return RoleResponse.builder()

                .id(
                        role.getId()
                )

                .roleName(
                        role.getRoleName()
                )

                .description(
                        role.getDescription()
                )

                .build();
    }
}