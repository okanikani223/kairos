package com.github.okanikani.kairos.locations.domains.models.entities;

import com.github.okanikani.kairos.locations.domains.models.vos.User;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 位置情報を表すエンティティ
 * 一意識別子、GPS座標（緯度・経度）、記録日時、所有者を保持する
 */
public record Location(
        Long id,            // 一意識別子（新規作成時はnull、DB保存後に採番される）
        double latitude,    // 緯度（-90.0 ～ 90.0）
        double longitude,   // 経度（-180.0 ～ 180.0）
        LocalDateTime recordedAt,  // 記録日時
        User user          // 位置情報の所有者
) {
    public Location {
        // IDは新規作成時はnull、DB保存後に採番される
        
        // 業務ルール: 緯度は-90.0～90.0の範囲内である必要がある
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException(
                String.format("緯度は-90.0～90.0の範囲で指定してください: %f", latitude)
            );
        }
        
        // 業務ルール: 経度は-180.0～180.0の範囲内である必要がある
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException(
                String.format("経度は-180.0～180.0の範囲で指定してください: %f", longitude)
            );
        }
        
        Objects.requireNonNull(recordedAt, "記録日時は必須です");
        Objects.requireNonNull(user, "ユーザーは必須です");
    }
}