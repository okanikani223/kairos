package com.github.okanikani.kairos.reports.applications.usecases.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 勤務時刻DTO
 * @param value 日時情報
 */
public record WorkTimeDto(LocalDateTime value) {
    public WorkTimeDto {
        Objects.requireNonNull(value, "勤務時刻は必須です");
    }
}