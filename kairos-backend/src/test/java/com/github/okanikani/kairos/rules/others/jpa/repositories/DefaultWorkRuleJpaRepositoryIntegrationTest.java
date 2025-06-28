package com.github.okanikani.kairos.rules.others.jpa.repositories;

import com.github.okanikani.kairos.rules.others.jpa.entities.DefaultWorkRuleJpaEntity;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DefaultWorkRuleJpaRepositoryの統合テスト
 * ユーザー・勤怠先の組み合わせ一意制約のテストを含む
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("DefaultWorkRuleJpaRepository統合テスト")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude="
})
class DefaultWorkRuleJpaRepositoryIntegrationTest {

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
    private DefaultWorkRuleJpaRepository defaultWorkRuleJpaRepository;

    private DefaultWorkRuleJpaEntity defaultRule1;
    private DefaultWorkRuleJpaEntity defaultRule2;
    private DefaultWorkRuleJpaEntity defaultRule3;

    @BeforeEach
    void setUp() {
        defaultWorkRuleJpaRepository.deleteAll();
        
        // デフォルト勤怠ルール1: user1, workPlace1
        defaultRule1 = new DefaultWorkRuleJpaEntity(
                1L,                                    // workPlaceId
                35.6762,                              // latitude (東京駅付近)
                139.7649,                             // longitude
                "user1",                              // userId
                LocalTime.of(9, 0),                   // standardStartTime
                LocalTime.of(18, 0),                  // standardEndTime
                LocalTime.of(12, 0),                  // breakStartTime
                LocalTime.of(13, 0)                   // breakEndTime
        );
        
        // デフォルト勤怠ルール2: user1, workPlace2
        defaultRule2 = new DefaultWorkRuleJpaEntity(
                2L,                                    // workPlaceId
                35.6895,                              // latitude (新宿駅付近)
                139.6917,                             // longitude
                "user1",                              // userId
                LocalTime.of(10, 0),                  // standardStartTime
                LocalTime.of(19, 0),                  // standardEndTime
                null,                                 // breakStartTime (休憩なし)
                null                                  // breakEndTime
        );
        
        // デフォルト勤怠ルール3: user2, workPlace1
        defaultRule3 = new DefaultWorkRuleJpaEntity(
                1L,                                    // workPlaceId
                35.6762,                              // latitude
                139.7649,                             // longitude
                "user2",                              // userId
                LocalTime.of(8, 30),                  // standardStartTime
                LocalTime.of(17, 30),                 // standardEndTime
                LocalTime.of(12, 0),                  // breakStartTime
                LocalTime.of(13, 0)                   // breakEndTime
        );
    }

