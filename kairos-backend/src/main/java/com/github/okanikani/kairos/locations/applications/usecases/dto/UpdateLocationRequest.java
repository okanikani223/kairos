package com.github.okanikani.kairos.locations.applications.usecases.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 位置情報更新リクエスト
 * 位置情報の緯度、経度、記録日時を更新する
 */
public record UpdateLocationRequest(
    double latitude,          // 緯度（-90.0 ～ 90.0）
    double longitude,         // 経度（-180.0 ～ 180.0）
    LocalDateTime recordedAt  // 記録日時
) {
    public UpdateLocationRequest {
        Objects.requireNonNull(recordedAt, "記録日時は必須です");
    }
}