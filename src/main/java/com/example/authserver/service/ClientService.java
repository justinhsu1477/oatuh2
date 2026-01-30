package com.example.authserver.service;

import com.example.authserver.entity.OAuthClient;
import com.example.authserver.repository.OAuthClientRepository;
import com.example.authserver.util.TokenGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ClientService {

    private final OAuthClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(OAuthClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new OAuth client. Returns client_id and plaintext secret (shown only once).
     */
    public Map<String, String> registerClient(String clientName, String redirectUri, String scopes) {
        String clientId = TokenGenerator.generateClientId();
        String rawSecret = TokenGenerator.generateClientSecret();

        OAuthClient client = new OAuthClient();
        client.setClientId(clientId);
        client.setClientSecret(passwordEncoder.encode(rawSecret));
        client.setClientName(clientName);
        client.setRedirectUri(redirectUri);
        client.setAllowedScopes(scopes != null ? scopes : "profile");
        clientRepository.save(client);

        return Map.of(
            "client_id", clientId,
            "client_secret", rawSecret,
            "client_name", clientName
        );
    }
}
