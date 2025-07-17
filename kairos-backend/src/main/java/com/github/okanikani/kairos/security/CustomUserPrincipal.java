package com.github.okanikani.kairos.security;

import com.github.okanikani.kairos.users.domains.models.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Spring Security用のユーザープリンシパル実装
 * UserエンティティをSpring SecurityのUserDetailsとして利用するためのアダプター
 */
public class CustomUserPrincipal implements UserDetails {
    
    private static final long serialVersionUID = 7239485610273958412L;
    
    private final User user;
    
    public CustomUserPrincipal(User user) {
        this.user = Objects.requireNonNull(user, "ユーザーは必須です");
    }
    
    /**
     * 元のUserエンティティを取得
     * 
     * @return Userエンティティ
     */
    public User getUser() {
        return user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.role().getRawAuthority()));
    }
    
    @Override
    public String getPassword() {
        return user.hashedPassword();
    }
    
    @Override
    public String getUsername() {
        return user.userId();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true; // アカウント期限切れは現在未実装
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true; // アカウントロックは現在未実装
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // パスワード期限切れは現在未実装
    }
    
    @Override
    public boolean isEnabled() {
        return user.enabled();
    }
}