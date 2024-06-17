package com.github.okanikani223.kairos.report.domains.models.vos;

import com.github.okanikani223.kairos.report.domains.models.constants.LeaveCategories;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkedRecord {
    LocalDate workedDate;
    boolean holiday;
    LeaveCategories leaveCategory;
    OffsetDateTime workStartDateTime;
    OffsetDateTime workEndDateTime;
    double workedSeconds;
    double overSeconds;
    String memo;

    public static WorkedRecordBuilder builder() {
        return new WorkedRecordBuilder();
    }

    public boolean isLeave() {
        return LeaveCategories.LEAVES.contains(leaveCategory);
    }

    public boolean isCompensation() {
        return LeaveCategories.COMPENSATIONS.contains(leaveCategory);
    }

    public boolean isSpecial() {
        return Objects.equals(LeaveCategories.SPECIAL, leaveCategory);
    }

    public static class WorkedRecordBuilder {
        private WorkRegulations workRegulations;
        private LocalDate workedDate;
        private boolean holiday;
        private LeaveCategories leaveCategory;
        private OffsetDateTime workStartDateTime;
        private OffsetDateTime workEndDateTime;
        private Double workedSeconds;
        private Double overSeconds;
        private String memo;

        public WorkedRecordBuilder workRegulations(WorkRegulations workRegulations) {
            this.workRegulations = workRegulations;
            return this;
        }

        public WorkedRecordBuilder workedDate(LocalDate workedDate) {
            this.workedDate = workedDate;
            return this;
        }

        public WorkedRecordBuilder holiday() {
            this.holiday = true;
            return this;
        }

        public WorkedRecordBuilder weekday() {
            this.holiday = false;
            return this;
        }

        public WorkedRecordBuilder leaveCategory(LeaveCategories leaveCategory) {
            this.leaveCategory = leaveCategory;
            return this;
        }

        public WorkedRecordBuilder workStartDateTime(OffsetDateTime workStartDateTime) {
            this.workStartDateTime = workStartDateTime;
            return this;
        }

        public WorkedRecordBuilder workEndDateTime(OffsetDateTime workEndDateTime) {
            this.workEndDateTime = workEndDateTime;
            return this;
        }

        public WorkedRecordBuilder workedSeconds(Double workedSeconds) {
            this.workedSeconds = workedSeconds;
            return this;
        }

        public WorkedRecordBuilder overSeconds(Double overSeconds) {
            this.overSeconds = overSeconds;
            return this;
        }

        public WorkedRecordBuilder memo(String memo) {
            this.memo = memo;
            return this;
        }

        public WorkedRecord build() {
            if (Objects.isNull(this.workStartDateTime)) throw new IllegalStateException("Work start time is required.");
            if (Objects.isNull(this.workEndDateTime)) throw new IllegalStateException("Work end time is required.");

            var workedDate = Objects.isNull(this.workedDate) ? workStartDateTime.toLocalDate() : this.workedDate;
            var workedSeconds = Objects.isNull(this.workedSeconds) ? calcWorkedSeconds(workStartDateTime, workEndDateTime) : this.workedSeconds;
            var overSeconds = Objects.isNull(this.overSeconds) ? calcOverSeconds(workedSeconds) : this.overSeconds;
            var leaveCategory = Objects.isNull(this.leaveCategory) ? LeaveCategories.NONE : this.leaveCategory;

            return new WorkedRecord(
                    workedDate,
                    holiday,
                    leaveCategory,
                    workStartDateTime,
                    workEndDateTime,
                    workedSeconds,
                    overSeconds,
                    memo
            );
        }

        private Double calcOverSeconds(Double workedSeconds) {
            if (Objects.isNull(workRegulations)) throw new IllegalStateException("No work rules have been set.");
            var overSeconds = workedSeconds - workRegulations.regulatedWorkingSeconds();
            return Math.max(overSeconds, 0.0);
        }

        private Double calcWorkedSeconds(OffsetDateTime workStartDateTime, OffsetDateTime workEndDateTime) {
            if (Objects.isNull(workRegulations)) throw new IllegalStateException("No work rules have been set.");
            if (workStartDateTime.isEqual(workEndDateTime)) return 0.0;
            if (!workRegulations.hasDoneRest(workStartDateTime, workEndDateTime)) return (double) ChronoUnit.SECONDS.between(workStartDateTime, workEndDateTime);

            return (ChronoUnit.SECONDS.between(workStartDateTime, workEndDateTime) - workRegulations.regulatedRestTime());
        }
    }
}
