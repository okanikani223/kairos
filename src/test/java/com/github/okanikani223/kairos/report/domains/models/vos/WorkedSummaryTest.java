package com.github.okanikani223.kairos.report.domains.models.vos;

import com.github.okanikani223.kairos.report.domains.models.constants.LeaveCategories;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkedSummaryTest {
    private static final WorkRegulations WORK_REGULATIONS = new WorkRegulations(
            OffsetTime.of(9, 0, 0, 0, ZoneOffset.ofHours(9)),
            OffsetTime.of(17, 45, 0, 0, ZoneOffset.ofHours(9)),
            7.75 * 60 * 60,
            OffsetTime.of(12, 0, 0, 0, ZoneOffset.ofHours(9)),
            OffsetTime.of(13, 0, 0, 0, ZoneOffset.ofHours(9)),
            1.0 * 60 * 60
    );
    private static final List<WorkedRecord> defaultWorkedRecords = Arrays.asList(
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 16, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 16, 18, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 17, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 17, 18, 15, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .holiday()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 18, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 18, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .holiday()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 19, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 19, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 20, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 20, 18, 15, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .leaveCategory(LeaveCategories.LEAVE_AM)
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 21, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 21, 12, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 22, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 22, 19, 15, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 23, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 23, 19, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 24, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 24, 18, 15, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .holiday()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 25, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 25, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .holiday()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 26, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 26, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 27, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 27, 17, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 28, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 28, 17, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 29, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 29, 17, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 30, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 30, 19, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 5, 31, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 5, 31, 17, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .holiday()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .holiday()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 2, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 2, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 3, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 3, 19, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 4, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 4, 18, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 5, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 5, 18, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 6, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 6, 18, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 7, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 7, 21, 30, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .holiday()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 8, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 8, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .holiday()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 9, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 9, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 10, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 10, 18, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 11, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 11, 19, 15, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 12, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 12, 18, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 13, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 13, 18, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 14, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 14, 18, 45, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build(),
            WorkedRecord.builder()
                    .holiday()
                    .workStartDateTime(OffsetDateTime.of(2024, 6, 15, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workEndDateTime(OffsetDateTime.of(2024, 6, 15, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                    .workRegulations(WORK_REGULATIONS).build()
    );
    @Test
    void build_ParameterSetWithoutWorkedRecords_CreateNewInstance() {
        var summary = WorkedSummary.builder()
                .totalWorkedDays(21.5)
                .totalLeaveDays(0.5)
                .totalCompensationDays(0.5)
                .totalSpecialLeaveDays(0.5)
                .totalWorkedSeconds(187.25)
                .totalOverSeconds(21.5)
                .totalWorkedOnDayOffSeconds(0.0)
                .build();

        assertNotNull(summary);
        assertEquals(21.5, summary.getTotalWorkedDays());
        assertEquals(0.5, summary.getTotalLeaveDays());
        assertEquals(0.5, summary.getTotalCompensationDays());
        assertEquals(0.5, summary.getTotalSpecialLeaveDays());
        assertEquals(187.25, summary.getTotalWorkedSeconds());
        assertEquals(21.5, summary.getTotalOverSeconds());
        assertEquals(0.0, summary.getTotalWorkedOnDayOffSeconds());
    }

    @Test
    void build_WorkedRecordsSet_CreateNewInstance(){
        var summary = WorkedSummary.builder().workedRecords(defaultWorkedRecords).build();

        assertNotNull(summary);
        assertEquals(21.5, summary.getTotalWorkedDays());
        assertEquals(0.5, summary.getTotalLeaveDays());
        assertEquals(0.0, summary.getTotalCompensationDays());
        assertEquals(0.0, summary.getTotalSpecialLeaveDays());
        assertEquals(187.25 * 60 * 60, summary.getTotalWorkedSeconds());
        assertEquals(21.5 * 60 * 60, summary.getTotalOverSeconds());
        assertEquals(0.0, summary.getTotalWorkedOnDayOffSeconds());
    }

    @Test
    void build_EmptyWorkedRecordsSet_CreateNewInstance(){
        var summary = WorkedSummary.builder().workedRecords(Collections.emptyList()).build();

        assertNotNull(summary);
        assertEquals(0.0, summary.getTotalWorkedDays());
        assertEquals(0.0, summary.getTotalLeaveDays());
        assertEquals(0.0, summary.getTotalCompensationDays());
        assertEquals(0.0, summary.getTotalSpecialLeaveDays());
        assertEquals(0.0, summary.getTotalWorkedSeconds());
        assertEquals(0.0, summary.getTotalOverSeconds());
        assertEquals(0.0, summary.getTotalWorkedOnDayOffSeconds());
    }

    @Test
    void build_NotSetParameter_CreateNewInstance() {
        var summary = WorkedSummary.builder().build();

        assertNotNull(summary);
        assertEquals(0.0, summary.getTotalWorkedDays());
        assertEquals(0.0, summary.getTotalLeaveDays());
        assertEquals(0.0, summary.getTotalCompensationDays());
        assertEquals(0.0, summary.getTotalSpecialLeaveDays());
        assertEquals(0.0, summary.getTotalWorkedSeconds());
        assertEquals(0.0, summary.getTotalOverSeconds());
        assertEquals(0.0, summary.getTotalWorkedOnDayOffSeconds());
    }
}