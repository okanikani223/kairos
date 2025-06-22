package com.github.okanikani223.kairos.reports.domains.models.vos;

import java.util.Objects;

/**
 * 勤怠表の所有者を表わすクラス
 * @param userId 所有者(ユーザー)のID
 */
public record User(String userId) {
    public User {
        Objects.requireNonNull(userId, "userIdは必須です");
        if (userId.isBlank()) {
            throw new IllegalArgumentException("userIdは空文字列にできません");
        }
    }
}
