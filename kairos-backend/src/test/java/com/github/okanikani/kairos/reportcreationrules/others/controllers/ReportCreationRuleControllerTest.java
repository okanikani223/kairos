package com.github.okanikani.kairos.reportcreationrules.others.controllers;

import com.github.okanikani.kairos.reportcreationrules.applications.usecases.FindAllReportCreationRulesUsecase;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.RegisterReportCreationRuleUsecase;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.ReportCreationRuleResponse;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReportCreationRuleControllerTest {

    private ReportCreationRuleController reportCreationRuleController;

    @Mock
    private RegisterReportCreationRuleUsecase registerReportCreationRuleUsecase;
    
    @Mock
    private FindAllReportCreationRulesUsecase findAllReportCreationRulesUsecase;
    
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportCreationRuleController = new ReportCreationRuleController(registerReportCreationRuleUsecase, findAllReportCreationRulesUsecase);
    }

    @Test
    void findReportCreationRule_正常ケース_200ステータスとレポート作成ルールを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
        ReportCreationRuleResponse expectedResponse = new ReportCreationRuleResponse(
            1L, new UserDto("testuser"), 1, 15
        );
        
        when(findAllReportCreationRulesUsecase.execute(anyString())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ReportCreationRuleResponse> response = reportCreationRuleController.findReportCreationRule(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("testuser", response.getBody().user().userId());
        assertEquals(1, response.getBody().closingDay());
        assertEquals(15, response.getBody().timeCalculationUnitMinutes());
        verify(findAllReportCreationRulesUsecase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findReportCreationRule_レポート作成ルールが存在しない場合_404ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findAllReportCreationRulesUsecase.execute(anyString())).thenReturn(null);

        // Act
        ResponseEntity<ReportCreationRuleResponse> response = reportCreationRuleController.findReportCreationRule(authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(findAllReportCreationRulesUsecase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findReportCreationRule_ユースケースで例外発生_500ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findAllReportCreationRulesUsecase.execute(anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<ReportCreationRuleResponse> response = reportCreationRuleController.findReportCreationRule(authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(findAllReportCreationRulesUsecase, times(1)).execute(eq("testuser"));
    }

    @Test
    void constructor_nullRegisterUsecase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new ReportCreationRuleController(null, findAllReportCreationRulesUsecase)
        );
        assertEquals("registerReportCreationRuleUsecaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullFindAllUsecase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new ReportCreationRuleController(registerReportCreationRuleUsecase, null)
        );
        assertEquals("findAllReportCreationRulesUsecaseは必須です", exception.getMessage());
    }
}