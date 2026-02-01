package com.example.authserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 跳過 csrf 檢查
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/token", "/h2-console/**")
                )

                // 允許同源的網頁 進入
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin) // for H2 console
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/css/**", "/h2-console/**").permitAll()
                        .requestMatchers("/token").permitAll()
                        .requestMatchers("/userinfo").permitAll()
                        .requestMatchers("/authorize").authenticated()  // 驗證 才能進入
                        .requestMatchers("/clients").authenticated() // 驗證 才能進入
                        .anyRequest().authenticated() // 驗證 才能進入
                )
                .formLogin(form -> form
                        .loginPage("/login") //指定自訂的登入頁面路徑為 /login
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
