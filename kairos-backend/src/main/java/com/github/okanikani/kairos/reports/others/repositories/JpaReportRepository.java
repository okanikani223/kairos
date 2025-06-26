package com.github.okanikani.kairos.reports.others.repositories;

import com.github.okanikani.kairos.reports.domains.models.constants.LeaveType;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.repositories.ReportRepository;
import com.github.okanikani.kairos.reports.domains.models.vos.*;
import com.github.okanikani.kairos.reports.others.jpa.entities.*;
import com.github.okanikani.kairos.reports.others.jpa.repositories.ReportJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 勤怠表リポジトリのJPA実装
 * 
 * 業務要件: ドメインモデルとJPAエンティティ間の変換とデータ永続化を担当
 */
@Repository
@Primary
public class JpaReportRepository implements ReportRepository {

    private final ReportJpaRepository reportJpaRepository;

    public JpaReportRepository(ReportJpaRepository reportJpaRepository) {
        this.reportJpaRepository = reportJpaRepository;
    }

    @Override
    public void save(Report report) {
        ReportJpaEntity jpaEntity = toJpaEntity(report);
        reportJpaRepository.save(jpaEntity);
    }

    @Override
    public Report find(YearMonth yearMonth, User user) {
        return reportJpaRepository.findByYearMonthAndUserId(yearMonth, user.userId())
                .map(this::toDomainModel)
                .orElse(null);
    }

    @Override
    public List<Report> findAll() {
        return reportJpaRepository.findAll()
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public void update(Report report) {
        // JPAでは保存操作がupsertとして動作するため、saveメソッドを使用
        save(report);
    }

    @Override
    public void delete(YearMonth yearMonth, User user) {
        ReportId reportId = new ReportId(yearMonth, user.userId());
        reportJpaRepository.deleteById(reportId);
    }

    public List<Report> findByUser(User user) {
        return reportJpaRepository.findByUserId(user.userId())
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    public boolean exists(YearMonth yearMonth, User user) {
        return reportJpaRepository.existsByYearMonthAndUserId(yearMonth, user.userId());
    }

    /**
     * ドメインモデルをJPAエンティティに変換
     */
    private ReportJpaEntity toJpaEntity(Report report) {
        ReportId reportId = new ReportId(report.yearMonth(), report.owner().userId());
        
        SummaryJpaEntity summaryJpa = new SummaryJpaEntity(
                report.summary().workDays(),
                report.summary().paidLeaveDays(),
                report.summary().compensatoryLeaveDays(),
                report.summary().specialLeaveDays(),
                report.summary().totalWorkTime(),
                report.summary().totalOvertime(),
                report.summary().totalHolidayWork()
        );

        ReportJpaEntity reportJpa = new ReportJpaEntity(reportId, report.status(), summaryJpa);

        // 勤務日詳細の変換と追加
        for (Detail detail : report.workDays()) {
            DetailJpaEntity detailJpa = new DetailJpaEntity(
                    detail.workDate(),
                    detail.isHoliday(),
                    detail.leaveType(),
                    detail.startDateTime() != null ? detail.startDateTime().value() : null,
                    detail.endDateTime() != null ? detail.endDateTime().value() : null,
                    detail.workingHours(),
                    detail.overtimeHours(),
                    detail.holidayWorkHours(),
                    detail.note()
            );
            reportJpa.addWorkDay(detailJpa);
        }

        return reportJpa;
    }

    /**
     * JPAエンティティをドメインモデルに変換
     */
    private Report toDomainModel(ReportJpaEntity jpaEntity) {
        User user = new User(jpaEntity.getId().getUserId());
        
        Summary summary = new Summary(
                jpaEntity.getSummary().getWorkDays(),
                jpaEntity.getSummary().getPaidLeaveDays(),
                jpaEntity.getSummary().getCompensatoryLeaveDays(),
                jpaEntity.getSummary().getSpecialLeaveDays(),
                jpaEntity.getSummary().getTotalWorkTime(),
                jpaEntity.getSummary().getTotalOvertime(),
                jpaEntity.getSummary().getTotalHolidayWork()
        );

        List<Detail> workDays = jpaEntity.getWorkDays().stream()
                .map(this::toDetailDomain)
                .collect(Collectors.toList());

        return new Report(
                jpaEntity.getId().getYearMonth(),
                user,
                jpaEntity.getStatus(),
                workDays,
                summary
        );
    }

    /**
     * DetailJpaEntityをDetailドメインモデルに変換
     */
    private Detail toDetailDomain(DetailJpaEntity jpaEntity) {
        WorkTime startTime = jpaEntity.getStartDateTime() != null 
                ? new WorkTime(jpaEntity.getStartDateTime()) 
                : null;
        WorkTime endTime = jpaEntity.getEndDateTime() != null 
                ? new WorkTime(jpaEntity.getEndDateTime()) 
                : null;

        return new Detail(
                jpaEntity.getWorkDate(),
                jpaEntity.isHoliday(),
                jpaEntity.getLeaveType(),
                startTime,
                endTime,
                jpaEntity.getWorkingHours(),
                jpaEntity.getOvertimeHours(),
                jpaEntity.getHolidayWorkHours(),
                jpaEntity.getNote()
        );
    }
}