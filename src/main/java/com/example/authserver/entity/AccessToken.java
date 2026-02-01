package com.example.authserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "access_tokens")
public class AccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "access_token", unique = true, nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", unique = true)
    private String refreshToken;

    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 500)
    private String scope;

    @Column(name = "access_expires", nullable = false)
    private LocalDateTime accessExpires;

    @Column(name = "refresh_expires")
    private LocalDateTime refreshExpires;

    private boolean revoked = false; // 撤銷

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
