package com.github.okanikani.kairos.rules.others.jpa.repositories;

import com.github.okanikani.kairos.rules.others.jpa.entities.WorkRuleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 勤怠ルールのSpring Data JPAリポジトリ
 * 
 * 業務要件: 勤怠ルールの永続化操作とクエリ機能を提供
 */
@Repository
public interface WorkRuleJpaRepository extends JpaRepository<WorkRuleJpaEntity, Long> {

    /**
     * ユーザーIDで勤怠ルールを検索（所属開始日の降順）
     * 
     * @param userId ユーザーID
     * @return 該当ユーザーの勤怠ルール一覧
     */
    @Query("SELECT w FROM WorkRuleJpaEntity w WHERE w.userId = :userId ORDER BY w.membershipStartDate DESC")
    List<WorkRuleJpaEntity> findByUserIdOrderByMembershipStartDateDesc(@Param("userId") String userId);

    /**
     * 勤怠先IDで勤怠ルールを検索
     * 
     * @param workPlaceId 勤怠先ID
     * @return 該当勤怠先の勤怠ルール一覧
     */
    @Query("SELECT w FROM WorkRuleJpaEntity w WHERE w.workPlaceId = :workPlaceId ORDER BY w.membershipStartDate")
    List<WorkRuleJpaEntity> findByWorkPlaceIdOrderByMembershipStartDate(@Param("workPlaceId") Long workPlaceId);

    /**
     * ユーザーIDと有効日で勤怠ルールを検索
     * 業務要件: 指定日時点で有効な勤怠ルールを取得
     * 
     * @param userId ユーザーID
     * @param effectiveDate 有効日
     * @return 有効な勤怠ルール（存在しない場合はEmpty）
     */
    @Query("SELECT w FROM WorkRuleJpaEntity w WHERE w.userId = :userId AND w.membershipStartDate <= :effectiveDate AND w.membershipEndDate >= :effectiveDate")
    Optional<WorkRuleJpaEntity> findByUserIdAndEffectiveDate(@Param("userId") String userId, 
                                                             @Param("effectiveDate") LocalDate effectiveDate);

    /**
     * ユーザーIDと期間で重複する勤怠ルールを検索
     * 業務要件: 所属期間の重複チェック用
     * 
     * @param userId ユーザーID
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 重複する勤怠ルール一覧
     */
    @Query("SELECT w FROM WorkRuleJpaEntity w WHERE w.userId = :userId AND " +
           "((w.membershipStartDate <= :endDate) AND (w.membershipEndDate >= :startDate))")
    List<WorkRuleJpaEntity> findOverlappingRules(@Param("userId") String userId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * ユーザーIDと期間で重複する勤怠ルールを検索（ID除外）
     * 業務要件: 更新時の重複チェック用（自分自身を除外）
     * 
     * @param userId ユーザーID
     * @param startDate 開始日
     * @param endDate 終了日
     * @param excludeId 除外するID
     * @return 重複する勤怠ルール一覧
     */
    @Query("SELECT w FROM WorkRuleJpaEntity w WHERE w.userId = :userId AND w.id != :excludeId AND " +
           "((w.membershipStartDate <= :endDate) AND (w.membershipEndDate >= :startDate))")
    List<WorkRuleJpaEntity> findOverlappingRulesExcludingId(@Param("userId") String userId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate,
                                                            @Param("excludeId") Long excludeId);

    /**
     * 有効期間内の勤怠ルールを検索（管理者用）
     * 
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 該当期間に有効な勤怠ルール一覧
     */
    @Query("SELECT w FROM WorkRuleJpaEntity w WHERE " +
           "((w.membershipStartDate <= :endDate) AND (w.membershipEndDate >= :startDate)) " +
           "ORDER BY w.userId, w.membershipStartDate")
    List<WorkRuleJpaEntity> findActiveRulesInPeriod(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
}