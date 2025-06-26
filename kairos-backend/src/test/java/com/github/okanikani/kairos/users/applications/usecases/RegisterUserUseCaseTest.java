package com.github.okanikani.kairos.users.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.DuplicateResourceException;
import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.users.applications.usecases.dto.RegisterRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.RegisterUserRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.UserResponse;
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
 * RegisterUserUseCaseのテスト
 * ユーザー登録ロジックの動作を確認
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("RegisterUserUseCase")
class RegisterUserUseCaseTest {
    
    @MockitoBean
    private UserRepository userRepository;
    
    @MockitoBean
    private PasswordService passwordService;
    
    @Autowired
    private RegisterUserUseCase registerUserUseCase;
    
    @BeforeEach
    void setUp() {
        // パスワードサービスのデフォルト動作を設定
        when(passwordService.hashPassword(anyString())).thenReturn("hashedPassword");
    }
    
    @Test
    @DisplayName("execute_API用リクエスト_正常ケース_ユーザーが登録される")
    void execute_API用リクエスト_正常ケース_ユーザーが登録される() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "testuser123",
            "テストユーザー",
            "test@example.com",
            "TestPassword123!",
            "USER"
        );
        
        User savedUser = new User(
            1L, "testuser123", "テストユーザー", "test@example.com",
            "hashedPassword", Role.USER, true, LocalDateTime.now(), null
        );
        
        when(userRepository.existsByUserId("testuser123")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        UserResponse response = registerUserUseCase.execute(request);
        
        // Then
        assertThat(response.userId()).isEqualTo("testuser123");
        assertThat(response.username()).isEqualTo("テストユーザー");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.role()).isEqualTo("USER");
        
        verify(passwordService).hashPassword("TestPassword123!");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("execute_API用リクエスト_ロール未指定_USERロールで登録される")
    void execute_API用リクエスト_ロール未指定_USERロールで登録される() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "testuser123",
            "テストユーザー",
            "test@example.com",
            "TestPassword123!",
            null // ロール未指定
        );
        
        User savedUser = new User(
            1L, "testuser123", "テストユーザー", "test@example.com",
            "hashedPassword", Role.USER, true, LocalDateTime.now(), null
        );
        
        when(userRepository.existsByUserId("testuser123")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        UserResponse response = registerUserUseCase.execute(request);
        
        // Then
        assertThat(response.role()).isEqualTo("USER");
    }
    
    @Test
    @DisplayName("execute_従来版リクエスト_正常ケース_ユーザーが登録される")
    void execute_従来版リクエスト_正常ケース_ユーザーが登録される() {
        // Given
        RegisterUserRequest request = new RegisterUserRequest(
            "testuser123",
            "テストユーザー",
            "test@example.com",
            "TestPassword123!",
            "TestPassword123!"
        );
        
        User savedUser = new User(
            1L, "testuser123", "テストユーザー", "test@example.com",
            "hashedPassword", Role.USER, true, LocalDateTime.now(), null
        );
        
        when(userRepository.existsByUserId("testuser123")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        UserResponse response = registerUserUseCase.execute(request);
        
        // Then
        assertThat(response.userId()).isEqualTo("testuser123");
        assertThat(response.role()).isEqualTo("USER");
    }
    
    @Test
    @DisplayName("execute_ユーザーID重複_例外が発生する")
    void execute_ユーザーID重複_例外が発生する() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "existinguser",
            "テストユーザー",
            "test@example.com",
            "TestPassword123!",
            "USER"
        );
        
        when(userRepository.existsByUserId("existinguser")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> registerUserUseCase.execute(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("ユーザーID「existinguser」は既に使用されています");
    }
    
    @Test
    @DisplayName("execute_メールアドレス重複_例外が発生する")
    void execute_メールアドレス重複_例外が発生する() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "testuser123",
            "テストユーザー",
            "existing@example.com",
            "TestPassword123!",
            "USER"
        );
        
        when(userRepository.existsByUserId("testuser123")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> registerUserUseCase.execute(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("メールアドレス「existing@example.com」は既に使用されています");
    }
    
    @Test
    @DisplayName("execute_弱いパスワード_例外が発生する")
    void execute_弱いパスワード_例外が発生する() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "testuser123",
            "テストユーザー",
            "test@example.com",
            "weak", // 弱いパスワード
            "USER"
        );
        
        when(userRepository.existsByUserId("testuser123")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> registerUserUseCase.execute(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("8文字以上で入力してください");
    }
    
    @Test
    @DisplayName("execute_無効なロール_例外が発生する")
    void execute_無効なロール_例外が発生する() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "testuser123",
            "テストユーザー",
            "test@example.com",
            "TestPassword123!",
            "INVALID_ROLE"
        );
        
        when(userRepository.existsByUserId("testuser123")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> registerUserUseCase.execute(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("無効なロールが指定されました");
    }
    
    @Test
    @DisplayName("execute_従来版リクエスト_パスワード確認不一致_例外が発生する")
    void execute_従来版リクエスト_パスワード確認不一致_例外が発生する() {
        // Given
        RegisterUserRequest request = new RegisterUserRequest(
            "testuser123",
            "テストユーザー",
            "test@example.com",
            "TestPassword123!",
            "DifferentPassword123!" // 確認パスワードが異なる
        );
        
        when(userRepository.existsByUserId("testuser123")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> registerUserUseCase.execute(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("パスワードとパスワード確認が一致しません");
    }
    
    @Test
    @DisplayName("execute_nullリクエスト_例外が発生する")
    void execute_nullリクエスト_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> registerUserUseCase.execute((RegisterRequest) null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("リクエストは必須です");
    }
    
    @Test
    @DisplayName("execute_API用リクエスト_ADMIN指定_ADMINロールで登録される")
    void execute_API用リクエスト_ADMIN指定_ADMINロールで登録される() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "admin123",
            "管理者",
            "admin@example.com",
            "AdminPassword123!",
            "ADMIN"
        );
        
        User savedUser = new User(
            1L, "admin123", "管理者", "admin@example.com",
            "hashedPassword", Role.ADMIN, true, LocalDateTime.now(), null
        );
        
        when(userRepository.existsByUserId("admin123")).thenReturn(false);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        UserResponse response = registerUserUseCase.execute(request);
        
        // Then
        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(response.userId()).isEqualTo("admin123");
    }
}