package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UpdateWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 勤務ルール更新ユースケース
 */
@Service
public class UpdateWorkRuleUseCase {

    private final WorkRuleRepository workRuleRepository;

    public UpdateWorkRuleUseCase(WorkRuleRepository workRuleRepository) {
        this.workRuleRepository = Objects.requireNonNull(workRuleRepository, "workRuleRepositoryは必須です");
    }

    /**
     * 指定したIDの勤務ルールを更新する
     * 
     * @param workRuleId 勤務ルールID
     * @param request 更新リクエスト
     * @param userId ユーザーID
     * @return 更新された勤務ルールレスポンス
     * @throws NullPointerException workRuleId、request、またはuserIdがnullの場合
     * @throws ResourceNotFoundException 勤務ルールが存在しない場合
     * @throws AuthorizationException 権限がない場合
     */
    public WorkRuleResponse execute(Long workRuleId, UpdateWorkRuleRequest request, String userId) {
        Objects.requireNonNull(workRuleId, "workRuleIdは必須です");
        Objects.requireNonNull(request, "requestは必須です");
        Objects.requireNonNull(userId, "userIdは必須です");
        
        WorkRule existingWorkRule = workRuleRepository.findById(workRuleId);
        if (existingWorkRule == null) {
            throw new ResourceNotFoundException("指定された勤務ルールが存在しません");
        }
        
        // ユーザー権限チェック: 自分の勤務ルールのみ更新可能
        if (!existingWorkRule.user().userId().equals(userId)) {
            throw new AuthorizationException("この勤務ルールを更新する権限がありません");
        }
        
        // 新しい勤務ルールエンティティを作成（リクエストの値で更新）
        User user = new User(request.user().userId());
        WorkRule updatedWorkRule = new WorkRule(
            workRuleId,  // IDは既存のものを維持
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
        
        workRuleRepository.save(updatedWorkRule);
        
        return toWorkRuleResponse(updatedWorkRule);
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