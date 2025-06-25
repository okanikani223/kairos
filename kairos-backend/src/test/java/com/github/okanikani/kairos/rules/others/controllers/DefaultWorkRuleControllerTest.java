package com.github.okanikani.kairos.rules.others.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.okanikani.kairos.commons.controllers.GlobalExceptionHandler;
import com.github.okanikani.kairos.rules.applications.usecases.FindAllDefaultWorkRulesUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.RegisterDefaultWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DefaultWorkRuleController.class)
@Import(GlobalExceptionHandler.class)
class DefaultWorkRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterDefaultWorkRuleUseCase registerDefaultWorkRuleUseCase;
    
    @MockitoBean
    private FindAllDefaultWorkRulesUseCase findAllDefaultWorkRulesUseCase;
    
    @MockitoBean
    private JwtService jwtService;
    
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser(username = "testuser")
    void findAllDefaultWorkRules_正常ケース_200ステータスとデフォルト勤務ルール一覧を返す() throws Exception {
        // Arrange
        List<DefaultWorkRuleResponse> expectedDefaultWorkRules = Arrays.asList(
            new DefaultWorkRuleResponse(1L, 100L, 35.6812, 139.7671, new UserDto("testuser"),
                LocalTime.of(9, 0), LocalTime.of(18, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0)),
            new DefaultWorkRuleResponse(2L, 200L, 35.6813, 139.7672, new UserDto("testuser"),
                LocalTime.of(10, 0), LocalTime.of(19, 0),
                null, null)
        );
        
        when(findAllDefaultWorkRulesUseCase.execute(eq("testuser"))).thenReturn(expectedDefaultWorkRules);

        // Act & Assert
        mockMvc.perform(get("/api/default-work-rules")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].workPlaceId").value(100))
                .andExpect(jsonPath("$[0].user.userId").value("testuser"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].workPlaceId").value(200));
        
        verify(findAllDefaultWorkRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findAllDefaultWorkRules_デフォルト勤務ルールが存在しない場合_200ステータスと空リストを返す() throws Exception {
        // Arrange
        when(findAllDefaultWorkRulesUseCase.execute(eq("testuser"))).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/default-work-rules")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        
        verify(findAllDefaultWorkRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findAllDefaultWorkRules_ユースケースで例外発生_500ステータスを返す() throws Exception {
        // Arrange
        when(findAllDefaultWorkRulesUseCase.execute(eq("testuser")))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act & Assert
        mockMvc.perform(get("/api/default-work-rules")
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"));
        
        verify(findAllDefaultWorkRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    void constructor_nullRegisterUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new DefaultWorkRuleController(null, findAllDefaultWorkRulesUseCase)
        );
        assertEquals("registerDefaultWorkRuleUseCaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullFindAllUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new DefaultWorkRuleController(registerDefaultWorkRuleUseCase, null)
        );
        assertEquals("findAllDefaultWorkRulesUseCaseは必須です", exception.getMessage());
    }
}