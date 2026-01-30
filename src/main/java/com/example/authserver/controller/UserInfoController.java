package com.example.authserver.controller;

import com.example.authserver.entity.AccessToken;
import com.example.authserver.entity.User;
import com.example.authserver.repository.UserRepository;
import com.example.authserver.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class UserInfoController {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public UserInfoController(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @GetMapping("/userinfo")
    public ResponseEntity<?> userinfo(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
        }

        String bearerToken = authHeader.substring(7);
        AccessToken token = tokenService.validateAccessToken(bearerToken);
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
        }

        User user = userRepository.findById(token.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "user_not_found"));
        }

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("sub", String.valueOf(user.getId()));
        info.put("name", user.getUsername());
        info.put("display_name", user.getDisplayName());

        // If scope includes email
        String scope = token.getScope();
        if (scope != null && scope.contains("email")) {
            info.put("email", user.getEmail());
        }

        return ResponseEntity.ok(info);
    }
}
