package com.github.okanikani.kairos.users.domains.models.entities;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * ユーザーエンティティ
 * 認証・認可システムで使用するユーザー情報を管理
 */
public record User(
        Long id,                    // 一意識別子（新規作成時はnull）
        String userId,              // ユーザーID（ログイン用）
        String username,            // 表示名
        String email,               // メールアドレス
        String hashedPassword,      // ハッシュ化されたパスワード
        Role role,                  // ユーザーロール
        boolean enabled,            // アカウント有効フラグ
        LocalDateTime createdAt,    // アカウント作成日時
        LocalDateTime lastLoginAt   // 最終ログイン日時（null可）
) {
    
    // パスワード強度チェック用の正規表現
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    // ユーザーID形式チェック用の正規表現（英数字とハイフン、アンダースコアのみ）
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,50}$");
    
    // メールアドレス形式チェック用の正規表現（簡易版）
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    public User {
        // 必須フィールドのバリデーション
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        Objects.requireNonNull(username, "ユーザー名は必須です");
        Objects.requireNonNull(email, "メールアドレスは必須です");
        Objects.requireNonNull(hashedPassword, "パスワードは必須です");
        Objects.requireNonNull(role, "ロールは必須です");
        Objects.requireNonNull(createdAt, "作成日時は必須です");
        
        // ユーザーIDの形式チェック
        if (!USER_ID_PATTERN.matcher(userId).matches()) {
            throw new ValidationException("ユーザーIDは3-50文字の英数字、ハイフン、アンダースコアのみ使用可能です");
        }
        
        // ユーザー名の長さチェック
        if (username.trim().isEmpty() || username.length() > 100) {
            throw new ValidationException("ユーザー名は1-100文字で入力してください");
        }
        
        // メールアドレスの形式チェック
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("有効なメールアドレスを入力してください");
        }
        
        // メールアドレスの長さチェック
        if (email.length() > 255) {
            throw new ValidationException("メールアドレスは255文字以内で入力してください");
        }
        
        // ハッシュ化済みパスワードの基本チェック（空でないこと）
        if (hashedPassword.trim().isEmpty()) {
            throw new ValidationException("パスワードが設定されていません");
        }
    }
    
    /**
     * 新規ユーザー作成用のファクトリメソッド（デフォルトロール）
     * 
     * @param userId ユーザーID
     * @param username ユーザー名
     * @param email メールアドレス
     * @param rawPassword 平文パスワード（この時点で強度チェック）
     * @param passwordHashFunction パスワードハッシュ化関数
     * @return 新規ユーザーエンティティ
     */
    public static User createNew(
            String userId, 
            String username, 
            String email, 
            String rawPassword,
            java.util.function.Function<String, String> passwordHashFunction) {
        
        return createNewWithRole(userId, username, email, rawPassword, Role.USER, passwordHashFunction);
    }
    
    /**
     * 新規ユーザー作成用のファクトリメソッド（ロール指定）
     * 
     * @param userId ユーザーID
     * @param username ユーザー名
     * @param email メールアドレス
     * @param rawPassword 平文パスワード（この時点で強度チェック）
     * @param role ユーザーロール
     * @param passwordHashFunction パスワードハッシュ化関数
     * @return 新規ユーザーエンティティ
     */
    public static User createNewWithRole(
            String userId, 
            String username, 
            String email, 
            String rawPassword,
            Role role,
            java.util.function.Function<String, String> passwordHashFunction) {
        
        // 平文パスワードの強度チェック
        validatePasswordStrength(rawPassword);
        
        // パスワードのハッシュ化
        String hashedPassword = passwordHashFunction.apply(rawPassword);
        
        return new User(
            null,                           // IDは新規作成時はnull
            userId,
            username,
            email,
            hashedPassword,
            role,                           // 指定されたロール
            true,                           // デフォルトで有効
            LocalDateTime.now(),            // 現在時刻
            null                            // 最終ログイン日時は未設定
        );
    }
    
    /**
     * 最終ログイン日時を更新した新しいインスタンスを返す
     * 
     * @param loginTime ログイン日時
     * @return 最終ログイン日時が更新されたUserインスタンス
     */
    public User withLastLogin(LocalDateTime loginTime) {
        Objects.requireNonNull(loginTime, "ログイン日時は必須です");
        
        return new User(
            this.id,
            this.userId,
            this.username,
            this.email,
            this.hashedPassword,
            this.role,
            this.enabled,
            this.createdAt,
            loginTime
        );
    }
    
    /**
     * アカウント無効化した新しいインスタンスを返す
     * 
     * @return アカウントが無効化されたUserインスタンス
     */
    public User disable() {
        return new User(
            this.id,
            this.userId,
            this.username,
            this.email,
            this.hashedPassword,
            this.role,
            false,  // 無効化
            this.createdAt,
            this.lastLoginAt
        );
    }
    
    /**
     * ロールを変更した新しいインスタンスを返す
     * 
     * @param newRole 新しいロール
     * @return ロールが変更されたUserインスタンス
     */
    public User withRole(Role newRole) {
        Objects.requireNonNull(newRole, "ロールは必須です");
        
        return new User(
            this.id,
            this.userId,
            this.username,
            this.email,
            this.hashedPassword,
            newRole,
            this.enabled,
            this.createdAt,
            this.lastLoginAt
        );
    }
    
    /**
     * パスワード強度の検証
     * 
     * @param password 検証対象のパスワード
     * @throws ValidationException パスワードが要件を満たさない場合
     */
    public static void validatePasswordStrength(String password) {
        Objects.requireNonNull(password, "パスワードは必須です");
        
        if (password.length() < 8) {
            throw new ValidationException("パスワードは8文字以上で入力してください");
        }
        
        if (password.length() > 128) {
            throw new ValidationException("パスワードは128文字以内で入力してください");
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException(
                "パスワードは以下の条件を満たす必要があります：" +
                "大文字、小文字、数字、特殊文字(@$!%*?&)をそれぞれ1文字以上含む"
            );
        }
    }
    
    /**
     * ユーザーが有効で認証可能かチェック
     * 
     * @return 認証可能な場合true
     */
    public boolean isAuthenticatable() {
        return enabled && hashedPassword != null && !hashedPassword.trim().isEmpty();
    }
}