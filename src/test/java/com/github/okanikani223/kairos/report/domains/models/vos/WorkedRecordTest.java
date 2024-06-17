package com.github.okanikani223.kairos.report.domains.models.vos;

import com.github.okanikani223.kairos.report.domains.models.constants.LeaveCategories;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class WorkedRecordTest {
    private static final WorkRegulations WORK_REGULATIONS = new WorkRegulations(
            OffsetTime.of(9, 0, 0, 0, ZoneOffset.ofHours(9)),
            OffsetTime.of(17, 45, 0, 0, ZoneOffset.ofHours(9)),
            7.5 * 60 * 60,
            OffsetTime.of(12, 0, 0, 0, ZoneOffset.ofHours(9)),
            OffsetTime.of(13, 0, 0, 0, ZoneOffset.ofHours(9)),
            1.0 * 60 * 60
    );
    @Test
    void build_AllParameterSet_CreateNewInstance() {
        var record = WorkedRecord.builder()
                .workedDate(LocalDate.of(2024, 2, 1))
                .weekday()
                .leaveCategory(LeaveCategories.LEAVE_PM)
                .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)))
                .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(17, 30), ZoneOffset.ofHours(9)))
                .workedSeconds(7.5 * 60 * 60)
                .overSeconds(0.0)
                .memo("test")
                .build();

        assertNotNull(record);
        assertEquals(LocalDate.of(2024, 2, 1), record.getWorkedDate());
        assertFalse(record.isHoliday());
        assertEquals(LeaveCategories.LEAVE_PM, record.getLeaveCategory());
        assertEquals(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)), record.getWorkStartDateTime());
        assertEquals(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(17, 30), ZoneOffset.ofHours(9)), record.getWorkEndDateTime());
        assertEquals(7.5 * 60 * 60, record.getWorkedSeconds());
        assertEquals(0.0, record.getOverSeconds());
        assertEquals("test", record.getMemo());
    }

    @Test
    void build_RequiredParamSet_CreateNewInstance() {
        var record = WorkedRecord.builder()
                .workRegulations(WORK_REGULATIONS)
                .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)))
                .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                .build();

        assertNotNull(record);
        assertEquals(LocalDate.of(2024, 2, 1), record.getWorkedDate());
        assertFalse(record.isHoliday());
        assertEquals(LeaveCategories.NONE, record.getLeaveCategory());
        assertEquals(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)), record.getWorkStartDateTime());
        assertEquals(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)), record.getWorkEndDateTime());
        assertEquals(8.0 * 60 * 60, record.getWorkedSeconds());
        assertEquals(0.5 * 60 * 60, record.getOverSeconds());
        assertNull(record.getMemo());
    }

    @Test
    void build_NotSetWorkStartDateTime_ThrownIllegalStateException() {
        var actual = assertThrows(IllegalStateException.class,
                () -> WorkedRecord.builder()
                        .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                        .build()
        );

        assertEquals("Work start time is required.", actual.getMessage());
    }

    @Test
    void build_NotSetWorkEndDateTime_ThrownIllegalStateException() {
        var actual = assertThrows(IllegalStateException.class,
                () -> WorkedRecord.builder()
                        .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                        .build()
        );

        assertEquals("Work end time is required.", actual.getMessage());
    }

    @Test
    void build_CalculateWorkRecordNotSetWorkRegulations_ThrownIllegalStateException() {
        var actual = assertThrows(IllegalStateException.class,
                () -> WorkedRecord.builder()
                        .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                        .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                        .build()
        );

        assertEquals("No work rules have been set.", actual.getMessage());
    }

    @Test
    void isLeave_LeaveIsNotSet_ReturnFalse() {
        var record = WorkedRecord.builder()
                .workRegulations(WORK_REGULATIONS)
                .leaveCategory(LeaveCategories.NONE)
                .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)))
                .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                .build();

        assertFalse(record.isLeave());
    }

    @Test
    void isLeave_LeaveIsSet_ReturnTrue() {
        var record = WorkedRecord.builder()
                .workRegulations(WORK_REGULATIONS)
                .leaveCategory(LeaveCategories.LEAVE)
                .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)))
                .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                .build();

        assertTrue(record.isLeave());

        record = WorkedRecord.builder()
                .workRegulations(WORK_REGULATIONS)
                .leaveCategory(LeaveCategories.LEAVE_AM)
                .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)))
                .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                .build();

        assertTrue(record.isLeave());

        record = WorkedRecord.builder()
                .workRegulations(WORK_REGULATIONS)
                .leaveCategory(LeaveCategories.LEAVE_PM)
                .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)))
                .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                .build();

        assertTrue(record.isLeave());
    }

    @Test
    void isCompensation_CompensationIsNotSet_ReturnFalse() {
        var record = WorkedRecord.builder()
                .workRegulations(WORK_REGULATIONS)
                .leaveCategory(LeaveCategories.NONE)
                .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)))
                .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                .build();

        assertFalse(record.isLeave());
    }

    @Test
    void isCompensation_CompensationIsSet_ReturnTrue() {
        var record = WorkedRecord.builder()
                .workRegulations(WORK_REGULATIONS)
                .leaveCategory(LeaveCategories.COMPENSATION)
                .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)))
                .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                .build();

        assertTrue(record.isCompensation());

        record = WorkedRecord.builder()
                .workRegulations(WORK_REGULATIONS)
                .leaveCategory(LeaveCategories.COMPENSATION_AM)
                .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)))
                .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                .build();

        assertTrue(record.isCompensation());

        record = WorkedRecord.builder()
                .workRegulations(WORK_REGULATIONS)
                .leaveCategory(LeaveCategories.COMPENSATION_PM)
                .workStartDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(9, 0), ZoneOffset.ofHours(9)))
                .workEndDateTime(OffsetDateTime.of(LocalDate.of(2024, 2, 1), LocalTime.of(18, 0), ZoneOffset.ofHours(9)))
                .build();

        assertTrue(record.isCompensation());
    }
}