package com.hrms.hrms.modules.admin.controller;

import com.hrms.hrms.modules.admin.dto.UserAccessRequest;
import com.hrms.hrms.modules.admin.dto.UserAccessResponse;
import com.hrms.hrms.modules.admin.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // GET ALL REGISTERED USERS
    @GetMapping("/users")
    public ResponseEntity<List<UserAccessResponse>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    // GET USER BY EMAIL
    @GetMapping("/users/by-email")
    public ResponseEntity<UserAccessResponse> getUserByEmail(
            @RequestParam String email) {
        return ResponseEntity.ok(adminUserService.getUserByEmail(email));
    }

    // UPDATE USER ACCESS BY EMAIL
    @PatchMapping("/users/access")
    public ResponseEntity<UserAccessResponse> updateUserAccess(
            @RequestParam String email,
            @Valid @RequestBody UserAccessRequest request) {
        return ResponseEntity.ok(adminUserService.updateUserAccess(email, request));
    }
}