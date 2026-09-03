package com.hrms.hrms.modules.role.controller;

import com.hrms.hrms.modules.role.dto.RoleResponse;
import com.hrms.hrms.modules.role.dto.UserAccessRequest;
import com.hrms.hrms.modules.role.dto.UserAccessResponse;
import com.hrms.hrms.modules.role.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // =========================================================
    // GET ALL USER ACCESS RECORDS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {

        return ResponseEntity.ok(
                roleService.getAllRoles()
        );
    }

    // =========================================================
    // GET USER ACCESS BY ROLE UUID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRoleById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                roleService.getRoleById(id)
        );
    }

    // =========================================================
    // UPDATE USER ACCESS
    // =========================================================

    @PatchMapping("/access")
    public ResponseEntity<UserAccessResponse> updateUserAccess(
            @RequestParam String email,
            @Valid @RequestBody UserAccessRequest request
    ) {

        return ResponseEntity.ok(
                roleService.updateUserAccess(email, request)
        );
    }
}