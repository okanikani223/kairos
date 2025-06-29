package com.github.okanikani.kairos.reportcreationrules.domains.models.repositories;

import com.github.okanikani.kairos.reportcreationrules.domains.models.entities.ReportCreationRule;
import com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User;

import java.util.List;

/**
 * 勤怠作成ルールリポジトリインターフェース
 * 勤怠作成ルールの永続化を担当
 */
public interface ReportCreationRuleRepository {
    
    /**
     * 勤怠作成ルールを保存する
     * @param reportCreationRule 保存する勤怠作成ルール
     * @return 保存された勤怠作成ルール（IDが設定される）
     */
    ReportCreationRule save(ReportCreationRule reportCreationRule);
    
    /**
     * IDで勤怠作成ルールを検索する
     * @param id 勤怠作成ルールID
     * @return 勤怠作成ルール（存在しない場合はnull）
     */
    ReportCreationRule findById(Long id);
    
    /**
     * ユーザーの勤怠作成ルールを取得する
     * @param user ユーザー
     * @return 勤怠作成ルール（存在しない場合はnull）
     */
    ReportCreationRule findByUser(User user);
    
    /**
     * 勤怠作成ルールを削除する
     * @param id 削除する勤怠作成ルールID
     */
    void deleteById(Long id);
    
    /**
     * 全ての勤怠作成ルールを取得する
     * @return 全勤怠作成ルール一覧
     */
    List<ReportCreationRule> findAll();
}