package com.example.authserver.service;

import com.example.authserver.entity.AuthorizationCode;
import com.example.authserver.entity.OAuthClient;
import com.example.authserver.repository.AuthorizationCodeRepository;
import com.example.authserver.repository.OAuthClientRepository;
import com.example.authserver.util.TokenGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthorizationService {

    private final OAuthClientRepository clientRepository;
    private final AuthorizationCodeRepository codeRepository;

    public AuthorizationService(OAuthClientRepository clientRepository,
                                AuthorizationCodeRepository codeRepository) {
        this.clientRepository = clientRepository;
        this.codeRepository = codeRepository;
    }

    public OAuthClient validateAuthRequest(String clientId, String redirectUri, String responseType) {
        if (!"code".equals(responseType)) {
            throw new IllegalArgumentException("Unsupported response_type: " + responseType);
        }
        OAuthClient client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown client_id"));
        if (!client.getRedirectUri().equals(redirectUri)) {
            throw new IllegalArgumentException("Invalid redirect_uri");
        }
        return client;
    }

    public String createAuthorizationCode(Long userId, String clientId, String redirectUri, String scope) {
        String code = TokenGenerator.generateCode();
        AuthorizationCode authCode = new AuthorizationCode();
        authCode.setCode(code);
        authCode.setClientId(clientId);
        authCode.setUserId(userId);
        authCode.setRedirectUri(redirectUri);
        authCode.setScope(scope);
        authCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        codeRepository.save(authCode);
        return code;
    }
}
