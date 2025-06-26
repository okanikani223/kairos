package com.github.okanikani.kairos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthControllerの最低限のテスト
 * 正常ケースのみを検証し、エラーケースは統合テストで確認
 */
@WebMvcTest(AuthController.class)
@Import(AuthControllerTestSecurityConfig.class)
@DisplayName("AuthController - 基本機能テスト")
class AuthControllerSimpleTest {
    
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
                .andExpect(jsonPath("$.user.userId").value("testuser123"));
        
        verify(authenticationUseCase).authenticate(any(LoginRequest.class));
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
                .andExpect(jsonPath("$.role").value("USER"));
        
        verify(registerUserUseCase).execute(any(RegisterRequest.class));
    }
}