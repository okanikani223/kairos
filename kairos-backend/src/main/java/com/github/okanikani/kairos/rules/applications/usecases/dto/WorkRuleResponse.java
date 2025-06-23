package com.github.okanikani.kairos.rules.applications.usecases.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record WorkRuleResponse(
        Long id,
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
) {
}