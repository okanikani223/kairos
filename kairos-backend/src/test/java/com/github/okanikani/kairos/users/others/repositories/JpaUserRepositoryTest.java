package com.github.okanikani.kairos.users.others.repositories;

import com.github.okanikani.kairos.users.domains.models.entities.Role;
import com.github.okanikani.kairos.users.domains.models.entities.User;
import com.github.okanikani.kairos.users.others.jpa.entities.UserJpaEntity;
import com.github.okanikani.kairos.users.others.jpa.repositories.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JpaUserRepositoryのUnit Test
 * 
 * テスト対象: ドメインモデルとJPAエンティティ間の変換と全CRUD操作
 */
@ExtendWith(MockitoExtension.class)
class JpaUserRepositoryTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private JpaUserRepository jpaUserRepository;

    private User testUser;
    private UserJpaEntity testJpaEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        testUser = new User(
                1L,                                     // id
                "test-user-001",                        // userId
                "テストユーザー",                        // username
                "test@example.com",                     // email
                "hashedPassword123",                    // hashedPassword
                Role.USER,                              // role
                true,                                   // enabled
                now,                                    // createdAt
                now                                     // lastLoginAt
        );

        testJpaEntity = new UserJpaEntity(
                "test-user-001",
                "テストユーザー",
                "test@example.com",
                "hashedPassword123",
                Role.USER,
                true,
                now,
                now
        );
    }

    @Test
    void save_正常なUser_正常に保存されドメインモデルが返される() {
        // Given
        when(userJpaRepository.save(any(UserJpaEntity.class))).thenReturn(testJpaEntity);

        // When
        User result = jpaUserRepository.save(testUser);

        // Then
        verify(userJpaRepository).save(any(UserJpaEntity.class));
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo("test-user-001");
        assertThat(result.username()).isEqualTo("テストユーザー");
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.role()).isEqualTo(Role.USER);
        assertThat(result.enabled()).isTrue();
    }

    @Test
    void save_nullのUser_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> jpaUserRepository.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ユーザーは必須です");
    }

    @Test
    void findById_存在するID_対応するUserが返される() {
        // Given
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(testJpaEntity));

        // When
        Optional<User> result = jpaUserRepository.findById(1L);

        // Then
        verify(userJpaRepository).findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo("test-user-001");
        assertThat(result.get().username()).isEqualTo("テストユーザー");
    }

    @Test
    void findById_存在しないID_空のOptionalが返される() {
        // Given
        when(userJpaRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = jpaUserRepository.findById(999L);

        // Then
        verify(userJpaRepository).findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void findById_nullのID_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> jpaUserRepository.findById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("IDは必須です");
    }

    @Test
    void findByUserId_存在するユーザーID_対応するUserが返される() {
        // Given
        when(userJpaRepository.findByUserId("test-user-001")).thenReturn(Optional.of(testJpaEntity));

        // When
        Optional<User> result = jpaUserRepository.findByUserId("test-user-001");

        // Then
        verify(userJpaRepository).findByUserId("test-user-001");
        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo("test-user-001");
    }

    @Test
    void findByUserId_存在しないユーザーID_空のOptionalが返される() {
        // Given
        when(userJpaRepository.findByUserId("non-existent")).thenReturn(Optional.empty());

        // When
        Optional<User> result = jpaUserRepository.findByUserId("non-existent");

        // Then
        verify(userJpaRepository).findByUserId("non-existent");
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_nullのユーザーID_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> jpaUserRepository.findByUserId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ユーザーIDは必須です");
    }

    @Test
    void findByEmail_存在するメールアドレス_対応するUserが返される() {
        // Given
        when(userJpaRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(testJpaEntity));

        // When
        Optional<User> result = jpaUserRepository.findByEmail("test@example.com");

        // Then
        verify(userJpaRepository).findByEmailIgnoreCase("test@example.com");
        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_存在しないメールアドレス_空のOptionalが返される() {
        // Given
        when(userJpaRepository.findByEmailIgnoreCase("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        Optional<User> result = jpaUserRepository.findByEmail("nonexistent@example.com");

        // Then
        verify(userJpaRepository).findByEmailIgnoreCase("nonexistent@example.com");
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_nullのメールアドレス_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> jpaUserRepository.findByEmail(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("メールアドレスは必須です");
    }

    @Test
    void findAll_ユーザーが存在する場合_全てのUserリストが返される() {
        // Given
        List<UserJpaEntity> jpaEntities = List.of(testJpaEntity);
        when(userJpaRepository.findAll()).thenReturn(jpaEntities);

        // When
        List<User> result = jpaUserRepository.findAll();

        // Then
        verify(userJpaRepository).findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo("test-user-001");
    }

    @Test
    void findByEnabledTrue_有効なユーザーが存在する場合_有効なUserリストが返される() {
        // Given
        List<UserJpaEntity> enabledEntities = List.of(testJpaEntity);
        when(userJpaRepository.findByEnabledTrue()).thenReturn(enabledEntities);

        // When
        List<User> result = jpaUserRepository.findByEnabledTrue();

        // Then
        verify(userJpaRepository).findByEnabledTrue();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).enabled()).isTrue();
        assertThat(result.get(0).userId()).isEqualTo("test-user-001");
    }

    @Test
    void existsByUserId_存在するユーザーID_trueが返される() {
        // Given
        when(userJpaRepository.existsByUserId("test-user-001")).thenReturn(true);

        // When
        boolean result = jpaUserRepository.existsByUserId("test-user-001");

        // Then
        verify(userJpaRepository).existsByUserId("test-user-001");
        assertThat(result).isTrue();
    }

    @Test
    void existsByUserId_存在しないユーザーID_falseが返される() {
        // Given
        when(userJpaRepository.existsByUserId("non-existent")).thenReturn(false);

        // When
        boolean result = jpaUserRepository.existsByUserId("non-existent");

        // Then
        verify(userJpaRepository).existsByUserId("non-existent");
        assertThat(result).isFalse();
    }

    @Test
    void existsByUserId_nullのユーザーID_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> jpaUserRepository.existsByUserId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ユーザーIDは必須です");
    }

    @Test
    void existsByEmail_存在するメールアドレス_trueが返される() {
        // Given
        when(userJpaRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        // When
        boolean result = jpaUserRepository.existsByEmail("test@example.com");

        // Then
        verify(userJpaRepository).existsByEmailIgnoreCase("test@example.com");
        assertThat(result).isTrue();
    }

    @Test
    void existsByEmail_存在しないメールアドレス_falseが返される() {
        // Given
        when(userJpaRepository.existsByEmailIgnoreCase("nonexistent@example.com")).thenReturn(false);

        // When
        boolean result = jpaUserRepository.existsByEmail("nonexistent@example.com");

        // Then
        verify(userJpaRepository).existsByEmailIgnoreCase("nonexistent@example.com");
        assertThat(result).isFalse();
    }

    @Test
    void existsByEmail_nullのメールアドレス_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> jpaUserRepository.existsByEmail(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("メールアドレスは必須です");
    }

    @Test
    void deleteById_存在するID_正常に削除される() {
        // When
        jpaUserRepository.deleteById(1L);

        // Then
        verify(userJpaRepository).deleteById(1L);
    }

    @Test
    void deleteById_nullのID_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> jpaUserRepository.deleteById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("IDは必須です");
    }

    @Test
    void deleteByUserId_存在するユーザーID_正常に削除される() {
        // When
        jpaUserRepository.deleteByUserId("test-user-001");

        // Then
        verify(userJpaRepository).deleteByUserId("test-user-001");
    }

    @Test
    void deleteByUserId_nullのユーザーID_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> jpaUserRepository.deleteByUserId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ユーザーIDは必須です");
    }

    @Test
    void existsById_存在するID_trueが返される() {
        // Given
        when(userJpaRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = jpaUserRepository.existsById(1L);

        // Then
        verify(userJpaRepository).existsById(1L);
        assertThat(result).isTrue();
    }

    @Test
    void existsById_存在しないID_falseが返される() {
        // Given
        when(userJpaRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = jpaUserRepository.existsById(999L);

        // Then
        verify(userJpaRepository).existsById(999L);
        assertThat(result).isFalse();
    }

    @Test
    void existsById_nullのID_例外が発生する() {
        // When & Then
        assertThatThrownBy(() -> jpaUserRepository.existsById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("IDは必須です");
    }

    @Test
    void 異なるロールのユーザー_正常に変換される() {
        // Given: ADMINロールのユーザー
        LocalDateTime now = LocalDateTime.now();
        User adminUser = new User(
                2L, "admin-001", "管理者", "admin@example.com",
                "adminPassword", Role.ADMIN, true, now, now
        );
        
        UserJpaEntity adminJpaEntity = new UserJpaEntity(
                "admin-001", "管理者", "admin@example.com",
                "adminPassword", Role.ADMIN, true, now, now
        );

        when(userJpaRepository.save(any(UserJpaEntity.class))).thenReturn(adminJpaEntity);

        // When
        User result = jpaUserRepository.save(adminUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(Role.ADMIN);
        assertThat(result.userId()).isEqualTo("admin-001");
        assertThat(result.username()).isEqualTo("管理者");
    }

    @Test
    void 無効化されたユーザー_正常に変換される() {
        // Given: 無効化されたユーザー
        LocalDateTime now = LocalDateTime.now();
        User disabledUser = new User(
                3L, "disabled-001", "無効ユーザー", "disabled@example.com",
                "disabledPassword", Role.USER, false, now, null
        );
        
        UserJpaEntity disabledJpaEntity = new UserJpaEntity(
                "disabled-001", "無効ユーザー", "disabled@example.com",
                "disabledPassword", Role.USER, false, now, null
        );

        when(userJpaRepository.save(any(UserJpaEntity.class))).thenReturn(disabledJpaEntity);

        // When
        User result = jpaUserRepository.save(disabledUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.enabled()).isFalse();
        assertThat(result.lastLoginAt()).isNull();
        assertThat(result.userId()).isEqualTo("disabled-001");
    }
}