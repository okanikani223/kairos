package com.github.okanikani.kairos.rules.domains.models.entities;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.rules.domains.models.vos.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * 勤怠ルールエンティティ
 * 勤怠先の勤務規則と位置情報を管理する
 */
public record WorkRule(
        Long id,                          // 一意識別子（新規作成時はnull）
        Long workPlaceId,                 // 勤怠先ID
        double latitude,                  // 勤怠先の緯度（-90.0 ～ 90.0）
        double longitude,                 // 勤怠先の経度（-180.0 ～ 180.0）
        User user,                        // ユーザー
        LocalTime standardStartTime,      // 規定勤怠開始時刻
        LocalTime standardEndTime,        // 規定勤怠終了時刻
        LocalTime breakStartTime,         // 規定休憩開始時刻
        LocalTime breakEndTime,           // 規定休憩終了時刻
        LocalDate membershipStartDate,    // 所属開始日
        LocalDate membershipEndDate       // 所属終了日
) {
    public WorkRule {
        // 業務ルール: 勤怠先IDは必須
        if (workPlaceId == null) {
            throw new ValidationException("勤怠先IDは必須です");
        }
        
        // 業務ルール: 緯度は-90.0～90.0の範囲内である必要がある
        if (latitude < -90.0 || latitude > 90.0) {
            throw new ValidationException(
                String.format("緯度は-90.0～90.0の範囲で指定してください: %f", latitude)
            );
        }
        
        // 業務ルール: 経度は-180.0～180.0の範囲内である必要がある
        if (longitude < -180.0 || longitude > 180.0) {
            throw new ValidationException(
                String.format("経度は-180.0～180.0の範囲で指定してください: %f", longitude)
            );
        }
        
        // 業務ルール: ユーザーは必須
        Objects.requireNonNull(user, "ユーザーは必須です");
        
        // 業務ルール: 規定勤怠時刻は必須
        Objects.requireNonNull(standardStartTime, "規定勤怠開始時刻は必須です");
        Objects.requireNonNull(standardEndTime, "規定勤怠終了時刻は必須です");
        
        // 業務ルール: 規定休憩時刻（nullの場合は休憩なし）
        if (breakStartTime != null && breakEndTime == null) {
            Objects.requireNonNull(breakEndTime, "規定休憩終了時刻は必須です");
        }
        if (breakEndTime != null && breakStartTime == null) {
            Objects.requireNonNull(breakStartTime, "規定休憩開始時刻は必須です");
        }
        
        // 業務ルール: 所属期間は必須
        Objects.requireNonNull(membershipStartDate, "所属開始日は必須です");
        Objects.requireNonNull(membershipEndDate, "所属終了日は必須です");
        
        // 業務ルール: 所属開始日は所属終了日より前である必要がある
        if (membershipStartDate.isAfter(membershipEndDate)) {
            throw new ValidationException("所属開始日は所属終了日より前である必要があります");
        }
        
        // 業務ルール: 規定勤怠開始時刻と終了時刻は異なる必要がある（日をまたぐ勤務も許可）
        if (standardStartTime.equals(standardEndTime)) {
            throw new ValidationException("規定勤怠開始時刻と規定勤怠終了時刻は異なる時刻である必要があります");
        }
        
        // 業務ルール: 規定休憩開始時刻と終了時刻は異なる必要がある（日をまたぐ休憩も許可）
        if (breakStartTime != null && breakEndTime != null && breakStartTime.equals(breakEndTime)) {
            throw new ValidationException("規定休憩開始時刻と規定休憩終了時刻は異なる時刻である必要があります");
        }
    }
}