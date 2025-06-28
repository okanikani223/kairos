package com.github.okanikani.kairos.rules.others.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.okanikani.kairos.rules.others.controllers.WorkRuleController.RegisterWorkRuleRequestBody;
import com.github.okanikani.kairos.rules.others.controllers.WorkRuleController.UpdateWorkRuleRequestBody;
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

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WorkRules API統合テスト
 * 勤務ルール管理の完全なAPIフローを検証
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-integration-testing-must-be-at-least-256-bits",
    "jwt.expiration=86400000"
})
@DisplayName("WorkRules API統合テスト")
class WorkRuleApiIntegrationTest {
    
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
    private com.github.okanikani.kairos.rules.others.repositories.InMemoryWorkRuleRepository workRuleRepository;
    
    private String testUserId;
    private String jwtToken;
    
    @BeforeEach
    void setUp() throws Exception {
        // テスト用のリポジトリをクリア（InMemoryRepositoryの場合）
        if (userRepository instanceof com.github.okanikani.kairos.users.others.repositories.InMemoryUserRepository) {
            ((com.github.okanikani.kairos.users.others.repositories.InMemoryUserRepository) userRepository).clear();
        }
        if (workRuleRepository != null) {
            workRuleRepository.clear();
        }
        
        // テスト用ユーザーを登録してJWTトークンを取得
        testUserId = "workruletest001";
        setupTestUserAndToken();
    }
    
