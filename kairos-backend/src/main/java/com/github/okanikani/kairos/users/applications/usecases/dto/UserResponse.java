package com.github.okanikani.kairos.users.applications.usecases.dto;

import java.time.LocalDateTime;

/**
 * ユーザーレスポンスDTO
 * パスワード情報は含まない
 */
public record UserResponse(
    Long id,
    String userId,
    String username,
    String email,
    String role,
    String roleDisplayName,
    boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt
) {}