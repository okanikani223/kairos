package com.github.okanikani.kairos.reports.applications.usecases.dto;

import java.time.YearMonth;
import java.util.Objects;

/**
 * 勤怠表取得リクエスト
 */
public record FindReportRequest(
    YearMonth yearMonth,
    UserDto user
) {
    public FindReportRequest {
        Objects.requireNonNull(yearMonth, "yearMonthは必須です");
        Objects.requireNonNull(user, "userは必須です");
    }
}