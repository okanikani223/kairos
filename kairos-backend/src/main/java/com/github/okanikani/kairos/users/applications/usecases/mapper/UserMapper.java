package com.github.okanikani.kairos.users.applications.usecases.mapper;

import com.github.okanikani.kairos.users.applications.usecases.dto.UserResponse;
import com.github.okanikani.kairos.users.domains.models.entities.User;

import java.util.Objects;

/**
 * ユーザーマッパー
 * ドメインオブジェクトとDTO間の変換を担当
 */
public class UserMapper {
    
    private UserMapper() {
        // ユーティリティクラスなのでインスタンス化を防ぐ
    }
    
    /**
     * ユーザーエンティティをユーザーレスポンスDTOに変換
     * パスワード情報は含まない
     * 
     * @param user ユーザーエンティティ
     * @return ユーザーレスポンスDTO
     */
    public static UserResponse toUserResponse(User user) {
        Objects.requireNonNull(user, "ユーザーは必須です");
        
        return new UserResponse(
            user.id(),
            user.userId(),
            user.username(),
            user.email(),
            user.role().getRawAuthority(),
            user.role().getDisplayName(),
            user.enabled(),
            user.createdAt(),
            user.lastLoginAt()
        );
    }
}