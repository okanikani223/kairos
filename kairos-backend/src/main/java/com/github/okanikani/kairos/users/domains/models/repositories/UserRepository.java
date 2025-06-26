package com.github.okanikani.kairos.users.domains.models.repositories;

import com.github.okanikani.kairos.users.domains.models.entities.User;

import java.util.List;
import java.util.Optional;

/**
 * ユーザーリポジトリインターフェース
 * ユーザーデータの永続化と取得を担当
 */
public interface UserRepository {
    
    /**
     * ユーザーを保存
     * 
     * @param user 保存するユーザー
     * @return 保存されたユーザー（IDが自動採番される）
     */
    User save(User user);
    
    /**
     * IDでユーザーを検索
     * 
     * @param id ユーザーID
     * @return 見つかったユーザー
     */
    Optional<User> findById(Long id);
    
    /**
     * ユーザーIDでユーザーを検索
     * 
     * @param userId ユーザーID（ログイン用）
     * @return 見つかったユーザー
     */
    Optional<User> findByUserId(String userId);
    
    /**
     * メールアドレスでユーザーを検索
     * 
     * @param email メールアドレス
     * @return 見つかったユーザー
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 全ユーザーを取得
     * 
     * @return 全ユーザーのリスト
     */
    List<User> findAll();
    
    /**
     * 有効なユーザーのみを取得
     * 
     * @return 有効なユーザーのリスト
     */
    List<User> findByEnabledTrue();
    
    /**
     * ユーザーIDが既に存在するかチェック
     * 
     * @param userId 確認するユーザーID
     * @return 存在する場合true
     */
    boolean existsByUserId(String userId);
    
    /**
     * メールアドレスが既に存在するかチェック
     * 
     * @param email 確認するメールアドレス
     * @return 存在する場合true
     */
    boolean existsByEmail(String email);
    
    /**
     * ユーザーを削除
     * 
     * @param id 削除するユーザーのID
     */
    void deleteById(Long id);
    
    /**
     * 指定されたユーザーIDのユーザーを削除
     * 
     * @param userId 削除するユーザーのユーザーID
     */
    void deleteByUserId(String userId);
    
    /**
     * ユーザーが存在するかチェック
     * 
     * @param id ユーザーID
     * @return 存在する場合true
     */
    boolean existsById(Long id);
}