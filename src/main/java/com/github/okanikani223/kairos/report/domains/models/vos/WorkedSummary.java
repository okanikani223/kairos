package com.github.okanikani223.kairos.report.domains.models.vos;

import com.github.okanikani223.kairos.report.domains.models.constants.LeaveCategories;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Objects;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkedSummary {
    Double totalWorkedDays;
    Double totalLeaveDays;
    Double totalCompensationDays;
    Double totalSpecialLeaveDays;
    Double totalWorkedSeconds;
    Double totalOverSeconds;
    Double totalWorkedOnDayOffSeconds;

    public static WorkedSummaryBuilder builder() {
        return new WorkedSummaryBuilder();
    }

    public static class WorkedSummaryBuilder {
        private Double totalWorkedDays;
        private Double totalLeaveDays;
        private Double totalCompensationDays;
        private Double totalSpecialLeaveDays;
        private Double totalWorkedSeconds;
        private Double totalOverSeconds;
        private Double totalWorkedOnDayOffSeconds;
        private List<WorkedRecord> workedRecords;

        public WorkedSummaryBuilder totalWorkedDays(Double totalWorkedDays) {
            this.totalWorkedDays = totalWorkedDays;
            return this;
        }

        public WorkedSummaryBuilder totalLeaveDays(Double totalLeaveDays) {
            this.totalLeaveDays = totalLeaveDays;
            return this;
        }

        public WorkedSummaryBuilder totalCompensationDays(Double totalCompensationDays) {
            this.totalCompensationDays = totalCompensationDays;
            return this;
        }

        public WorkedSummaryBuilder totalSpecialLeaveDays(Double totalSpecialLeaveDays) {
            this.totalSpecialLeaveDays = totalSpecialLeaveDays;
            return this;
        }

        public WorkedSummaryBuilder totalWorkedSeconds(Double totalWorkedSeconds) {
            this.totalWorkedSeconds = totalWorkedSeconds;
            return this;
        }

        public WorkedSummaryBuilder totalOverSeconds(Double totalOverSeconds) {
            this.totalOverSeconds = totalOverSeconds;
            return this;
        }

        public WorkedSummaryBuilder totalWorkedOnDayOffSeconds(Double totalWorkedOnDayOffSeconds) {
            this.totalWorkedOnDayOffSeconds = totalWorkedOnDayOffSeconds;
            return this;
        }

        public WorkedSummaryBuilder workedRecords(List<WorkedRecord> workedRecords) {
            this.workedRecords = workedRecords;
            return this;
        }

        public WorkedSummary build() {

            var totalWorkedDays = Objects.isNull(this.totalWorkedDays) ? calcTotalWorkedDays(workedRecords) : this.totalWorkedDays;
            var totalLeaveDays = Objects.isNull(this.totalLeaveDays) ? calcTotalLeaveDays(workedRecords) : this.totalLeaveDays;
            var totalCompensationDays = Objects.isNull(this.totalCompensationDays) ? calcTotalCompensationDays(workedRecords) : this.totalCompensationDays;
            var totalSpecialLeaveDays = Objects.isNull(this.totalSpecialLeaveDays) ? calcTotalSpecialLeaveDays(workedRecords) : this.totalSpecialLeaveDays;
            var totalWorkedSeconds = Objects.isNull(this.totalWorkedSeconds) ? calcTotalWorkedSeconds(workedRecords) : this.totalWorkedSeconds;
            var totalOverSeconds = Objects.isNull(this.totalOverSeconds) ? calcTotalOverSeconds(workedRecords) : this.totalOverSeconds;
            var totalWorkedOnDayOffSeconds = Objects.isNull(this.totalWorkedOnDayOffSeconds) ? calcTotalWorkedOnDayOffSeconds(workedRecords) : this.totalWorkedOnDayOffSeconds;

            return new WorkedSummary(
                    totalWorkedDays,
                    totalLeaveDays,
                    totalCompensationDays,
                    totalSpecialLeaveDays,
                    totalWorkedSeconds,
                    totalOverSeconds,
                    totalWorkedOnDayOffSeconds
            );
        }

        private Double calcTotalWorkedSeconds(List<WorkedRecord> workedRecords) {
            if (Objects.isNull(workedRecords) || workedRecords.isEmpty()) return 0.0;

            return workedRecords.stream()
                    .filter(record -> !record.isHoliday())
                    .mapToDouble(WorkedRecord::getWorkedSeconds)
                    .sum();
        }

        private Double calcTotalWorkedOnDayOffSeconds(List<WorkedRecord> workedRecords) {
            if (Objects.isNull(workedRecords) || workedRecords.isEmpty()) return 0.0;

            return workedRecords.stream()
                    .filter(WorkedRecord::isHoliday)
                    .mapToDouble(WorkedRecord::getWorkedSeconds)
                    .sum();
        }

        private Double calcTotalOverSeconds(List<WorkedRecord> workedRecords) {
            if (Objects.isNull(workedRecords) || workedRecords.isEmpty()) return 0.0;

            return workedRecords.stream()
                    .mapToDouble(WorkedRecord::getOverSeconds)
                    .sum();
        }

        private Double calcTotalSpecialLeaveDays(List<WorkedRecord> workedRecords) {
            if (Objects.isNull(workedRecords) || workedRecords.isEmpty()) return 0.0;

            return workedRecords.stream()
                    .filter(WorkedRecord::isSpecial)
                    .map(WorkedRecord::getLeaveCategory)
                    .mapToDouble(LeaveCategories::getConsumedDays)
                    .sum();
        }

        private Double calcTotalCompensationDays(List<WorkedRecord> workedRecords) {
            if (Objects.isNull(workedRecords) || workedRecords.isEmpty()) return 0.0;

            return workedRecords.stream()
                    .filter(WorkedRecord::isCompensation)
                    .map(WorkedRecord::getLeaveCategory)
                    .mapToDouble(LeaveCategories::getConsumedDays)
                    .sum();
        }

        private Double calcTotalLeaveDays(List<WorkedRecord> workedRecords) {
            if (Objects.isNull(workedRecords) || workedRecords.isEmpty()) return 0.0;

            return workedRecords.stream()
                    .filter(WorkedRecord::isLeave)
                    .map(WorkedRecord::getLeaveCategory)
                    .mapToDouble(LeaveCategories::getConsumedDays)
                    .sum();
        }

        private Double calcTotalWorkedDays(List<WorkedRecord> workedRecords) {
            if (Objects.isNull(workedRecords) || workedRecords.isEmpty()) return 0.0;
            var totalDays = workedRecords.size();
            var holidayCount = workedRecords.stream().filter(WorkedRecord::isHoliday).count();
            var leaveCount = workedRecords.stream()
                    .map(WorkedRecord::getLeaveCategory)
                    .mapToDouble(LeaveCategories::getConsumedDays)
                    .sum();

            return totalDays - holidayCount - leaveCount;
        }
    }
}
