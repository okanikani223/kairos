package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 勤務ルール削除ユースケース
 */
@Service
public class DeleteWorkRuleUseCase {

    private final WorkRuleRepository workRuleRepository;

    public DeleteWorkRuleUseCase(WorkRuleRepository workRuleRepository) {
        this.workRuleRepository = Objects.requireNonNull(workRuleRepository, "workRuleRepositoryは必須です");
    }

    /**
     * 指定したIDの勤務ルールを削除する
     * 
     * @param workRuleId 勤務ルールID
     * @param userId ユーザーID
     * @throws NullPointerException workRuleIdまたはuserIdがnullの場合
     * @throws IllegalArgumentException 勤務ルールが存在しない場合または権限がない場合
     */
    public void execute(Long workRuleId, String userId) {
        Objects.requireNonNull(workRuleId, "workRuleIdは必須です");
        Objects.requireNonNull(userId, "userIdは必須です");
        
        WorkRule workRule = workRuleRepository.findById(workRuleId);
        if (workRule == null) {
            throw new IllegalArgumentException("指定された勤務ルールが存在しません");
        }
        
        // ユーザー権限チェック: 自分の勤務ルールのみ削除可能
        if (!workRule.user().userId().equals(userId)) {
            throw new IllegalArgumentException("この勤務ルールを削除する権限がありません");
        }
        
        workRuleRepository.deleteById(workRuleId);
    }
}