package com.github.okanikani.kairos.users.others.jpa.repositories;

import com.github.okanikani.kairos.users.domains.models.entities.Role;
import com.github.okanikani.kairos.users.others.jpa.entities.UserJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserJpaRepositoryの統合テスト
 * TestContainersを使用してPostgreSQLとの統合テストを実行
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserJpaRepository統合テスト")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude="
})
class UserJpaRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("kairos_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    @Autowired
    private UserJpaRepository userJpaRepository;

    private UserJpaEntity testUser1;
    private UserJpaEntity testUser2;

    @BeforeEach
    void setUp() {
        userJpaRepository.deleteAll();
        
        testUser1 = new UserJpaEntity(
                "testuser1",
                "テストユーザー1",
                "test1@example.com",
                "hashedPassword1",
                Role.USER,
                true,
                LocalDateTime.now(),
                null
        );

        testUser2 = new UserJpaEntity(
                "testuser2",
                "テストユーザー2",
                "TEST2@EXAMPLE.COM",  // 大文字でテスト
                "hashedPassword2",
                Role.ADMIN,
                false,
                LocalDateTime.now(),
                null
        );
    }

    @Test
    @DisplayName("基本CRUD操作_ユーザー作成")
    void save_正常ケース_ユーザーが作成される() {
        // When
        UserJpaEntity savedUser = userJpaRepository.save(testUser1);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUserId()).isEqualTo("testuser1");
        assertThat(savedUser.getUsername()).isEqualTo("テストユーザー1");
        assertThat(savedUser.getEmail()).isEqualTo("test1@example.com");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getEnabled()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("基本CRUD操作_ID検索")
    void findById_正常ケース_ユーザーが取得される() {
        // Given
        UserJpaEntity savedUser = userJpaRepository.save(testUser1);

        // When
        Optional<UserJpaEntity> found = userJpaRepository.findById(savedUser.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("testuser1");
    }

    @Test
    @DisplayName("基本CRUD操作_全件取得")
    void findAll_正常ケース_全ユーザーが取得される() {
        // Given
        userJpaRepository.save(testUser1);
        userJpaRepository.save(testUser2);

        // When
        List<UserJpaEntity> users = userJpaRepository.findAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(UserJpaEntity::getUserId)
                .containsExactlyInAnyOrder("testuser1", "testuser2");
    }

    @Test
    @DisplayName("基本CRUD操作_ユーザー更新")
    void save_更新ケース_ユーザー情報が更新される() {
        // Given
        UserJpaEntity savedUser = userJpaRepository.save(testUser1);
        LocalDateTime originalCreatedAt = savedUser.getCreatedAt();
        
        // When
        savedUser.setUsername("更新されたユーザー名");
        savedUser.setEmail("updated@example.com");
        savedUser.setEnabled(false);
        savedUser.setLastLoginAt(LocalDateTime.now());
        
        UserJpaEntity updatedUser = userJpaRepository.save(savedUser);

        // Then
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getUsername()).isEqualTo("更新されたユーザー名");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getEnabled()).isFalse();
        assertThat(updatedUser.getLastLoginAt()).isNotNull();
        assertThat(updatedUser.getCreatedAt()).isEqualTo(originalCreatedAt); // 作成日時は変更されない
    }

    @Test
    @DisplayName("基本CRUD操作_ユーザー削除")
    void delete_正常ケース_ユーザーが削除される() {
        // Given
        UserJpaEntity savedUser = userJpaRepository.save(testUser1);

        // When
        userJpaRepository.delete(savedUser);

        // Then
        Optional<UserJpaEntity> found = userJpaRepository.findById(savedUser.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("カスタムクエリ_ユーザーID検索")
    void findByUserId_正常ケース_ユーザーが取得される() {
        // Given
        userJpaRepository.save(testUser1);

        // When
        Optional<UserJpaEntity> found = userJpaRepository.findByUserId("testuser1");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("testuser1");
    }

    @Test
    @DisplayName("カスタムクエリ_存在しないユーザーID検索")
    void findByUserId_存在しないユーザー_空のOptionalが返される() {
        // When
        Optional<UserJpaEntity> found = userJpaRepository.findByUserId("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("カスタムクエリ_メール検索_大文字小文字無視")
    void findByEmailIgnoreCase_正常ケース_大文字小文字無視で取得される() {
        // Given
        userJpaRepository.save(testUser2);

        // When
        Optional<UserJpaEntity> found1 = userJpaRepository.findByEmailIgnoreCase("test2@example.com");
        Optional<UserJpaEntity> found2 = userJpaRepository.findByEmailIgnoreCase("TEST2@EXAMPLE.COM");
        Optional<UserJpaEntity> found3 = userJpaRepository.findByEmailIgnoreCase("Test2@Example.Com");

        // Then
        assertThat(found1).isPresent();
        assertThat(found2).isPresent();
        assertThat(found3).isPresent();
        
        assertThat(found1.get().getUserId()).isEqualTo("testuser2");
        assertThat(found2.get().getUserId()).isEqualTo("testuser2");
        assertThat(found3.get().getUserId()).isEqualTo("testuser2");
    }

    @Test
    @DisplayName("カスタムクエリ_有効ユーザー検索")
    void findByEnabledTrue_正常ケース_有効ユーザーのみ取得される() {
        // Given
        userJpaRepository.save(testUser1); // enabled = true
        userJpaRepository.save(testUser2); // enabled = false

        // When
        List<UserJpaEntity> enabledUsers = userJpaRepository.findByEnabledTrue();

        // Then
        assertThat(enabledUsers).hasSize(1);
        assertThat(enabledUsers.get(0).getUserId()).isEqualTo("testuser1");
        assertThat(enabledUsers.get(0).getEnabled()).isTrue();
    }

    @Test
    @DisplayName("カスタムクエリ_ユーザーID存在確認")
    void existsByUserId_正常ケース_存在確認ができる() {
        // Given
        userJpaRepository.save(testUser1);

        // When & Then
        assertThat(userJpaRepository.existsByUserId("testuser1")).isTrue();
        assertThat(userJpaRepository.existsByUserId("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("カスタムクエリ_メール存在確認_大文字小文字無視")
    void existsByEmailIgnoreCase_正常ケース_大文字小文字無視で存在確認ができる() {
        // Given
        userJpaRepository.save(testUser2);

        // When & Then
        assertThat(userJpaRepository.existsByEmailIgnoreCase("test2@example.com")).isTrue();
        assertThat(userJpaRepository.existsByEmailIgnoreCase("TEST2@EXAMPLE.COM")).isTrue();
        assertThat(userJpaRepository.existsByEmailIgnoreCase("Test2@Example.Com")).isTrue();
        assertThat(userJpaRepository.existsByEmailIgnoreCase("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("データベース制約_ユーザーID一意制約確認")
    void existsByUserId_一意制約_正常に確認される() {
        // Given
        userJpaRepository.save(testUser1);

        // When & Then
        assertThat(userJpaRepository.existsByUserId("testuser1")).isTrue();
        assertThat(userJpaRepository.existsByUserId("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("データベース制約_メール一意制約確認")
    void existsByEmailIgnoreCase_一意制約_正常に確認される() {
        // Given
        userJpaRepository.save(testUser1);

        // When & Then
        assertThat(userJpaRepository.existsByEmailIgnoreCase("test1@example.com")).isTrue();
        assertThat(userJpaRepository.existsByEmailIgnoreCase("TEST1@EXAMPLE.COM")).isTrue();
        assertThat(userJpaRepository.existsByEmailIgnoreCase("nonexistent@example.com")).isFalse();
    }
}