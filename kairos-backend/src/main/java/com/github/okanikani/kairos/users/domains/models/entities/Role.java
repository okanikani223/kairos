package com.github.okanikani.kairos.users.domains.models.entities;

/**
 * ユーザーロール列挙型
 * アクセス制御とユーザー権限管理に使用
 */
public enum Role {
    
    /**
     * 一般ユーザー
     * 自分自身の勤怠データのみアクセス可能
     */
    USER("USER", "一般ユーザー"),
    
    /**
     * 管理者
     * 全ユーザーの勤怠データにアクセス可能
     * ユーザー管理機能を使用可能
     */
    ADMIN("ADMIN", "管理者"),
    
    /**
     * システム管理者
     * 全ての機能へのアクセス可能
     * システム設定の変更可能
     */
    SYSTEM_ADMIN("SYSTEM_ADMIN", "システム管理者");
    
    private final String authority;
    private final String displayName;
    
    Role(String authority, String displayName) {
        this.authority = authority;
        this.displayName = displayName;
    }
    
    /**
     * Spring Securityで使用する権限名を取得
     * 
     * @return 権限名（ROLE_プレフィックス付き）
     */
    public String getAuthority() {
        return "ROLE_" + authority;
    }
    
    /**
     * 内部での権限名を取得（ROLE_プレフィックスなし）
     * 
     * @return 内部権限名
     */
    public String getRawAuthority() {
        return authority;
    }
    
    /**
     * 表示用のロール名を取得
     * 
     * @return 表示名
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 管理者権限を持つかチェック
     * 
     * @return 管理者権限がある場合true
     */
    public boolean isAdmin() {
        return this == ADMIN || this == SYSTEM_ADMIN;
    }
    
    /**
     * システム管理者権限を持つかチェック
     * 
     * @return システム管理者権限がある場合true
     */
    public boolean isSystemAdmin() {
        return this == SYSTEM_ADMIN;
    }
    
    /**
     * 他のユーザーのデータにアクセス可能かチェック
     * 
     * @return 他ユーザーのデータアクセス可能な場合true
     */
    public boolean canAccessOtherUsersData() {
        return isAdmin();
    }
    
    /**
     * ユーザー管理機能を使用可能かチェック
     * 
     * @return ユーザー管理可能な場合true
     */
    public boolean canManageUsers() {
        return isAdmin();
    }
    
    /**
     * システム設定を変更可能かチェック
     * 
     * @return システム設定変更可能な場合true
     */
    public boolean canManageSystem() {
        return isSystemAdmin();
    }
}