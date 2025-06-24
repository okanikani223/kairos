package com.github.okanikani.kairos.rules.domains.service;

import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 勤怠ルールドメインサービス
 * 勤怠ルールのビジネスロジックを管理
 */
@Service
public class WorkRuleDomainService {
    
    /**
     * 指定された期間が既存の勤怠ルールと重複しているかチェックする
     * @param existingRules 既存の勤怠ルール一覧
     * @param newStartDate 新規登録する所属開始日
     * @param newEndDate 新規登録する所属終了日
     * @return 重複している場合true
     */
    public boolean hasOverlappingPeriod(List<WorkRule> existingRules, LocalDate newStartDate, LocalDate newEndDate) {
        return existingRules.stream()
            .anyMatch(existingRule -> isPeriodsOverlapping(
                existingRule.membershipStartDate(), 
                existingRule.membershipEndDate(),
                newStartDate, 
                newEndDate
            ));
    }
    
    /**
     * 2つの期間が重複しているかチェックする
     * @param start1 期間1の開始日
     * @param end1 期間1の終了日
     * @param start2 期間2の開始日
     * @param end2 期間2の終了日
     * @return 重複している場合true
     */
    private boolean isPeriodsOverlapping(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        // 期間の重複判定：
        // 期間1: [start1, end1]
        // 期間2: [start2, end2]
        // 重複しない条件: end1 < start2 または end2 < start1
        // 重複する条件: 上記の否定
        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }
}