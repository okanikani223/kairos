package com.github.okanikani.kairos.locations.domains.models.repositories;

import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.vos.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 位置情報のCRUDを担当するインターフェース
 */
public interface LocationRepository {
    /**
     * 位置情報を保存する
     * @param location 保存する位置情報（IDがnullの場合は新規作成、IDがある場合は更新）
     * @return 保存された位置情報（DBで採番されたIDを含む）
     */
    Location save(Location location);

    /**
     * 指定した日時範囲の位置情報を取得する
     * @param startDateTime 開始日時
     * @param endDateTime 終了日時
     * @return 指定範囲の位置情報リスト（記録日時の昇順）
     */
    List<Location> findByDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * 指定したユーザーの指定した日時範囲の位置情報を取得する
     * @param user 対象ユーザー
     * @param startDateTime 開始日時
     * @param endDateTime 終了日時
     * @return 指定ユーザーの指定範囲の位置情報リスト（記録日時の昇順）
     */
    List<Location> findByUserAndDateTimeRange(User user, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * 指定した日の位置情報を取得する
     * @param date 対象日（時刻は00:00:00から23:59:59まで）
     * @return 指定日の位置情報リスト（記録日時の昇順）
     */
    List<Location> findByDate(LocalDateTime date);

    /**
     * すべての位置情報を取得する
     * @return 全位置情報リスト（記録日時の昇順）
     */
    List<Location> findAll();

    /**
     * 指定したユーザーの位置情報を全て取得する
     * @param user 対象ユーザー
     * @return 指定ユーザーの位置情報リスト（記録日時の昇順）
     */
    List<Location> findByUser(User user);

    /**
     * IDで位置情報を取得する
     * @param id 位置情報のID
     * @return 位置情報（存在しない場合はnull）
     */
    Location findById(Long id);

    /**
     * 位置情報を削除する
     * @param id 削除する位置情報のID
     */
    void deleteById(Long id);
}