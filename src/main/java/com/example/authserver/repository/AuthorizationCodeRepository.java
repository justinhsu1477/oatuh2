package com.example.authserver.repository;

import com.example.authserver.entity.AuthorizationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthorizationCodeRepository extends JpaRepository<AuthorizationCode, Long> {
    Optional<AuthorizationCode> findByCode(String code);
}
