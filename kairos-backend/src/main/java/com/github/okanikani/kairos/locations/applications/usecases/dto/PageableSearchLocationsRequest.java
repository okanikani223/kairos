package com.github.okanikani.kairos.locations.applications.usecases.dto;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import java.time.LocalDateTime;

/**
 * ページネーション対応位置情報検索リクエスト
 * 
 * @param startDateTime 検索開始日時（この日時以降）
 * @param endDateTime 検索終了日時（この日時以前）
 * @param page ページ番号（0から開始）
 * @param size 1ページあたりの件数
 */
public record PageableSearchLocationsRequest(
    LocalDateTime startDateTime,
    LocalDateTime endDateTime,
    int page,
    int size
) {
    /**
     * バリデーション用コンストラクタ
     */
    public PageableSearchLocationsRequest {
        // 必須パラメータのnullチェック
        if (startDateTime == null) {
            throw new NullPointerException("startDateTimeは必須です");
        }
        if (endDateTime == null) {
            throw new NullPointerException("endDateTimeは必須です");
        }
        
        // 開始日時が終了日時より後の場合はエラー
        if (startDateTime.isAfter(endDateTime)) {
            throw new ValidationException("開始日時は終了日時より前である必要があります");
        }
        
        // ページネーションパラメータのバリデーション
        if (page < 0) {
            throw new ValidationException("ページ番号は0以上である必要があります");
        }
        if (size <= 0) {
            throw new ValidationException("ページサイズは1以上である必要があります");
        }
        if (size > 100) {
            throw new ValidationException("ページサイズは100以下である必要があります");
        }
    }
}