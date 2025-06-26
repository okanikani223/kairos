package com.github.okanikani.kairos.users.others.jpa.repositories;

import com.github.okanikani.kairos.users.others.jpa.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ユーザーJPAリポジトリ
 * Spring Data JPAを使用したデータベースアクセス
 */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    
    /**
     * ユーザーIDでユーザーを検索
     * 
     * @param userId ユーザーID
     * @return 見つかったユーザー
     */
    Optional<UserJpaEntity> findByUserId(String userId);
    
    /**
     * メールアドレスでユーザーを検索（大文字小文字区別なし）
     * 
     * @param email メールアドレス
     * @return 見つかったユーザー
     */
    @Query("SELECT u FROM UserJpaEntity u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<UserJpaEntity> findByEmailIgnoreCase(@Param("email") String email);
    
    /**
     * 有効なユーザーのみを取得
     * 
     * @return 有効なユーザーのリスト
     */
    List<UserJpaEntity> findByEnabledTrue();
    
    /**
     * ユーザーIDが既に存在するかチェック
     * 
     * @param userId 確認するユーザーID
     * @return 存在する場合true
     */
    boolean existsByUserId(String userId);
    
    /**
     * メールアドレスが既に存在するかチェック（大文字小文字区別なし）
     * 
     * @param email 確認するメールアドレス
     * @return 存在する場合true
     */
    @Query("SELECT COUNT(u) > 0 FROM UserJpaEntity u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);
    
    /**
     * 指定されたユーザーIDのユーザーを削除
     * 
     * @param userId 削除するユーザーのユーザーID
     */
    void deleteByUserId(String userId);
}