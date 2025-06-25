package com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto;

/**
 * 勤怠作成ルールレスポンスDTO
 */
public record ReportCreationRuleResponse(
        Long id,                              // 一意識別子
        UserDto user,                         // ユーザー情報
        int closingDay,                       // 勤怠締め日（月の日：1-31）
        int timeCalculationUnitMinutes        // 勤怠時間計算単位（分：1-60）
) {
}