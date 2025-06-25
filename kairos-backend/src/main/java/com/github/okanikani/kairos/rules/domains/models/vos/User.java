package com.github.okanikani.kairos.rules.domains.models.vos;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import java.util.Objects;

/**
 * Rules domain User value object
 * ユーザーを表現する値オブジェクト（rulesドメイン専用）
 */
public record User(
        String userId
) {
    public User {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        if (userId.trim().isEmpty()) {
            throw new ValidationException("ユーザーIDは空文字列であってはいけません");
        }
    }
}