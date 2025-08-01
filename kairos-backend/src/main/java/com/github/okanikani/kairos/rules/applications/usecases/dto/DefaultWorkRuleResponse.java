package com.github.okanikani.kairos.rules.applications.usecases.dto;

import java.time.LocalTime;

/**
 * デフォルト勤怠ルールレスポンスDTO
 */
public record DefaultWorkRuleResponse(
        Long id,                          // 一意識別子
        Long workPlaceId,                 // 勤怠先ID
        double latitude,                  // 勤怠先の緯度
        double longitude,                 // 勤怠先の経度
        UserDto user,                     // ユーザー情報
        LocalTime standardStartTime,      // 規定勤怠開始時刻
        LocalTime standardEndTime,        // 規定勤怠終了時刻
        LocalTime breakStartTime,         // 規定休憩開始時刻
        LocalTime breakEndTime            // 規定休憩終了時刻
) {
}