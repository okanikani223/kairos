package com.github.okanikani.kairos.reports.applications.usecases.mapper;

import com.github.okanikani.kairos.reports.applications.usecases.dto.*;
import com.github.okanikani.kairos.reports.domains.models.constants.LeaveType;
import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.vos.*;

import java.util.List;

/**
 * ドメインモデルとDTOの変換を担当するマッパー
 */
public class ReportMapper {
    
    /**
     * UserDtoからUserドメインオブジェクトに変換
     */
    public static User toUser(UserDto dto) {
        return new User(dto.userId());
    }
    
    /**
     * UserドメインオブジェクトからUserDtoに変換
     */
    public static UserDto toUserDto(User user) {
        return new UserDto(user.userId());
    }
    
    /**
     * WorkTimeDtoからWorkTimeドメインオブジェクトに変換
     */
    public static WorkTime toWorkTime(WorkTimeDto dto) {
        if (dto == null) return null;
        return new WorkTime(dto.value());
    }
    
    /**
     * WorkTimeドメインオブジェクトからWorkTimeDtoに変換
     */
    public static WorkTimeDto toWorkTimeDto(WorkTime workTime) {
        if (workTime == null) return null;
        return new WorkTimeDto(workTime.value());
    }
    
    /**
     * DetailDtoからDetailドメインオブジェクトに変換
     */
    public static Detail toDetail(DetailDto dto) {
        LeaveType leaveType = dto.leaveType() != null ? 
            LeaveType.valueOf(dto.leaveType()) : null;
            
        return new Detail(
            dto.workDate(),
            dto.isHoliday(),
            leaveType,
            toWorkTime(dto.startDateTime()),
            toWorkTime(dto.endDateTime()),
            dto.workingHours(),
            dto.overtimeHours(),
            dto.holidayWorkHours(),
            dto.note()
        );
    }
    
    /**
     * DetailドメインオブジェクトからDetailDtoに変換
     */
    public static DetailDto toDetailDto(Detail detail) {
        String leaveTypeName = detail.leaveType() != null ? 
            detail.leaveType().name() : null;
            
        return new DetailDto(
            detail.workDate(),
            detail.isHoliday(),
            leaveTypeName,
            toWorkTimeDto(detail.startDateTime()),
            toWorkTimeDto(detail.endDateTime()),
            detail.workingHours(),
            detail.overtimeHours(),
            detail.holidayWorkHours(),
            detail.note()
        );
    }
    
    /**
     * SummaryドメインオブジェクトからSummaryDtoに変換
     */
    public static SummaryDto toSummaryDto(Summary summary) {
        return new SummaryDto(
            summary.workDays(),
            summary.paidLeaveDays(),
            summary.compensatoryLeaveDays(),
            summary.specialLeaveDays(),
            summary.totalWorkTime(),
            summary.totalOvertime(),
            summary.totalHolidayWork()
        );
    }
    
    /**
     * ReportドメインオブジェクトからReportResponseに変換
     */
    public static ReportResponse toReportResponse(Report report) {
        List<DetailDto> workDayDtos = report.workDays().stream()
            .map(ReportMapper::toDetailDto)
            .toList();
            
        return new ReportResponse(
            report.yearMonth(),
            toUserDto(report.owner()),
            report.status().name(),
            workDayDtos,
            toSummaryDto(report.summary())
        );
    }
}