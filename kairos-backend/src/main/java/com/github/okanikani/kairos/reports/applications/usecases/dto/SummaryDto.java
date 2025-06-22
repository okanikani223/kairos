package com.github.okanikani.kairos.reports.applications.usecases.dto;

import java.time.Duration;

/**
 * 勤怠集計情報DTO
 * @param workDays 就業日数
 * @param paidLeaveDays 有給日数
 * @param compensatoryLeaveDays 代休日数
 * @param specialLeaveDays 特休日数
 * @param totalWorkTime 総就業時間
 * @param totalOvertime 総残業時間
 * @param totalHolidayWork 総休出時間
 */
public record SummaryDto(
        double workDays,
        double paidLeaveDays,
        double compensatoryLeaveDays,
        double specialLeaveDays,
        Duration totalWorkTime,
        Duration totalOvertime,
        Duration totalHolidayWork
) {}