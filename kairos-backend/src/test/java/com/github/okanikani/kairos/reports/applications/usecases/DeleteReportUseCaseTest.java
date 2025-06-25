package com.github.okanikani.kairos.reports.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.BusinessRuleViolationException;
import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.reports.applications.usecases.dto.DeleteReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.repositories.ReportRepository;
import com.github.okanikani.kairos.reports.domains.models.vos.Summary;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DeleteReportUseCaseTest {

    private DeleteReportUseCase deleteReportUseCase;

    @Mock
    private ReportRepository reportRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deleteReportUseCase = new DeleteReportUseCase(reportRepository);
    }

    @Test
    void execute_正常ケース_存在する勤怠表を削除できる() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        DeleteReportRequest request = new DeleteReportRequest(yearMonth, userDto);

        Report existingReport = new Report(
            yearMonth,
            user,
            ReportStatus.NOT_SUBMITTED, // 未提出なので削除可能
            List.of(),
            Summary.EMPTY
        );

        when(reportRepository.find(eq(yearMonth), eq(user))).thenReturn(existingReport);

        // Act & Assert
        assertDoesNotThrow(() -> deleteReportUseCase.execute(request));
        verify(reportRepository, times(1)).delete(eq(yearMonth), eq(user));
    }

    @Test
    void execute_異常ケース_存在しない勤怠表を削除しようとすると例外が発生する() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        DeleteReportRequest request = new DeleteReportRequest(yearMonth, userDto);

        when(reportRepository.find(eq(yearMonth), eq(user))).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> deleteReportUseCase.execute(request)
        );
        assertEquals("削除対象の勤怠表が存在しません: 2024-01", exception.getMessage());
        verify(reportRepository, never()).delete(any(), any());
    }

    @Test
    void execute_異常ケース_提出済みの勤怠表は削除不可() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        DeleteReportRequest request = new DeleteReportRequest(yearMonth, userDto);

        Report submittedReport = new Report(
            yearMonth,
            user,
            ReportStatus.SUBMITTED, // 提出済みなので削除不可
            List.of(),
            Summary.EMPTY
        );

        when(reportRepository.find(eq(yearMonth), eq(user))).thenReturn(submittedReport);

        // Act & Assert
        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> deleteReportUseCase.execute(request)
        );
        assertEquals("ステータスがSUBMITTEDの勤怠表は削除できません", exception.getMessage());
        verify(reportRepository, never()).delete(any(), any());
    }

    @Test
    void execute_異常ケース_承認済みの勤怠表は削除不可() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        DeleteReportRequest request = new DeleteReportRequest(yearMonth, userDto);

        Report approvedReport = new Report(
            yearMonth,
            user,
            ReportStatus.APPROVED, // 承認済みなので削除不可
            List.of(),
            Summary.EMPTY
        );

        when(reportRepository.find(eq(yearMonth), eq(user))).thenReturn(approvedReport);

        // Act & Assert
        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> deleteReportUseCase.execute(request)
        );
        assertEquals("ステータスがAPPROVEDの勤怠表は削除できません", exception.getMessage());
        verify(reportRepository, never()).delete(any(), any());
    }

    @Test
    void execute_異常ケース_nullのリクエストで例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> deleteReportUseCase.execute(null)
        );
        assertEquals("requestは必須です", exception.getMessage());
        verify(reportRepository, never()).delete(any(), any());
    }
}