package com.github.okanikani.kairos.reports.applications.usecases.dto;

import java.time.YearMonth;
import java.util.Objects;

public record GenerateReportFromLocationRequest(
        YearMonth yearMonth,
        UserDto user
) {
    public GenerateReportFromLocationRequest {
        Objects.requireNonNull(yearMonth, "yearMonthは必須です");
        Objects.requireNonNull(user, "userは必須です");
    }
}