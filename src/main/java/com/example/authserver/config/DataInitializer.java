package com.example.authserver.config;

import com.example.authserver.entity.OAuthClient;
import com.example.authserver.entity.User;
import com.example.authserver.repository.OAuthClientRepository;
import com.example.authserver.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 應用程式啟動時，自動建立預設的測試帳號與 Client
 * 方便開發與面試 Demo 使用
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final OAuthClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           OAuthClientRepository clientRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // ========== 建立預設使用者（Resource Owner）==========
        if (!userRepository.existsByUsername("testuser")) {
            User user = new User();
            user.setUsername("testuser");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setEmail("testuser@example.com");
            user.setDisplayName("測試使用者");
            userRepository.save(user);
            System.out.println("=== 預設使用者已建立 ===");
            System.out.println("  帳號: testuser");
            System.out.println("  密碼: password123");
        }

        // ========== 建立預設 Client（第三方應用）==========
        if (clientRepository.findByClientId("test-client").isEmpty()) {
            OAuthClient client = new OAuthClient();
            client.setClientId("test-client");
            client.setClientSecret(passwordEncoder.encode("test-secret"));
            client.setClientName("測試應用");
            client.setRedirectUri("http://localhost:3000/callback");
            client.setAllowedScopes("profile email");
            clientRepository.save(client);
            System.out.println("=== 預設 Client 已建立 ===");
            System.out.println("  client_id: test-client");
            System.out.println("  client_secret: test-secret");
            System.out.println("  redirect_uri: http://localhost:3000/callback");
        }
    }
}
