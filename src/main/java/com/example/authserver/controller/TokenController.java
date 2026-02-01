package com.example.authserver.controller;

import com.example.authserver.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(@RequestParam("grant_type") String grantType,
                                   @RequestParam(value = "code", required = false) String code,
                                   @RequestParam(value = "redirect_uri", required = false) String redirectUri,
                                   @RequestParam("client_id") String clientId,
                                   @RequestParam("client_secret") String clientSecret,
                                   @RequestParam(value = "refresh_token", required = false) String refreshToken) {
        try {
            Map<String, Object> result;
            if ("authorization_code".equals(grantType)) { // 使用者剛完成授權，Client 拿到 code 後立刻呼叫
                result = tokenService.exchangeCode(code, clientId, clientSecret, redirectUri);
            } else if ("refresh_token".equals(grantType)) { // access_token 過期了，Client 用 refresh_token 換新的
                result = tokenService.refreshToken(refreshToken, clientId, clientSecret);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "unsupported_grant_type"));
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_request", "error_description", e.getMessage()));
        }
    }
}
