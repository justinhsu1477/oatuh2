package com.example.authserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "authorization_codes")
public class AuthorizationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 128)
    private String code;

    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 外鍵關聯：authorization_codes.user_id → users.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "redirect_uri", nullable = false, length = 500)
    private String redirectUri; // 導向回前端

    @Column(length = 500)
    private String scope;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private boolean used = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
