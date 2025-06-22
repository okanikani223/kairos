package com.github.okanikani.kairos.reports.domains.models.constants;

import java.util.Arrays;
import java.util.List;

/**
 * 休暇区分を表わす定数
 */
public enum LeaveType {
    PAID_LEAVE(1.0),             // 有給（1日）
    PAID_LEAVE_AM(0.5),          // 午前有給（0.5日）
    PAID_LEAVE_PM(0.5),          // 午後有給（0.5日）
    COMPENSATORY_LEAVE(1.0),     // 代休（1日）
    COMPENSATORY_LEAVE_AM(0.5),  // 午前代休（0.5日）
    COMPENSATORY_LEAVE_PM(0.5),  // 午後代休（0.5日）
    SPECIAL_LEAVE(1.0);          // 特別休暇（1日）

    public static final List<LeaveType> PAID_LEAVES = Arrays.asList(PAID_LEAVE, PAID_LEAVE_AM, PAID_LEAVE_PM);

    public static final List<LeaveType> COMPENSATORY_LEAVES = Arrays.asList(COMPENSATORY_LEAVE, COMPENSATORY_LEAVE_AM, COMPENSATORY_LEAVE_PM);

    private final double days;

    LeaveType(double days) {
        this.days = days;
    }

    public double getDays() {
        return days;
    }
}
