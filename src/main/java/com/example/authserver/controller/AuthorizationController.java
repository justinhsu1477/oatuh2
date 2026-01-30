package com.example.authserver.controller;

import com.example.authserver.entity.OAuthClient;
import com.example.authserver.entity.User;
import com.example.authserver.repository.UserRepository;
import com.example.authserver.service.AuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthorizationController {

    private final AuthorizationService authorizationService;
    private final UserRepository userRepository;

    public AuthorizationController(AuthorizationService authorizationService,
                                   UserRepository userRepository) {
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/authorize")
    public String authorize(@RequestParam("response_type") String responseType,
                            @RequestParam("client_id") String clientId,
                            @RequestParam("redirect_uri") String redirectUri,
                            @RequestParam(value = "scope", defaultValue = "profile") String scope,
                            @RequestParam(value = "state", required = false) String state,
                            Model model) {
        try {
            OAuthClient client = authorizationService.validateAuthRequest(clientId, redirectUri, responseType);
            model.addAttribute("clientName", client.getClientName());
            model.addAttribute("clientId", clientId);
            model.addAttribute("redirectUri", redirectUri);
            model.addAttribute("scope", scope);
            model.addAttribute("state", state);
            return "consent";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "consent";
        }
    }

    @PostMapping("/authorize")
    public String approveOrDeny(@RequestParam("client_id") String clientId,
                                @RequestParam("redirect_uri") String redirectUri,
                                @RequestParam(value = "scope", defaultValue = "profile") String scope,
                                @RequestParam(value = "state", required = false) String state,
                                @RequestParam("decision") String decision,
                                Authentication authentication) {
        if ("deny".equals(decision)) {
            String redirect = redirectUri + "?error=access_denied";
            if (state != null) redirect += "&state=" + state;
            return "redirect:" + redirect;
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String code = authorizationService.createAuthorizationCode(
                user.getId(), clientId, redirectUri, scope);

        String redirect = redirectUri + "?code=" + code;
        if (state != null) redirect += "&state=" + state;
        return "redirect:" + redirect;
    }
}
