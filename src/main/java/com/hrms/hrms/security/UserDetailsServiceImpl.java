package com.hrms.hrms.security;

import com.hrms.hrms.modules.auth.entity.User;
import com.hrms.hrms.modules.auth.entity.UserStatus;
import com.hrms.hrms.modules.auth.repository.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // Find user using email
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        // Convert database user into Spring Security user
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPassword())
                // Add role as authority
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName())))
                .disabled(user.getStatus() == UserStatus.INACTIVE)
                .build();
    }
}