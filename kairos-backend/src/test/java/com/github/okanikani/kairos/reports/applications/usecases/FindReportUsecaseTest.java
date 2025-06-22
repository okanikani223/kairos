package com.github.okanikani.kairos.reports.applications.usecases;

import com.github.okanikani.kairos.TestSecurityConfig;
import com.github.okanikani.kairos.reports.applications.usecases.dto.FindReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.ReportResponse;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.repositories.ReportRepository;
import com.github.okanikani.kairos.reports.domains.models.vos.Summary;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FindReportUsecaseTest {

    @MockitoBean
    private ReportRepository reportRepository;

    @Autowired
    private FindReportUsecase usecase;

    @Test
    void execute_正常ケース_勤怠表が取得される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("user001");
        FindReportRequest request = new FindReportRequest(yearMonth, userDto);

        User user = new User("user001");
        Summary summary = new Summary(
            20.0,
            2.0,
            0.0,
            0.0,
            Duration.ofHours(160),
            Duration.ofHours(10),
            Duration.ZERO
        );
        
        Report mockReport = new Report(
            yearMonth,
            user,
            ReportStatus.NOT_SUBMITTED,
            List.of(),
            summary
        );

        when(reportRepository.find(eq(yearMonth), any(User.class))).thenReturn(mockReport);

        // Act
        ReportResponse response = usecase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(yearMonth, response.yearMonth());
        assertEquals(userDto.userId(), response.owner().userId());
        assertEquals(ReportStatus.NOT_SUBMITTED.name(), response.status());
        assertNotNull(response.summary());
        
        verify(reportRepository, times(1)).find(eq(yearMonth), any(User.class));
    }

    @Test
    void execute_勤怠表が存在しない_nullが返される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("user001");
        FindReportRequest request = new FindReportRequest(yearMonth, userDto);

        when(reportRepository.find(eq(yearMonth), any(User.class))).thenReturn(null);

        // Act
        ReportResponse response = usecase.execute(request);

        // Assert
        assertNull(response);
        verify(reportRepository, times(1)).find(eq(yearMonth), any(User.class));
    }

    @Test
    void execute_nullリクエスト_例外が発生する() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> usecase.execute(null));
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new FindReportUsecase(null));
    }
}