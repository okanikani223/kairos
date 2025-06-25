package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.DuplicateResourceException;
import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterDefaultWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.mapper.DefaultWorkRuleMapper;
import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.DefaultWorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * デフォルト勤怠ルール登録ユースケース
 */
@Service
public class RegisterDefaultWorkRuleUseCase {
    
    private final DefaultWorkRuleRepository defaultWorkRuleRepository;
    
    /**
     * コンストラクタ
     * @param defaultWorkRuleRepository デフォルト勤怠ルールリポジトリ
     */
    public RegisterDefaultWorkRuleUseCase(DefaultWorkRuleRepository defaultWorkRuleRepository) {
        this.defaultWorkRuleRepository = Objects.requireNonNull(defaultWorkRuleRepository, "defaultWorkRuleRepositoryは必須です");
    }
    
    /**
     * デフォルト勤怠ルールを登録する
     * @param request 登録リクエスト
     * @return 登録されたデフォルト勤怠ルール情報
     * @throws DuplicateResourceException 同一ユーザー・同一勤怠先で既にデフォルト勤怠ルールが存在する場合
     */
    public DefaultWorkRuleResponse execute(RegisterDefaultWorkRuleRequest request) {
        Objects.requireNonNull(request, "リクエストは必須です");
        
        // ドメインモデルに変換
        DefaultWorkRule defaultWorkRule = DefaultWorkRuleMapper.toDomain(request);
        User user = defaultWorkRule.user();
        Long workPlaceId = defaultWorkRule.workPlaceId();
        
        // 重複チェック：同一ユーザー・同一勤怠先のデフォルト勤怠ルールが既に存在するかチェック
        DefaultWorkRule existingDefaultWorkRule = defaultWorkRuleRepository.findByUserAndWorkPlaceId(user, workPlaceId);
        if (existingDefaultWorkRule != null) {
            throw new DuplicateResourceException("指定されたユーザーと勤怠先の組み合わせでデフォルト勤怠ルールが既に存在します");
        }
        
        // デフォルト勤怠ルールを保存
        defaultWorkRuleRepository.save(defaultWorkRule);
        
        // レスポンスに変換して返却
        return DefaultWorkRuleMapper.toResponse(defaultWorkRule);
    }
}