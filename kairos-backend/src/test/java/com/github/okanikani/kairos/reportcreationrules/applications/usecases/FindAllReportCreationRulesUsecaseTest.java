package com.github.okanikani.kairos.reportcreationrules.applications.usecases;

import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.ReportCreationRuleResponse;
import com.github.okanikani.kairos.reportcreationrules.domains.models.entities.ReportCreationRule;
import com.github.okanikani.kairos.reportcreationrules.domains.models.repositories.ReportCreationRuleRepository;
import com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FindAllReportCreationRulesUsecaseTest {

    private FindAllReportCreationRulesUsecase findAllReportCreationRulesUsecase;

    @Mock
    private ReportCreationRuleRepository reportCreationRuleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        findAllReportCreationRulesUsecase = new FindAllReportCreationRulesUsecase(reportCreationRuleRepository);
    }

    @Test
    void execute_正常ケース_ユーザーのレポート作成ルールが取得される() {
        // Arrange
        String userId = "testuser";
        User user = new User(userId);
        
        ReportCreationRule reportCreationRule = new ReportCreationRule(1L, user, 1, 15);

        when(reportCreationRuleRepository.findByUser(eq(user))).thenReturn(reportCreationRule);

        // Act
        ReportCreationRuleResponse response = findAllReportCreationRulesUsecase.execute(userId);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(userId, response.user().userId());
        assertEquals(1, response.closingDay());
        assertEquals(15, response.timeCalculationUnitMinutes());
        
        verify(reportCreationRuleRepository, times(1)).findByUser(eq(user));
    }

    @Test
    void execute_レポート作成ルールが存在しない場合_nullが返される() {
        // Arrange
        String userId = "testuser";
        User user = new User(userId);
        
        when(reportCreationRuleRepository.findByUser(eq(user))).thenReturn(null);

        // Act
        ReportCreationRuleResponse response = findAllReportCreationRulesUsecase.execute(userId);

        // Assert
        assertNull(response);
        verify(reportCreationRuleRepository, times(1)).findByUser(eq(user));
    }

    @Test
    void execute_nullUserId_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> findAllReportCreationRulesUsecase.execute(null)
        );
        assertEquals("userIdは必須です", exception.getMessage());
        verify(reportCreationRuleRepository, never()).findByUser(any());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new FindAllReportCreationRulesUsecase(null)
        );
        assertEquals("reportCreationRuleRepositoryは必須です", exception.getMessage());
    }
}