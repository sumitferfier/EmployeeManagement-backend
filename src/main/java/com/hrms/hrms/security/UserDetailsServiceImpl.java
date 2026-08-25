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
    private final UserRepository userRepository;


    public UserDetailsServiceImpl(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        // FIND USER BY EMAIL
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        // CREATE AUTHORITIES FROM BOOLEAN ACCESS
        List<GrantedAuthority> authorities = new ArrayList<>();


        //isAdmin = true, role_admin
                 if (user.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        //isEmployee = true, role_employee
        if (user.isEmployee()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
        }

        // RETURN SPRING SECURITY USER
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}