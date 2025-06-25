package com.github.okanikani.kairos.reports.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UpdateReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.ReportResponse;
import com.github.okanikani.kairos.reports.applications.usecases.mapper.ReportMapper;
import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.repositories.ReportRepository;
import com.github.okanikani.kairos.reports.domains.models.vos.Detail;
import com.github.okanikani.kairos.reports.domains.models.vos.Summary;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import com.github.okanikani.kairos.reports.domains.service.SummaryFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UpdateReportUseCase {
    
    private final ReportRepository reportRepository;
    
    public UpdateReportUseCase(ReportRepository reportRepository) {
        this.reportRepository = Objects.requireNonNull(reportRepository, "reportRepositoryは必須です");
    }
    
    public ReportResponse execute(UpdateReportRequest request) {
        Objects.requireNonNull(request, "requestは必須です");
        
        User user = ReportMapper.toUser(request.user());
        List<Detail> workDays = request.workDays().stream()
            .map(ReportMapper::toDetail)
            .toList();
        
        // 業務ルール: 更新対象の勤怠表が存在することを確認
        // 存在しない場合は例外を発生させ、新規作成との混同を防ぐ
        Report existingReport = reportRepository.find(request.yearMonth(), user);
        if (existingReport == null) {
            throw new ResourceNotFoundException(
                String.format("更新対象の勤怠表が存在しません: %s", request.yearMonth())
            );
        }
        
        // 業務ルール: 提出済み・承認済みの勤怠表は更新不可
        // 理由: 給与計算処理後の変更は労務管理上問題となるため
        if (existingReport.status() == ReportStatus.SUBMITTED || 
            existingReport.status() == ReportStatus.APPROVED) {
            throw new IllegalArgumentException(
                String.format("ステータスが%sの勤怠表は更新できません", existingReport.status())
            );
        }
        
        // サマリ情報を再計算
        Summary summary = SummaryFactory.from(workDays);
        
        // 既存のステータスを維持して更新
        Report updatedReport = new Report(
            request.yearMonth(),
            user,
            existingReport.status(), // 更新時は既存ステータスを維持する業務ルール
            workDays,
            summary
        );
        
        reportRepository.update(updatedReport);
        
        return ReportMapper.toReportResponse(updatedReport);
    }
}