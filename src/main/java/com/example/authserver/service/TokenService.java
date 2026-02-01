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
import java.util.*;

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
        // 驗證 Client 身份
        var client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("無效的 client_id"));
        if (!passwordEncoder.matches(clientSecret, client.getClientSecret())) {
            throw new IllegalArgumentException("client_secret 驗證失敗");
        }

        // 驗證 Authorization Code
        AuthorizationCode authCode = codeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("無效的授權碼"));
        if (authCode.isUsed()) {
            throw new IllegalArgumentException("授權碼已被使用（一次性）");
        }
        if (authCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("授權碼已過期（10 分鐘有效）");
        }
        if (!authCode.getClientId().equals(clientId)) {
            throw new IllegalArgumentException("授權碼的 client_id 與請求不一致");
        }
        if (!authCode.getRedirectUri().equals(redirectUri)) {
            throw new IllegalArgumentException("redirect_uri 與授權時不一致");
        }

        // 標記授權碼已使用
        authCode.setUsed(true);
        codeRepository.save(authCode);

        // 產生 Token
        return createTokens(clientId, authCode.getUserId(), authCode.getScope());
    }

    public Map<String, Object> refreshToken(String refreshToken, String clientId, String clientSecret) {
        // 驗證 Client 身份
        var client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("無效的 client_id"));
        if (!passwordEncoder.matches(clientSecret, client.getClientSecret())) {
            throw new IllegalArgumentException("client_secret 驗證失敗");
        }

        // 驗證 Refresh Token
        AccessToken existing = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("無效的 refresh_token"));
        if (existing.isRevoked()) {
            throw new IllegalArgumentException("該 Token 已被撤銷");
        }
        if (existing.getRefreshExpires() != null && existing.getRefreshExpires().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh Token 已過期（30 天有效）");
        }
        if (!existing.getClientId().equals(clientId)) {
            throw new IllegalArgumentException("client_id 與原 Token 不一致");
        }

        // Refresh Token Rotation：撤銷舊 token，發放新 token
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

    // 產生全新的 access_token 和 refresh_token
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
