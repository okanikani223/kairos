package com.github.okanikani.kairos.rules.others.repositories;

import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
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
public class InMemoryWorkRuleRepository implements WorkRuleRepository {
    
    private final Map<Long, WorkRule> workRules = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public WorkRule save(WorkRule workRule) {
        Long id = workRule.id();
        if (id == null) {
            // 新規作成の場合は自動生成IDを設定
            id = idGenerator.getAndIncrement();
            workRule = new WorkRule(
                id,
                workRule.workPlaceId(),
                workRule.latitude(),
                workRule.longitude(),
                workRule.user(),
                workRule.standardStartTime(),
                workRule.standardEndTime(),
                workRule.breakStartTime(),
                workRule.breakEndTime(),
                workRule.membershipStartDate(),
                workRule.membershipEndDate()
            );
        }
        workRules.put(id, workRule);
        return workRule;
    }
    
    @Override
    public WorkRule findById(Long id) {
        return workRules.get(id);
    }
    
    @Override
    public List<WorkRule> findByUser(User user) {
        return workRules.values().stream()
            .filter(rule -> rule.user().equals(user))
            .toList();
    }
    
    @Override
    public List<WorkRule> findActiveByUserAndDate(User user, LocalDate targetDate) {
        return workRules.values().stream()
            .filter(rule -> rule.user().equals(user))
            .filter(rule -> isActiveDuring(rule, targetDate))
            .toList();
    }
    
    @Override
    public void deleteById(Long id) {
        workRules.remove(id);
    }
    
    /**
     * 指定日に勤怠ルールが有効かチェックする
     */
    private boolean isActiveDuring(WorkRule workRule, LocalDate targetDate) {
        LocalDate startDate = workRule.membershipStartDate();
        LocalDate endDate = workRule.membershipEndDate();
        
        return !targetDate.isBefore(startDate) && !targetDate.isAfter(endDate);
    }
    
    /**
     * テスト用のクリアメソッド
     */
    public void clear() {
        workRules.clear();
        idGenerator.set(1);
    }
}