package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 特定勤務ルール取得ユースケース
 */
@Service
public class FindWorkRuleByIdUseCase {

    private final WorkRuleRepository workRuleRepository;

    public FindWorkRuleByIdUseCase(WorkRuleRepository workRuleRepository) {
        this.workRuleRepository = Objects.requireNonNull(workRuleRepository, "workRuleRepositoryは必須です");
    }

    /**
     * 指定したIDの勤務ルールを取得する
     * 
     * @param workRuleId 勤務ルールID
     * @param userId ユーザーID
     * @return 勤務ルールレスポンス
     * @throws NullPointerException workRuleIdまたはuserIdがnullの場合
     * @throws ResourceNotFoundException 勤務ルールが存在しない場合
     * @throws AuthorizationException 権限がない場合
     */
    public WorkRuleResponse execute(Long workRuleId, String userId) {
        Objects.requireNonNull(workRuleId, "workRuleIdは必須です");
        Objects.requireNonNull(userId, "userIdは必須です");
        
        WorkRule workRule = workRuleRepository.findById(workRuleId);
        if (workRule == null) {
            throw new ResourceNotFoundException("指定された勤務ルールが存在しません");
        }
        
        // ユーザー権限チェック: 自分の勤務ルールのみアクセス可能
        if (!workRule.user().userId().equals(userId)) {
            throw new AuthorizationException("この勤務ルールにアクセスする権限がありません");
        }
        
        return toWorkRuleResponse(workRule);
    }

    /**
     * WorkRuleエンティティをWorkRuleResponseに変換する
     * 
     * @param workRule 勤務ルールエンティティ
     * @return 勤務ルールレスポンス
     */
    private WorkRuleResponse toWorkRuleResponse(WorkRule workRule) {
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