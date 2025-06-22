package com.github.okanikani.kairos.reports.domains.service;

import com.github.okanikani.kairos.reports.domains.models.constants.LeaveType;
import com.github.okanikani.kairos.reports.domains.models.vos.Detail;
import com.github.okanikani.kairos.reports.domains.models.vos.Summary;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class SummaryFactory {

    public static Summary from(List<Detail> details) {
        if (details == null || details.isEmpty()) {
            return Summary.EMPTY;
        }

        // 有給休暇日数計算: 午前/午後有給は0.5日、全日有給は1日として扱う業務ルール
        double paidLeave = details.stream()
                .map(Detail::leaveType)
                .filter(LeaveType.PAID_LEAVES::contains)
                .mapToDouble(LeaveType::getDays)
                .sum();

        // 代休日数計算: 休日出勤の代償として取得した代休日数を集計
        double compensatoryLeave = details.stream()
                .map(Detail::leaveType)
                .filter(LeaveType.COMPENSATORY_LEAVES::contains)
                .mapToDouble(LeaveType::getDays)
                .sum();

        // 特別休暇日数計算: 業務ルールにより部分取得不可のためcount()で固定1日として扱う
        // ※他の休暇と異なりgetDays()を使用しない理由は、企業ポリシー上特別休暇の部分取得が認められていないため
        double specialLeave = details.stream()
                .map(Detail::leaveType)
                .filter(l -> l == LeaveType.SPECIAL_LEAVE)
                .count();

        // 実働日数計算: 出勤対象日から全ての休暇取得日数を減算
        // 休日かつ休暇申請がない日は出勤扱いとなる業務ルール
        double workDays = details.stream()
                .filter(d -> d.leaveType() == null || !d.isHoliday())
                .count() - paidLeave - compensatoryLeave - specialLeave;

        // 総就業時間: 所定労働時間内の通常労働時間のみ集計（残業・休日出勤は別途集計）
        Duration totalWork = details.stream()
                .map(Detail::workingHours)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        // 総残業時間: 所定労働時間を超過した労働時間を集計（時間外手当等の算出に使用）
        Duration totalOvertime = details.stream()
                .map(Detail::overtimeHours)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        // 総休日出勤時間: 法定休日・所定休日での労働時間を集計（休出手当等の算出に使用）
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

