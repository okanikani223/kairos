package com.github.okanikani223.kairos.report.domains.models.vos;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Range;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Value
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkRegulations {
    OffsetTime regulatedWorkStartTime;
    OffsetTime regulatedWorkEndTime;
    Double regulatedWorkingSeconds;
    OffsetTime regulatedRestStartTime;
    OffsetTime regulatedRestEndTime;
    Double regulatedRestTime;

    public static WorkRegulationsBuilder builder() {
        return new WorkRegulationsBuilder();
    }

    public boolean hasDoneRest(OffsetDateTime workStartDateTime, OffsetDateTime workEndDateTime) {
        var workStartTime = workStartDateTime.toOffsetTime();
        var workEndTime = workEndDateTime.toOffsetTime();
        var restTimeRange = Range.of(regulatedRestStartTime, regulatedRestEndTime);

        return !restTimeRange.contains(workStartTime) && !restTimeRange.contains(workEndTime);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class WorkRegulationsBuilder {
        private OffsetTime regulatedWorkStartTime;
        private OffsetTime regulatedWorkEndTime;
        private OffsetTime regulatedRestStartTime;
        private OffsetTime regulatedRestEndTime;

        public WorkRegulationsBuilder regulatedWorkStartTime(OffsetTime regulatedWorkStartTime) {
            this.regulatedWorkStartTime = regulatedWorkStartTime;
            return this;
        }

        public WorkRegulationsBuilder regulatedWorkEndTime(OffsetTime regulatedWorkEndTime) {
            this.regulatedWorkEndTime = regulatedWorkEndTime;
            return this;
        }

        public WorkRegulationsBuilder regulatedRestStartTime(OffsetTime regulatedRestStartTime) {
            this.regulatedRestStartTime = regulatedRestStartTime;
            return this;
        }

        public WorkRegulationsBuilder regulatedRestEndTime(OffsetTime regulatedRestEndTime) {
            this.regulatedRestEndTime = regulatedRestEndTime;
            return this;
        }

        public WorkRegulations build() {
            if (Objects.isNull(regulatedWorkStartTime)) throw new IllegalStateException("Regulated work start time are required.");
            if (Objects.isNull(regulatedWorkEndTime)) throw new IllegalStateException("Regulated work end time are required.");
            if (Objects.isNull(regulatedRestStartTime)) throw new IllegalStateException("Regulated rest start time are required.");
            if (Objects.isNull(regulatedRestEndTime)) throw new IllegalStateException("Regulated rest end time are required.");

            var regulatedWorkingSeconds = calcRegulatedWorkingSeconds(regulatedWorkStartTime, regulatedWorkEndTime);
            var regulatedRestTime = calcRegulatedRestTime(regulatedRestStartTime, regulatedRestEndTime);

            return new WorkRegulations(
                    regulatedWorkStartTime,
                    regulatedWorkEndTime,
                    regulatedWorkingSeconds - regulatedRestTime,
                    regulatedRestStartTime,
                    regulatedRestEndTime,
                    regulatedRestTime
            );
        }

        private Double calcRegulatedRestTime(OffsetTime regulatedRestStartTime, OffsetTime regulatedRestEndTime) {
            return (double) ChronoUnit.SECONDS.between(regulatedRestStartTime, regulatedRestEndTime);
        }

        private Double calcRegulatedWorkingSeconds(OffsetTime regulatedWorkStartTime, OffsetTime regulatedWorkEndTime) {
            return (double) ChronoUnit.SECONDS.between(regulatedWorkStartTime, regulatedWorkEndTime);
        }
    }
}