    private void setupTestUserAndToken() throws Exception {
        // ユーザー登録
        RegisterRequest registerRequest = new RegisterRequest(
            testUserId,
            "勤務ルールテストユーザー",
            "workruletest@example.com",
            "WorkRuleTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        
        // ログインしてJWTトークンを取得
        LoginRequest loginRequest = new LoginRequest(testUserId, "WorkRuleTest123!");
        
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
    @DisplayName("完全な勤務ルールCRUDフロー_作成→取得→更新→削除")
    void 完全な勤務ルールCRUDフロー() throws Exception {
        // Step 1: 勤務ルール作成
        RegisterWorkRuleRequestBody createRequest = new RegisterWorkRuleRequestBody(
            1L,                              // workPlaceId
            35.6762,                         // latitude（東京駅）
            139.7649,                        // longitude（東京駅）
            LocalTime.of(9, 0),             // standardStartTime
            LocalTime.of(18, 0),            // standardEndTime
            LocalTime.of(12, 0),            // breakStartTime
            LocalTime.of(13, 0),            // breakEndTime
            LocalDate.of(2024, 1, 1),       // membershipStartDate
            LocalDate.of(2024, 12, 31)      // membershipEndDate
        );
        
        MvcResult createResult = mockMvc.perform(post("/api/work-rules")
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
                .andExpect(jsonPath("$.membershipStartDate").value("2024-01-01"))
                .andExpect(jsonPath("$.membershipEndDate").value("2024-12-31"))
                .andReturn();
        
        // レスポンスからIDを取得
        String responseContent = createResult.getResponse().getContentAsString();
        Long workRuleId = objectMapper.readTree(responseContent).get("id").asLong();
        
        // Step 2: 勤務ルール一覧取得
        mockMvc.perform(get("/api/work-rules")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(workRuleId))
                .andExpect(jsonPath("$[0].workPlaceId").value(1));
        
        // Step 3: ID指定で勤務ルール取得
        mockMvc.perform(get("/api/work-rules/" + workRuleId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workRuleId))
                .andExpect(jsonPath("$.workPlaceId").value(1))
                .andExpect(jsonPath("$.user.userId").value(testUserId));
        
        // Step 4: 勤務ルール更新
        UpdateWorkRuleRequestBody updateRequest = new UpdateWorkRuleRequestBody(
            2L,                              // workPlaceId（変更）
            35.6895,                         // latitude（新宿駅に変更）
            139.6917,                        // longitude（新宿駅に変更）
            LocalTime.of(10, 0),            // standardStartTime（変更）
            LocalTime.of(19, 0),            // standardEndTime（変更）
            LocalTime.of(12, 30),           // breakStartTime（変更）
            LocalTime.of(13, 30),           // breakEndTime（変更）
            LocalDate.of(2024, 2, 1),       // membershipStartDate（変更）
            LocalDate.of(2024, 11, 30)      // membershipEndDate（変更）
        );
        
        mockMvc.perform(put("/api/work-rules/" + workRuleId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workRuleId))
                .andExpect(jsonPath("$.workPlaceId").value(2))
                .andExpect(jsonPath("$.latitude").value(35.6895))
                .andExpect(jsonPath("$.longitude").value(139.6917))
                .andExpect(jsonPath("$.standardStartTime").value("10:00:00"))
                .andExpect(jsonPath("$.standardEndTime").value("19:00:00"));
        
        // Step 5: 勤務ルール削除
        mockMvc.perform(delete("/api/work-rules/" + workRuleId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
        
        // Step 6: 削除確認（404になることを確認）
        mockMvc.perform(get("/api/work-rules/" + workRuleId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("GPS座標境界値テスト")
    void GPS座標境界値テスト() throws Exception {
        // 有効な境界値（期間重複を避けるため異なる期間を設定）
        RegisterWorkRuleRequestBody[] validRequests = {
            new RegisterWorkRuleRequestBody(1L, 90.0, 180.0, 
                LocalTime.of(9, 0), LocalTime.of(18, 0), 
                LocalTime.of(12, 0), LocalTime.of(13, 0),
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31)),   // 最大値
            new RegisterWorkRuleRequestBody(2L, -90.0, -180.0, 
                LocalTime.of(9, 0), LocalTime.of(18, 0), 
                LocalTime.of(12, 0), LocalTime.of(13, 0),
                LocalDate.of(2024, 4, 1), LocalDate.of(2024, 6, 30)),   // 最小値
            new RegisterWorkRuleRequestBody(3L, 0.0, 0.0, 
                LocalTime.of(9, 0), LocalTime.of(18, 0), 
                LocalTime.of(12, 0), LocalTime.of(13, 0),
                LocalDate.of(2024, 7, 1), LocalDate.of(2024, 9, 30))    // 中央値
        };
        
        for (RegisterWorkRuleRequestBody request : validRequests) {
            mockMvc.perform(post("/api/work-rules")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.latitude").value(request.latitude()))
                    .andExpect(jsonPath("$.longitude").value(request.longitude()));
        }
    }
    
    @Test
    @DisplayName("認証エラーテスト_未認証でのアクセス")
    void 認証エラーテスト_未認証でのアクセス() throws Exception {
        // JWTトークンなしでアクセス
        mockMvc.perform(get("/api/work-rules"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/work-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/work-rules/1"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(put("/api/work-rules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(delete("/api/work-rules/1"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("バリデーションエラーテスト_無効なリクエスト")
    void バリデーションエラーテスト_無効なリクエスト() throws Exception {
        // 必須フィールド欠如
        String incompleteRequest = "{ \"workPlaceId\": 1, \"latitude\": 35.6762 }"; // longitude等欠如
        
        mockMvc.perform(post("/api/work-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteRequest))
                .andExpect(status().isBadRequest());
        
        // 無効なJSON
        String invalidJson = "{ invalid json }";
        
        mockMvc.perform(post("/api/work-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("存在しない勤務ルールアクセス_404エラー")
    void 存在しない勤務ルールアクセス_404エラー() throws Exception {
        // 存在しないIDで取得
        mockMvc.perform(get("/api/work-rules/999999")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
        
        // 存在しないIDで更新
        UpdateWorkRuleRequestBody updateRequest = new UpdateWorkRuleRequestBody(
            1L, 35.6762, 139.7649, 
            LocalTime.of(9, 0), LocalTime.of(18, 0), 
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );
        
        mockMvc.perform(put("/api/work-rules/999999")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
        
        // 存在しないIDで削除
        mockMvc.perform(delete("/api/work-rules/999999")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("複数ユーザーのデータ分離テスト")
    void 複数ユーザーのデータ分離テスト() throws Exception {
        // 2番目のユーザーを作成
        String secondUserId = "workruletest002";
        setupSecondUser(secondUserId);
        String secondUserToken = getTokenForUser(secondUserId);
        
        // 各ユーザーで勤務ルール作成
        RegisterWorkRuleRequestBody user1Request = new RegisterWorkRuleRequestBody(
            1L, 35.6762, 139.7649, 
            LocalTime.of(9, 0), LocalTime.of(18, 0), 
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );
        
        RegisterWorkRuleRequestBody user2Request = new RegisterWorkRuleRequestBody(
            2L, 35.6895, 139.6917, 
            LocalTime.of(10, 0), LocalTime.of(19, 0), 
            LocalTime.of(12, 30), LocalTime.of(13, 30),
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30)
        );
        
        // User1の勤務ルール作成
        mockMvc.perform(post("/api/work-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Request)))
                .andExpect(status().isCreated());
        
        // User2の勤務ルール作成
        mockMvc.perform(post("/api/work-rules")
                .header("Authorization", "Bearer " + secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Request)))
                .andExpect(status().isCreated());
        
        // User1は自分の勤務ルールのみ取得可能
        mockMvc.perform(get("/api/work-rules")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].workPlaceId").value(1));
        
        // User2は自分の勤務ルールのみ取得可能
        mockMvc.perform(get("/api/work-rules")
                .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].workPlaceId").value(2));
    }
    
    @Test
    @DisplayName("期間重複バリデーションテスト")
    void 期間重複バリデーションテスト() throws Exception {
        // 最初の勤務ルール作成（2024年1月〜6月）
        RegisterWorkRuleRequestBody firstRequest = new RegisterWorkRuleRequestBody(
            1L, 35.6762, 139.7649, 
            LocalTime.of(9, 0), LocalTime.of(18, 0), 
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30)
        );
        
        mockMvc.perform(post("/api/work-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());
        
        // 重複する期間の勤務ルール作成試行（2024年3月〜9月）
        RegisterWorkRuleRequestBody overlappingRequest = new RegisterWorkRuleRequestBody(
            2L, 35.6895, 139.6917, 
            LocalTime.of(10, 0), LocalTime.of(19, 0), 
            LocalTime.of(12, 30), LocalTime.of(13, 30),
            LocalDate.of(2024, 3, 1), LocalDate.of(2024, 9, 30)
        );
        
        // 重複期間のため409エラーになることを期待
        mockMvc.perform(post("/api/work-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overlappingRequest)))
                .andExpect(status().isConflict());
    }
    
    // ヘルパーメソッド
    
    private void setupSecondUser(String userId) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
            userId,
            "第2勤務ルールテストユーザー",
            userId + "@example.com",
            "SecondWorkRuleTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }
    
    private String getTokenForUser(String userId) throws Exception {
        LoginRequest loginRequest = new LoginRequest(userId, "SecondWorkRuleTest123!");
        
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