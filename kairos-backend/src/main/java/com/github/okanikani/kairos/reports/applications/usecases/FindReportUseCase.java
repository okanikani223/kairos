package com.github.okanikani.kairos.reports.applications.usecases;

import com.github.okanikani.kairos.reports.applications.usecases.dto.FindReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.ReportResponse;
import com.github.okanikani.kairos.reports.applications.usecases.mapper.ReportMapper;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.repositories.ReportRepository;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 勤怠表取得ユースケース
 */
@Service
public class FindReportUseCase {
    
    private final ReportRepository reportRepository;
    
    public FindReportUseCase(ReportRepository reportRepository) {
        this.reportRepository = Objects.requireNonNull(reportRepository, "reportRepositoryは必須です");
    }
    
    /**
     * 勤怠表を取得する
     * @param request 取得リクエスト
     * @return 勤怠表のレスポンス、見つからない場合はnull
     */
    public ReportResponse execute(FindReportRequest request) {
        Objects.requireNonNull(request, "requestは必須です");
        
        // DTOからドメインオブジェクトに変換
        User user = ReportMapper.toUser(request.user());
        
        // 勤怠表を検索
        Report report = reportRepository.find(request.yearMonth(), user);
        
        if (report == null) {
            return null;
        }
        
        // レスポンスDTOに変換して返却
        return ReportMapper.toReportResponse(report);
    }
}