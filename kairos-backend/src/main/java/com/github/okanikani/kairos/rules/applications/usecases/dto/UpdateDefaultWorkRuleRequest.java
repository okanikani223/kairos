package com.github.okanikani.kairos.rules.applications.usecases.dto;

import java.time.LocalTime;

/**
 * デフォルト勤務ルール更新リクエストDTO
 */
public record UpdateDefaultWorkRuleRequest(
        Long workPlaceId,
        double latitude,
        double longitude,
        LocalTime standardStartTime,
        LocalTime standardEndTime,
        LocalTime breakStartTime,
        LocalTime breakEndTime
) {
    public UpdateDefaultWorkRuleRequest {
        java.util.Objects.requireNonNull(workPlaceId, "workPlaceIdは必須です");
        java.util.Objects.requireNonNull(standardStartTime, "standardStartTimeは必須です");
        java.util.Objects.requireNonNull(standardEndTime, "standardEndTimeは必須です");
    }
}