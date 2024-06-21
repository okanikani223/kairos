package com.github.okanikani223.kairos.report.domains.models.vos;

import com.github.okanikani223.kairos.report.domains.models.constants.ReportStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Value
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkedHeader {
    YearMonth yearMonth;
    String ownerId;
    ReportStatus status;

    public static WorkedHeaderBuilder builder() {
        return new WorkedHeaderBuilder();
    }

    public static class WorkedHeaderBuilder {
        private YearMonth yearMonth;
        private String ownerId;
        private ReportStatus status;
        private List<WorkedRecord> workedRecords;

        public WorkedHeaderBuilder yearMonth(YearMonth yearMonth) {
            this.yearMonth = yearMonth;
            return this;
        }

        public WorkedHeaderBuilder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public WorkedHeaderBuilder status(ReportStatus status) {
            this.status = status;
            return this;
        }

        public WorkedHeaderBuilder workedRecords(List<WorkedRecord> workedRecords) {
            this.workedRecords = workedRecords;
            return this;
        }

        public WorkedHeader build() {
            if (Objects.isNull(ownerId) || ownerId.isBlank()) throw new IllegalStateException("ownerId is required");

            var yearMonth = Objects.isNull(this.yearMonth) ? computeReportYearMonth(workedRecords) : this.yearMonth;
            var status = Objects.isNull(this.status) ? ReportStatus.CREATED : this.status;

            return new WorkedHeader(yearMonth, ownerId, status);
        }

        private YearMonth computeReportYearMonth(List<WorkedRecord> workedRecords) {
            if (Objects.isNull(workedRecords) || workedRecords.isEmpty()) throw new IllegalStateException("If you want to leave the year and month of the report empty, please set the work record");
            var lastWorkedDate = workedRecords.stream()
                    .max(Comparator.nullsFirst(Comparator.comparing(WorkedRecord::getWorkedDate)))
                    .map(WorkedRecord::getWorkedDate)
                    .orElseThrow();

            return YearMonth.from(lastWorkedDate);
        }
    }
}
