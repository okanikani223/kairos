package com.github.okanikani.kairos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginResponse;
import com.github.okanikani.kairos.users.applications.usecases.dto.RegisterRequest;
import com.github.okanikani.kairos.users.domains.models.entities.Role;
import com.github.okanikani.kairos.users.domains.models.entities.User;
import com.github.okanikani.kairos.users.domains.models.repositories.UserRepository;
import com.github.okanikani.kairos.users.domains.services.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 認証フローの統合テスト
 * ユーザー登録からログイン、認証が必要なAPIへのアクセスまでの一連の流れを確認
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(IntegrationTestSecurityConfig.class)
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-integration-testing-must-be-at-least-256-bits",
    "jwt.expiration=86400000"
})
@DisplayName("認証フロー統合テスト")
class AuthenticationFlowIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    @Autowired
    private JwtService jwtService;
    
    @BeforeEach
    void setUp() {
        // テスト用のユーザーをクリア（InMemoryRepositoryの場合）
        if (userRepository instanceof com.github.okanikani.kairos.users.others.repositories.InMemoryUserRepository) {
            ((com.github.okanikani.kairos.users.others.repositories.InMemoryUserRepository) userRepository).clear();
        }
    }
    
    @Test
    @DisplayName("完全な認証フロー_ユーザー登録→ログイン→認証が必要なAPIアクセス")
    void 完全な認証フロー_ユーザー登録からログインまで() throws Exception {
        // Step 1: ユーザー登録
        RegisterRequest registerRequest = new RegisterRequest(
            "integrationuser",
            "統合テストユーザー",
            "integration@example.com",
            "IntegrationTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("integrationuser"))
                .andExpect(jsonPath("$.username").value("統合テストユーザー"))
                .andExpect(jsonPath("$.role").value("USER"));
        
        // Step 2: ログイン
        LoginRequest loginRequest = new LoginRequest("integrationuser", "IntegrationTest123!");
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.userId").value("integrationuser"))
                .andReturn();
        
        // JWTトークンを取得
        String responseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        String jwtToken = loginResponse.accessToken();
        
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken).isNotEmpty();
        
        // Step 3: 認証が必要なAPIへのアクセス（例：勤怠レポート取得）
        mockMvc.perform(get("/api/reports/2024/1")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound()); // データが存在しないので404だが、認証は通る
        
        // Step 4: 認証なしでのアクセス（401エラーになることを確認）
        mockMvc.perform(get("/api/reports/2024/1"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("事前登録ユーザーでのログインフロー")
    void 事前登録ユーザーでのログインフロー() throws Exception {
        // 事前にユーザーを登録
        User preRegisteredUser = User.createNew(
            "preuser123",
            "事前登録ユーザー",
            "preuser@example.com",
            "PrePassword123!",
            passwordService::hashPassword
        );
        userRepository.save(preRegisteredUser);
        
        // ログインテスト
        LoginRequest loginRequest = new LoginRequest("preuser123", "PrePassword123!");
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.userId").value("preuser123"))
                .andReturn();
        
        // 最終ログイン日時が更新されていることを確認
        User updatedUser = userRepository.findByUserId("preuser123").orElseThrow();
        assertThat(updatedUser.lastLoginAt()).isNotNull();
        assertThat(updatedUser.lastLoginAt()).isAfter(preRegisteredUser.createdAt());
    }
    
    @Test
    @DisplayName("管理者ユーザーの認証フロー")
    void 管理者ユーザーの認証フロー() throws Exception {
        // 管理者ユーザーを登録
        RegisterRequest adminRegisterRequest = new RegisterRequest(
            "admin123",
            "管理者",
            "admin@example.com",
            "AdminPassword123!",
            "ADMIN"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.roleDisplayName").value("管理者"));
        
        // 管理者としてログイン
        LoginRequest adminLoginRequest = new LoginRequest("admin123", "AdminPassword123!");
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andReturn();
        
        // JWTトークンの内容を確認
        String responseContent = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        String jwtToken = loginResponse.accessToken();
        
        // JWTトークンからユーザーIDを抽出
        String extractedUserId = jwtService.extractUsername(jwtToken);
        assertThat(extractedUserId).isEqualTo("admin123");
        
        // トークンの有効性を確認
        boolean isValid = jwtService.isTokenValid(jwtToken, "admin123");
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("無効なJWTトークンでのアクセス")
    void 無効なJWTトークンでのアクセス() throws Exception {
        String invalidToken = "invalid.jwt.token";
        
        mockMvc.perform(get("/api/reports/2024/1")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("期限切れJWTトークンでのアクセス")
    void 期限切れJWTトークンでのアクセス() throws Exception {
        // 極短い有効期限でトークンを生成（テスト用）
        String shortLivedToken = jwtService.generateTokenWithExpiration("testuser123", 1); // 1ms
        
        // トークンが期限切れになるまで待機
        Thread.sleep(10);
        
        mockMvc.perform(get("/api/reports/2024/1")
                .header("Authorization", "Bearer " + shortLivedToken))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("不正な形式のAuthorizationヘッダー")
    void 不正な形式のAuthorizationヘッダー() throws Exception {
        // "Bearer "プレフィックスなし
        mockMvc.perform(get("/api/reports/2024/1")
                .header("Authorization", "jwt.token.without.bearer"))
                .andExpect(status().isUnauthorized());
        
        // 空のトークン
        mockMvc.perform(get("/api/reports/2024/1")
                .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("ユーザー重複登録エラー")
    void ユーザー重複登録エラー() throws Exception {
        // 最初のユーザー登録
        RegisterRequest firstRequest = new RegisterRequest(
            "duplicateuser",
            "重複テストユーザー",
            "duplicate@example.com",
            "Password123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());
        
        // 同じユーザーIDで再登録（重複エラー）
        RegisterRequest duplicateRequest = new RegisterRequest(
            "duplicateuser", // 同じユーザーID
            "別のユーザー",
            "different@example.com",
            "DifferentPassword123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }
}