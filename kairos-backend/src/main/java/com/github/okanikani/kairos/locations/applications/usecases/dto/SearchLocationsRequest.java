package com.github.okanikani.kairos.locations.applications.usecases.dto;

import java.time.LocalDateTime;

/**
 * 位置情報検索リクエスト
 * 
 * @param startDateTime 検索開始日時（この日時以降）
 * @param endDateTime 検索終了日時（この日時以前）
 */
public record SearchLocationsRequest(
    LocalDateTime startDateTime,
    LocalDateTime endDateTime
) {
    /**
     * バリデーション用コンストラクタ
     */
    public SearchLocationsRequest {
        // 必須パラメータのnullチェック
        if (startDateTime == null) {
            throw new NullPointerException("startDateTimeは必須です");
        }
        if (endDateTime == null) {
            throw new NullPointerException("endDateTimeは必須です");
        }
        
        // 開始日時が終了日時より後の場合はエラー
        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("開始日時は終了日時より前である必要があります");
        }
    }
}