package com.github.okanikani.kairos.reports.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.reports.applications.usecases.dto.DetailDto;
import com.github.okanikani.kairos.reports.applications.usecases.dto.ReportResponse;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UpdateReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.reports.applications.usecases.dto.WorkTimeDto;
import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.repositories.ReportRepository;
import com.github.okanikani.kairos.reports.domains.models.vos.Summary;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UpdateReportUseCaseのテストクラス
 * 勤怠表更新機能の全ケースをカバー
 */
@ExtendWith(MockitoExtension.class)
class UpdateReportUseCaseTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private UpdateReportUseCase updateReportUseCase;

    @Test
    void constructor_リポジトリがnull_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new UpdateReportUseCase(null)
        );
        assertEquals("reportRepositoryは必須です", exception.getMessage());
    }

    @Test
    void execute_正常ケース_勤怠表が更新される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        DetailDto detailDto = new DetailDto(
            LocalDate.of(2024, 1, 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 18, 0)),
            Duration.ofHours(8),
            Duration.ofHours(1),
            Duration.ZERO,
            ""
        );
        UpdateReportRequest request = new UpdateReportRequest(
            yearMonth,
            userDto,
            List.of(detailDto)
        );

        User user = new User("testuser");
        Report existingReport = new Report(
            yearMonth,
            user,
            ReportStatus.NOT_SUBMITTED,
            Collections.emptyList(),
            new Summary(0.0, 0.0, 0.0, 0.0, Duration.ZERO, Duration.ZERO, Duration.ZERO)
        );

        when(reportRepository.find(eq(yearMonth), eq(user))).thenReturn(existingReport);
        doNothing().when(reportRepository).update(any(Report.class));

        // Act
        ReportResponse response = updateReportUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(yearMonth, response.yearMonth());
        assertEquals("testuser", response.owner().userId());
        assertEquals("NOT_SUBMITTED", response.status());
        assertEquals(1, response.workDays().size());

        verify(reportRepository, times(1)).find(eq(yearMonth), eq(user));
        verify(reportRepository, times(1)).update(any(Report.class));
    }

    @Test
    void execute_nullリクエスト_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> updateReportUseCase.execute(null)
        );
        assertEquals("requestは必須です", exception.getMessage());
    }

    @Test
    void execute_更新対象が存在しない_例外が発生する() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        UpdateReportRequest request = new UpdateReportRequest(
            yearMonth,
            userDto,
            Collections.emptyList()
        );

        User user = new User("testuser");
        when(reportRepository.find(eq(yearMonth), eq(user))).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> updateReportUseCase.execute(request)
        );
        assertEquals("更新対象の勤怠表が存在しません: 2024-01", exception.getMessage());

        verify(reportRepository, times(1)).find(eq(yearMonth), eq(user));
        verify(reportRepository, never()).update(any(Report.class));
    }

    @Test
    void execute_提出済み勤怠表_更新不可例外が発生する() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        UpdateReportRequest request = new UpdateReportRequest(
            yearMonth,
            userDto,
            Collections.emptyList()
        );

        User user = new User("testuser");
        Report submittedReport = new Report(
            yearMonth,
            user,
            ReportStatus.SUBMITTED,
            Collections.emptyList(),
            new Summary(0.0, 0.0, 0.0, 0.0, Duration.ZERO, Duration.ZERO, Duration.ZERO)
        );

        when(reportRepository.find(eq(yearMonth), eq(user))).thenReturn(submittedReport);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> updateReportUseCase.execute(request)
        );
        assertEquals("ステータスがSUBMITTEDの勤怠表は更新できません", exception.getMessage());

        verify(reportRepository, times(1)).find(eq(yearMonth), eq(user));
        verify(reportRepository, never()).update(any(Report.class));
    }

    @Test
    void execute_承認済み勤怠表_更新不可例外が発生する() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        UpdateReportRequest request = new UpdateReportRequest(
            yearMonth,
            userDto,
            Collections.emptyList()
        );

        User user = new User("testuser");
        Report approvedReport = new Report(
            yearMonth,
            user,
            ReportStatus.APPROVED,
            Collections.emptyList(),
            new Summary(0.0, 0.0, 0.0, 0.0, Duration.ZERO, Duration.ZERO, Duration.ZERO)
        );

        when(reportRepository.find(eq(yearMonth), eq(user))).thenReturn(approvedReport);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> updateReportUseCase.execute(request)
        );
        assertEquals("ステータスがAPPROVEDの勤怠表は更新できません", exception.getMessage());

        verify(reportRepository, times(1)).find(eq(yearMonth), eq(user));
        verify(reportRepository, never()).update(any(Report.class));
    }

    @Test
    void execute_空の勤務日リスト_空のサマリで更新される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        UpdateReportRequest request = new UpdateReportRequest(
            yearMonth,
            userDto,
            Collections.emptyList()
        );

        User user = new User("testuser");
        Report existingReport = new Report(
            yearMonth,
            user,
            ReportStatus.NOT_SUBMITTED,
            Collections.emptyList(),
            new Summary(0.0, 0.0, 0.0, 0.0, Duration.ZERO, Duration.ZERO, Duration.ZERO)
        );

        when(reportRepository.find(eq(yearMonth), eq(user))).thenReturn(existingReport);
        doNothing().when(reportRepository).update(any(Report.class));

        // Act
        ReportResponse response = updateReportUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.workDays().size());
        assertNotNull(response.summary());

        verify(reportRepository, times(1)).find(eq(yearMonth), eq(user));
        verify(reportRepository, times(1)).update(any(Report.class));
    }
}