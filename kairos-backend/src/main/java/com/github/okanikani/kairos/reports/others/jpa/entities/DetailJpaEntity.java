package com.github.okanikani.kairos.reports.others.jpa.entities;

import com.github.okanikani.kairos.reports.domains.models.constants.LeaveType;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 勤務日詳細のJPAエンティティ
 * 
 * 業務要件: 日次の勤務情報（出退勤時刻、休暇情報、勤務時間）を管理
 */
@Entity
@Table(name = "report_details")
public class DetailJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "report_year_month", referencedColumnName = "year_month"),
        @JoinColumn(name = "report_user_id", referencedColumnName = "user_id")
    })
    private ReportJpaEntity report;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "is_holiday", nullable = false)
    private boolean holiday;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", length = 30)
    private LeaveType leaveType;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "working_hours_minutes", nullable = false)
    private Long workingHoursMinutes;

    @Column(name = "overtime_hours_minutes", nullable = false)
    private Long overtimeHoursMinutes;

    @Column(name = "holiday_work_hours_minutes", nullable = false)
    private Long holidayWorkHoursMinutes;

    @Column(name = "note", length = 500)
    private String note;

    protected DetailJpaEntity() {
        // JPAのため
        this.workingHoursMinutes = 0L;
        this.overtimeHoursMinutes = 0L;
        this.holidayWorkHoursMinutes = 0L;
    }

    public DetailJpaEntity(LocalDate workDate, boolean isHoliday, LeaveType leaveType,
                          LocalDateTime startDateTime, LocalDateTime endDateTime,
                          Duration workingHours, Duration overtimeHours,
                          Duration holidayWorkHours, String note) {
        this.workDate = Objects.requireNonNull(workDate, "勤務日は必須です");
        this.holiday = isHoliday;
        this.leaveType = leaveType;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.workingHoursMinutes = workingHours != null ? workingHours.toMinutes() : 0L;
        this.overtimeHoursMinutes = overtimeHours != null ? overtimeHours.toMinutes() : 0L;
        this.holidayWorkHoursMinutes = holidayWorkHours != null ? holidayWorkHours.toMinutes() : 0L;
        this.note = note;
    }

    void setReport(ReportJpaEntity report) {
        this.report = report;
    }

    public Long getId() {
        return id;
    }

    public ReportJpaEntity getReport() {
        return report;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public boolean isHoliday() {
        return holiday;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public Duration getWorkingHours() {
        return Duration.ofMinutes(workingHoursMinutes);
    }

    public Duration getOvertimeHours() {
        return Duration.ofMinutes(overtimeHoursMinutes);
    }

    public Duration getHolidayWorkHours() {
        return Duration.ofMinutes(holidayWorkHoursMinutes);
    }

    public String getNote() {
        return note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetailJpaEntity that = (DetailJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}