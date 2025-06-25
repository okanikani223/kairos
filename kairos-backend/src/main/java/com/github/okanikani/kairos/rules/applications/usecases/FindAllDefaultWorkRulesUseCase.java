package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.DefaultWorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 全デフォルト勤務ルール取得ユースケース
 */
@Service
public class FindAllDefaultWorkRulesUseCase {

    private final DefaultWorkRuleRepository defaultWorkRuleRepository;

    public FindAllDefaultWorkRulesUseCase(DefaultWorkRuleRepository defaultWorkRuleRepository) {
        this.defaultWorkRuleRepository = Objects.requireNonNull(defaultWorkRuleRepository, "defaultWorkRuleRepositoryは必須です");
    }

    /**
     * 指定したユーザーの全デフォルト勤務ルールを取得する
     * 
     * @param userId ユーザーID
     * @return ユーザーの全デフォルト勤務ルールのリスト
     * @throws NullPointerException userIdがnullの場合
     */
    public List<DefaultWorkRuleResponse> execute(String userId) {
        Objects.requireNonNull(userId, "userIdは必須です");
        
        User user = new User(userId);
        List<DefaultWorkRule> defaultWorkRules = defaultWorkRuleRepository.findByUser(user);
        
        return defaultWorkRules.stream()
            .map(this::toDefaultWorkRuleResponse)
            .collect(Collectors.toList());
    }

    /**
     * DefaultWorkRuleエンティティをDefaultWorkRuleResponseに変換する
     * 
     * @param defaultWorkRule デフォルト勤務ルールエンティティ
     * @return デフォルト勤務ルールレスポンス
     */
    private DefaultWorkRuleResponse toDefaultWorkRuleResponse(DefaultWorkRule defaultWorkRule) {
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