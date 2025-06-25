package com.github.okanikani.kairos.reports.applications.usecases;

import com.github.okanikani.kairos.TestSecurityConfig;
import com.github.okanikani.kairos.reports.applications.usecases.dto.*;
import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.repositories.ReportRepository;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RegisterReportUseCaseTest {

    @MockitoBean
    private ReportRepository reportRepository;

    @Autowired
    private RegisterReportUseCase usecase;

    @Test
    void execute_正常ケース_勤怠表が登録される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("user001");
        
        DetailDto detailDto = new DetailDto(
            LocalDate.of(2024, 1, 15),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 15, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 15, 18, 0)),
            Duration.ofHours(8),
            Duration.ofHours(1),
            Duration.ZERO,
            "通常勤務"
        );
        
        RegisterReportRequest request = new RegisterReportRequest(
            yearMonth,
            userDto,
            List.of(detailDto)
        );

        // 既存の勤怠表が存在しない設定
        when(reportRepository.find(eq(yearMonth), any(User.class))).thenReturn(null);

        // Act
        ReportResponse response = usecase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(yearMonth, response.yearMonth());
        assertEquals(userDto.userId(), response.owner().userId());
        assertEquals(ReportStatus.NOT_SUBMITTED.name(), response.status());
        assertEquals(1, response.workDays().size());
        assertNotNull(response.summary());

        // Repositoryの呼び出し確認
        verify(reportRepository, times(1)).find(eq(yearMonth), any(User.class));
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void execute_既存勤怠表あり_例外が発生する() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("user001");
        
        RegisterReportRequest request = new RegisterReportRequest(
            yearMonth,
            userDto,
            List.of()
        );

        // 既存の勤怠表が存在する設定
        Report existingReport = mock(Report.class);
        when(reportRepository.find(eq(yearMonth), any(User.class))).thenReturn(existingReport);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> usecase.execute(request)
        );

        assertTrue(exception.getMessage().contains("既に存在します"));
        
        // saveが呼ばれないことを確認
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void execute_nullリクエスト_例外が発生する() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> usecase.execute(null));
    }

    @Test
    void execute_有給休暇含む勤怠表_正しく集計される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("user001");
        
        DetailDto workDay = new DetailDto(
            LocalDate.of(2024, 1, 15),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 15, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 15, 18, 0)),
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO,
            "通常勤務"
        );
        
        DetailDto paidLeaveDay = new DetailDto(
            LocalDate.of(2024, 1, 16),
            false,
            "PAID_LEAVE",
            null,
            null,
            Duration.ZERO,
            Duration.ZERO,
            Duration.ZERO,
            "有給休暇"
        );
        
        RegisterReportRequest request = new RegisterReportRequest(
            yearMonth,
            userDto,
            List.of(workDay, paidLeaveDay)
        );

        when(reportRepository.find(eq(yearMonth), any(User.class))).thenReturn(null);

        // Act
        ReportResponse response = usecase.execute(request);

        // Assert
        assertNotNull(response.summary());
        assertEquals(1.0, response.summary().paidLeaveDays());
        assertEquals(Duration.ofHours(8), response.summary().totalWorkTime());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new RegisterReportUseCase(null));
    }
}