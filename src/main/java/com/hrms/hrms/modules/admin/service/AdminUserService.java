package com.hrms.hrms.modules.admin.service;

import com.hrms.hrms.common.exception.ResourceNotFoundException;
import com.hrms.hrms.common.exception.BadRequestException;
import com.hrms.hrms.modules.admin.dto.UserAccessRequest;
import com.hrms.hrms.modules.admin.dto.UserAccessResponse;
import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder; // for restrict oun access modification
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AdminUserService {
    private final UserRepository userRepository;
    public AdminUserService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    // GET ALL REGISTERED USERS
    @Transactional(readOnly = true)
    public List<UserAccessResponse> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET USER BY EMAIL
    @Transactional(readOnly = true)
    public UserAccessResponse getUserByEmail(
            String email
    ) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return mapToResponse(user);
    }


    // =========================================================
    // UPDATE USER ACCESS BY EMAIL
    // =========================================================

    @Transactional
    public UserAccessResponse updateUserAccess(
            String email,
            UserAccessRequest request
    ) {

        // =====================================================
        // STEP 1: GET LOGGED-IN ADMIN EMAIL
        // =====================================================

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String loggedInAdminEmail =
                authentication.getName();


        // =====================================================
        // STEP 2: PREVENT ADMIN FROM CHANGING OWN ACCESS
        if (loggedInAdminEmail.equalsIgnoreCase(email)) {
            throw new BadRequestException("You cannot change your own access permissions.");
        }

        // STEP 3: FIND TARGET USER BY EMAIL
                User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );


        // =====================================================
        // STEP 4: UPDATE ACCESS
        // =====================================================

        user.setAdmin(
                request.getIsAdmin()
        );

        user.setEmployee(
                request.getIsEmployee()
        );


        // =====================================================
        // STEP 5: SAVE USER
        // =====================================================

        User updatedUser =
                userRepository.save(user);


        // =====================================================
        // STEP 6: RETURN RESPONSE
        // =====================================================

        return UserAccessResponse.builder()

                .userId(updatedUser.getId())

                .email(updatedUser.getEmail())

                .isAdmin(updatedUser.isAdmin())

                .isEmployee(updatedUser.isEmployee())

                .status(updatedUser.getStatus().name())

                .message(
                        "User access updated successfully"
                )

                .build();
    }


    // =========================================================
    // ENTITY → RESPONSE DTO
    // =========================================================

    private UserAccessResponse mapToResponse(
            User user
    ) {

        return UserAccessResponse.builder()

                .userId(
                        user.getId()
                )

                .email(
                        user.getEmail()
                )

                .isAdmin(
                        user.isAdmin()
                )

                .isEmployee(
                        user.isEmployee()
                )

                .status(
                        user.getStatus().name()
                )

                .build();
    }
}