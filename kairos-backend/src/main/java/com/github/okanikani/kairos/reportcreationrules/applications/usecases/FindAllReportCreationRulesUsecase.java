package com.github.okanikani.kairos.reportcreationrules.applications.usecases;

import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.ReportCreationRuleResponse;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.reportcreationrules.domains.models.entities.ReportCreationRule;
import com.github.okanikani.kairos.reportcreationrules.domains.models.repositories.ReportCreationRuleRepository;
import com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * レポート作成ルール取得ユースケース
 */
@Service
public class FindAllReportCreationRulesUsecase {

    private final ReportCreationRuleRepository reportCreationRuleRepository;

    public FindAllReportCreationRulesUsecase(ReportCreationRuleRepository reportCreationRuleRepository) {
        this.reportCreationRuleRepository = Objects.requireNonNull(reportCreationRuleRepository, "reportCreationRuleRepositoryは必須です");
    }

    /**
     * 指定したユーザーのレポート作成ルールを取得する
     * 
     * @param userId ユーザーID
     * @return ユーザーのレポート作成ルール（存在しない場合はnull）
     * @throws NullPointerException userIdがnullの場合
     */
    public ReportCreationRuleResponse execute(String userId) {
        Objects.requireNonNull(userId, "userIdは必須です");
        
        User user = new User(userId);
        ReportCreationRule reportCreationRule = reportCreationRuleRepository.findByUser(user);
        
        if (reportCreationRule == null) {
            return null;
        }
        
        return toReportCreationRuleResponse(reportCreationRule);
    }

    /**
     * ReportCreationRuleエンティティをReportCreationRuleResponseに変換する
     * 
     * @param reportCreationRule レポート作成ルールエンティティ
     * @return レポート作成ルールレスポンス
     */
    private ReportCreationRuleResponse toReportCreationRuleResponse(ReportCreationRule reportCreationRule) {
        UserDto userDto = new UserDto(reportCreationRule.user().userId());
        return new ReportCreationRuleResponse(
            reportCreationRule.id(),
            userDto,
            reportCreationRule.calculationStartDay(),
            reportCreationRule.timeCalculationUnitMinutes()
        );
    }
}