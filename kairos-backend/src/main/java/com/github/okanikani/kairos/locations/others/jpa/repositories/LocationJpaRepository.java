package com.github.okanikani.kairos.locations.others.jpa.repositories;

import com.github.okanikani.kairos.locations.others.jpa.entities.LocationJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 位置情報のSpring Data JPAリポジトリ
 * 
 * 業務要件: 位置情報の永続化操作とクエリ機能を提供
 */
@Repository
public interface LocationJpaRepository extends JpaRepository<LocationJpaEntity, Long> {
    
    // クエリパラメータ名の定数定義
    String PARAM_USER_ID = "userId";
    String PARAM_START_DATE_TIME = "startDateTime";
    String PARAM_END_DATE_TIME = "endDateTime";

    /**
     * ユーザーIDで位置情報を検索（記録日時の降順）
     * 
     * @param userId ユーザーID
     * @return 該当ユーザーの位置情報一覧
     */
    @Query("SELECT l FROM LocationJpaEntity l WHERE l.userId = :userId ORDER BY l.recordedAt DESC")
    List<LocationJpaEntity> findByUserIdOrderByRecordedAtDesc(@Param(PARAM_USER_ID) String userId);

    /**
     * ユーザーIDと期間で位置情報を検索
     * 
     * @param userId ユーザーID
     * @param startDateTime 開始日時
     * @param endDateTime 終了日時
     * @return 該当期間の位置情報一覧
     */
    @Query("SELECT l FROM LocationJpaEntity l WHERE l.userId = :userId AND l.recordedAt BETWEEN :startDateTime AND :endDateTime ORDER BY l.recordedAt")
    List<LocationJpaEntity> findByUserIdAndRecordedAtBetween(@Param(PARAM_USER_ID) String userId,
                                                             @Param(PARAM_START_DATE_TIME) LocalDateTime startDateTime,
                                                             @Param(PARAM_END_DATE_TIME) LocalDateTime endDateTime);

    /**
     * ユーザーIDと期間で位置情報をページネーション付きで検索
     * 
     * @param userId ユーザーID
     * @param startDateTime 開始日時
     * @param endDateTime 終了日時
     * @param pageable ページング情報
     * @return 該当期間の位置情報ページ
     */
    @Query("SELECT l FROM LocationJpaEntity l WHERE l.userId = :userId AND l.recordedAt BETWEEN :startDateTime AND :endDateTime")
    Page<LocationJpaEntity> findByUserIdAndRecordedAtBetween(@Param(PARAM_USER_ID) String userId,
                                                             @Param(PARAM_START_DATE_TIME) LocalDateTime startDateTime,
                                                             @Param(PARAM_END_DATE_TIME) LocalDateTime endDateTime,
                                                             Pageable pageable);

    /**
     * ユーザーIDで記録日時のみを取得（勤怠表生成用）
     * 
     * @param userId ユーザーID
     * @param startDateTime 開始日時
     * @param endDateTime 終了日時
     * @return 記録日時のリスト
     */
    @Query("SELECT l.recordedAt FROM LocationJpaEntity l WHERE l.userId = :userId AND l.recordedAt BETWEEN :startDateTime AND :endDateTime ORDER BY l.recordedAt")
    List<LocalDateTime> findRecordedAtByUserIdAndPeriod(@Param(PARAM_USER_ID) String userId,
                                                        @Param(PARAM_START_DATE_TIME) LocalDateTime startDateTime,
                                                        @Param(PARAM_END_DATE_TIME) LocalDateTime endDateTime);

    /**
     * 期間で位置情報を検索（管理者用）
     * 
     * @param startDateTime 開始日時
     * @param endDateTime 終了日時
     * @return 該当期間の全位置情報一覧
     */
    @Query("SELECT l FROM LocationJpaEntity l WHERE l.recordedAt BETWEEN :startDateTime AND :endDateTime ORDER BY l.recordedAt, l.userId")
    List<LocationJpaEntity> findByRecordedAtBetween(@Param(PARAM_START_DATE_TIME) LocalDateTime startDateTime,
                                                    @Param(PARAM_END_DATE_TIME) LocalDateTime endDateTime);

    /**
     * ユーザーの最新位置情報を取得
     * 
     * @param userId ユーザーID
     * @return 最新の位置情報（存在しない場合はnull）
     */
    @Query("SELECT l FROM LocationJpaEntity l WHERE l.userId = :userId ORDER BY l.recordedAt DESC LIMIT 1")
    LocationJpaEntity findLatestByUserId(@Param(PARAM_USER_ID) String userId);
}