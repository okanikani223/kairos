package com.github.okanikani223.kairos.report.domains.models.vos;

import com.github.okanikani223.kairos.report.domains.models.constants.LeaveCategories;
import com.github.okanikani223.kairos.report.domains.models.constants.ReportStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkedHeaderTest {

    private static final WorkRegulations WORK_REGULATIONS = WorkRegulations.builder()
            .regulatedWorkStartTime(OffsetTime.of(9, 0, 0, 0, ZoneOffset.ofHours(9)))
            .regulatedWorkEndTime(OffsetTime.of(17, 45, 0, 0, ZoneOffset.ofHours(9)))
            .regulatedRestStartTime(OffsetTime.of(12, 0, 0, 0, ZoneOffset.ofHours(9)))
            .regulatedRestEndTime(OffsetTime.of(13, 0, 0, 0, ZoneOffset.ofHours(9)))
            .build();

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
    void build_ParameterSetWithoutWorkRecords_CreateNewInstance() {
        var header = WorkedHeader.builder()
                .yearMonth(YearMonth.of(2024, 2))
                .ownerId("ownerId")
                .status(ReportStatus.SUBMITTED)
                .build();

        assertNotNull(header);
        assertEquals(YearMonth.of(2024, 2), header.yearMonth());
        assertEquals("ownerId", header.ownerId());
        assertEquals(ReportStatus.SUBMITTED, header.status());
    }
}