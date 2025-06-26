package com.github.okanikani.kairos.users.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.security.JwtService;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginResponse;
import com.github.okanikani.kairos.users.applications.usecases.mapper.UserMapper;
import com.github.okanikani.kairos.users.domains.models.entities.User;
import com.github.okanikani.kairos.users.domains.models.repositories.UserRepository;
import com.github.okanikani.kairos.users.domains.services.PasswordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 認証ユースケース
 * ユーザーのログイン処理とJWTトークン生成を担当
 */
@Service
public class AuthenticationUseCase {
    
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final long jwtExpirationMs;
    
    public AuthenticationUseCase(
            UserRepository userRepository,
            PasswordService passwordService,
            JwtService jwtService,
            @Value("${jwt.expiration}") long jwtExpirationMs) {
        
        this.userRepository = Objects.requireNonNull(userRepository, "userRepositoryは必須です");
        this.passwordService = Objects.requireNonNull(passwordService, "passwordServiceは必須です");
        this.jwtService = Objects.requireNonNull(jwtService, "jwtServiceは必須です");
        this.jwtExpirationMs = jwtExpirationMs;
    }
    
    /**
     * ユーザー認証とログイン処理
     * 
     * @param request ログインリクエスト
     * @return ログインレスポンス（JWTトークン含む）
     * @throws ValidationException 入力値が不正な場合
     * @throws AuthorizationException 認証失敗の場合
     */
    public LoginResponse authenticate(LoginRequest request) {
        Objects.requireNonNull(request, "リクエストは必須です");
        
        // 1. 入力値のバリデーション
        validateLoginRequest(request);
        
        // 2. ユーザーの存在確認
        User user = userRepository.findByUserId(request.userId())
            .orElseThrow(() -> new AuthorizationException("ユーザーIDまたはパスワードが正しくありません"));
        
        // 3. アカウント有効性の確認
        if (!user.isAuthenticatable()) {
            throw new AuthorizationException("アカウントが無効化されています");
        }
        
        // 4. パスワード認証
        boolean passwordMatches = passwordService.verifyPassword(request.password(), user.hashedPassword());
        if (!passwordMatches) {
            throw new AuthorizationException("ユーザーIDまたはパスワードが正しくありません");
        }
        
        // 5. パスワードのアップグレードが必要かチェック
        User updatedUser = user;
        if (passwordService.isPasswordUpgradeRequired(user.hashedPassword())) {
            String newHashedPassword = passwordService.upgradePassword(request.password());
            updatedUser = new User(
                user.id(),
                user.userId(),
                user.username(),
                user.email(),
                newHashedPassword,
                user.role(),
                user.enabled(),
                user.createdAt(),
                user.lastLoginAt()
            );
        }
        
        // 6. 最終ログイン日時を更新
        User userWithLogin = updatedUser.withLastLogin(LocalDateTime.now());
        User savedUser = userRepository.save(userWithLogin);
        
        // 7. JWTトークンを生成
        String accessToken = jwtService.generateToken(savedUser.userId());
        
        // 8. レスポンスを作成
        return LoginResponse.jwt(
            accessToken,
            jwtExpirationMs / 1000, // ミリ秒から秒に変換
            UserMapper.toUserResponse(savedUser)
        );
    }
    
    /**
     * ログインリクエストの基本バリデーション
     * 
     * @param request 検証対象のリクエスト
     * @throws ValidationException バリデーションエラーの場合
     */
    private void validateLoginRequest(LoginRequest request) {
        if (request.userId() == null || request.userId().trim().isEmpty()) {
            throw new ValidationException("ユーザーIDは必須です");
        }
        
        if (request.password() == null || request.password().trim().isEmpty()) {
            throw new ValidationException("パスワードは必須です");
        }
    }
}