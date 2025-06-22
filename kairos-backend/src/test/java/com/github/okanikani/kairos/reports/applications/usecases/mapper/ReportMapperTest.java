package com.github.okanikani.kairos.reports.applications.usecases.mapper;

import com.github.okanikani.kairos.reports.applications.usecases.dto.*;
import com.github.okanikani.kairos.reports.domains.models.constants.LeaveType;
import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.vos.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportMapperTest {

    @Test
    void toUser_UserDtoからUserに正しく変換される() {
        // Arrange
        UserDto dto = new UserDto("user001");

        // Act
        User user = ReportMapper.toUser(dto);

        // Assert
        assertEquals("user001", user.userId());
    }

    @Test
    void toUserDto_UserからUserDtoに正しく変換される() {
        // Arrange
        User user = new User("user001");

        // Act
        UserDto dto = ReportMapper.toUserDto(user);

        // Assert
        assertEquals("user001", dto.userId());
    }

    @Test
    void toWorkTime_WorkTimeDtoからWorkTimeに正しく変換される() {
        // Arrange
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 9, 0);
        WorkTimeDto dto = new WorkTimeDto(dateTime);

        // Act
        WorkTime workTime = ReportMapper.toWorkTime(dto);

        // Assert
        assertEquals(dateTime, workTime.value());
    }

    @Test
    void toWorkTime_nullを渡すとnullが返される() {
        // Act
        WorkTime workTime = ReportMapper.toWorkTime(null);

        // Assert
        assertNull(workTime);
    }

    @Test
    void toWorkTimeDto_WorkTimeからWorkTimeDtoに正しく変換される() {
        // Arrange
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 9, 0);
        WorkTime workTime = new WorkTime(dateTime);

        // Act
        WorkTimeDto dto = ReportMapper.toWorkTimeDto(workTime);

        // Assert
        assertEquals(dateTime, dto.value());
    }

    @Test
    void toWorkTimeDto_nullを渡すとnullが返される() {
        // Act
        WorkTimeDto dto = ReportMapper.toWorkTimeDto(null);

        // Assert
        assertNull(dto);
    }

    @Test
    void toDetail_DetailDtoからDetailに正しく変換される() {
        // Arrange
        LocalDate workDate = LocalDate.of(2024, 1, 15);
        WorkTimeDto startTime = new WorkTimeDto(LocalDateTime.of(2024, 1, 15, 9, 0));
        WorkTimeDto endTime = new WorkTimeDto(LocalDateTime.of(2024, 1, 15, 18, 0));
        
        DetailDto dto = new DetailDto(
            workDate,
            false,
            "PAID_LEAVE",
            startTime,
            endTime,
            Duration.ofHours(8),
            Duration.ofHours(1),
            Duration.ZERO,
            "テスト"
        );

        // Act
        Detail detail = ReportMapper.toDetail(dto);

        // Assert
        assertEquals(workDate, detail.workDate());
        assertFalse(detail.isHoliday());
        assertEquals(LeaveType.PAID_LEAVE, detail.leaveType());
        assertEquals(Duration.ofHours(8), detail.workingHours());
        assertEquals(Duration.ofHours(1), detail.overtimeHours());
        assertEquals("テスト", detail.note());
    }

    @Test
    void toDetail_休暇区分nullの場合正しく変換される() {
        // Arrange
        DetailDto dto = new DetailDto(
            LocalDate.of(2024, 1, 15),
            false,
            null,
            null,
            null,
            Duration.ZERO,
            Duration.ZERO,
            Duration.ZERO,
            null
        );

        // Act
        Detail detail = ReportMapper.toDetail(dto);

        // Assert
        assertNull(detail.leaveType());
    }

    @Test
    void toDetailDto_DetailからDetailDtoに正しく変換される() {
        // Arrange
        LocalDate workDate = LocalDate.of(2024, 1, 15);
        WorkTime startTime = new WorkTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        WorkTime endTime = new WorkTime(LocalDateTime.of(2024, 1, 15, 18, 0));
        
        Detail detail = new Detail(
            workDate,
            false,
            LeaveType.PAID_LEAVE,
            startTime,
            endTime,
            Duration.ofHours(8),
            Duration.ofHours(1),
            Duration.ZERO,
            "テスト"
        );

        // Act
        DetailDto dto = ReportMapper.toDetailDto(detail);

        // Assert
        assertEquals(workDate, dto.workDate());
        assertFalse(dto.isHoliday());
        assertEquals("PAID_LEAVE", dto.leaveType());
        assertEquals(Duration.ofHours(8), dto.workingHours());
        assertEquals(Duration.ofHours(1), dto.overtimeHours());
        assertEquals("テスト", dto.note());
    }

    @Test
    void toSummaryDto_SummaryからSummaryDtoに正しく変換される() {
        // Arrange
        Summary summary = new Summary(
            20.0,
            2.0,
            1.0,
            0.0,
            Duration.ofHours(160),
            Duration.ofHours(10),
            Duration.ZERO
        );

        // Act
        SummaryDto dto = ReportMapper.toSummaryDto(summary);

        // Assert
        assertEquals(20.0, dto.workDays());
        assertEquals(2.0, dto.paidLeaveDays());
        assertEquals(1.0, dto.compensatoryLeaveDays());
        assertEquals(0.0, dto.specialLeaveDays());
        assertEquals(Duration.ofHours(160), dto.totalWorkTime());
        assertEquals(Duration.ofHours(10), dto.totalOvertime());
        assertEquals(Duration.ZERO, dto.totalHolidayWork());
    }

    @Test
    void toReportResponse_ReportからReportResponseに正しく変換される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        User user = new User("user001");
        
        Detail detail = new Detail(
            LocalDate.of(2024, 1, 15),
            false,
            null,
            new WorkTime(LocalDateTime.of(2024, 1, 15, 9, 0)),
            new WorkTime(LocalDateTime.of(2024, 1, 15, 18, 0)),
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO,
            "通常勤務"
        );
        
        Summary summary = new Summary(
            1.0, 0.0, 0.0, 0.0,
            Duration.ofHours(8), Duration.ZERO, Duration.ZERO
        );
        
        Report report = new Report(
            yearMonth,
            user,
            ReportStatus.NOT_SUBMITTED,
            List.of(detail),
            summary
        );

        // Act
        ReportResponse response = ReportMapper.toReportResponse(report);

        // Assert
        assertEquals(yearMonth, response.yearMonth());
        assertEquals("user001", response.owner().userId());
        assertEquals("NOT_SUBMITTED", response.status());
        assertEquals(1, response.workDays().size());
        assertEquals(1.0, response.summary().workDays());
    }
}