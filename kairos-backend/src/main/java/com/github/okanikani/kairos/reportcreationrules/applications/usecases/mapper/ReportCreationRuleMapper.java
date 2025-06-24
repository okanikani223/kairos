package com.github.okanikani.kairos.reportcreationrules.applications.usecases.mapper;

import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.RegisterReportCreationRuleRequest;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.ReportCreationRuleResponse;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.reportcreationrules.domains.models.entities.ReportCreationRule;
import com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User;

/**
 * ReportCreationRuleのマッパークラス
 * ドメインモデルとDTOの変換を担当
 */
public class ReportCreationRuleMapper {
    
    /**
     * RegisterReportCreationRuleRequestからReportCreationRuleドメインエンティティに変換
     * @param request リクエストDTO
     * @return ReportCreationRuleエンティティ
     */
    public static ReportCreationRule toDomain(RegisterReportCreationRuleRequest request) {
        User user = new User(request.user().userId());
        
        return new ReportCreationRule(
            null, // 新規作成時はIDはnull
            user,
            request.calculationStartDay(),
            request.timeCalculationUnitMinutes()
        );
    }
    
    /**
     * ReportCreationRuleドメインエンティティからReportCreationRuleResponseに変換
     * @param reportCreationRule ReportCreationRuleエンティティ
     * @return レスポンスDTO
     */
    public static ReportCreationRuleResponse toResponse(ReportCreationRule reportCreationRule) {
        UserDto userDto = new UserDto(reportCreationRule.user().userId());
        
        return new ReportCreationRuleResponse(
            reportCreationRule.id(),
            userDto,
            reportCreationRule.calculationStartDay(),
            reportCreationRule.timeCalculationUnitMinutes()
        );
    }
}