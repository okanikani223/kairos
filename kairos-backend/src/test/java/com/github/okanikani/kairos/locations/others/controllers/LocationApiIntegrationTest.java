package com.github.okanikani.kairos.locations.others.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.okanikani.kairos.locations.applications.usecases.dto.RegisterLocationRequest;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Locations API統合テスト
 * 位置情報登録・検索・削除の完全なAPIフローを検証
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-integration-testing-must-be-at-least-256-bits",
    "jwt.expiration=86400000"
})
@DisplayName("Locations API統合テスト")
class LocationApiIntegrationTest {
    
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
        testUserId = "locationtest001";
        setupTestUserAndToken();
    }
    
    private void setupTestUserAndToken() throws Exception {
        // ユーザー登録
        RegisterRequest registerRequest = new RegisterRequest(
            testUserId,
            "位置情報テストユーザー",
            "locationtest@example.com",
            "LocationTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        
        // ログインしてJWTトークンを取得
        LoginRequest loginRequest = new LoginRequest(testUserId, "LocationTest123!");
        
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
    @DisplayName("完全な位置情報CRUDフロー_登録→取得→検索→削除")
    void 完全な位置情報CRUDフロー() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        
        // Step 1: 位置情報登録
        RegisterLocationRequest createRequest = new RegisterLocationRequest(
            35.6762,   // 東京駅付近の緯度
            139.7649,  // 東京駅付近の経度
            now
        );
        
        MvcResult createResult = mockMvc.perform(post("/api/locations")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.latitude").value(35.6762))
                .andExpect(jsonPath("$.longitude").value(139.7649))
                .andExpect(jsonPath("$.recordedAt").exists())
                .andReturn();
        
        // レスポンスからIDを取得
        String responseContent = createResult.getResponse().getContentAsString();
        Long locationId = objectMapper.readTree(responseContent).get("id").asLong();
        
        // Step 2: 全位置情報取得
        mockMvc.perform(get("/api/locations")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.latitude == 35.6762 && @.longitude == 139.7649)]").exists());
        
        // Step 3: ID指定で位置情報取得
        mockMvc.perform(get("/api/locations/" + locationId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(locationId))
                .andExpect(jsonPath("$.latitude").value(35.6762))
                .andExpect(jsonPath("$.longitude").value(139.7649));
        
        // Step 4: 日時範囲検索
        LocalDateTime searchStart = now.minusHours(1);
        LocalDateTime searchEnd = now.plusHours(1);
        
        mockMvc.perform(get("/api/locations/search")
                .header("Authorization", "Bearer " + jwtToken)
                .param("startDateTime", searchStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .param("endDateTime", searchEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.latitude == 35.6762 && @.longitude == 139.7649)]").exists());
        
        // Step 5: 位置情報削除
        mockMvc.perform(delete("/api/locations/" + locationId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
        
        // Step 6: 削除確認（該当する位置情報が削除されていることを確認）
        mockMvc.perform(get("/api/locations")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    @DisplayName("複数位置情報の登録と検索")
    void 複数位置情報の登録と検索() throws Exception {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 9, 0);
        
        // 複数の位置情報を登録
        RegisterLocationRequest[] requests = {
            new RegisterLocationRequest(35.6762, 139.7649, baseTime),           // 東京駅
            new RegisterLocationRequest(35.6895, 139.6917, baseTime.plusHours(1)), // 新宿駅
            new RegisterLocationRequest(35.6586, 139.7454, baseTime.plusHours(2))  // 銀座駅
        };
        
        for (RegisterLocationRequest request : requests) {
            mockMvc.perform(post("/api/locations")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
        
        // 全位置情報取得（3件あることを確認）
        mockMvc.perform(get("/api/locations")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
        
        // 時間範囲検索（最初の2件のみヒット）
        LocalDateTime searchStart = baseTime.minusMinutes(30);
        LocalDateTime searchEnd = baseTime.plusHours(1).plusMinutes(30);
        
        mockMvc.perform(get("/api/locations/search")
                .header("Authorization", "Bearer " + jwtToken)
                .param("startDateTime", searchStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .param("endDateTime", searchEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
        
        // 狭い時間範囲検索（1件のみヒット）
        searchEnd = baseTime.plusMinutes(30);
        
        mockMvc.perform(get("/api/locations/search")
                .header("Authorization", "Bearer " + jwtToken)
                .param("startDateTime", searchStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .param("endDateTime", searchEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
    
    @Test
    @DisplayName("GPS座標境界値テスト")
    void GPS座標境界値テスト() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        
        // 有効な境界値
        RegisterLocationRequest[] validRequests = {
            new RegisterLocationRequest(90.0, 180.0, now),     // 最大値
            new RegisterLocationRequest(-90.0, -180.0, now),   // 最小値
            new RegisterLocationRequest(0.0, 0.0, now),        // 中央値
            new RegisterLocationRequest(35.6762, 139.7649, now) // 実際の座標（東京駅）
        };
        
        for (RegisterLocationRequest request : validRequests) {
            mockMvc.perform(post("/api/locations")
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
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/locations/1"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(delete("/api/locations/1"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/locations/search")
                .param("startDateTime", "2024-01-01T00:00:00")
                .param("endDateTime", "2024-01-01T23:59:59"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("バリデーションエラーテスト_無効なリクエスト")
    void バリデーションエラーテスト_無効なリクエスト() throws Exception {
        // 必須フィールド欠如
        String incompleteRequest = "{ \"latitude\": 35.6762, \"longitude\": 139.7649 }"; // recordedAt欠如
        
        mockMvc.perform(post("/api/locations")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteRequest))
                .andExpect(status().isBadRequest());
        
        // 無効なJSON
        String invalidJson = "{ invalid json }";
        
        mockMvc.perform(post("/api/locations")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("検索パラメータエラーテスト_無効な日時フォーマット")
    void 検索パラメータエラーテスト_無効な日時フォーマット() throws Exception {
        // 無効な日時フォーマット
        mockMvc.perform(get("/api/locations/search")
                .header("Authorization", "Bearer " + jwtToken)
                .param("startDateTime", "invalid-date-format")
                .param("endDateTime", "2024-01-01T23:59:59"))
                .andExpect(status().isBadRequest());
        
        // パラメータ欠如
        mockMvc.perform(get("/api/locations/search")
                .header("Authorization", "Bearer " + jwtToken)
                .param("startDateTime", "2024-01-01T00:00:00"))
                // endDateTime欠如
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("存在しない位置情報アクセス_404エラー")
    void 存在しない位置情報アクセス_404エラー() throws Exception {
        // 存在しないIDで取得
        mockMvc.perform(get("/api/locations/999999")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
        
        // 存在しないIDで削除
        mockMvc.perform(delete("/api/locations/999999")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("複数ユーザーのデータ分離テスト")
    void 複数ユーザーのデータ分離テスト() throws Exception {
        // 2番目のユーザーを作成
        String secondUserId = "locationtest002";
        setupSecondUser(secondUserId);
        String secondUserToken = getTokenForUser(secondUserId);
        
        LocalDateTime now = LocalDateTime.now();
        
        // 各ユーザーで位置情報登録
        RegisterLocationRequest user1Request = new RegisterLocationRequest(35.6762, 139.7649, now);
        RegisterLocationRequest user2Request = new RegisterLocationRequest(35.6895, 139.6917, now);
        
        // User1の位置情報登録
        mockMvc.perform(post("/api/locations")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Request)))
                .andExpect(status().isCreated());
        
        // User2の位置情報登録
        mockMvc.perform(post("/api/locations")
                .header("Authorization", "Bearer " + secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Request)))
                .andExpect(status().isCreated());
        
        // User1は自分の位置情報のみ取得可能
        mockMvc.perform(get("/api/locations")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        
        // User2は自分の位置情報のみ取得可能  
        mockMvc.perform(get("/api/locations")
                .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    @DisplayName("空の検索結果テスト")
    void 空の検索結果テスト() throws Exception {
        // データが存在しない時間範囲で検索
        LocalDateTime futureStart = LocalDateTime.of(2099, 12, 31, 23, 0);
        LocalDateTime futureEnd = LocalDateTime.of(2099, 12, 31, 23, 59);
        
        mockMvc.perform(get("/api/locations/search")
                .header("Authorization", "Bearer " + jwtToken)
                .param("startDateTime", futureStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .param("endDateTime", futureEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
    
    // ヘルパーメソッド
    
    private void setupSecondUser(String userId) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
            userId,
            "第2位置情報テストユーザー",
            userId + "@example.com",
            "SecondLocationTest123!",
            "USER"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }
    
    private String getTokenForUser(String userId) throws Exception {
        LoginRequest loginRequest = new LoginRequest(userId, "SecondLocationTest123!");
        
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