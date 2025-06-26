package com.github.okanikani.kairos.reports.others.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Duration;

/**
 * 勤怠表の集計情報（埋め込み可能エンティティ）
 * 
 * 業務要件: 月次の勤怠データを集計した情報を保持
 */
@Embeddable
public class SummaryJpaEntity {

    @Column(name = "work_days", nullable = false)
    private double workDays;

    @Column(name = "paid_leave_days", nullable = false)
    private double paidLeaveDays;

    @Column(name = "compensatory_leave_days", nullable = false)
    private double compensatoryLeaveDays;

    @Column(name = "special_leave_days", nullable = false)
    private double specialLeaveDays;

    @Column(name = "total_work_time_minutes", nullable = false)
    private Long totalWorkTimeMinutes;

    @Column(name = "total_overtime_minutes", nullable = false)
    private Long totalOvertimeMinutes;

    @Column(name = "total_holiday_work_minutes", nullable = false)
    private Long totalHolidayWorkMinutes;

    protected SummaryJpaEntity() {
        // JPAのため
        this.workDays = 0.0;
        this.paidLeaveDays = 0.0;
        this.compensatoryLeaveDays = 0.0;
        this.specialLeaveDays = 0.0;
        this.totalWorkTimeMinutes = 0L;
        this.totalOvertimeMinutes = 0L;
        this.totalHolidayWorkMinutes = 0L;
    }

    public SummaryJpaEntity(double workDays, double paidLeaveDays, double compensatoryLeaveDays,
                           double specialLeaveDays, Duration totalWorkTime, Duration totalOvertime,
                           Duration totalHolidayWork) {
        this.workDays = workDays;
        this.paidLeaveDays = paidLeaveDays;
        this.compensatoryLeaveDays = compensatoryLeaveDays;
        this.specialLeaveDays = specialLeaveDays;
        this.totalWorkTimeMinutes = totalWorkTime != null ? totalWorkTime.toMinutes() : 0L;
        this.totalOvertimeMinutes = totalOvertime != null ? totalOvertime.toMinutes() : 0L;
        this.totalHolidayWorkMinutes = totalHolidayWork != null ? totalHolidayWork.toMinutes() : 0L;
    }

    public double getWorkDays() {
        return workDays;
    }

    public double getPaidLeaveDays() {
        return paidLeaveDays;
    }

    public double getCompensatoryLeaveDays() {
        return compensatoryLeaveDays;
    }

    public double getSpecialLeaveDays() {
        return specialLeaveDays;
    }

    public Duration getTotalWorkTime() {
        return Duration.ofMinutes(totalWorkTimeMinutes);
    }

    public Duration getTotalOvertime() {
        return Duration.ofMinutes(totalOvertimeMinutes);
    }

    public Duration getTotalHolidayWork() {
        return Duration.ofMinutes(totalHolidayWorkMinutes);
    }
}