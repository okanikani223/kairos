package com.github.okanikani.kairos.rules.applications.usecases.dto;

import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;

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
    /**
     * DefaultWorkRuleエンティティから DefaultWorkRuleResponseを作成する
     * @param rule デフォルト勤怠ルールエンティティ
     * @return DefaultWorkRuleResponse
     */
    public static DefaultWorkRuleResponse from(DefaultWorkRule rule) {
        return new DefaultWorkRuleResponse(
            rule.id(),
            rule.workPlaceId(),
            rule.latitude(),
            rule.longitude(),
            new UserDto(rule.user().userId()),
            rule.standardStartTime(),
            rule.standardEndTime(),
            rule.breakStartTime(),
            rule.breakEndTime()
        );
    }
}