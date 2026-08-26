package com.hrms.hrms.modules.auth.token.repository;

import com.hrms.hrms.modules.auth.token.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BlacklistedTokenRepository
        extends JpaRepository<BlacklistedToken, UUID> {

    boolean existsByToken(String token);
}