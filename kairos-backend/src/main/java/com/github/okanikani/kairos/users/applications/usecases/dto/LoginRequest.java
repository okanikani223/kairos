package com.github.okanikani.kairos.users.applications.usecases.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * ログインリクエストDTO
 */
public record LoginRequest(
    
    @NotBlank(message = "ユーザーIDは必須です")
    String userId,
    
    @NotBlank(message = "パスワードは必須です")
    String password
) {}