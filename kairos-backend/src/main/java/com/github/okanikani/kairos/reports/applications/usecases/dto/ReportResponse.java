package com.github.okanikani.kairos.reports.applications.usecases.dto;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * 勤怠表レスポンスDTO
 * @param yearMonth 勤怠年月
 * @param owner 所有者
 * @param status ステータス（文字列表現）
 * @param workDays 勤務日情報一覧
 * @param summary サマリ情報
 */
public record ReportResponse(
        YearMonth yearMonth,
        UserDto owner,
        String status,
        List<DetailDto> workDays,
        SummaryDto summary
) {
    public ReportResponse {
        Objects.requireNonNull(yearMonth, "yearMonthは必須です");
        Objects.requireNonNull(owner, "ownerは必須です");
        Objects.requireNonNull(status, "statusは必須です");
        Objects.requireNonNull(workDays, "workDaysは必須です");
        Objects.requireNonNull(summary, "summaryは必須です");
    }
}