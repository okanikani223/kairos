package com.github.okanikani.kairos.users.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.security.JwtService;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginResponse;
import com.github.okanikani.kairos.users.domains.models.entities.Role;
import com.github.okanikani.kairos.users.domains.models.entities.User;
import com.github.okanikani.kairos.users.domains.models.repositories.UserRepository;
import com.github.okanikani.kairos.users.domains.services.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthenticationUseCaseのテスト
 * ログイン認証機能の動作を確認
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("AuthenticationUseCase")
class AuthenticationUseCaseTest {
    
    @MockitoBean
    private UserRepository userRepository;
    
    @MockitoBean
    private PasswordService passwordService;
    
    @MockitoBean
    private JwtService jwtService;
    
    @Autowired
    private AuthenticationUseCase authenticationUseCase;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User(
            1L,
            "testuser123",
            "テストユーザー",
            "test@example.com",
            "hashedPassword",
            Role.USER,
            true,
            LocalDateTime.now().minusDays(1),
            null
        );
    }
    
    @Test
    @DisplayName("authenticate_正常ケース_JWTトークンが返される")
    void authenticate_正常ケース_JWTトークンが返される() {
        // Given
        LoginRequest request = new LoginRequest("testuser123", "TestPassword123!");
        
        when(userRepository.findByUserId("testuser123")).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword("TestPassword123!", "hashedPassword")).thenReturn(true);
        when(passwordService.isPasswordUpgradeRequired("hashedPassword")).thenReturn(false);
        when(jwtService.generateToken("testuser123")).thenReturn("jwt.access.token");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        LoginResponse response = authenticationUseCase.authenticate(request);
        
        // Then
        assertThat(response.accessToken()).isEqualTo("jwt.access.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isGreaterThan(0);
        assertThat(response.user().userId()).isEqualTo("testuser123");
        assertThat(response.user().username()).isEqualTo("テストユーザー");
        
        verify(userRepository).save(any(User.class)); // 最終ログイン日時更新
    }
    
    @Test
    @DisplayName("authenticate_存在しないユーザーID_例外が発生する")
    void authenticate_存在しないユーザーID_例外が発生する() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent", "TestPassword123!");
        
        when(userRepository.findByUserId("nonexistent")).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> authenticationUseCase.authenticate(request))
            .isInstanceOf(AuthorizationException.class)
            .hasMessageContaining("ユーザーIDまたはパスワードが正しくありません");
    }
    
    @Test
    @DisplayName("authenticate_無効なパスワード_例外が発生する")
    void authenticate_無効なパスワード_例外が発生する() {
        // Given
        LoginRequest request = new LoginRequest("testuser123", "WrongPassword!");
        
        when(userRepository.findByUserId("testuser123")).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword("WrongPassword!", "hashedPassword")).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> authenticationUseCase.authenticate(request))
            .isInstanceOf(AuthorizationException.class)
            .hasMessageContaining("ユーザーIDまたはパスワードが正しくありません");
    }
    
    @Test
    @DisplayName("authenticate_無効化されたアカウント_例外が発生する")
    void authenticate_無効化されたアカウント_例外が発生する() {
        // Given
        LoginRequest request = new LoginRequest("testuser123", "TestPassword123!");
        User disabledUser = testUser.disable();
        
        when(userRepository.findByUserId("testuser123")).thenReturn(Optional.of(disabledUser));
        
        // When & Then
        assertThatThrownBy(() -> authenticationUseCase.authenticate(request))
            .isInstanceOf(AuthorizationException.class)
            .hasMessageContaining("アカウントが無効化されています");
    }
    
    @Test
    @DisplayName("authenticate_パスワードアップグレード必要_新しいハッシュで保存される")
    void authenticate_パスワードアップグレード必要_新しいハッシュで保存される() {
        // Given
        LoginRequest request = new LoginRequest("testuser123", "TestPassword123!");
        String upgradedHash = "upgradedHashedPassword";
        
        when(userRepository.findByUserId("testuser123")).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword("TestPassword123!", "hashedPassword")).thenReturn(true);
        when(passwordService.isPasswordUpgradeRequired("hashedPassword")).thenReturn(true);
        when(passwordService.upgradePassword("TestPassword123!")).thenReturn(upgradedHash);
        when(jwtService.generateToken("testuser123")).thenReturn("jwt.access.token");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        LoginResponse response = authenticationUseCase.authenticate(request);
        
        // Then
        assertThat(response.accessToken()).isEqualTo("jwt.access.token");
        
        // パスワードアップグレードが実行されたことを確認
        verify(passwordService).upgradePassword("TestPassword123!");
        verify(userRepository).save(argThat(user -> 
            user.hashedPassword().equals(upgradedHash)
        ));
    }
    
    @Test
    @DisplayName("authenticate_最終ログイン日時更新_現在時刻が設定される")
    void authenticate_最終ログイン日時更新_現在時刻が設定される() {
        // Given
        LoginRequest request = new LoginRequest("testuser123", "TestPassword123!");
        LocalDateTime beforeTest = LocalDateTime.now().minusSeconds(1);
        
        when(userRepository.findByUserId("testuser123")).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword("TestPassword123!", "hashedPassword")).thenReturn(true);
        when(passwordService.isPasswordUpgradeRequired("hashedPassword")).thenReturn(false);
        when(jwtService.generateToken("testuser123")).thenReturn("jwt.access.token");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        authenticationUseCase.authenticate(request);
        
        // Then
        verify(userRepository).save(argThat(user -> {
            LocalDateTime afterTest = LocalDateTime.now().plusSeconds(1);
            return user.lastLoginAt() != null 
                && user.lastLoginAt().isAfter(beforeTest)
                && user.lastLoginAt().isBefore(afterTest);
        }));
    }
    
    @Test
    @DisplayName("authenticate_nullリクエスト_例外が発生する")
    void authenticate_nullリクエスト_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> authenticationUseCase.authenticate(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("リクエストは必須です");
    }
    
    @Test
    @DisplayName("authenticate_空のユーザーID_例外が発生する")
    void authenticate_空のユーザーID_例外が発生する() {
        // Given
        LoginRequest request = new LoginRequest("", "TestPassword123!");
        
        // When & Then
        assertThatThrownBy(() -> authenticationUseCase.authenticate(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("ユーザーIDは必須です");
    }
    
    @Test
    @DisplayName("authenticate_空のパスワード_例外が発生する")
    void authenticate_空のパスワード_例外が発生する() {
        // Given
        LoginRequest request = new LoginRequest("testuser123", "");
        
        // When & Then
        assertThatThrownBy(() -> authenticationUseCase.authenticate(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("パスワードは必須です");
    }
    
    @Test
    @DisplayName("authenticate_JWTトークン有効期限_秒単位で返される")
    void authenticate_JWTトークン有効期限_秒単位で返される() {
        // Given
        LoginRequest request = new LoginRequest("testuser123", "TestPassword123!");
        
        when(userRepository.findByUserId("testuser123")).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword("TestPassword123!", "hashedPassword")).thenReturn(true);
        when(passwordService.isPasswordUpgradeRequired("hashedPassword")).thenReturn(false);
        when(jwtService.generateToken("testuser123")).thenReturn("jwt.access.token");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        LoginResponse response = authenticationUseCase.authenticate(request);
        
        // Then
        // デフォルトJWT有効期限（86400000ms = 86400秒 = 24時間）を確認
        assertThat(response.expiresIn()).isEqualTo(86400);
    }
    
    @Test
    @DisplayName("authenticate_管理者ユーザー_適切なロール情報が返される")
    void authenticate_管理者ユーザー_適切なロール情報が返される() {
        // Given
        LoginRequest request = new LoginRequest("admin123", "AdminPassword123!");
        User adminUser = new User(
            2L, "admin123", "管理者", "admin@example.com",
            "hashedPassword", Role.ADMIN, true, LocalDateTime.now(), null
        );
        
        when(userRepository.findByUserId("admin123")).thenReturn(Optional.of(adminUser));
        when(passwordService.verifyPassword("AdminPassword123!", "hashedPassword")).thenReturn(true);
        when(passwordService.isPasswordUpgradeRequired("hashedPassword")).thenReturn(false);
        when(jwtService.generateToken("admin123")).thenReturn("jwt.admin.token");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        
        // When
        LoginResponse response = authenticationUseCase.authenticate(request);
        
        // Then
        assertThat(response.user().role()).isEqualTo("ADMIN");
        assertThat(response.user().roleDisplayName()).isEqualTo("管理者");
    }
}