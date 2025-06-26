package com.github.okanikani.kairos.security;

import com.github.okanikani.kairos.users.domains.models.entities.Role;
import com.github.okanikani.kairos.users.domains.models.entities.User;
import com.github.okanikani.kairos.users.domains.models.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CustomUserDetailsServiceのテスト
 * Spring Security統合機能の動作を確認
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("CustomUserDetailsService")
class CustomUserDetailsServiceTest {
    
    @MockitoBean
    private UserRepository userRepository;
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User(
            1L,
            "testuser123",
            "テストユーザー",
            "test@example.com",
            "hashedPassword",
            Role.USER,
            true,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusHours(1)
        );
    }
    
    @Test
    @DisplayName("loadUserByUsername_正常ケース_UserDetailsが返される")
    void loadUserByUsername_正常ケース_UserDetailsが返される() {
        // Given
        when(userRepository.findByUserId("testuser123")).thenReturn(Optional.of(testUser));
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser123");
        
        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser123");
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        
        // 権限の確認
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("USER");
        
        verify(userRepository).findByUserId("testuser123");
    }
    
    @Test
    @DisplayName("loadUserByUsername_管理者ユーザー_ADMIN権限が付与される")
    void loadUserByUsername_管理者ユーザー_ADMIN権限が付与される() {
        // Given
        User adminUser = new User(
            2L, "admin123", "管理者", "admin@example.com",
            "hashedPassword", Role.ADMIN, true, LocalDateTime.now(), null
        );
        
        when(userRepository.findByUserId("admin123")).thenReturn(Optional.of(adminUser));
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin123");
        
        // Then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ADMIN");
    }
    
    @Test
    @DisplayName("loadUserByUsername_存在しないユーザー_例外が発生する")
    void loadUserByUsername_存在しないユーザー_例外が発生する() {
        // Given
        when(userRepository.findByUserId("nonexistent")).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("ユーザーが見つかりません: nonexistent");
        
        verify(userRepository).findByUserId("nonexistent");
    }
    
    @Test
    @DisplayName("loadUserByUsername_無効化されたユーザー_例外が発生する")
    void loadUserByUsername_無効化されたユーザー_例外が発生する() {
        // Given
        User disabledUser = testUser.disable();
        when(userRepository.findByUserId("testuser123")).thenReturn(Optional.of(disabledUser));
        
        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("testuser123"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("アカウントが無効化されています: testuser123");
        
        verify(userRepository).findByUserId("testuser123");
    }
    
    @Test
    @DisplayName("loadUserByUsername_CustomUserPrincipal_元のUserエンティティにアクセス可能")
    void loadUserByUsername_CustomUserPrincipal_元のUserエンティティにアクセス可能() {
        // Given
        when(userRepository.findByUserId("testuser123")).thenReturn(Optional.of(testUser));
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser123");
        
        // Then
        assertThat(userDetails).isInstanceOf(CustomUserPrincipal.class);
        
        CustomUserPrincipal principal = (CustomUserPrincipal) userDetails;
        User originalUser = principal.getUser();
        
        assertThat(originalUser).isNotNull();
        assertThat(originalUser.id()).isEqualTo(1L);
        assertThat(originalUser.userId()).isEqualTo("testuser123");
        assertThat(originalUser.username()).isEqualTo("テストユーザー");
        assertThat(originalUser.email()).isEqualTo("test@example.com");
        assertThat(originalUser.role()).isEqualTo(Role.USER);
    }
    
    @Test
    @DisplayName("CustomUserPrincipal_AccountStatus_適切なステータスが返される")
    void CustomUserPrincipal_AccountStatus_適切なステータスが返される() {
        // Given
        when(userRepository.findByUserId("testuser123")).thenReturn(Optional.of(testUser));
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser123");
        
        // Then
        // 現在の実装では以下はすべてtrueを返す
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        
        // enabledはユーザーエンティティの値に依存
        assertThat(userDetails.isEnabled()).isEqualTo(testUser.enabled());
    }
    
    @Test
    @DisplayName("loadUserByUsername_nullユーザーID_例外が発生する")
    void loadUserByUsername_nullユーザーID_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(null))
            .isInstanceOf(UsernameNotFoundException.class);
    }
    
    @Test
    @DisplayName("loadUserByUsername_空のユーザーID_例外が発生する")
    void loadUserByUsername_空のユーザーID_例外が発生する() {
        // Given
        when(userRepository.findByUserId("")).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(""))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("ユーザーが見つかりません:");
    }
    
    @Test
    @DisplayName("loadUserByUsername_システム管理者ユーザー_SYSTEM_ADMIN権限が付与される")
    void loadUserByUsername_システム管理者ユーザー_SYSTEM_ADMIN権限が付与される() {
        // Given
        User systemAdminUser = new User(
            3L, "sysadmin123", "システム管理者", "sysadmin@example.com",
            "hashedPassword", Role.SYSTEM_ADMIN, true, LocalDateTime.now(), null
        );
        
        when(userRepository.findByUserId("sysadmin123")).thenReturn(Optional.of(systemAdminUser));
        
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("sysadmin123");
        
        // Then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("SYSTEM_ADMIN");
    }
}