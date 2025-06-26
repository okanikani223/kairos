package com.github.okanikani.kairos.reportcreationrules.others.jpa.repositories;

import com.github.okanikani.kairos.reportcreationrules.others.jpa.entities.ReportCreationRuleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 勤怠作成ルールのSpring Data JPAリポジトリ
 * 
 * 業務要件: 勤怠作成ルールの永続化操作とクエリ機能を提供
 */
@Repository
public interface ReportCreationRuleJpaRepository extends JpaRepository<ReportCreationRuleJpaEntity, Long> {

    /**
     * ユーザーIDで勤怠作成ルールを検索
     * 業務要件: ユーザーごとに一つの勤怠作成ルールのみ登録可能
     * 
     * @param userId ユーザーID
     * @return 勤怠作成ルール（存在しない場合はEmpty）
     */
    @Query("SELECT r FROM ReportCreationRuleJpaEntity r WHERE r.userId = :userId")
    Optional<ReportCreationRuleJpaEntity> findByUserId(@Param("userId") String userId);

    /**
     * ユーザーIDで勤怠作成ルールが存在するかチェック
     * 
     * @param userId ユーザーID
     * @return 存在する場合true
     */
    @Query("SELECT COUNT(r) > 0 FROM ReportCreationRuleJpaEntity r WHERE r.userId = :userId")
    boolean existsByUserId(@Param("userId") String userId);

    /**
     * ユーザーIDで勤怠作成ルールが存在するかチェック（ID除外）
     * 業務要件: 更新時の重複チェック用（自分自身を除外）
     * 
     * @param userId ユーザーID
     * @param excludeId 除外するID
     * @return 存在する場合true
     */
    @Query("SELECT COUNT(r) > 0 FROM ReportCreationRuleJpaEntity r WHERE r.userId = :userId AND r.id != :excludeId")
    boolean existsByUserIdExcludingId(@Param("userId") String userId, @Param("excludeId") Long excludeId);
}