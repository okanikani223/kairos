package com.github.okanikani.kairos.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * AuthControllerTest専用のセキュリティ設定
 * 認証エンドポイントを無認証でアクセス可能にしてコントローラーテストを実行可能にする
 */
@TestConfiguration
@EnableWebSecurity
public class AuthControllerTestSecurityConfig {

    @Bean
    public SecurityFilterChain authControllerTestSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}