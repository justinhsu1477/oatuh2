package com.example.authserver.repository;

import com.example.authserver.entity.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OAuthClientRepository extends JpaRepository<OAuthClient, Long> {
    Optional<OAuthClient> findByClientId(String clientId);
}
