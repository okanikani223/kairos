package com.github.okanikani.kairos.rules.applications.usecases.dto;

import com.github.okanikani.kairos.reports.applications.usecases.dto.UserDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public record RegisterWorkRuleRequest(
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
    public RegisterWorkRuleRequest {
        Objects.requireNonNull(workPlaceId, "勤怠先IDは必須です");
        Objects.requireNonNull(user, "ユーザーは必須です");
        Objects.requireNonNull(standardStartTime, "規定勤怠開始時刻は必須です");
        Objects.requireNonNull(standardEndTime, "規定勤怠終了時刻は必須です");
        Objects.requireNonNull(membershipStartDate, "所属開始日は必須です");
        Objects.requireNonNull(membershipEndDate, "所属終了日は必須です");
    }
}