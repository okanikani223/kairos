package com.github.okanikani.kairos.rules.others.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.okanikani.kairos.rules.others.controllers.DefaultWorkRuleController.RegisterDefaultWorkRuleRequestBody;
import com.github.okanikani.kairos.security.JwtService;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginResponse;
import com.github.okanikani.kairos.users.applications.usecases.dto.RegisterRequest;
import com.github.okanikani.kairos.users.domains.models.repositories.UserRepository;
import com.github.okanikani.kairos.users.domains.services.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DefaultWorkRules API統合テスト
 * デフォルト勤務ルール管理の完全なAPIフローを検証
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-integration-testing-must-be-at-least-256-bits",
    "jwt.expiration=86400000"
})
@DisplayName("DefaultWorkRules API統合テスト")
class DefaultWorkRuleApiIntegrationTest {
    
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
    
    @Autowired(required = false)
    private com.github.okanikani.kairos.rules.others.repositories.InMemoryDefaultWorkRuleRepository defaultWorkRuleRepository;
    
    private String testUserId;
    private String jwtToken;
    
    @BeforeEach
    void setUp() throws Exception {
        // テスト用のリポジトリをクリア（InMemoryRepositoryの場合）
        if (userRepository instanceof com.github.okanikani.kairos.users.others.repositories.InMemoryUserRepository) {
            ((com.github.okanikani.kairos.users.others.repositories.InMemoryUserRepository) userRepository).clear();
        }
        if (defaultWorkRuleRepository != null) {
            defaultWorkRuleRepository.clear();
        }
        
        // テスト用ユーザーを登録してJWTトークンを取得
        testUserId = "defaultworkruletest001";
        setupTestUserAndToken();
    }
    
