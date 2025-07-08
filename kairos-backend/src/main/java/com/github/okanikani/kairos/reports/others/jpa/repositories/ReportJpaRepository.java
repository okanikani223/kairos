package com.github.okanikani.kairos.reports.others.jpa.repositories;

import com.github.okanikani.kairos.reports.others.jpa.entities.ReportId;
import com.github.okanikani.kairos.reports.others.jpa.entities.ReportJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * 勤怠表のSpring Data JPAリポジトリ
 * 
 * 業務要件: 勤怠表の永続化操作とクエリ機能を提供
 */
@Repository
public interface ReportJpaRepository extends JpaRepository<ReportJpaEntity, ReportId> {

    String USER_ID_PARAM = "userId";

    /**
     * ユーザーIDで勤怠表を検索
     * 
     * @param userId ユーザーID
     * @return 該当ユーザーの勤怠表一覧
     */
    @Query("SELECT r FROM ReportJpaEntity r WHERE r.id.userId = :userId ORDER BY r.id.yearMonth DESC")
    List<ReportJpaEntity> findByUserId(@Param(USER_ID_PARAM) String userId);

    /**
     * 年月とユーザーIDで勤怠表を検索
     * 
     * @param yearMonth 年月
     * @param userId ユーザーID
     * @return 該当する勤怠表（存在しない場合はEmpty）
     */
    @Query("SELECT r FROM ReportJpaEntity r WHERE r.id.yearMonth = :yearMonth AND r.id.userId = :userId")
    Optional<ReportJpaEntity> findByYearMonthAndUserId(@Param("yearMonth") YearMonth yearMonth, 
                                                       @Param(USER_ID_PARAM) String userId);

    /**
     * 年月で勤怠表を検索（管理者用）
     * 
     * @param yearMonth 年月
     * @return 該当年月の全勤怠表一覧
     */
    @Query("SELECT r FROM ReportJpaEntity r WHERE r.id.yearMonth = :yearMonth ORDER BY r.id.userId")
    List<ReportJpaEntity> findByYearMonth(@Param("yearMonth") YearMonth yearMonth);

    /**
     * ステータスで勤怠表を検索
     * 
     * @param status ステータス
     * @return 該当ステータスの勤怠表一覧
     */
    @Query("SELECT r FROM ReportJpaEntity r WHERE r.status = :status ORDER BY r.id.yearMonth DESC, r.id.userId")
    List<ReportJpaEntity> findByStatus(@Param("status") com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus status);

    /**
     * ユーザーIDとステータスで勤怠表を検索
     * 
     * @param userId ユーザーID
     * @param status ステータス
     * @return 該当する勤怠表一覧
     */
    @Query("SELECT r FROM ReportJpaEntity r WHERE r.id.userId = :userId AND r.status = :status ORDER BY r.id.yearMonth DESC")
    List<ReportJpaEntity> findByUserIdAndStatus(@Param(USER_ID_PARAM) String userId, 
                                                @Param("status") com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus status);

    /**
     * 年月とユーザーIDで勤怠表が存在するかチェック
     * 
     * @param yearMonth 年月
     * @param userId ユーザーID
     * @return 存在する場合true
     */
    @Query("SELECT COUNT(r) > 0 FROM ReportJpaEntity r WHERE r.id.yearMonth = :yearMonth AND r.id.userId = :userId")
    boolean existsByYearMonthAndUserId(@Param("yearMonth") YearMonth yearMonth, @Param(USER_ID_PARAM) String userId);
}