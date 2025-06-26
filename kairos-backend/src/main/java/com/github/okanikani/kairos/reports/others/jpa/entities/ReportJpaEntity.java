package com.github.okanikani.kairos.reports.others.jpa.entities;

import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 勤怠表のJPAエンティティ
 * 
 * 業務要件: 月次の勤怠表を管理し、勤務日詳細と集計情報を保持
 */
@Entity
@Table(name = "reports")
public class ReportJpaEntity {

    @EmbeddedId
    private ReportId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetailJpaEntity> workDays = new ArrayList<>();

    @Embedded
    private SummaryJpaEntity summary;

    protected ReportJpaEntity() {
        // JPAのため
    }

    public ReportJpaEntity(ReportId id, ReportStatus status, SummaryJpaEntity summary) {
        this.id = Objects.requireNonNull(id, "IDは必須です");
        this.status = Objects.requireNonNull(status, "ステータスは必須です");
        this.summary = Objects.requireNonNull(summary, "サマリーは必須です");
    }

    public void addWorkDay(DetailJpaEntity detail) {
        workDays.add(detail);
        detail.setReport(this);
    }

    public void removeWorkDay(DetailJpaEntity detail) {
        workDays.remove(detail);
        detail.setReport(null);
    }

    public ReportId getId() {
        return id;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = Objects.requireNonNull(status, "ステータスは必須です");
    }

    public List<DetailJpaEntity> getWorkDays() {
        return new ArrayList<>(workDays);
    }

    public SummaryJpaEntity getSummary() {
        return summary;
    }

    public void setSummary(SummaryJpaEntity summary) {
        this.summary = Objects.requireNonNull(summary, "サマリーは必須です");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportJpaEntity that = (ReportJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}