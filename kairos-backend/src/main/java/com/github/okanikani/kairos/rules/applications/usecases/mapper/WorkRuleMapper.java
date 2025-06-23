package com.github.okanikani.kairos.rules.applications.usecases.mapper;

import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.vos.User;

public class WorkRuleMapper {
    
    /**
     * RegisterWorkRuleRequestからWorkRuleエンティティに変換
     */
    public static WorkRule toWorkRule(RegisterWorkRuleRequest request) {
        User user = new User(request.user().userId());
        
        return new WorkRule(
            null, // 新規作成時はIDはnull
            request.workPlaceId(),
            request.latitude(),
            request.longitude(),
            user,
            request.standardStartTime(),
            request.standardEndTime(),
            request.breakStartTime(),
            request.breakEndTime(),
            request.membershipStartDate(),
            request.membershipEndDate()
        );
    }
    
    /**
     * WorkRuleエンティティからWorkRuleResponseに変換
     */
    public static WorkRuleResponse toWorkRuleResponse(WorkRule workRule) {
        UserDto userDto = new UserDto(workRule.user().userId());
        
        return new WorkRuleResponse(
            workRule.id(),
            workRule.workPlaceId(),
            workRule.latitude(),
            workRule.longitude(),
            userDto,
            workRule.standardStartTime(),
            workRule.standardEndTime(),
            workRule.breakStartTime(),
            workRule.breakEndTime(),
            workRule.membershipStartDate(),
            workRule.membershipEndDate()
        );
    }
}