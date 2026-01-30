package com.example.authserver.controller;

import com.example.authserver.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ClientRegistrationController {

    private final ClientService clientService;

    public ClientRegistrationController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/clients")
    public ResponseEntity<Map<String, String>> registerClient(
            @RequestParam String clientName,
            @RequestParam String redirectUri,
            @RequestParam(required = false) String scopes) {
        Map<String, String> result = clientService.registerClient(clientName, redirectUri, scopes);
        return ResponseEntity.ok(result);
    }
}
