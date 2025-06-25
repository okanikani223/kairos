package com.github.okanikani.kairos.reports.applications.usecases;

import com.github.okanikani.kairos.reports.applications.usecases.dto.DeleteReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.mapper.ReportMapper;
import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.repositories.ReportRepository;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DeleteReportUseCase {
    
    private final ReportRepository reportRepository;
    
    public DeleteReportUseCase(ReportRepository reportRepository) {
        this.reportRepository = Objects.requireNonNull(reportRepository, "reportRepositoryは必須です");
    }
    
    public void execute(DeleteReportRequest request) {
        Objects.requireNonNull(request, "requestは必須です");
        
        User user = ReportMapper.toUser(request.user());
        
        // 業務ルール: 削除対象の勤怠表が存在することを確認
        Report existingReport = reportRepository.find(request.yearMonth(), user);
        if (existingReport == null) {
            throw new IllegalArgumentException(
                String.format("削除対象の勤怠表が存在しません: %s", request.yearMonth())
            );
        }
        
        // 業務ルール: 提出済み・承認済みの勤怠表は削除不可
        // 理由: 承認フローを経た勤怠表は労務管理上削除してはならない
        if (existingReport.status() == ReportStatus.SUBMITTED || 
            existingReport.status() == ReportStatus.APPROVED) {
            throw new IllegalArgumentException(
                String.format("ステータスが%sの勤怠表は削除できません", existingReport.status())
            );
        }
        
        reportRepository.delete(request.yearMonth(), user);
    }
}