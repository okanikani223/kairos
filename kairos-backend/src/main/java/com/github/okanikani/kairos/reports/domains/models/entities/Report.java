package com.github.okanikani223.kairos.reports.domains.models.entities;

import com.github.okanikani223.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani223.kairos.reports.domains.models.vos.Detail;
import com.github.okanikani223.kairos.reports.domains.models.vos.Summary;
import com.github.okanikani223.kairos.reports.domains.models.vos.User;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * 勤怠表を表わすクラス
 * @param yearMonth 勤怠年月
 * @param owner 所有者
 * @param status ステータス
 * @param workDays 勤務日情報一覧
 * @param summary サマリ情報
 */
public record Report(
        YearMonth yearMonth,
        User owner,
        ReportStatus status,
        List<Detail> workDays,
        Summary summary
) {
    public Report {
        Objects.requireNonNull(yearMonth, "yearMonthは必須です");
        Objects.requireNonNull(owner, "ownerは必須です");
        Objects.requireNonNull(status, "statusは必須です");
        Objects.requireNonNull(workDays, "workDaysは必須です");
        Objects.requireNonNull(summary, "summaryは必須です");
    }
}
