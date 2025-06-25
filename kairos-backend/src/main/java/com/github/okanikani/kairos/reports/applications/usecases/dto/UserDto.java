package com.github.okanikani.kairos.reports.applications.usecases.dto;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import java.util.Objects;

/**
 * ユーザー情報DTO
 * @param userId ユーザーID
 */
public record UserDto(String userId) {
    public UserDto {
        Objects.requireNonNull(userId, "userIdは必須です");
        if (userId.isBlank()) {
            throw new ValidationException("userIdは空文字列にできません");
        }
    }
}