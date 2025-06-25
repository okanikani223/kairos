package com.github.okanikani.kairos.locations.domains.models.vos;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import java.util.Objects;

/**
 * 位置情報ドメインにおけるユーザーを表すバリューオブジェクト
 * @param userId ユーザーID
 */
public record User(String userId) {
    public User {
        Objects.requireNonNull(userId, "userIdは必須です");
        if (userId.isBlank()) {
            throw new ValidationException("userIdは空文字列にできません");
        }
    }
}