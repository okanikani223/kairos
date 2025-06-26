package com.github.okanikani.kairos.reports.others.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.YearMonth;
import java.util.Objects;

/**
 * 勤怠表の複合主キー（年月とユーザーID）
 * 
 * 業務要件: 同一ユーザーの同一年月の勤怠表は一意
 */
@Embeddable
public class ReportId implements Serializable {

    @Column(name = "year_month", nullable = false, length = 7)
    private YearMonth yearMonth;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    protected ReportId() {
        // JPAのため
    }

    public ReportId(YearMonth yearMonth, String userId) {
        this.yearMonth = Objects.requireNonNull(yearMonth, "年月は必須です");
        this.userId = Objects.requireNonNull(userId, "ユーザーIDは必須です");
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportId reportId = (ReportId) o;
        return Objects.equals(yearMonth, reportId.yearMonth) &&
               Objects.equals(userId, reportId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(yearMonth, userId);
    }
}