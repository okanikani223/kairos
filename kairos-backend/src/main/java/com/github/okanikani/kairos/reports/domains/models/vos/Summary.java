package com.github.okanikani223.kairos.reports.domains.models.vos;

import java.time.Duration;

/**
 * 勤怠の集計情報を表わすクラス
 * @param workDays 就業日数
 * @param paidLeaveDays 有給日数（午前/午後有給は0.5日として加算）
 * @param compensatoryLeaveDays 代休日数（午前/午後代休は0.5日として加算）
 * @param specialLeaveDays 特休日数
 * @param totalWorkTime 総就業時間
 * @param totalOvertime 総残業時間
 * @param totalHolidayWork 総休出時間
 */
public record Summary(
        double workDays,
        double paidLeaveDays,
        double compensatoryLeaveDays,
        double specialLeaveDays,
        Duration totalWorkTime,
        Duration totalOvertime,
        Duration totalHolidayWork
) {
    public static final Summary EMPTY = new Summary(0, 0, 0, 0, Duration.ZERO, Duration.ZERO, Duration.ZERO);
}

