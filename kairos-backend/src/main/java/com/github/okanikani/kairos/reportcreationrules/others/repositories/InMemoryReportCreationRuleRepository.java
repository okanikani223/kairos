package com.github.okanikani.kairos.reportcreationrules.others.repositories;

import com.github.okanikani.kairos.reportcreationrules.domains.models.entities.ReportCreationRule;
import com.github.okanikani.kairos.reportcreationrules.domains.models.repositories.ReportCreationRuleRepository;
import com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User;
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
public class InMemoryReportCreationRuleRepository implements ReportCreationRuleRepository {
    
    private final Map<Long, ReportCreationRule> reportCreationRules = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public ReportCreationRule save(ReportCreationRule reportCreationRule) {
        Long id = reportCreationRule.id();
        ReportCreationRule ruleToSave = reportCreationRule;
        if (id == null) {
            // 新規作成の場合は自動生成IDを設定
            id = idGenerator.getAndIncrement();
            ruleToSave = new ReportCreationRule(
                id,
                reportCreationRule.user(),
                reportCreationRule.closingDay(),
                reportCreationRule.timeCalculationUnitMinutes()
            );
        }
        reportCreationRules.put(id, ruleToSave);
        return ruleToSave;
    }
    
    @Override
    public ReportCreationRule findById(Long id) {
        return reportCreationRules.get(id);
    }
    
    @Override
    public ReportCreationRule findByUser(User user) {
        return reportCreationRules.values().stream()
            .filter(rule -> rule.user().equals(user))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public void deleteById(Long id) {
        reportCreationRules.remove(id);
    }
    
    @Override
    public List<ReportCreationRule> findAll() {
        return List.copyOf(reportCreationRules.values());
    }
    
    /**
     * テスト用のクリアメソッド
     */
    public void clear() {
        reportCreationRules.clear();
        idGenerator.set(1);
    }
}