package com.github.okanikani.kairos.reports.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.DuplicateResourceException;
import com.github.okanikani.kairos.reports.applications.usecases.dto.RegisterReportRequest;
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

/**
 * 勤怠表登録ユースケース
 */
@Service
public class RegisterReportUseCase {
    
    private final ReportRepository reportRepository;
    
    public RegisterReportUseCase(ReportRepository reportRepository) {
        this.reportRepository = Objects.requireNonNull(reportRepository, "reportRepositoryは必須です");
    }
    
    /**
     * 勤怠表を登録する
     * @param request 登録リクエスト
     * @return 登録された勤怠表のレスポンス
     */
    public ReportResponse execute(RegisterReportRequest request) {
        Objects.requireNonNull(request, "requestは必須です");
        
        // DTOからドメインオブジェクトに変換
        User user = ReportMapper.toUser(request.user());
        List<Detail> workDays = request.workDays().stream()
            .map(ReportMapper::toDetail)
            .toList();
        
        // 業務ルール: 同一ユーザー・同一年月の勤怠表は重複登録不可
        // 理由: 給与計算の二重処理やデータ不整合を防止するため
        Report existingReport = reportRepository.find(request.yearMonth(), user);
        if (existingReport != null) {
            throw new DuplicateResourceException(
                String.format("指定された年月(%s)の勤怠表は既に存在します", request.yearMonth())
            );
        }
        
        // サマリ情報を生成
        Summary summary = SummaryFactory.from(workDays);
        
        // 勤怠表エンティティを作成
        Report report = new Report(
            request.yearMonth(),
            user,
            ReportStatus.NOT_SUBMITTED, // 新規作成時は必ず未提出状態から開始する業務ルール
            workDays,
            summary
        );
        
        // 保存
        reportRepository.save(report);
        
        // レスポンスDTOに変換して返却
        return ReportMapper.toReportResponse(report);
    }
}