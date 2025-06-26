package com.github.okanikani.kairos.users.applications.usecases.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ユーザー登録リクエストDTO
 */
public record RegisterRequest(
    
    @NotBlank(message = "ユーザーIDは必須です")
    @Size(min = 3, max = 50, message = "ユーザーIDは3文字以上50文字以下で入力してください")
    String userId,
    
    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 1, max = 100, message = "ユーザー名は1文字以上100文字以下で入力してください")
    String username,
    
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    @Size(max = 255, message = "メールアドレスは255文字以下で入力してください")
    String email,
    
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 128, message = "パスワードは8文字以上128文字以下で入力してください")
    String password,
    
    String role // オプション。指定しない場合はUSERになる
) {}