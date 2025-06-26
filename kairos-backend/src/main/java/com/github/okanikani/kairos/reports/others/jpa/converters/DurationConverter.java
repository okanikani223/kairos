package com.github.okanikani.kairos.reports.others.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Duration;

/**
 * Duration型とLong型（分単位）の相互変換を行うJPAコンバーター
 * 
 * 業務要件: 勤務時間・残業時間・休出時間を分単位でデータベースに保存
 */
@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Long> {

    @Override
    public Long convertToDatabaseColumn(Duration duration) {
        return duration != null ? duration.toMinutes() : 0L;
    }

    @Override
    public Duration convertToEntityAttribute(Long minutes) {
        return minutes != null ? Duration.ofMinutes(minutes) : Duration.ZERO;
    }
}