    private void setupTestUserAndToken() throws Exception {
        // ユーザー登録
        RegisterRequest registerRequest = new RegisterRequest(
            testUserId,
            "デフォルト勤務ルールテストユーザー",
            "defaultworkruletest@example.com",
            "DefaultWorkRuleTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        
        // ログインしてJWTトークンを取得
        LoginRequest loginRequest = new LoginRequest(testUserId, "DefaultWorkRuleTest123!");
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String responseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        jwtToken = loginResponse.accessToken();
    }
    
    @Test
    @DisplayName("完全なデフォルト勤務ルールフロー_作成→取得")
    void 完全なデフォルト勤務ルールフロー() throws Exception {
        // Step 1: デフォルト勤務ルール作成
        RegisterDefaultWorkRuleRequestBody createRequest = new RegisterDefaultWorkRuleRequestBody(
            1L,                              // workPlaceId
            35.6762,                         // latitude（東京駅）
            139.7649,                        // longitude（東京駅）
            LocalTime.of(9, 0),             // standardStartTime
            LocalTime.of(18, 0),            // standardEndTime
            LocalTime.of(12, 0),            // breakStartTime
            LocalTime.of(13, 0)             // breakEndTime
        );
        
        MvcResult createResult = mockMvc.perform(post("/api/default-work-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.workPlaceId").value(1))
                .andExpect(jsonPath("$.latitude").value(35.6762))
                .andExpect(jsonPath("$.longitude").value(139.7649))
                .andExpect(jsonPath("$.user.userId").value(testUserId))
                .andExpect(jsonPath("$.standardStartTime").value("09:00:00"))
                .andExpect(jsonPath("$.standardEndTime").value("18:00:00"))
                .andExpect(jsonPath("$.breakStartTime").value("12:00:00"))
                .andExpect(jsonPath("$.breakEndTime").value("13:00:00"))
                .andReturn();
        
        // レスポンスからIDを取得
        String responseContent = createResult.getResponse().getContentAsString();
        Long defaultWorkRuleId = objectMapper.readTree(responseContent).get("id").asLong();
        
        // Step 2: デフォルト勤務ルール一覧取得
        mockMvc.perform(get("/api/default-work-rules")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(defaultWorkRuleId))
                .andExpect(jsonPath("$[0].workPlaceId").value(1))
                .andExpect(jsonPath("$[0].user.userId").value(testUserId));
    }
    
    @Test
    @DisplayName("複数デフォルト勤務ルールの作成と取得")
    void 複数デフォルト勤務ルールの作成と取得() throws Exception {
        // 複数のデフォルト勤務ルールを作成
        RegisterDefaultWorkRuleRequestBody[] requests = {
            new RegisterDefaultWorkRuleRequestBody(1L, 35.6762, 139.7649, 
                LocalTime.of(9, 0), LocalTime.of(18, 0), 
                LocalTime.of(12, 0), LocalTime.of(13, 0)),  // 東京駅
            new RegisterDefaultWorkRuleRequestBody(2L, 35.6895, 139.6917, 
                LocalTime.of(10, 0), LocalTime.of(19, 0), 
                LocalTime.of(12, 30), LocalTime.of(13, 30)), // 新宿駅
            new RegisterDefaultWorkRuleRequestBody(3L, 35.6586, 139.7454, 
                LocalTime.of(8, 30), LocalTime.of(17, 30), 
                LocalTime.of(12, 0), LocalTime.of(13, 0))   // 銀座駅
        };
        
        for (RegisterDefaultWorkRuleRequestBody request : requests) {
            mockMvc.perform(post("/api/default-work-rules")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
        
        // 全デフォルト勤務ルール取得（3件あることを確認）
        mockMvc.perform(get("/api/default-work-rules")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }
    
    @Test
    @DisplayName("GPS座標境界値テスト")
    void GPS座標境界値テスト() throws Exception {
        // 有効な境界値
        RegisterDefaultWorkRuleRequestBody[] validRequests = {
            new RegisterDefaultWorkRuleRequestBody(1L, 90.0, 180.0, 
                LocalTime.of(9, 0), LocalTime.of(18, 0), 
                LocalTime.of(12, 0), LocalTime.of(13, 0)),     // 最大値
            new RegisterDefaultWorkRuleRequestBody(2L, -90.0, -180.0, 
                LocalTime.of(9, 0), LocalTime.of(18, 0), 
                LocalTime.of(12, 0), LocalTime.of(13, 0)),     // 最小値
            new RegisterDefaultWorkRuleRequestBody(3L, 0.0, 0.0, 
                LocalTime.of(9, 0), LocalTime.of(18, 0), 
                LocalTime.of(12, 0), LocalTime.of(13, 0)),     // 中央値
            new RegisterDefaultWorkRuleRequestBody(4L, 35.6762, 139.7649, 
                LocalTime.of(9, 0), LocalTime.of(18, 0), 
                LocalTime.of(12, 0), LocalTime.of(13, 0))      // 実際の座標（東京駅）
        };
        
        for (RegisterDefaultWorkRuleRequestBody request : validRequests) {
            mockMvc.perform(post("/api/default-work-rules")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.latitude").value(request.latitude()))
                    .andExpect(jsonPath("$.longitude").value(request.longitude()));
        }
    }
    
    @Test
    @DisplayName("時刻境界値テスト")
    void 時刻境界値テスト() throws Exception {
        // 有効な時刻パターン
        RegisterDefaultWorkRuleRequestBody[] validRequests = {
            new RegisterDefaultWorkRuleRequestBody(1L, 35.6762, 139.7649, 
                LocalTime.of(0, 0), LocalTime.of(23, 59), 
                LocalTime.of(12, 0), LocalTime.of(13, 0)),     // 最大時刻範囲
            new RegisterDefaultWorkRuleRequestBody(2L, 35.6762, 139.7649, 
                LocalTime.of(6, 0), LocalTime.of(15, 0), 
                LocalTime.of(11, 30), LocalTime.of(12, 30)),   // 早朝勤務
            new RegisterDefaultWorkRuleRequestBody(3L, 35.6762, 139.7649, 
                LocalTime.of(22, 0), LocalTime.of(6, 0), 
                LocalTime.of(2, 0), LocalTime.of(3, 0))        // 夜勤（日をまたぐ）
        };
        
        for (RegisterDefaultWorkRuleRequestBody request : validRequests) {
            mockMvc.perform(post("/api/default-work-rules")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.standardStartTime").exists())
                    .andExpect(jsonPath("$.standardEndTime").exists());
        }
    }
    
    @Test
    @DisplayName("認証エラーテスト_未認証でのアクセス")
    void 認証エラーテスト_未認証でのアクセス() throws Exception {
        // JWTトークンなしでアクセス
        mockMvc.perform(get("/api/default-work-rules"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/default-work-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("バリデーションエラーテスト_無効なリクエスト")
    void バリデーションエラーテスト_無効なリクエスト() throws Exception {
        // 必須フィールド欠如
        String incompleteRequest = "{ \"workPlaceId\": 1, \"latitude\": 35.6762 }"; // longitude等欠如
        
        mockMvc.perform(post("/api/default-work-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteRequest))
                .andExpect(status().isBadRequest());
        
        // 無効なJSON
        String invalidJson = "{ invalid json }";
        
        mockMvc.perform(post("/api/default-work-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("複数ユーザーのデータ分離テスト")
    void 複数ユーザーのデータ分離テスト() throws Exception {
        // 2番目のユーザーを作成
        String secondUserId = "defaultworkruletest002";
        setupSecondUser(secondUserId);
        String secondUserToken = getTokenForUser(secondUserId);
        
        // 各ユーザーでデフォルト勤務ルール作成
        RegisterDefaultWorkRuleRequestBody user1Request = new RegisterDefaultWorkRuleRequestBody(
            1L, 35.6762, 139.7649, 
            LocalTime.of(9, 0), LocalTime.of(18, 0), 
            LocalTime.of(12, 0), LocalTime.of(13, 0)
        );
        
        RegisterDefaultWorkRuleRequestBody user2Request = new RegisterDefaultWorkRuleRequestBody(
            2L, 35.6895, 139.6917, 
            LocalTime.of(10, 0), LocalTime.of(19, 0), 
            LocalTime.of(12, 30), LocalTime.of(13, 30)
        );
        
        // User1のデフォルト勤務ルール作成
        mockMvc.perform(post("/api/default-work-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Request)))
                .andExpect(status().isCreated());
        
        // User2のデフォルト勤務ルール作成
        mockMvc.perform(post("/api/default-work-rules")
                .header("Authorization", "Bearer " + secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Request)))
                .andExpect(status().isCreated());
        
        // User1は自分のデフォルト勤務ルールのみ取得可能
        mockMvc.perform(get("/api/default-work-rules")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].workPlaceId").value(1))
                .andExpect(jsonPath("$[0].user.userId").value(testUserId));
        
        // User2は自分のデフォルト勤務ルールのみ取得可能
        mockMvc.perform(get("/api/default-work-rules")
                .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].workPlaceId").value(2))
                .andExpect(jsonPath("$[0].user.userId").value(secondUserId));
    }
    
    @Test
    @DisplayName("空のデフォルト勤務ルール一覧取得テスト")
    void 空のデフォルト勤務ルール一覧取得テスト() throws Exception {
        // デフォルト勤務ルールを何も作成せずに一覧取得
        mockMvc.perform(get("/api/default-work-rules")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
    
    // ヘルパーメソッド
    
    private void setupSecondUser(String userId) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
            userId,
            "第2デフォルト勤務ルールテストユーザー",
            userId + "@example.com",
            "SecondDefaultWorkRuleTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }
    
    private String getTokenForUser(String userId) throws Exception {
        LoginRequest loginRequest = new LoginRequest(userId, "SecondDefaultWorkRuleTest123!");
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String responseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        return loginResponse.accessToken();
    }
}