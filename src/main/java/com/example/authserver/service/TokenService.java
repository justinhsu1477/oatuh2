package com.example.authserver.service;

import com.example.authserver.entity.AccessToken;
import com.example.authserver.entity.AuthorizationCode;
import com.example.authserver.repository.AccessTokenRepository;
import com.example.authserver.repository.AuthorizationCodeRepository;
import com.example.authserver.repository.OAuthClientRepository;
import com.example.authserver.util.TokenGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TokenService {

    private final AuthorizationCodeRepository codeRepository;
    private final AccessTokenRepository tokenRepository;
    private final OAuthClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenService(AuthorizationCodeRepository codeRepository,
                        AccessTokenRepository tokenRepository,
                        OAuthClientRepository clientRepository,
                        PasswordEncoder passwordEncoder) {
        this.codeRepository = codeRepository;
        this.tokenRepository = tokenRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> exchangeCode(String code, String clientId, String clientSecret, String redirectUri) {
        // Authenticate client
        var client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown client_id"));
        if (!passwordEncoder.matches(clientSecret, client.getClientSecret())) {
            throw new IllegalArgumentException("Invalid client_secret");
        }

        // Validate authorization code
        AuthorizationCode authCode = codeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid authorization code"));
        if (authCode.isUsed()) {
            throw new IllegalArgumentException("Authorization code already used");
        }
        if (authCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Authorization code expired");
        }
        if (!authCode.getClientId().equals(clientId)) {
            throw new IllegalArgumentException("Client mismatch");
        }
        if (!authCode.getRedirectUri().equals(redirectUri)) {
            throw new IllegalArgumentException("redirect_uri mismatch");
        }

        // Mark code as used
        authCode.setUsed(true);
        codeRepository.save(authCode);

        // Generate tokens
        return createTokens(clientId, authCode.getUserId(), authCode.getScope());
    }

    public Map<String, Object> refreshToken(String refreshToken, String clientId, String clientSecret) {
        var client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown client_id"));
        if (!passwordEncoder.matches(clientSecret, client.getClientSecret())) {
            throw new IllegalArgumentException("Invalid client_secret");
        }

        AccessToken existing = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh_token"));
        if (existing.isRevoked()) {
            throw new IllegalArgumentException("Token revoked");
        }
        if (existing.getRefreshExpires() != null && existing.getRefreshExpires().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token expired");
        }
        if (!existing.getClientId().equals(clientId)) {
            throw new IllegalArgumentException("Client mismatch");
        }

        // Revoke old token
        existing.setRevoked(true);
        tokenRepository.save(existing);

        return createTokens(clientId, existing.getUserId(), existing.getScope());
    }

    public AccessToken validateAccessToken(String bearerToken) {
        AccessToken token = tokenRepository.findByAccessToken(bearerToken).orElse(null);
        if (token == null || token.isRevoked() || token.getAccessExpires().isBefore(LocalDateTime.now())) {
            return null;
        }
        return token;
    }

    private Map<String, Object> createTokens(String clientId, Long userId, String scope) {
        String accessTokenStr = TokenGenerator.generateToken();
        String refreshTokenStr = TokenGenerator.generateToken();

        AccessToken token = new AccessToken();
        token.setAccessToken(accessTokenStr);
        token.setRefreshToken(refreshTokenStr);
        token.setClientId(clientId);
        token.setUserId(userId);
        token.setScope(scope);
        token.setAccessExpires(LocalDateTime.now().plusHours(1));
        token.setRefreshExpires(LocalDateTime.now().plusDays(30));
        tokenRepository.save(token);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", accessTokenStr);
        response.put("token_type", "Bearer");
        response.put("expires_in", 3600);
        response.put("refresh_token", refreshTokenStr);
        response.put("scope", scope);
        return response;
    }
}
