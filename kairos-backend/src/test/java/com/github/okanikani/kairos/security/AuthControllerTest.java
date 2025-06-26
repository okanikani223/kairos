package com.github.okanikani.kairos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.DuplicateResourceException;
import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.users.applications.usecases.AuthenticationUseCase;
import com.github.okanikani.kairos.users.applications.usecases.RegisterUserUseCase;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginResponse;
import com.github.okanikani.kairos.users.applications.usecases.dto.RegisterRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthControllerの統合テスト
 * REST APIエンドポイントの動作を確認
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private AuthenticationUseCase authenticationUseCase;
    
    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;
    
    @MockitoBean
    private JwtService jwtService;
    
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Test
    @DisplayName("POST /api/auth/login_正常ケース_JWTトークンが返される")
    void login_正常ケース_JWTトークンが返される() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser123", "TestPassword123!");
        UserResponse userResponse = new UserResponse(
            1L, "testuser123", "テストユーザー", "test@example.com",
            "USER", "一般ユーザー", true, LocalDateTime.now(), LocalDateTime.now()
        );
        LoginResponse response = LoginResponse.jwt("jwt.access.token", 86400, userResponse);
        
        when(authenticationUseCase.authenticate(any(LoginRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("jwt.access.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400))
                .andExpect(jsonPath("$.user.userId").value("testuser123"))
                .andExpect(jsonPath("$.user.username").value("テストユーザー"))
                .andExpect(jsonPath("$.user.role").value("USER"));
        
        verify(authenticationUseCase).authenticate(any(LoginRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/auth/login_認証失敗_401エラーが返される")
    void login_認証失敗_401エラーが返される() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser123", "WrongPassword!");
        
        when(authenticationUseCase.authenticate(any(LoginRequest.class)))
            .thenThrow(new AuthorizationException("ユーザーIDまたはパスワードが正しくありません"));
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("POST /api/auth/login_バリデーションエラー_400エラーが返される")
    void login_バリデーションエラー_400エラーが返される() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("", ""); // 空の値
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/auth/register_正常ケース_ユーザーが作成される")
    void register_正常ケース_ユーザーが作成される() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
            "newuser123",
            "新規ユーザー",
            "newuser@example.com",
            "NewPassword123!",
            "USER"
        );
        
        UserResponse response = new UserResponse(
            1L, "newuser123", "新規ユーザー", "newuser@example.com",
            "USER", "一般ユーザー", true, LocalDateTime.now(), null
        );
        
        when(registerUserUseCase.execute(any(RegisterRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("newuser123"))
                .andExpect(jsonPath("$.username").value("新規ユーザー"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true));
        
        verify(registerUserUseCase).execute(any(RegisterRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/auth/register_ユーザーID重複_409エラーが返される")
    void register_ユーザーID重複_409エラーが返される() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
            "existinguser",
            "重複ユーザー",
            "duplicate@example.com",
            "Password123!",
            "USER"
        );
        
        when(registerUserUseCase.execute(any(RegisterRequest.class)))
            .thenThrow(new DuplicateResourceException("ユーザーID「existinguser」は既に使用されています"));
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isConflict());
    }
    
    @Test
    @DisplayName("POST /api/auth/register_バリデーションエラー_400エラーが返される")
    void register_バリデーションエラー_400エラーが返される() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
            "ab", // 短すぎるユーザーID
            "",   // 空のユーザー名
            "invalid-email", // 無効なメールアドレス
            "weak", // 弱いパスワード
            "USER"
        );
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/auth/register_弱いパスワード_400エラーが返される")
    void register_弱いパスワード_400エラーが返される() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
            "testuser123",
            "テストユーザー",
            "test@example.com",
            "weak", // 弱いパスワード
            "USER"
        );
        
        when(registerUserUseCase.execute(any(RegisterRequest.class)))
            .thenThrow(new ValidationException("パスワードは8文字以上で入力してください"));
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/auth/register_ロール未指定_USERで作成される")
    void register_ロール未指定_USERで作成される() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
            "testuser123",
            "テストユーザー",
            "test@example.com",
            "TestPassword123!",
            null // ロール未指定
        );
        
        UserResponse response = new UserResponse(
            1L, "testuser123", "テストユーザー", "test@example.com",
            "USER", "一般ユーザー", true, LocalDateTime.now(), null
        );
        
        when(registerUserUseCase.execute(any(RegisterRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }
    
    @Test
    @DisplayName("POST /api/auth/login_不正なJSON_400エラーが返される")
    void login_不正なJSON_400エラーが返される() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/auth/register_不正なJSON_400エラーが返される")
    void register_不正なJSON_400エラーが返される() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/auth/login_Content-Type未指定_415エラーが返される")
    void login_ContentType未指定_415エラーが返される() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser123", "TestPassword123!");
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())) // Content-Type未指定
                .andExpect(status().isUnsupportedMediaType());
    }
    
    @Test
    @DisplayName("GET /api/auth/login_許可されないメソッド_405エラーが返される")
    void login_許可されないメソッド_405エラーが返される() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }
}