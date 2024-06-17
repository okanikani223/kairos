package com.github.okanikani223.kairos.report.domains.models.vos;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class WorkRegulationsTest {

    @Test
    void hasDoneRest_WeekDayWork_ReturnTrue() {
        var regulations = new WorkRegulations(
                OffsetTime.of(9, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetTime.of(17, 45, 0, 0, ZoneOffset.ofHours(9)),
                7.5 * 60 * 60,
                OffsetTime.of(12, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetTime.of(13, 0, 0, 0, ZoneOffset.ofHours(9)),
                1.0 * 60 * 60
        );

        assertTrue(regulations.hasDoneRest(
                OffsetDateTime.of(2024, 2, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetDateTime.of(2024, 2, 1, 17, 45, 0, 0, ZoneOffset.ofHours(9))
        ));
    }

    @Test
    void hasDoneRest_AMOff_ReturnFalse() {
        var regulations = new WorkRegulations(
                OffsetTime.of(9, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetTime.of(17, 45, 0, 0, ZoneOffset.ofHours(9)),
                7.5 * 60 * 60,
                OffsetTime.of(12, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetTime.of(13, 0, 0, 0, ZoneOffset.ofHours(9)),
                1.0 * 60 * 60
        );

        assertFalse(regulations.hasDoneRest(
                OffsetDateTime.of(2024, 2, 1, 13, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetDateTime.of(2024, 2, 1, 17, 45, 0, 0, ZoneOffset.ofHours(9))
        ));
    }

    @Test
    void hasDoneRest_PMOff_ReturnFalse() {
        var regulations = new WorkRegulations(
                OffsetTime.of(9, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetTime.of(17, 45, 0, 0, ZoneOffset.ofHours(9)),
                7.5 * 60 * 60,
                OffsetTime.of(12, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetTime.of(13, 0, 0, 0, ZoneOffset.ofHours(9)),
                1.0 * 60 * 60
        );

        assertFalse(regulations.hasDoneRest(
                OffsetDateTime.of(2024, 2, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetDateTime.of(2024, 2, 1, 12, 0, 0, 0, ZoneOffset.ofHours(9))
        ));
    }
}