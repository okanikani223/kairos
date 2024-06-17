package com.github.okanikani223.kairos.report.domains.models.vos;

import org.apache.commons.lang3.Range;

import java.time.OffsetDateTime;
import java.time.OffsetTime;

public record WorkRegulations(
        OffsetTime regulatedWorkStartTime,
        OffsetTime regulatedWorkEndTime,
        Double regulatedWorkingSeconds,
        OffsetTime regulatedRestStartTime,
        OffsetTime regulatedRestEndTime,
        Double regulatedRestTime
) {
    public boolean hasDoneRest(OffsetDateTime workStartDateTime, OffsetDateTime workEndDateTime) {
        var workStartTime = workStartDateTime.toOffsetTime();
        var workEndTime = workEndDateTime.toOffsetTime();
        var restTimeRange = Range.of(regulatedRestStartTime, regulatedRestEndTime);

        return !restTimeRange.contains(workStartTime) && !restTimeRange.contains(workEndTime);
    }
}
