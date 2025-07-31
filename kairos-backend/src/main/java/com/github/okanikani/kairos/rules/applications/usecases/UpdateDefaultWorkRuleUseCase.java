package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UpdateDefaultWorkRuleRequest;
import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.DefaultWorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.springframework.stereotype.Service;

/**
 * デフォルト勤務ルール更新ユースケース
 */
@Service
public class UpdateDefaultWorkRuleUseCase {
    
    private final DefaultWorkRuleRepository repository;
    
    /**
     * コンストラクタ
     * @param repository デフォルト勤務ルールリポジトリ
     */
    public UpdateDefaultWorkRuleUseCase(DefaultWorkRuleRepository repository) {
        this.repository = java.util.Objects.requireNonNull(repository, "repositoryは必須です");
    }
    
    /**
     * デフォルト勤務ルールを更新する
     * @param id デフォルト勤務ルールID
     * @param request 更新リクエスト
     * @param userId リクエストユーザーID（権限チェック用）
     * @return 更新されたデフォルト勤務ルール情報
     */
    public DefaultWorkRuleResponse execute(Long id, UpdateDefaultWorkRuleRequest request, String userId) {
        // 既存のルールを取得
        DefaultWorkRule existingRule = repository.findById(id);
        if (existingRule == null) {
            throw new IllegalArgumentException("指定されたデフォルト勤務ルールが見つかりません");
        }
        
        // ユーザー権限チェック
        if (!existingRule.user().userId().equals(userId)) {
            throw new SecurityException("このデフォルト勤務ルールを更新する権限がありません");
        }
        
        // 更新されたルールを作成
        User user = new User(userId);
        DefaultWorkRule updatedRule = new DefaultWorkRule(
                request.workPlaceId(),
                request.latitude(),
                request.longitude(),
                user,
                request.standardStartTime(),
                request.standardEndTime(),
                request.breakStartTime(),
                request.breakEndTime()
        );
        
        // リポジトリで更新（既存のルールを削除して新規保存）
        repository.deleteById(id);
        DefaultWorkRule savedRule = repository.save(updatedRule);
        
        return DefaultWorkRuleResponse.from(savedRule);
    }
}