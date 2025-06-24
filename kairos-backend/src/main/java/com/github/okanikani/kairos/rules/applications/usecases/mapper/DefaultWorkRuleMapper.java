package com.github.okanikani.kairos.rules.applications.usecases.mapper;

import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterDefaultWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;
import com.github.okanikani.kairos.rules.domains.models.vos.User;

/**
 * DefaultWorkRuleのマッパークラス
 * ドメインモデルとDTOの変換を担当
 */
public class DefaultWorkRuleMapper {
    
    /**
     * RegisterDefaultWorkRuleRequestからDefaultWorkRuleドメインエンティティに変換
     * @param request リクエストDTO
     * @return DefaultWorkRuleエンティティ
     */
    public static DefaultWorkRule toDomain(RegisterDefaultWorkRuleRequest request) {
        User user = new User(request.user().userId());
        
        return new DefaultWorkRule(
            null, // 新規作成時はIDはnull
            request.workPlaceId(),
            request.latitude(),
            request.longitude(),
            user,
            request.standardStartTime(),
            request.standardEndTime(),
            request.breakStartTime(),
            request.breakEndTime()
        );
    }
    
    /**
     * DefaultWorkRuleドメインエンティティからDefaultWorkRuleResponseに変換
     * @param defaultWorkRule DefaultWorkRuleエンティティ
     * @return レスポンスDTO
     */
    public static DefaultWorkRuleResponse toResponse(DefaultWorkRule defaultWorkRule) {
        UserDto userDto = new UserDto(defaultWorkRule.user().userId());
        
        return new DefaultWorkRuleResponse(
            defaultWorkRule.id(),
            defaultWorkRule.workPlaceId(),
            defaultWorkRule.latitude(),
            defaultWorkRule.longitude(),
            userDto,
            defaultWorkRule.standardStartTime(),
            defaultWorkRule.standardEndTime(),
            defaultWorkRule.breakStartTime(),
            defaultWorkRule.breakEndTime()
        );
    }
}