package com.github.okanikani.kairos.reportcreationrules.others.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.okanikani.kairos.reportcreationrules.others.controllers.ReportCreationRuleController.RegisterReportCreationRuleRequestBody;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReportCreationRules API統合テスト
 * 勤怠作成ルール管理の完全なAPIフローを検証
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-integration-testing-must-be-at-least-256-bits",
    "jwt.expiration=86400000"
})
@DisplayName("ReportCreationRules API統合テスト")
class ReportCreationRuleApiIntegrationTest {
    
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
    private com.github.okanikani.kairos.reportcreationrules.others.repositories.InMemoryReportCreationRuleRepository reportCreationRuleRepository;
    
    private String testUserId;
    private String jwtToken;
    
    @BeforeEach
    void setUp() throws Exception {
        // テスト用のリポジトリをクリア（InMemoryRepositoryの場合）
        if (userRepository instanceof com.github.okanikani.kairos.users.others.repositories.InMemoryUserRepository) {
            ((com.github.okanikani.kairos.users.others.repositories.InMemoryUserRepository) userRepository).clear();
        }
        if (reportCreationRuleRepository != null) {
            reportCreationRuleRepository.clear();
        }
        
        // テスト用ユーザーを登録してJWTトークンを取得
        testUserId = "reportcreationruletest001";
        setupTestUserAndToken();
    }
    
