package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.DefaultWorkRuleRepository;
import org.springframework.stereotype.Service;

/**
 * IDによるデフォルト勤務ルール取得ユースケース
 */
@Service
public class FindDefaultWorkRuleByIdUseCase {
    
    private final DefaultWorkRuleRepository repository;
    
    /**
     * コンストラクタ
     * @param repository デフォルト勤務ルールリポジトリ
     */
    public FindDefaultWorkRuleByIdUseCase(DefaultWorkRuleRepository repository) {
        this.repository = java.util.Objects.requireNonNull(repository, "repositoryは必須です");
    }
    
    /**
     * 指定されたIDのデフォルト勤務ルールを取得する
     * @param id デフォルト勤務ルールID
     * @param userId リクエストユーザーID（権限チェック用）
     * @return デフォルト勤務ルール情報
     */
    public DefaultWorkRuleResponse execute(Long id, String userId) {
        DefaultWorkRule rule = repository.findById(id);
        if (rule == null) {
            throw new IllegalArgumentException("指定されたデフォルト勤務ルールが見つかりません");
        }
        
        // ユーザー権限チェック
        if (!rule.user().userId().equals(userId)) {
            throw new SecurityException("このデフォルト勤務ルールにアクセスする権限がありません");
        }
        
        return DefaultWorkRuleResponse.from(rule);
    }
}