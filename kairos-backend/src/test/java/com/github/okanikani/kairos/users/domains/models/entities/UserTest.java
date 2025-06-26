package com.github.okanikani.kairos.users.domains.models.entities;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * Userエンティティのテスト
 * ドメインロジックとバリデーション機能の動作を確認
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("User")
class UserTest {
    
    private final Function<String, String> dummyHashFunction = password -> "hashed_" + password;
    
    @Test
    @DisplayName("createNew_正常ケース_ユーザーが作成される")
    void createNew_正常ケース_ユーザーが作成される() {
        // Given
        String userId = "testuser123";
        String username = "テストユーザー";
        String email = "test@example.com";
        String password = "TestPassword123!";
        
        // When
        User user = User.createNew(userId, username, email, password, dummyHashFunction);
        
        // Then
        assertThat(user.id()).isNull(); // 新規作成時はnull
        assertThat(user.userId()).isEqualTo(userId);
        assertThat(user.username()).isEqualTo(username);
        assertThat(user.email()).isEqualTo(email);
        assertThat(user.hashedPassword()).isEqualTo("hashed_" + password);
        assertThat(user.role()).isEqualTo(Role.USER); // デフォルトロール
        assertThat(user.enabled()).isTrue(); // デフォルトで有効
        assertThat(user.createdAt()).isNotNull();
        assertThat(user.lastLoginAt()).isNull(); // 初期は未ログイン
    }
    
    @Test
    @DisplayName("createNewWithRole_正常ケース_指定ロールでユーザーが作成される")
    void createNewWithRole_正常ケース_指定ロールでユーザーが作成される() {
        // Given
        String userId = "admin123";
        String username = "管理者";
        String email = "admin@example.com";
        String password = "AdminPassword123!";
        Role role = Role.ADMIN;
        
        // When
        User user = User.createNewWithRole(userId, username, email, password, role, dummyHashFunction);
        
        // Then
        assertThat(user.role()).isEqualTo(Role.ADMIN);
        assertThat(user.userId()).isEqualTo(userId);
    }
    
    @Test
    @DisplayName("validatePasswordStrength_正常ケース_例外が発生しない")
    void validatePasswordStrength_正常ケース_例外が発生しない() {
        // Given
        String strongPassword = "TestPassword123!";
        
        // When & Then
        assertThatNoException().isThrownBy(() -> User.validatePasswordStrength(strongPassword));
    }
    
    @Test
    @DisplayName("validatePasswordStrength_短すぎるパスワード_例外が発生する")
    void validatePasswordStrength_短すぎるパスワード_例外が発生する() {
        // Given
        String shortPassword = "Test1!";
        
        // When & Then
        assertThatThrownBy(() -> User.validatePasswordStrength(shortPassword))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("8文字以上で入力してください");
    }
    
    @Test
    @DisplayName("validatePasswordStrength_長すぎるパスワード_例外が発生する")
    void validatePasswordStrength_長すぎるパスワード_例外が発生する() {
        // Given
        String longPassword = "A".repeat(129) + "1!";
        
        // When & Then
        assertThatThrownBy(() -> User.validatePasswordStrength(longPassword))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("128文字以内で入力してください");
    }
    
    @Test
    @DisplayName("validatePasswordStrength_大文字なし_例外が発生する")
    void validatePasswordStrength_大文字なし_例外が発生する() {
        // Given
        String noUppercasePassword = "testpassword123!";
        
        // When & Then
        assertThatThrownBy(() -> User.validatePasswordStrength(noUppercasePassword))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("大文字、小文字、数字、特殊文字");
    }
    
    @Test
    @DisplayName("validatePasswordStrength_小文字なし_例外が発生する")
    void validatePasswordStrength_小文字なし_例外が発生する() {
        // Given
        String noLowercasePassword = "TESTPASSWORD123!";
        
        // When & Then
        assertThatThrownBy(() -> User.validatePasswordStrength(noLowercasePassword))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("大文字、小文字、数字、特殊文字");
    }
    
