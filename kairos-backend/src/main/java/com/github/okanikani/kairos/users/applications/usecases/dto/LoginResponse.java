package com.github.okanikani.kairos.users.applications.usecases.dto;

/**
 * ログインレスポンスDTO
 */
public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UserResponse user
) {
    
    /**
     * JWTアクセストークンでのレスポンス生成
     * 
     * @param accessToken JWTアクセストークン
     * @param expiresIn 有効期限（秒）
     * @param user ユーザー情報
     * @return ログインレスポンス
     */
    public static LoginResponse jwt(String accessToken, long expiresIn, UserResponse user) {
        return new LoginResponse(accessToken, "Bearer", expiresIn, user);
    }
}