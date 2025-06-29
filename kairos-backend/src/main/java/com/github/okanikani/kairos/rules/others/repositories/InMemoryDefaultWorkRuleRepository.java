package com.github.okanikani.kairos.rules.others.repositories;

import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.DefaultWorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ※これは開発・テスト用の一時的な実装です。
 * 本番環境ではデータベースを使用した実装に置き換える必要があります。
 * TODO: PostgreSQL等を使用した永続化実装への置き換え
 */
@Repository
@Profile("dev")
public class InMemoryDefaultWorkRuleRepository implements DefaultWorkRuleRepository {
    
    private final Map<Long, DefaultWorkRule> defaultWorkRules = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public DefaultWorkRule save(DefaultWorkRule defaultWorkRule) {
        Long id = defaultWorkRule.id();
        if (id == null) {
            // 新規作成の場合は自動生成IDを設定
            id = idGenerator.getAndIncrement();
            defaultWorkRule = new DefaultWorkRule(
                id,
                defaultWorkRule.workPlaceId(),
                defaultWorkRule.latitude(),
                defaultWorkRule.longitude(),
                defaultWorkRule.user(),
                defaultWorkRule.standardStartTime(),
                defaultWorkRule.standardEndTime(),
                defaultWorkRule.breakStartTime(),
                defaultWorkRule.breakEndTime()
            );
        }
        defaultWorkRules.put(id, defaultWorkRule);
        return defaultWorkRule;
    }
    
    @Override
    public DefaultWorkRule findById(Long id) {
        return defaultWorkRules.get(id);
    }
    
    @Override
    public List<DefaultWorkRule> findByUser(User user) {
        return defaultWorkRules.values().stream()
            .filter(rule -> rule.user().equals(user))
            .toList();
    }
    
    @Override
    public List<DefaultWorkRule> findByWorkPlaceId(Long workPlaceId) {
        return defaultWorkRules.values().stream()
            .filter(rule -> rule.workPlaceId().equals(workPlaceId))
            .toList();
    }
    
    @Override
    public DefaultWorkRule findByUserAndWorkPlaceId(User user, Long workPlaceId) {
        return defaultWorkRules.values().stream()
            .filter(rule -> rule.user().equals(user) && rule.workPlaceId().equals(workPlaceId))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public void deleteById(Long id) {
        defaultWorkRules.remove(id);
    }
    
    @Override
    public List<DefaultWorkRule> findAll() {
        return List.copyOf(defaultWorkRules.values());
    }
    
    /**
     * テスト用のクリアメソッド
     */
    public void clear() {
        defaultWorkRules.clear();
        idGenerator.set(1);
    }
}