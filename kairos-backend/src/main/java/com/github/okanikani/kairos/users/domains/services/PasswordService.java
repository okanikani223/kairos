package com.github.okanikani.kairos.users.domains.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * パスワード暗号化・検証サービス
 * BCryptを使用したセキュアなパスワード管理を提供
 */
@Service
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder;
    
    public PasswordService() {
        // BCryptPasswordEncoder（強度12）を使用
        // 強度12は現在（2025年）のセキュリティ基準に適合
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }
    
    // テスト用コンストラクタ（依存性注入対応）
    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoderは必須です");
    }
    
    /**
     * パスワードをハッシュ化
     * 
     * @param rawPassword 平文パスワード
     * @return ハッシュ化されたパスワード
     * @throws IllegalArgumentException パスワードがnullまたは空の場合
     */
    public String hashPassword(String rawPassword) {
        Objects.requireNonNull(rawPassword, "パスワードは必須です");
        
        if (rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("パスワードは空にできません");
        }
        
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * パスワードを検証
     * 
     * @param rawPassword 平文パスワード
     * @param hashedPassword ハッシュ化されたパスワード
     * @return パスワードが一致する場合true
     * @throws IllegalArgumentException パラメータがnullまたは空の場合
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        Objects.requireNonNull(rawPassword, "平文パスワードは必須です");
        Objects.requireNonNull(hashedPassword, "ハッシュ化パスワードは必須です");
        
        if (rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("平文パスワードは空にできません");
        }
        
        if (hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("ハッシュ化パスワードは空にできません");
        }
        
        try {
            return passwordEncoder.matches(rawPassword, hashedPassword);
        } catch (Exception e) {
            // パスワード検証中にエラーが発生した場合は false を返す
            // セキュリティのため詳細なエラー情報は隠蔽
            return false;
        }
    }
    
    /**
     * パスワードがアップグレード必要かチェック
     * BCryptの強度が変更された場合などに使用
     * 
     * @param hashedPassword 確認するハッシュ化パスワード
     * @return アップグレードが必要な場合true
     */
    public boolean isPasswordUpgradeRequired(String hashedPassword) {
        Objects.requireNonNull(hashedPassword, "ハッシュ化パスワードは必須です");
        
        if (hashedPassword.trim().isEmpty()) {
            return true;
        }
        
        try {
            return passwordEncoder.upgradeEncoding(hashedPassword);
        } catch (Exception e) {
            // エラーが発生した場合はアップグレードが必要とみなす
            return true;
        }
    }
    
    /**
     * パスワードをアップグレード（再ハッシュ化）
     * 
     * @param rawPassword 平文パスワード
     * @return 新しい強度でハッシュ化されたパスワード
     */
    public String upgradePassword(String rawPassword) {
        return hashPassword(rawPassword);
    }
}