package com.github.okanikani.kairos.reports.applications.usecases.dto;

import java.time.LocalDate;
import java.time.Duration;

/**
 * 勤務日詳細情報DTO
 * @param workDate 勤務日付
 * @param isHoliday 休日フラグ
 * @param leaveType 休暇区分（文字列表現）
 * @param startDateTime 勤務開始日時
 * @param endDateTime 勤務終了日時
 * @param workingHours 就業時間
 * @param overtimeHours 残業時間
 * @param holidayWorkHours 休出時間
 * @param note 特記事項
 */
public record DetailDto(
        LocalDate workDate,
        boolean isHoliday,
        String leaveType,
        WorkTimeDto startDateTime,
        WorkTimeDto endDateTime,
        Duration workingHours,
        Duration overtimeHours,
        Duration holidayWorkHours,
        String note
) {
    public DetailDto {
        if (workDate == null) {
            throw new IllegalArgumentException("勤務日付は必須です");
        }
        
        // null安全のための補正
        workingHours = workingHours != null ? workingHours : Duration.ZERO;
        overtimeHours = overtimeHours != null ? overtimeHours : Duration.ZERO;
        holidayWorkHours = holidayWorkHours != null ? holidayWorkHours : Duration.ZERO;
    }
}