    @Test
    @DisplayName("基本CRUD操作_デフォルト勤怠ルール作成")
    void save_正常ケース_デフォルト勤怠ルールが作成される() {
        // When
        DefaultWorkRuleJpaEntity savedRule = defaultWorkRuleJpaRepository.save(defaultRule1);

        // Then
        assertThat(savedRule.getId()).isNotNull();
        assertThat(savedRule.getWorkPlaceId()).isEqualTo(1L);
        assertThat(savedRule.getUserId()).isEqualTo("user1");
        assertThat(savedRule.getLatitude()).isEqualTo(35.6762);
        assertThat(savedRule.getLongitude()).isEqualTo(139.7649);
        assertThat(savedRule.getStandardStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(savedRule.getStandardEndTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(savedRule.getBreakStartTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(savedRule.getBreakEndTime()).isEqualTo(LocalTime.of(13, 0));
    }

    @Test
    @DisplayName("基本CRUD操作_ID検索")
    void findById_正常ケース_デフォルト勤怠ルールが取得される() {
        // Given
        DefaultWorkRuleJpaEntity savedRule = defaultWorkRuleJpaRepository.save(defaultRule1);

        // When
        Optional<DefaultWorkRuleJpaEntity> found = defaultWorkRuleJpaRepository.findById(savedRule.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("user1");
        assertThat(found.get().getWorkPlaceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("基本CRUD操作_全件取得")
    void findAll_正常ケース_全デフォルト勤怠ルールが取得される() {
        // Given
        defaultWorkRuleJpaRepository.save(defaultRule1);
        defaultWorkRuleJpaRepository.save(defaultRule2);
        defaultWorkRuleJpaRepository.save(defaultRule3);

        // When
        List<DefaultWorkRuleJpaEntity> rules = defaultWorkRuleJpaRepository.findAll();

        // Then
        assertThat(rules).hasSize(3);
        assertThat(rules)
                .extracting(DefaultWorkRuleJpaEntity::getUserId)
                .containsExactlyInAnyOrder("user1", "user1", "user2");
    }

    @Test
    @DisplayName("基本CRUD操作_デフォルト勤怠ルール削除")
    void delete_正常ケース_デフォルト勤怠ルールが削除される() {
        // Given
        DefaultWorkRuleJpaEntity savedRule = defaultWorkRuleJpaRepository.save(defaultRule1);

        // When
        defaultWorkRuleJpaRepository.delete(savedRule);

        // Then
        Optional<DefaultWorkRuleJpaEntity> found = defaultWorkRuleJpaRepository.findById(savedRule.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("カスタムクエリ_ユーザー別検索")
    void findByUserId_正常ケース_ユーザーのデフォルト勤怠ルールが取得される() {
        // Given
        defaultWorkRuleJpaRepository.save(defaultRule1); // user1, workPlace1
        defaultWorkRuleJpaRepository.save(defaultRule2); // user1, workPlace2
        defaultWorkRuleJpaRepository.save(defaultRule3); // user2, workPlace1

        // When
        List<DefaultWorkRuleJpaEntity> user1Rules = defaultWorkRuleJpaRepository.findByUserId("user1");
        List<DefaultWorkRuleJpaEntity> user2Rules = defaultWorkRuleJpaRepository.findByUserId("user2");

        // Then
        assertThat(user1Rules).hasSize(2);
        assertThat(user1Rules)
                .extracting(DefaultWorkRuleJpaEntity::getUserId)
                .containsOnly("user1");
        assertThat(user1Rules)
                .extracting(DefaultWorkRuleJpaEntity::getWorkPlaceId)
                .containsExactlyInAnyOrder(1L, 2L);
        
        assertThat(user2Rules).hasSize(1);
        assertThat(user2Rules.get(0).getUserId()).isEqualTo("user2");
        assertThat(user2Rules.get(0).getWorkPlaceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("カスタムクエリ_勤怠先別検索")
    void findByWorkPlaceId_正常ケース_勤怠先のデフォルト勤怠ルールが取得される() {
        // Given
        defaultWorkRuleJpaRepository.save(defaultRule1); // user1, workPlace1
        defaultWorkRuleJpaRepository.save(defaultRule2); // user1, workPlace2
        defaultWorkRuleJpaRepository.save(defaultRule3); // user2, workPlace1

        // When
        List<DefaultWorkRuleJpaEntity> workPlace1Rules = defaultWorkRuleJpaRepository.findByWorkPlaceId(1L);
        List<DefaultWorkRuleJpaEntity> workPlace2Rules = defaultWorkRuleJpaRepository.findByWorkPlaceId(2L);

        // Then
        assertThat(workPlace1Rules).hasSize(2);
        assertThat(workPlace1Rules)
                .extracting(DefaultWorkRuleJpaEntity::getWorkPlaceId)
                .containsOnly(1L);
        assertThat(workPlace1Rules)
                .extracting(DefaultWorkRuleJpaEntity::getUserId)
                .containsExactlyInAnyOrder("user1", "user2");
        
        assertThat(workPlace2Rules).hasSize(1);
        assertThat(workPlace2Rules.get(0).getWorkPlaceId()).isEqualTo(2L);
        assertThat(workPlace2Rules.get(0).getUserId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("カスタムクエリ_ユーザー・勤怠先組み合わせ検索")
    void findByUserIdAndWorkPlaceId_正常ケース_特定組み合わせのルールが取得される() {
        // Given
        defaultWorkRuleJpaRepository.save(defaultRule1); // user1, workPlace1
        defaultWorkRuleJpaRepository.save(defaultRule2); // user1, workPlace2
        defaultWorkRuleJpaRepository.save(defaultRule3); // user2, workPlace1

        // When
        Optional<DefaultWorkRuleJpaEntity> rule1 = defaultWorkRuleJpaRepository
                .findByUserIdAndWorkPlaceId("user1", 1L);
        Optional<DefaultWorkRuleJpaEntity> rule2 = defaultWorkRuleJpaRepository
                .findByUserIdAndWorkPlaceId("user1", 2L);
        Optional<DefaultWorkRuleJpaEntity> rule3 = defaultWorkRuleJpaRepository
                .findByUserIdAndWorkPlaceId("user2", 1L);
        Optional<DefaultWorkRuleJpaEntity> ruleNotFound = defaultWorkRuleJpaRepository
                .findByUserIdAndWorkPlaceId("user2", 2L);

        // Then
        assertThat(rule1).isPresent();
        assertThat(rule1.get().getUserId()).isEqualTo("user1");
        assertThat(rule1.get().getWorkPlaceId()).isEqualTo(1L);
        
        assertThat(rule2).isPresent();
        assertThat(rule2.get().getUserId()).isEqualTo("user1");
        assertThat(rule2.get().getWorkPlaceId()).isEqualTo(2L);
        
        assertThat(rule3).isPresent();
        assertThat(rule3.get().getUserId()).isEqualTo("user2");
        assertThat(rule3.get().getWorkPlaceId()).isEqualTo(1L);
        
        assertThat(ruleNotFound).isEmpty();
    }

    @Test
    @DisplayName("カスタムクエリ_存在確認")
    void existsByUserIdAndWorkPlaceId_正常ケース_存在確認ができる() {
        // Given
        defaultWorkRuleJpaRepository.save(defaultRule1); // user1, workPlace1

        // When & Then
        assertThat(defaultWorkRuleJpaRepository.existsByUserIdAndWorkPlaceId("user1", 1L)).isTrue();
        assertThat(defaultWorkRuleJpaRepository.existsByUserIdAndWorkPlaceId("user1", 2L)).isFalse();
        assertThat(defaultWorkRuleJpaRepository.existsByUserIdAndWorkPlaceId("user2", 1L)).isFalse();
        assertThat(defaultWorkRuleJpaRepository.existsByUserIdAndWorkPlaceId("nonexistent", 1L)).isFalse();
    }

    @Test
    @DisplayName("カスタムクエリ_存在確認_ID除外")
    void existsByUserIdAndWorkPlaceIdExcludingId_正常ケース_自分以外の存在確認ができる() {
        // Given
        DefaultWorkRuleJpaEntity savedRule1 = defaultWorkRuleJpaRepository.save(defaultRule1); // user1, workPlace1
        defaultWorkRuleJpaRepository.save(defaultRule2); // user1, workPlace2

        // When & Then
        // 自分自身を除外して存在確認
        assertThat(defaultWorkRuleJpaRepository
                .existsByUserIdAndWorkPlaceIdExcludingId("user1", 1L, savedRule1.getId())).isFalse();
        assertThat(defaultWorkRuleJpaRepository
                .existsByUserIdAndWorkPlaceIdExcludingId("user1", 2L, savedRule1.getId())).isTrue();
        assertThat(defaultWorkRuleJpaRepository
                .existsByUserIdAndWorkPlaceIdExcludingId("user2", 1L, savedRule1.getId())).isFalse();
    }

    @Test
    @DisplayName("GPS座標値検証_有効な範囲")
    void save_有効なGPS座標_正常に保存される() {
        // Given
        DefaultWorkRuleJpaEntity validRule = new DefaultWorkRuleJpaEntity(
                1L,
                -90.0, // 最小値
                -180.0, // 最小値
                "validUser",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                null,
                null
        );

        // When
        DefaultWorkRuleJpaEntity savedRule = defaultWorkRuleJpaRepository.save(validRule);

        // Then
        assertThat(savedRule.getLatitude()).isEqualTo(-90.0);
        assertThat(savedRule.getLongitude()).isEqualTo(-180.0);
    }

    @Test
    @DisplayName("GPS座標値検証_境界値")
    void save_境界値GPS座標_正常に保存される() {
        // Given
        DefaultWorkRuleJpaEntity boundaryRule1 = new DefaultWorkRuleJpaEntity(
                1L,
                90.0, // 最大値
                180.0, // 最大値
                "boundaryUser1",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                null,
                null
        );
        
        DefaultWorkRuleJpaEntity boundaryRule2 = new DefaultWorkRuleJpaEntity(
                2L,
                0.0, // 中央値
                0.0, // 中央値
                "boundaryUser2",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                null,
                null
        );

        // When
        DefaultWorkRuleJpaEntity saved1 = defaultWorkRuleJpaRepository.save(boundaryRule1);
        DefaultWorkRuleJpaEntity saved2 = defaultWorkRuleJpaRepository.save(boundaryRule2);

        // Then
        assertThat(saved1.getLatitude()).isEqualTo(90.0);
        assertThat(saved1.getLongitude()).isEqualTo(180.0);
        assertThat(saved2.getLatitude()).isEqualTo(0.0);
        assertThat(saved2.getLongitude()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("休憩時間なしパターン")
    void save_休憩時間なし_正常に保存される() {
        // Given
        DefaultWorkRuleJpaEntity noBreakRule = new DefaultWorkRuleJpaEntity(
                1L,
                35.6762,
                139.7649,
                "noBreakUser",
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                null, // 休憩開始時刻なし
                null  // 休憩終了時刻なし
        );

        // When
        DefaultWorkRuleJpaEntity savedRule = defaultWorkRuleJpaRepository.save(noBreakRule);

        // Then
        assertThat(savedRule.getBreakStartTime()).isNull();
        assertThat(savedRule.getBreakEndTime()).isNull();
        assertThat(savedRule.getStandardStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(savedRule.getStandardEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    @DisplayName("ユーザー・勤怠先の組み合わせ一意性検証")
    void save_同一組み合わせ_一意制約が機能する() {
        // Given
        defaultWorkRuleJpaRepository.save(defaultRule1); // user1, workPlace1

        // When & Then
        // 同じユーザー・勤怠先の組み合わせが既に存在することを確認
        assertThat(defaultWorkRuleJpaRepository.existsByUserIdAndWorkPlaceId("user1", 1L)).isTrue();
        
        // 異なる組み合わせは存在しないことを確認
        assertThat(defaultWorkRuleJpaRepository.existsByUserIdAndWorkPlaceId("user1", 3L)).isFalse();
        assertThat(defaultWorkRuleJpaRepository.existsByUserIdAndWorkPlaceId("user3", 1L)).isFalse();
    }
}