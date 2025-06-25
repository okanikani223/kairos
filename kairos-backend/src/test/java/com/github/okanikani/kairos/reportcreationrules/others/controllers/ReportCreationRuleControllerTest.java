package com.github.okanikani.kairos.reportcreationrules.others.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.okanikani.kairos.commons.controllers.GlobalExceptionHandler;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.FindAllReportCreationRulesUseCase;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.RegisterReportCreationRuleUseCase;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.ReportCreationRuleResponse;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.UserDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportCreationRuleController.class)
@Import(GlobalExceptionHandler.class)
class ReportCreationRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterReportCreationRuleUseCase registerReportCreationRuleUseCase;
    
    @MockitoBean
    private FindAllReportCreationRulesUseCase findAllReportCreationRulesUseCase;
    
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
    void findReportCreationRule_正常ケース_200ステータスとレポート作成ルールを返す() throws Exception {
        // Arrange
        ReportCreationRuleResponse expectedResponse = new ReportCreationRuleResponse(
            1L, new UserDto("testuser"), 1, 15
        );
        when(findAllReportCreationRulesUseCase.execute(eq("testuser"))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/report-creation-rules")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.userId").value("testuser"))
                .andExpect(jsonPath("$.closingDay").value(1))
                .andExpect(jsonPath("$.timeCalculationUnitMinutes").value(15));
        
        verify(findAllReportCreationRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findReportCreationRule_レポート作成ルールが存在しない場合_404ステータスを返す() throws Exception {
        // Arrange
        when(findAllReportCreationRulesUseCase.execute(anyString())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/report-creation-rules")
                .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(findAllReportCreationRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findReportCreationRule_ユースケースで例外発生_500ステータスを返す() throws Exception {
        // Arrange
        when(findAllReportCreationRulesUseCase.execute(anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act & Assert
        mockMvc.perform(get("/api/report-creation-rules")
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"));
        
        verify(findAllReportCreationRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    void constructor_nullRegisterUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new ReportCreationRuleController(null, findAllReportCreationRulesUseCase)
        );
        assertEquals("registerReportCreationRuleUseCaseは必須です", exception.getMessage());
    }

    @Test  
    void constructor_nullFindUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new ReportCreationRuleController(registerReportCreationRuleUseCase, null)
        );
        assertEquals("findAllReportCreationRulesUseCaseは必須です", exception.getMessage());
    }
}