package com.github.okanikani.kairos.reports.others.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.okanikani.kairos.reports.applications.usecases.dto.DetailDto;
import com.github.okanikani.kairos.reports.applications.usecases.dto.GenerateReportFromLocationRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.RegisterReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UpdateReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.reports.applications.usecases.dto.WorkTimeDto;
import com.github.okanikani.kairos.security.JwtService;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginResponse;
import com.github.okanikani.kairos.users.applications.usecases.dto.RegisterRequest;
import com.github.okanikani.kairos.users.domains.models.entities.User;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Reports API統合テスト
 * 認証からレポート操作まの完全なAPIフローを検証
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-integration-testing-must-be-at-least-256-bits",
    "jwt.expiration=86400000"
})
@DisplayName("Reports API統合テスト")
class ReportApiIntegrationTest {
    
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
    
    private String testUserId;
    private String jwtToken;
    
    @BeforeEach
    void setUp() throws Exception {
        // テスト用のユーザーをクリア（InMemoryRepositoryの場合）
        if (userRepository instanceof com.github.okanikani.kairos.users.others.repositories.InMemoryUserRepository) {
            ((com.github.okanikani.kairos.users.others.repositories.InMemoryUserRepository) userRepository).clear();
        }
        
        // テスト用ユーザーを登録してJWTトークンを取得
        testUserId = "reporttest001";
        setupTestUserAndToken();
    }
    