    @Test
    @DisplayName("validatePasswordStrength_数字なし_例外が発生する")
    void validatePasswordStrength_数字なし_例外が発生する() {
        // Given
        String noDigitPassword = "TestPassword!";
        
        // When & Then
        assertThatThrownBy(() -> User.validatePasswordStrength(noDigitPassword))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("大文字、小文字、数字、特殊文字");
    }
    
    @Test
    @DisplayName("validatePasswordStrength_特殊文字なし_例外が発生する")
    void validatePasswordStrength_特殊文字なし_例外が発生する() {
        // Given
        String noSpecialCharPassword = "TestPassword123";
        
        // When & Then
        assertThatThrownBy(() -> User.validatePasswordStrength(noSpecialCharPassword))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("大文字、小文字、数字、特殊文字");
    }
    
    @Test
    @DisplayName("User_不正なユーザーID_例外が発生する")
    void User_不正なユーザーID_例外が発生する() {
        // Given
        String invalidUserId = "te"; // 短すぎる
        
        // When & Then
        assertThatThrownBy(() -> new User(
            null, invalidUserId, "テストユーザー", "test@example.com", 
            "hashedPassword", Role.USER, true, LocalDateTime.now(), null
        ))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("3-50文字の英数字");
    }
    
    @Test
    @DisplayName("User_不正なメールアドレス_例外が発生する")
    void User_不正なメールアドレス_例外が発生する() {
        // Given
        String invalidEmail = "invalid-email";
        
        // When & Then
        assertThatThrownBy(() -> new User(
            null, "testuser123", "テストユーザー", invalidEmail, 
            "hashedPassword", Role.USER, true, LocalDateTime.now(), null
        ))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("有効なメールアドレス");
    }
    
    @Test
    @DisplayName("withLastLogin_正常ケース_最終ログイン日時が更新される")
    void withLastLogin_正常ケース_最終ログイン日時が更新される() {
        // Given
        User user = User.createNew("testuser123", "テストユーザー", "test@example.com", 
                                  "TestPassword123!", dummyHashFunction);
        LocalDateTime loginTime = LocalDateTime.now();
        
        // When
        User updatedUser = user.withLastLogin(loginTime);
        
        // Then
        assertThat(updatedUser.lastLoginAt()).isEqualTo(loginTime);
        assertThat(updatedUser.userId()).isEqualTo(user.userId()); // 他フィールドは変更なし
    }
    
    @Test
    @DisplayName("withRole_正常ケース_ロールが変更される")
    void withRole_正常ケース_ロールが変更される() {
        // Given
        User user = User.createNew("testuser123", "テストユーザー", "test@example.com", 
                                  "TestPassword123!", dummyHashFunction);
        
        // When
        User updatedUser = user.withRole(Role.ADMIN);
        
        // Then
        assertThat(updatedUser.role()).isEqualTo(Role.ADMIN);
        assertThat(updatedUser.userId()).isEqualTo(user.userId()); // 他フィールドは変更なし
    }
    
    @Test
    @DisplayName("disable_正常ケース_アカウントが無効化される")
    void disable_正常ケース_アカウントが無効化される() {
        // Given
        User user = User.createNew("testuser123", "テストユーザー", "test@example.com", 
                                  "TestPassword123!", dummyHashFunction);
        
        // When
        User disabledUser = user.disable();
        
        // Then
        assertThat(disabledUser.enabled()).isFalse();
        assertThat(disabledUser.userId()).isEqualTo(user.userId()); // 他フィールドは変更なし
    }
    
    @Test
    @DisplayName("isAuthenticatable_有効ユーザー_trueが返される")
    void isAuthenticatable_有効ユーザー_trueが返される() {
        // Given
        User user = User.createNew("testuser123", "テストユーザー", "test@example.com", 
                                  "TestPassword123!", dummyHashFunction);
        
        // When & Then
        assertThat(user.isAuthenticatable()).isTrue();
    }
    
    @Test
    @DisplayName("isAuthenticatable_無効ユーザー_falseが返される")
    void isAuthenticatable_無効ユーザー_falseが返される() {
        // Given
        User user = User.createNew("testuser123", "テストユーザー", "test@example.com", 
                                  "TestPassword123!", dummyHashFunction).disable();
        
        // When & Then
        assertThat(user.isAuthenticatable()).isFalse();
    }
}