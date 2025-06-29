package com.github.okanikani.kairos.rules.domains.models.entities;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.rules.domains.models.vos.User;

import java.time.LocalTime;
import java.util.Objects;

/**
 * デフォルト勤怠ルールエンティティ
 * 所属期間がないデフォルトの勤怠ルール設定を表現するドメインモデル
 * 
 * 通常の勤怠ルールと異なり、所属期間の制約がないため、
 * より柔軟な勤怠ルール設定が可能
 */
public record DefaultWorkRule(
        Long id,                          // 一意識別子（新規作成時はnull）
        Long workPlaceId,                 // 勤怠先ID
        double latitude,                  // 勤怠先の緯度（-90.0 ～ 90.0）
        double longitude,                 // 勤怠先の経度（-180.0 ～ 180.0）
        User user,                        // ユーザー
        LocalTime standardStartTime,      // 規定勤怠開始時刻
        LocalTime standardEndTime,        // 規定勤怠終了時刻
        LocalTime breakStartTime,         // 規定休憩開始時刻（null可能）
        LocalTime breakEndTime            // 規定休憩終了時刻（null可能）
) {
    
    /**
     * コンストラクタ
     * 各フィールドのバリデーションを実行
     */
    public DefaultWorkRule {
        // 必須フィールドのnullチェック
        Objects.requireNonNull(workPlaceId, "勤怠先IDは必須です");
        Objects.requireNonNull(user, "ユーザーは必須です");
        Objects.requireNonNull(standardStartTime, "規定勤怠開始時刻は必須です");
        Objects.requireNonNull(standardEndTime, "規定勤怠終了時刻は必須です");
        
        // GPS座標の範囲チェック
        if (latitude < -90.0 || latitude > 90.0) {
            throw new ValidationException("緯度は-90.0から90.0の範囲で指定してください");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new ValidationException("経度は-180.0から180.0の範囲で指定してください");
        }
        
        // 勤怠時刻の論理チェック（日をまたぐ勤務も許可するため、同じ時刻のみ拒否）
        if (standardStartTime.equals(standardEndTime)) {
            throw new ValidationException("規定勤怠開始時刻と規定勤怠終了時刻は異なる時刻である必要があります");
        }
        
        // 休憩時刻のバリデーション
        validateBreakTimes(breakStartTime, breakEndTime);
    }
    
    /**
     * 休憩時刻の妥当性をチェックする
     * 
     * @param breakStartTime 休憩開始時刻
     * @param breakEndTime 休憩終了時刻
     */
    private void validateBreakTimes(LocalTime breakStartTime, LocalTime breakEndTime) {
        // 休憩時刻は両方nullか両方non-nullである必要がある
        if ((breakStartTime == null) != (breakEndTime == null)) {
            throw new ValidationException("休憩時刻は開始時刻と終了時刻の両方を設定するか、両方ともnullにしてください");
        }
        
        // 両方non-nullの場合、開始時刻と終了時刻が異なる必要がある（日をまたぐ休憩も許可）
        if (breakStartTime != null && breakEndTime != null) {
            if (breakStartTime.equals(breakEndTime)) {
                throw new ValidationException("規定休憩開始時刻と規定休憩終了時刻は異なる時刻である必要があります");
            }
        }
    }
}