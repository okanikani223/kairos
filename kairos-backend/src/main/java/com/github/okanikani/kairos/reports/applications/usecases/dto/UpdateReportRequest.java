package com.github.okanikani.kairos.reports.applications.usecases.dto;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public record UpdateReportRequest(
        YearMonth yearMonth,
        UserDto user,
        List<DetailDto> workDays
) {
    public UpdateReportRequest {
        Objects.requireNonNull(yearMonth, "yearMonthは必須です");
        Objects.requireNonNull(user, "userは必須です");
        Objects.requireNonNull(workDays, "workDaysは必須です");
    }
}