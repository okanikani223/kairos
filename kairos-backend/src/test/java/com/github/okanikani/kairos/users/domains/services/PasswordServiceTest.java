package com.github.okanikani.kairos.users.domains.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

/**
 * PasswordServiceのテスト
 * パスワード暗号化・検証機能の動作を確認
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("PasswordService")
class PasswordServiceTest {
    
    private PasswordService passwordService;
    
    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }
    
    @Test
    @DisplayName("hashPassword_正常ケース_BCryptハッシュが生成される")
    void hashPassword_正常ケース_BCryptハッシュが生成される() {
        // Given
        String rawPassword = "TestPassword123!";
        
        // When
        String hashedPassword = passwordService.hashPassword(rawPassword);
        
        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(hashedPassword).isNotEqualTo(rawPassword);
        assertThat(hashedPassword).startsWith("$2a$12$"); // BCrypt強度12の形式
        assertThat(hashedPassword).hasSize(60); // BCryptハッシュは60文字
    }
    
    @Test
    @DisplayName("hashPassword_同じパスワード_異なるハッシュが生成される")
    void hashPassword_同じパスワード_異なるハッシュが生成される() {
        // Given
        String rawPassword = "TestPassword123!";
        
        // When
        String hash1 = passwordService.hashPassword(rawPassword);
        String hash2 = passwordService.hashPassword(rawPassword);
        
        // Then
        assertThat(hash1).isNotEqualTo(hash2); // ソルトにより異なるハッシュ
    }
    
    @Test
    @DisplayName("verifyPassword_正しいパスワード_trueが返される")
    void verifyPassword_正しいパスワード_trueが返される() {
        // Given
        String rawPassword = "TestPassword123!";
        String hashedPassword = passwordService.hashPassword(rawPassword);
        
        // When
        boolean result = passwordService.verifyPassword(rawPassword, hashedPassword);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("verifyPassword_間違ったパスワード_falseが返される")
    void verifyPassword_間違ったパスワード_falseが返される() {
        // Given
        String correctPassword = "TestPassword123!";
        String wrongPassword = "WrongPassword456!";
        String hashedPassword = passwordService.hashPassword(correctPassword);
        
        // When
        boolean result = passwordService.verifyPassword(wrongPassword, hashedPassword);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("isPasswordUpgradeRequired_新しいハッシュ_falseが返される")
    void isPasswordUpgradeRequired_新しいハッシュ_falseが返される() {
        // Given
        String password = "TestPassword123!";
        String hashedPassword = passwordService.hashPassword(password);
        
        // When
        boolean result = passwordService.isPasswordUpgradeRequired(hashedPassword);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("isPasswordUpgradeRequired_古いハッシュ_trueが返される")
    void isPasswordUpgradeRequired_古いハッシュ_trueが返される() {
        // Given（強度10の古いハッシュを模擬）
        String oldHashedPassword = "$2a$10$abcdefghijklmnopqrstuvwxyz123456789"; // 模擬的な古いハッシュ
        
        // When
        boolean result = passwordService.isPasswordUpgradeRequired(oldHashedPassword);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("upgradePassword_正常ケース_新しいハッシュが生成される")
    void upgradePassword_正常ケース_新しいハッシュが生成される() {
        // Given
        String rawPassword = "TestPassword123!";
        
        // When
        String upgradedHash = passwordService.upgradePassword(rawPassword);
        
        // Then
        assertThat(upgradedHash).isNotNull();
        assertThat(upgradedHash).startsWith("$2a$12$"); // 最新の強度12
        assertThat(passwordService.verifyPassword(rawPassword, upgradedHash)).isTrue();
    }
    
    @Test
    @DisplayName("hashPassword_nullパスワード_例外が発生する")
    void hashPassword_nullパスワード_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> passwordService.hashPassword(null))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    @DisplayName("verifyPassword_nullパスワード_例外が発生する")
    void verifyPassword_nullパスワード_例外が発生する() {
        // Given
        String hashedPassword = passwordService.hashPassword("TestPassword123!");
        
        // When & Then
        assertThatThrownBy(() -> passwordService.verifyPassword(null, hashedPassword))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    @DisplayName("verifyPassword_nullハッシュ_例外が発生する")
    void verifyPassword_nullハッシュ_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> passwordService.verifyPassword("TestPassword123!", null))
            .isInstanceOf(NullPointerException.class);
    }
}