    private void setupTestUserAndToken() throws Exception {
        // ユーザー登録
        RegisterRequest registerRequest = new RegisterRequest(
            testUserId,
            "勤怠作成ルールテストユーザー",
            "reportcreationruletest@example.com",
            "ReportCreationRuleTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        
        // ログインしてJWTトークンを取得
        LoginRequest loginRequest = new LoginRequest(testUserId, "ReportCreationRuleTest123!");
        
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
    @DisplayName("完全な勤怠作成ルールフロー_作成→取得")
    void 完全な勤怠作成ルールフロー() throws Exception {
        // Step 1: 勤怠作成ルール作成
        RegisterReportCreationRuleRequestBody createRequest = new RegisterReportCreationRuleRequestBody(
            25,    // closingDay（毎月25日締め）
            15     // timeCalculationUnitMinutes（15分単位）
        );
        
        MvcResult createResult = mockMvc.perform(post("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.user.userId").value(testUserId))
                .andExpect(jsonPath("$.closingDay").value(25))
                .andExpect(jsonPath("$.timeCalculationUnitMinutes").value(15))
                .andReturn();
        
        // レスポンスからIDを取得
        String responseContent = createResult.getResponse().getContentAsString();
        Long ruleId = objectMapper.readTree(responseContent).get("id").asLong();
        
        // Step 2: 勤怠作成ルール取得
        mockMvc.perform(get("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ruleId))
                .andExpect(jsonPath("$.user.userId").value(testUserId))
                .andExpect(jsonPath("$.closingDay").value(25))
                .andExpect(jsonPath("$.timeCalculationUnitMinutes").value(15));
    }
    
    @Test
    @DisplayName("月締め日境界値テスト")
    void 月締め日境界値テスト() throws Exception {
        // 有効な締め日
        int[] validClosingDays = {1, 15, 25, 28, 31};
        
        for (int i = 0; i < validClosingDays.length; i++) {
            int closingDay = validClosingDays[i];
            
            // 新しいユーザーを作成（ユニーク制約のため）
            String userId = "rcr_test_" + (i + 1);
            setupUserForClosingDayTest(userId);
            String token = getTokenForUser(userId, "ClosingDayTest123!");
            
            RegisterReportCreationRuleRequestBody request = new RegisterReportCreationRuleRequestBody(
                closingDay,
                15  // timeCalculationUnitMinutes
            );
            
            mockMvc.perform(post("/api/report-creation-rules")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.closingDay").value(closingDay));
        }
    }
    
    @Test
    @DisplayName("時間計算単位境界値テスト")
    void 時間計算単位境界値テスト() throws Exception {
        // 有効な時間計算単位
        int[] validTimeUnits = {1, 5, 10, 15, 30, 60};
        
        for (int i = 0; i < validTimeUnits.length; i++) {
            int timeUnit = validTimeUnits[i];
            
            // 新しいユーザーを作成（ユニーク制約のため）
            String userId = "rct_test_" + (i + 1);
            setupUserForTimeUnitTest(userId);
            String token = getTokenForUser(userId, "TimeUnitTest123!");
            
            RegisterReportCreationRuleRequestBody request = new RegisterReportCreationRuleRequestBody(
                25,      // closingDay
                timeUnit // timeCalculationUnitMinutes
            );
            
            mockMvc.perform(post("/api/report-creation-rules")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.timeCalculationUnitMinutes").value(timeUnit));
        }
    }
    
    @Test
    @DisplayName("認証エラーテスト_未認証でのアクセス")
    void 認証エラーテスト_未認証でのアクセス() throws Exception {
        // JWTトークンなしでアクセス
        mockMvc.perform(get("/api/report-creation-rules"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/report-creation-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("バリデーションエラーテスト_無効なリクエスト")
    void バリデーションエラーテスト_無効なリクエスト() throws Exception {
        // 必須フィールド欠如
        String incompleteRequest = "{ \"closingDay\": 25 }"; // timeCalculationUnitMinutes欠如
        
        mockMvc.perform(post("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteRequest))
                .andExpect(status().isBadRequest());
        
        // 無効なJSON
        String invalidJson = "{ invalid json }";
        
        mockMvc.perform(post("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("境界値外バリデーションテスト_無効な値")
    void 境界値外バリデーションテスト_無効な値() throws Exception {
        // 無効な締め日（32日）
        RegisterReportCreationRuleRequestBody invalidClosingDayRequest = 
            new RegisterReportCreationRuleRequestBody(32, 15);
        
        mockMvc.perform(post("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidClosingDayRequest)))
                .andExpect(status().isBadRequest());
        
        // 無効な時間計算単位（0分）
        RegisterReportCreationRuleRequestBody invalidTimeUnitRequest = 
            new RegisterReportCreationRuleRequestBody(25, 0);
        
        mockMvc.perform(post("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTimeUnitRequest)))
                .andExpect(status().isBadRequest());
        
        // 無効な時間計算単位（61分）
        RegisterReportCreationRuleRequestBody invalidTimeUnit2Request = 
            new RegisterReportCreationRuleRequestBody(25, 61);
        
        mockMvc.perform(post("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTimeUnit2Request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("勤怠作成ルール重複登録テスト_同一ユーザー")
    void 勤怠作成ルール重複登録テスト_同一ユーザー() throws Exception {
        // 最初の勤怠作成ルール作成
        RegisterReportCreationRuleRequestBody firstRequest = new RegisterReportCreationRuleRequestBody(25, 15);
        
        mockMvc.perform(post("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());
        
        // 同一ユーザーで再度勤怠作成ルール作成試行
        RegisterReportCreationRuleRequestBody duplicateRequest = new RegisterReportCreationRuleRequestBody(30, 30);
        
        // 重複のため409エラーになることを期待
        mockMvc.perform(post("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }
    
    @Test
    @DisplayName("複数ユーザーのデータ分離テスト")
    void 複数ユーザーのデータ分離テスト() throws Exception {
        // 2番目のユーザーを作成
        String secondUserId = "reportcreationruletest002";
        setupSecondUser(secondUserId);
        String secondUserToken = getTokenForUser(secondUserId, "SecondReportCreationRuleTest123!");
        
        // 各ユーザーで勤怠作成ルール作成
        RegisterReportCreationRuleRequestBody user1Request = new RegisterReportCreationRuleRequestBody(25, 15);
        RegisterReportCreationRuleRequestBody user2Request = new RegisterReportCreationRuleRequestBody(30, 30);
        
        // User1の勤怠作成ルール作成
        mockMvc.perform(post("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Request)))
                .andExpect(status().isCreated());
        
        // User2の勤怠作成ルール作成
        mockMvc.perform(post("/api/report-creation-rules")
                .header("Authorization", "Bearer " + secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Request)))
                .andExpect(status().isCreated());
        
        // User1は自分の勤怠作成ルールのみ取得可能
        mockMvc.perform(get("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.userId").value(testUserId))
                .andExpect(jsonPath("$.closingDay").value(25))
                .andExpect(jsonPath("$.timeCalculationUnitMinutes").value(15));
        
        // User2は自分の勤怠作成ルールのみ取得可能
        mockMvc.perform(get("/api/report-creation-rules")
                .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.userId").value(secondUserId))
                .andExpect(jsonPath("$.closingDay").value(30))
                .andExpect(jsonPath("$.timeCalculationUnitMinutes").value(30));
    }
    
    @Test
    @DisplayName("勤怠作成ルール未登録時の取得テスト_404エラー")
    void 勤怠作成ルール未登録時の取得テスト_404エラー() throws Exception {
        // 勤怠作成ルールを作成せずに取得
        mockMvc.perform(get("/api/report-creation-rules")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
    
    // ヘルパーメソッド
    
    private void setupUserForClosingDayTest(String userId) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
            userId,
            "締め日テストユーザー",
            userId + "@example.com",
            "ClosingDayTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }
    
    private void setupUserForTimeUnitTest(String userId) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
            userId,
            "時間単位テストユーザー",
            userId + "@example.com",
            "TimeUnitTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }
    
    private void setupSecondUser(String userId) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
            userId,
            "第2勤怠作成ルールテストユーザー",
            userId + "@example.com",
            "SecondReportCreationRuleTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }
    
    private String getTokenForUser(String userId, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(userId, password);
        
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