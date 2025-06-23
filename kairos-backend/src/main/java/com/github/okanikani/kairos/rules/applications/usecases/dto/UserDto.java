package com.github.okanikani.kairos.rules.applications.usecases.dto;

import java.util.Objects;

/**
 * Rules domain User DTO
 * ユーザー情報のデータ転送オブジェクト（rulesドメイン専用）
 */
public record UserDto(
        String userId
) {
    public UserDto {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
    }
}