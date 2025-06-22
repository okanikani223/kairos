package com.github.okanikani.kairos.reports.applications.usecases.dto;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * 勤怠表登録リクエストDTO
 * @param yearMonth 勤怠年月
 * @param user 所有者
 * @param workDays 勤務日情報一覧
 */
public record RegisterReportRequest(
        YearMonth yearMonth,
        UserDto user,
        List<DetailDto> workDays
) {
    public RegisterReportRequest {
        Objects.requireNonNull(yearMonth, "yearMonthは必須です");
        Objects.requireNonNull(user, "userは必須です");
        Objects.requireNonNull(workDays, "workDaysは必須です");
    }
}