package com.github.okanikani.kairos.reports.applications.usecases.dto;

import java.util.Objects;

/**
 * ユーザー情報DTO
 * @param userId ユーザーID
 */
public record UserDto(String userId) {
    public UserDto {
        Objects.requireNonNull(userId, "userIdは必須です");
        if (userId.isBlank()) {
            throw new IllegalArgumentException("userIdは空文字列にできません");
        }
    }
}