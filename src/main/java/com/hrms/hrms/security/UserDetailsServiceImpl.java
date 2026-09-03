package com.hrms.hrms.security;
import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    // =====================================================
    // DEPENDENCY
    // =====================================================

    private final UserRepository userRepository;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public UserDetailsServiceImpl(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }


    // =====================================================
    // LOAD USER BY EMAIL
    // =====================================================

    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        // =================================================
        // STEP 1: FIND USER BY EMAIL
        // =================================================

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );


        // =================================================
        // STEP 2: CREATE AUTHORITIES
        // =================================================

        List<GrantedAuthority> authorities =
                new ArrayList<>();


        /*
         * Role record can be null.
         *
         * Example:
         * A newly registered user may exist in the users table
         * but Admin has not yet assigned access.
         */
        if (user.getRole() != null) {

            // =============================================
            // ADMIN ACCESS
            // =============================================

            if (user.getRole().isAdmin()) {

                authorities.add(
                        new SimpleGrantedAuthority(
                                "ROLE_ADMIN"
                        )
                );
            }


            // =============================================
            // EMPLOYEE ACCESS
            // =============================================

            if (user.getRole().isEmployee()) {

                authorities.add(
                        new SimpleGrantedAuthority(
                                "ROLE_EMPLOYEE"
                        )
                );
            }
        }


        // =================================================
        // STEP 3: RETURN SPRING SECURITY USER
        // =================================================

        return new org.springframework.security.core.userdetails.User(

                user.getEmail(),

                user.getPassword(),

                authorities
        );
    }
}