package com.github.okanikani.kairos.rules.domains.models.repositories;

import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;
import com.github.okanikani.kairos.rules.domains.models.vos.User;

import java.util.List;

/**
 * デフォルト勤怠ルールリポジトリインターフェース
 * デフォルト勤怠ルールの永続化を担当
 */
public interface DefaultWorkRuleRepository {
    
    /**
     * デフォルト勤怠ルールを保存する
     * @param defaultWorkRule 保存するデフォルト勤怠ルール
     */
    void save(DefaultWorkRule defaultWorkRule);
    
    /**
     * IDでデフォルト勤怠ルールを検索する
     * @param id デフォルト勤怠ルールID
     * @return デフォルト勤怠ルール（存在しない場合はnull）
     */
    DefaultWorkRule findById(Long id);
    
    /**
     * ユーザーのデフォルト勤怠ルール一覧を取得する
     * @param user ユーザー
     * @return デフォルト勤怠ルール一覧
     */
    List<DefaultWorkRule> findByUser(User user);
    
    /**
     * 勤怠先IDでデフォルト勤怠ルール一覧を取得する
     * @param workPlaceId 勤怠先ID
     * @return デフォルト勤怠ルール一覧
     */
    List<DefaultWorkRule> findByWorkPlaceId(Long workPlaceId);
    
    /**
     * ユーザーと勤怠先IDの組み合わせでデフォルト勤怠ルールを検索する
     * @param user ユーザー
     * @param workPlaceId 勤怠先ID
     * @return デフォルト勤怠ルール（存在しない場合はnull）
     */
    DefaultWorkRule findByUserAndWorkPlaceId(User user, Long workPlaceId);
    
    /**
     * デフォルト勤怠ルールを削除する
     * @param id 削除するデフォルト勤怠ルールID
     */
    void deleteById(Long id);
    
    /**
     * 全てのデフォルト勤怠ルールを取得する
     * @return 全デフォルト勤怠ルール一覧
     */
    List<DefaultWorkRule> findAll();
}