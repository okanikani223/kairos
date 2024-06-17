package com.github.okanikani223.kairos.report.domains.models.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum LeaveCategories {
    LEAVE(1.0),
    LEAVE_AM(0.5),
    LEAVE_PM(0.5),
    COMPENSATION(1.0),
    COMPENSATION_AM(0.5),
    COMPENSATION_PM(0.5),
    SPECIAL(1.0),
    NONE(0.0);

    private final double consumedDays;
    LeaveCategories(double consumedDays) {
        this.consumedDays = consumedDays;
    }

    public static List<LeaveCategories> LEAVES = Arrays.asList(LEAVE, LEAVE_AM, LEAVE_PM);
    public static List<LeaveCategories> COMPENSATIONS = Arrays.asList(COMPENSATION, COMPENSATION_AM, COMPENSATION_PM);
}
