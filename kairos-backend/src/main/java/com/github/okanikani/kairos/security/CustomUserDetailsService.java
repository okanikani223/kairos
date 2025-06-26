package com.github.okanikani.kairos.security;

import com.github.okanikani.kairos.users.domains.models.entities.User;
import com.github.okanikani.kairos.users.domains.models.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Spring Security用のユーザー詳細サービス実装
 * JWT認証とSpring Securityコンテキストの連携を担当
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepositoryは必須です");
    }
    
    /**
     * ユーザーIDでユーザー詳細を取得
     * Spring Securityの認証コンテキストで使用される
     * 
     * @param userId ユーザーID
     * @return UserDetails Spring Security形式のユーザー情報
     * @throws UsernameNotFoundException ユーザーが見つからない場合
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + userId));
        
        if (!user.isAuthenticatable()) {
            throw new UsernameNotFoundException("アカウントが無効化されています: " + userId);
        }
        
        return new CustomUserPrincipal(user);
    }
}