    private void setupTestUserAndToken() throws Exception {
        // ユーザー登録
        RegisterRequest registerRequest = new RegisterRequest(
            testUserId,
            "レポートテストユーザー",
            "reporttest@example.com",
            "ReportTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        
        // ログインしてJWTトークンを取得
        LoginRequest loginRequest = new LoginRequest(testUserId, "ReportTest123!");
        
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
    @DisplayName("完全なレポートCRUDフロー_作成→取得→更新→削除")
    void 完全なレポートCRUDフロー() throws Exception {
        YearMonth yearMonth = YearMonth.of(2024, 1);
        
        // Step 1: レポート作成
        RegisterReportRequest createRequest = createSampleRegisterRequest(yearMonth, testUserId);
        
        MvcResult createResult = mockMvc.perform(post("/api/reports")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.yearMonth").value("2024-01"))
                .andExpect(jsonPath("$.owner.userId").value(testUserId))
                .andExpect(jsonPath("$.status").value("NOT_SUBMITTED"))
                .andExpect(jsonPath("$.workDays").isArray())
                .andExpect(jsonPath("$.summary").exists())
                .andReturn();
        
        // Step 2: レポート取得
        mockMvc.perform(get("/api/reports/2024/1")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth").value("2024-01"))
                .andExpect(jsonPath("$.owner.userId").value(testUserId))
                .andExpect(jsonPath("$.status").value("NOT_SUBMITTED"));
        
        // Step 3: レポート更新
        UpdateReportRequest updateRequest = createSampleUpdateRequest(yearMonth, testUserId);
        
        mockMvc.perform(put("/api/reports/2024/1")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth").value("2024-01"));
        
        // Step 4: レポート削除
        mockMvc.perform(delete("/api/reports/2024/1")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
        
        // Step 5: 削除確認（404になることを確認）
        mockMvc.perform(get("/api/reports/2024/1")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("位置情報からレポート生成フロー")
    void 位置情報からレポート生成フロー() throws Exception {
        YearMonth yearMonth = YearMonth.of(2024, 2);
        
        // 位置情報からレポート生成リクエスト
        GenerateReportFromLocationRequest generateRequest = new GenerateReportFromLocationRequest(
            yearMonth,
            new UserDto(testUserId)
        );
        
        mockMvc.perform(post("/api/reports/generate")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.yearMonth").value("2024-02"))
                .andExpect(jsonPath("$.owner.userId").value(testUserId))
                .andExpect(jsonPath("$.status").value("NOT_SUBMITTED"));
    }
    
    @Test
    @DisplayName("認証エラーテスト_未認証でのアクセス")
    void 認証エラーテスト_未認証でのアクセス() throws Exception {
        // JWTトークンなしでアクセス
        mockMvc.perform(get("/api/reports/2024/1"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(put("/api/reports/2024/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(delete("/api/reports/2024/1"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("認可エラーテスト_他ユーザーのデータアクセス")
    void 認可エラーテスト_他ユーザーのデータアクセス() throws Exception {
        // 別のユーザーIDでリクエスト作成
        String otherUserId = "otheruser";
        RegisterReportRequest unauthorizedRequest = createSampleRegisterRequest(
            YearMonth.of(2024, 1), otherUserId
        );
        
        // 認証されたユーザーと異なるユーザーIDでレポート作成を試行
        mockMvc.perform(post("/api/reports")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unauthorizedRequest)))
                .andExpect(status().isForbidden());
        
        // 更新でも同様のテスト
        UpdateReportRequest unauthorizedUpdateRequest = createSampleUpdateRequest(
            YearMonth.of(2024, 1), otherUserId
        );
        
        mockMvc.perform(put("/api/reports/2024/1")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unauthorizedUpdateRequest)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("バリデーションエラーテスト_無効な年月")
    void バリデーションエラーテスト_無効な年月() throws Exception {
        // 無効な月（13月）
        mockMvc.perform(get("/api/reports/2024/13")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest());
        
        // 無効な月（0月）
        mockMvc.perform(get("/api/reports/2024/0")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest());
        
        // パスパラメータとボディの年月不一致
        UpdateReportRequest mismatchRequest = createSampleUpdateRequest(
            YearMonth.of(2024, 2), testUserId  // ボディは2月
        );
        
        mockMvc.perform(put("/api/reports/2024/1")  // URLは1月
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mismatchRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("存在しないレポート取得_404エラー")
    void 存在しないレポート取得_404エラー() throws Exception {
        mockMvc.perform(get("/api/reports/2099/12")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("無効なJSONリクエスト_400エラー")
    void 無効なJSONリクエスト_400エラー() throws Exception {
        String invalidJson = "{ invalid json }";
        
        mockMvc.perform(post("/api/reports")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("必須フィールド欠如_400エラー")
    void 必須フィールド欠如_400エラー() throws Exception {
        // yearMonthが欠如したリクエスト
        String incompleteRequest = "{ \"user\": { \"userId\": \"" + testUserId + "\" }, \"workDays\": [] }";
        
        mockMvc.perform(post("/api/reports")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteRequest))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("複数ユーザーのデータ分離テスト")
    void 複数ユーザーのデータ分離テスト() throws Exception {
        // 2番目のユーザーを作成
        String secondUserId = "reporttest002";
        setupSecondUser(secondUserId);
        String secondUserToken = getTokenForUser(secondUserId);
        
        // 各ユーザーでレポート作成
        YearMonth yearMonth = YearMonth.of(2024, 3);
        
        RegisterReportRequest user1Request = createSampleRegisterRequest(yearMonth, testUserId);
        RegisterReportRequest user2Request = createSampleRegisterRequest(yearMonth, secondUserId);
        
        // User1のレポート作成
        mockMvc.perform(post("/api/reports")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Request)))
                .andExpect(status().isCreated());
        
        // User2のレポート作成
        mockMvc.perform(post("/api/reports")
                .header("Authorization", "Bearer " + secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Request)))
                .andExpect(status().isCreated());
        
        // User1は自分のレポートのみ取得可能
        mockMvc.perform(get("/api/reports/2024/3")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.userId").value(testUserId));
        
        // User2は自分のレポートのみ取得可能
        mockMvc.perform(get("/api/reports/2024/3")
                .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.userId").value(secondUserId));
    }
    
    // ヘルパーメソッド
    
    private RegisterReportRequest createSampleRegisterRequest(YearMonth yearMonth, String userId) {
        DetailDto detail1 = new DetailDto(
            LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(), 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(), 1, 18, 0)),
            Duration.ofHours(8),
            Duration.ofHours(1),
            Duration.ZERO,
            "通常勤務"
        );
        
        DetailDto detail2 = new DetailDto(
            LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 2),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(), 2, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(), 2, 17, 30)),
            Duration.ofHours(7).plusMinutes(30),
            Duration.ZERO,
            Duration.ZERO,
            "早退"
        );
        
        return new RegisterReportRequest(
            yearMonth,
            new UserDto(userId),
            List.of(detail1, detail2)
        );
    }
    
    private UpdateReportRequest createSampleUpdateRequest(YearMonth yearMonth, String userId) {
        DetailDto updatedDetail = new DetailDto(
            LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(), 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(), 1, 19, 0)),
            Duration.ofHours(9),
            Duration.ofHours(2),
            Duration.ZERO,
            "残業あり"
        );
        
        return new UpdateReportRequest(
            yearMonth,
            new UserDto(userId),
            List.of(updatedDetail)
        );
    }
    
    private void setupSecondUser(String userId) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
            userId,
            "第2テストユーザー",
            userId + "@example.com",
            "SecondTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }
    
    private String getTokenForUser(String userId) throws Exception {
        LoginRequest loginRequest = new LoginRequest(userId, "SecondTest123!");
        
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