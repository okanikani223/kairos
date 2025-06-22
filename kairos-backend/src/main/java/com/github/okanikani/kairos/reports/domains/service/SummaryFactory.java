package com.github.okanikani223.kairos.reports.domains.service;

import com.github.okanikani223.kairos.reports.domains.models.constants.LeaveType;
import com.github.okanikani223.kairos.reports.domains.models.vos.Detail;
import com.github.okanikani223.kairos.reports.domains.models.vos.Summary;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class SummaryFactory {

    public static Summary from(List<Detail> details) {
        if (details == null || details.isEmpty()) {
            return Summary.EMPTY;
        }

        double paidLeave = details.stream()
                .map(Detail::leaveType)
                .filter(LeaveType.PAID_LEAVES::contains)
                .mapToDouble(LeaveType::getDays)
                .sum();

        double compensatoryLeave = details.stream()
                .map(Detail::leaveType)
                .filter(LeaveType.COMPENSATORY_LEAVES::contains)
                .mapToDouble(LeaveType::getDays)
                .sum();

        double specialLeave = details.stream()
                .map(Detail::leaveType)
                .filter(l -> l == LeaveType.SPECIAL_LEAVE)
                .count();

        double workDays = details.stream()
                .filter(d -> d.leaveType() == null || !d.isHoliday())
                .count() - paidLeave - compensatoryLeave - specialLeave;

        Duration totalWork = details.stream()
                .map(Detail::workingHours)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        Duration totalOvertime = details.stream()
                .map(Detail::overtimeHours)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        Duration totalHolidayWork = details.stream()
                .map(Detail::holidayWorkHours)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        return new Summary(
                workDays,
                paidLeave,
                compensatoryLeave,
                specialLeave,
                totalWork,
                totalOvertime,
                totalHolidayWork
        );
    }
}

