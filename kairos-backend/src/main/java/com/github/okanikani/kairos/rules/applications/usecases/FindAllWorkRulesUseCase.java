package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 全勤務ルール取得ユースケース
 */
@Service
public class FindAllWorkRulesUseCase {

    private final WorkRuleRepository workRuleRepository;

    public FindAllWorkRulesUseCase(WorkRuleRepository workRuleRepository) {
        this.workRuleRepository = Objects.requireNonNull(workRuleRepository, "workRuleRepositoryは必須です");
    }

    /**
     * 指定したユーザーの全勤務ルールを取得する
     * 
     * @param userId ユーザーID
     * @return ユーザーの全勤務ルールのリスト
     * @throws NullPointerException userIdがnullの場合
     */
    public List<WorkRuleResponse> execute(String userId) {
        Objects.requireNonNull(userId, "userIdは必須です");
        
        User user = new User(userId);
        List<WorkRule> workRules = workRuleRepository.findByUser(user);
        
        return workRules.stream()
            .map(this::toWorkRuleResponse)
            .collect(Collectors.toList());
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