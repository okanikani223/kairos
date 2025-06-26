package com.github.okanikani.kairos.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 統合テスト用のセキュリティ設定
 * CSRF無効化のみを行い、その他の認証設定は既存の設定を使用
 */
@TestConfiguration
public class IntegrationTestSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain integrationTestSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}