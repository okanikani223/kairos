package com.github.okanikani.kairos.reportcreationrules.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.DuplicateResourceException;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.RegisterReportCreationRuleRequest;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.ReportCreationRuleResponse;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.reportcreationrules.domains.models.entities.ReportCreationRule;
import com.github.okanikani.kairos.reportcreationrules.domains.models.repositories.ReportCreationRuleRepository;
import com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RegisterReportCreationRuleUseCaseのテストクラス
 */
class RegisterReportCreationRuleUseCaseTest {

    @Mock
    private ReportCreationRuleRepository reportCreationRuleRepository;

    private RegisterReportCreationRuleUseCase registerReportCreationRuleUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registerReportCreationRuleUseCase = new RegisterReportCreationRuleUseCase(reportCreationRuleRepository);
    }

    @Test
    void execute_正常ケース_勤怠作成ルールが登録される() {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        RegisterReportCreationRuleRequest request = new RegisterReportCreationRuleRequest(
            userDto,
            15,   // 15日締め
            15    // 15分単位
        );

        User user = new User("testuser");
        ReportCreationRule savedRule = new ReportCreationRule(1L, user, 15, 15);
        when(reportCreationRuleRepository.findByUser(user)).thenReturn(null);
        when(reportCreationRuleRepository.save(any(ReportCreationRule.class))).thenReturn(savedRule);

        // Act
        ReportCreationRuleResponse response = registerReportCreationRuleUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.user().userId());
        assertEquals(15, response.closingDay());
        assertEquals(15, response.timeCalculationUnitMinutes());

        verify(reportCreationRuleRepository, times(1)).findByUser(user);
        verify(reportCreationRuleRepository, times(1)).save(any(ReportCreationRule.class));
    }

    @Test
    void execute_既存勤怠作成ルールあり_例外が発生する() {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        RegisterReportCreationRuleRequest request = new RegisterReportCreationRuleRequest(
            userDto,
            15,
            15
        );

        User user = new User("testuser");
        ReportCreationRule existingRule = new ReportCreationRule(
            1L, user, 20, 30
        );
        when(reportCreationRuleRepository.findByUser(user)).thenReturn(existingRule);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> registerReportCreationRuleUseCase.execute(request)
        );
        assertEquals("指定されたユーザーの勤怠作成ルールが既に存在します", exception.getMessage());

        verify(reportCreationRuleRepository, times(1)).findByUser(user);
        verify(reportCreationRuleRepository, never()).save(any());
    }

    @Test
    void execute_月末締め_勤怠作成ルールが登録される() {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        RegisterReportCreationRuleRequest request = new RegisterReportCreationRuleRequest(
            userDto,
            31,   // 月末締め
            30    // 30分単位
        );

        User user = new User("testuser");
        ReportCreationRule savedRule = new ReportCreationRule(1L, user, 31, 30);
        when(reportCreationRuleRepository.findByUser(user)).thenReturn(null);
        when(reportCreationRuleRepository.save(any(ReportCreationRule.class))).thenReturn(savedRule);

        // Act
        ReportCreationRuleResponse response = registerReportCreationRuleUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(31, response.closingDay());
        assertEquals(30, response.timeCalculationUnitMinutes());

        verify(reportCreationRuleRepository, times(1)).save(any(ReportCreationRule.class));
    }

    @Test
    void execute_1分単位計算_勤怠作成ルールが登録される() {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        RegisterReportCreationRuleRequest request = new RegisterReportCreationRuleRequest(
            userDto,
            1,    // 1日締め
            1     // 1分単位
        );

        User user = new User("testuser");
        ReportCreationRule savedRule = new ReportCreationRule(1L, user, 1, 1);
        when(reportCreationRuleRepository.findByUser(user)).thenReturn(null);
        when(reportCreationRuleRepository.save(any(ReportCreationRule.class))).thenReturn(savedRule);

        // Act
        ReportCreationRuleResponse response = registerReportCreationRuleUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.closingDay());
        assertEquals(1, response.timeCalculationUnitMinutes());

        verify(reportCreationRuleRepository, times(1)).save(any(ReportCreationRule.class));
    }

    @Test
    void execute_nullリクエスト_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> registerReportCreationRuleUseCase.execute(null)
        );
        assertEquals("リクエストは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new RegisterReportCreationRuleUseCase(null)
        );
        assertEquals("reportCreationRuleRepositoryは必須です", exception.getMessage());
    }
}