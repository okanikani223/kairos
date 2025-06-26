package com.github.okanikani.kairos.rules.others.jpa.repositories;

import com.github.okanikani.kairos.rules.others.jpa.entities.DefaultWorkRuleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * デフォルト勤怠ルールのSpring Data JPAリポジトリ
 * 
 * 業務要件: デフォルト勤怠ルールの永続化操作とクエリ機能を提供
 */
@Repository
public interface DefaultWorkRuleJpaRepository extends JpaRepository<DefaultWorkRuleJpaEntity, Long> {

    /**
     * ユーザーIDでデフォルト勤怠ルールを検索
     * 
     * @param userId ユーザーID
     * @return 該当ユーザーのデフォルト勤怠ルール一覧
     */
    @Query("SELECT d FROM DefaultWorkRuleJpaEntity d WHERE d.userId = :userId ORDER BY d.workPlaceId")
    List<DefaultWorkRuleJpaEntity> findByUserId(@Param("userId") String userId);

    /**
     * 勤怠先IDでデフォルト勤怠ルールを検索
     * 
     * @param workPlaceId 勤怠先ID
     * @return 該当勤怠先のデフォルト勤怠ルール一覧
     */
    @Query("SELECT d FROM DefaultWorkRuleJpaEntity d WHERE d.workPlaceId = :workPlaceId ORDER BY d.userId")
    List<DefaultWorkRuleJpaEntity> findByWorkPlaceId(@Param("workPlaceId") Long workPlaceId);

    /**
     * ユーザーIDと勤怠先IDでデフォルト勤怠ルールを検索
     * 業務要件: 同一ユーザー・同一勤怠先の組み合わせは一意
     * 
     * @param userId ユーザーID
     * @param workPlaceId 勤怠先ID
     * @return デフォルト勤怠ルール（存在しない場合はEmpty）
     */
    @Query("SELECT d FROM DefaultWorkRuleJpaEntity d WHERE d.userId = :userId AND d.workPlaceId = :workPlaceId")
    Optional<DefaultWorkRuleJpaEntity> findByUserIdAndWorkPlaceId(@Param("userId") String userId, 
                                                                  @Param("workPlaceId") Long workPlaceId);

    /**
     * ユーザーIDと勤怠先IDの組み合わせが存在するかチェック
     * 
     * @param userId ユーザーID
     * @param workPlaceId 勤怠先ID
     * @return 存在する場合true
     */
    @Query("SELECT COUNT(d) > 0 FROM DefaultWorkRuleJpaEntity d WHERE d.userId = :userId AND d.workPlaceId = :workPlaceId")
    boolean existsByUserIdAndWorkPlaceId(@Param("userId") String userId, @Param("workPlaceId") Long workPlaceId);

    /**
     * ユーザーIDと勤怠先IDの組み合わせが存在するかチェック（ID除外）
     * 業務要件: 更新時の重複チェック用（自分自身を除外）
     * 
     * @param userId ユーザーID
     * @param workPlaceId 勤怠先ID
     * @param excludeId 除外するID
     * @return 存在する場合true
     */
    @Query("SELECT COUNT(d) > 0 FROM DefaultWorkRuleJpaEntity d WHERE d.userId = :userId AND d.workPlaceId = :workPlaceId AND d.id != :excludeId")
    boolean existsByUserIdAndWorkPlaceIdExcludingId(@Param("userId") String userId, 
                                                    @Param("workPlaceId") Long workPlaceId, 
                                                    @Param("excludeId") Long excludeId);
}