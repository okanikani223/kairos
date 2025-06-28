package com.github.okanikani.kairos.rules.domains.models.repositories;

import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.vos.User;

import java.time.LocalDate;
import java.util.List;

/**
 * 勤怠ルールリポジトリインターフェース
 */
public interface WorkRuleRepository {
    
    /**
     * 勤怠ルールを保存する
     * @param workRule 保存する勤怠ルール
     * @return 保存された勤怠ルール（IDが設定される）
     */
    WorkRule save(WorkRule workRule);
    
    /**
     * IDで勤怠ルールを検索する
     * @param id 勤怠ルールID
     * @return 勤怠ルール（存在しない場合はnull）
     */
    WorkRule findById(Long id);
    
    /**
     * ユーザーの勤怠ルール一覧を取得する
     * @param user ユーザー
     * @return 勤怠ルール一覧
     */
    List<WorkRule> findByUser(User user);
    
    /**
     * 指定日時点で有効なユーザーの勤怠ルール一覧を取得する
     * @param user ユーザー
     * @param targetDate 対象日
     * @return 有効な勤怠ルール一覧
     */
    List<WorkRule> findActiveByUserAndDate(User user, LocalDate targetDate);
    
    /**
     * 勤怠ルールを削除する
     * @param id 削除する勤怠ルールID
     */
    void deleteById(Long id);
}