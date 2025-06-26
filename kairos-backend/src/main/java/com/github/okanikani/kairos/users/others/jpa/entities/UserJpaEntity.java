package com.github.okanikani.kairos.users.others.jpa.entities;

import com.github.okanikani.kairos.users.domains.models.entities.Role;
import com.github.okanikani.kairos.users.domains.models.entities.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ユーザーJPAエンティティ
 * データベース永続化用のユーザー情報
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_users_email", columnList = "email", unique = true)
})
public class UserJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;
    
    @Column(name = "username", nullable = false, length = 100)
    private String username;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "hashed_password", nullable = false, length = 255)
    private String hashedPassword;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    // デフォルトコンストラクタ（JPA必須）
    protected UserJpaEntity() {}
    
    public UserJpaEntity(
            String userId,
            String username,
            String email,
            String hashedPassword,
            Role role,
            Boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime lastLoginAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }
    
    /**
     * ドメインモデルからJPAエンティティに変換
     * 
     * @param user ドメインユーザー
     * @return JPAエンティティ
     */
    public static UserJpaEntity fromDomain(User user) {
        Objects.requireNonNull(user, "ユーザーは必須です");
        
        UserJpaEntity entity = new UserJpaEntity(
            user.userId(),
            user.username(),
            user.email(),
            user.hashedPassword(),
            user.role(),
            user.enabled(),
            user.createdAt(),
            user.lastLoginAt()
        );
        
        if (user.id() != null) {
            entity.id = user.id();
        }
        
        return entity;
    }
    
    /**
     * JPAエンティティからドメインモデルに変換
     * 
     * @return ドメインユーザー
     */
    public User toDomain() {
        return new User(
            this.id,
            this.userId,
            this.username,
            this.email,
            this.hashedPassword,
            this.role,
            this.enabled,
            this.createdAt,
            this.lastLoginAt
        );
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getHashedPassword() {
        return hashedPassword;
    }
    
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserJpaEntity that = (UserJpaEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }
    
    @Override
    public String toString() {
        return "UserJpaEntity{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                ", createdAt=" + createdAt +
                ", lastLoginAt=" + lastLoginAt +
                '}';
    }
}