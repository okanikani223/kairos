package com.github.okanikani.kairos.reportcreationrules.applications.usecases;

import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.RegisterReportCreationRuleRequest;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.ReportCreationRuleResponse;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.mapper.ReportCreationRuleMapper;
import com.github.okanikani.kairos.reportcreationrules.domains.models.entities.ReportCreationRule;
import com.github.okanikani.kairos.reportcreationrules.domains.models.repositories.ReportCreationRuleRepository;
import com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 勤怠作成ルール登録ユースケース
 */
@Service
public class RegisterReportCreationRuleUseCase {
    
    private final ReportCreationRuleRepository reportCreationRuleRepository;
    
    /**
     * コンストラクタ
     * @param reportCreationRuleRepository 勤怠作成ルールリポジトリ
     */
    public RegisterReportCreationRuleUseCase(ReportCreationRuleRepository reportCreationRuleRepository) {
        this.reportCreationRuleRepository = Objects.requireNonNull(reportCreationRuleRepository, "reportCreationRuleRepositoryは必須です");
    }
    
    /**
     * 勤怠作成ルールを登録する
     * @param request 登録リクエスト
     * @return 登録された勤怠作成ルール情報
     * @throws IllegalArgumentException 指定されたユーザーの勤怠作成ルールが既に存在する場合
     */
    public ReportCreationRuleResponse execute(RegisterReportCreationRuleRequest request) {
        Objects.requireNonNull(request, "リクエストは必須です");
        
        // ドメインモデルに変換
        ReportCreationRule reportCreationRule = ReportCreationRuleMapper.toDomain(request);
        User user = reportCreationRule.user();
        
        // 重複チェック：同一ユーザーの勤怠作成ルールが既に存在するかチェック
        ReportCreationRule existingRule = reportCreationRuleRepository.findByUser(user);
        if (existingRule != null) {
            throw new IllegalArgumentException("指定されたユーザーの勤怠作成ルールが既に存在します");
        }
        
        // 勤怠作成ルールを保存
        reportCreationRuleRepository.save(reportCreationRule);
        
        // レスポンスに変換して返却
        return ReportCreationRuleMapper.toResponse(reportCreationRule);
    }
}