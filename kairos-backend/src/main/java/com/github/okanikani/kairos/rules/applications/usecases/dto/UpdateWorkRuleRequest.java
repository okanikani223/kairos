package com.github.okanikani.kairos.rules.applications.usecases.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 勤務ルール更新リクエスト
 */
public record UpdateWorkRuleRequest(
    Long workPlaceId,
    double latitude,
    double longitude,
    UserDto user,
    LocalTime standardStartTime,
    LocalTime standardEndTime,
    LocalTime breakStartTime,
    LocalTime breakEndTime,
    LocalDate membershipStartDate,
    LocalDate membershipEndDate
) {}