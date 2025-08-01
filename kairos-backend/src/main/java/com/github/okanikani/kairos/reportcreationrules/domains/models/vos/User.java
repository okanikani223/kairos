package com.github.okanikani.kairos.reportcreationrules.domains.models.vos;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import java.util.Objects;

/**
 * ユーザーバリューオブジェクト（勤怠作成ルールドメイン用）
 * DDD原則により各ドメインコンテキスト毎に独自のUserモデルを定義
 */
public record User(
        String userId
) {
    /**
     * コンストラクタ
     * @param userId ユーザーID
     */
    public User {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        if (userId.trim().isEmpty()) {
            throw new ValidationException("ユーザーIDは空文字列であってはいけません");
        }
    }
}