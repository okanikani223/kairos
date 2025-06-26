package com.github.okanikani.kairos.users.applications.usecases.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * ユーザー登録リクエストDTO
 */
public record RegisterUserRequest(
    
    @NotBlank(message = "ユーザーIDは必須です")
    @Size(min = 3, max = 50, message = "ユーザーIDは3-50文字で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "ユーザーIDは英数字、ハイフン、アンダースコアのみ使用可能です")
    String userId,
    
    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 1, max = 100, message = "ユーザー名は1-100文字で入力してください")
    String username,
    
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    String email,
    
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 128, message = "パスワードは8-128文字で入力してください")
    String password,
    
    @NotBlank(message = "パスワード確認は必須です")
    String confirmPassword
) {}