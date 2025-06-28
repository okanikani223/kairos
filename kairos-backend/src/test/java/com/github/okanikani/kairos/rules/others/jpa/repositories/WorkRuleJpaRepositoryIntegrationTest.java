package com.github.okanikani.kairos.rules.others.jpa.repositories;

import com.github.okanikani.kairos.rules.others.jpa.entities.WorkRuleJpaEntity;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WorkRuleJpaRepositoryの統合テスト
 * 勤怠ルールの期間管理と重複チェック機能のテストを含む
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("WorkRuleJpaRepository統合テスト")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude="
})
class WorkRuleJpaRepositoryIntegrationTest {

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
    private WorkRuleJpaRepository workRuleJpaRepository;

    private WorkRuleJpaEntity workRule1;
    private WorkRuleJpaEntity workRule2;
    private WorkRuleJpaEntity workRule3;

    @BeforeEach
    void setUp() {
        workRuleJpaRepository.deleteAll();
        
        // 勤怠ルール1: 2024年1月1日〜6月30日
        workRule1 = new WorkRuleJpaEntity(
                1L,                                    // workPlaceId
                35.6762,                              // latitude (東京駅付近)
                139.7649,                             // longitude
                "user1",                              // userId
                LocalTime.of(9, 0),                   // standardStartTime
                LocalTime.of(18, 0),                  // standardEndTime
                LocalTime.of(12, 0),                  // breakStartTime
                LocalTime.of(13, 0),                  // breakEndTime
                LocalDate.of(2024, 1, 1),            // membershipStartDate
                LocalDate.of(2024, 6, 30)            // membershipEndDate
        );
        
        // 勤怠ルール2: 同一ユーザーで期間が異なる（2024年7月1日〜12月31日）
        workRule2 = new WorkRuleJpaEntity(
                2L,                                    // workPlaceId
                35.6895,                              // latitude (新宿駅付近)
                139.6917,                             // longitude
                "user1",                              // userId
                LocalTime.of(10, 0),                  // standardStartTime
                LocalTime.of(19, 0),                  // standardEndTime
                null,                                 // breakStartTime (休憩なし)
                null,                                 // breakEndTime
                LocalDate.of(2024, 7, 1),            // membershipStartDate
                LocalDate.of(2024, 12, 31)           // membershipEndDate
        );
        
        // 勤怠ルール3: 別ユーザー
        workRule3 = new WorkRuleJpaEntity(
                1L,                                    // workPlaceId
                35.6762,                              // latitude
                139.7649,                             // longitude
                "user2",                              // userId
                LocalTime.of(8, 30),                  // standardStartTime
                LocalTime.of(17, 30),                 // standardEndTime
                LocalTime.of(12, 0),                  // breakStartTime
                LocalTime.of(13, 0),                  // breakEndTime
                LocalDate.of(2024, 1, 1),            // membershipStartDate
                LocalDate.of(2024, 12, 31)           // membershipEndDate
        );
    }

    @Test
    @DisplayName("基本CRUD操作_勤怠ルール作成")
    void save_正常ケース_勤怠ルールが作成される() {
        // When
        WorkRuleJpaEntity savedRule = workRuleJpaRepository.save(workRule1);

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
        assertThat(savedRule.getMembershipStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(savedRule.getMembershipEndDate()).isEqualTo(LocalDate.of(2024, 6, 30));
    }

    @Test
    @DisplayName("基本CRUD操作_ID検索")
    void findById_正常ケース_勤怠ルールが取得される() {
        // Given
        WorkRuleJpaEntity savedRule = workRuleJpaRepository.save(workRule1);

        // When
        Optional<WorkRuleJpaEntity> found = workRuleJpaRepository.findById(savedRule.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("user1");
        assertThat(found.get().getWorkPlaceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("基本CRUD操作_全件取得")
    void findAll_正常ケース_全勤怠ルールが取得される() {
        // Given
        workRuleJpaRepository.save(workRule1);
        workRuleJpaRepository.save(workRule2);
        workRuleJpaRepository.save(workRule3);

        // When
        List<WorkRuleJpaEntity> rules = workRuleJpaRepository.findAll();

        // Then
        assertThat(rules).hasSize(3);
        assertThat(rules)
                .extracting(WorkRuleJpaEntity::getUserId)
                .containsExactlyInAnyOrder("user1", "user1", "user2");
    }

    @Test
    @DisplayName("基本CRUD操作_勤怠ルール削除")
    void delete_正常ケース_勤怠ルールが削除される() {
        // Given
        WorkRuleJpaEntity savedRule = workRuleJpaRepository.save(workRule1);

        // When
        workRuleJpaRepository.delete(savedRule);

        // Then
        Optional<WorkRuleJpaEntity> found = workRuleJpaRepository.findById(savedRule.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("カスタムクエリ_ユーザー別検索_所属開始日降順")
    void findByUserIdOrderByMembershipStartDateDesc_正常ケース_ユーザーの勤怠ルールが降順で取得される() {
        // Given
        workRuleJpaRepository.save(workRule1); // 2024/01/01-06/30
        workRuleJpaRepository.save(workRule2); // 2024/07/01-12/31
        workRuleJpaRepository.save(workRule3); // user2

        // When
        List<WorkRuleJpaEntity> user1Rules = workRuleJpaRepository.findByUserIdOrderByMembershipStartDateDesc("user1");
        List<WorkRuleJpaEntity> user2Rules = workRuleJpaRepository.findByUserIdOrderByMembershipStartDateDesc("user2");

        // Then
        assertThat(user1Rules).hasSize(2);
        assertThat(user1Rules)
                .extracting(WorkRuleJpaEntity::getUserId)
                .containsOnly("user1");
        // 降順なので最新（7月開始）が最初に来る
        assertThat(user1Rules.get(0).getMembershipStartDate()).isEqualTo(LocalDate.of(2024, 7, 1));
        assertThat(user1Rules.get(1).getMembershipStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        
        assertThat(user2Rules).hasSize(1);
        assertThat(user2Rules.get(0).getUserId()).isEqualTo("user2");
    }

    @Test
    @DisplayName("カスタムクエリ_勤怠先別検索")
    void findByWorkPlaceIdOrderByMembershipStartDate_正常ケース_勤怠先の勤怠ルールが取得される() {
        // Given
        workRuleJpaRepository.save(workRule1); // workPlaceId=1
        workRuleJpaRepository.save(workRule2); // workPlaceId=2
        workRuleJpaRepository.save(workRule3); // workPlaceId=1

        // When
        List<WorkRuleJpaEntity> workPlace1Rules = workRuleJpaRepository.findByWorkPlaceIdOrderByMembershipStartDate(1L);
        List<WorkRuleJpaEntity> workPlace2Rules = workRuleJpaRepository.findByWorkPlaceIdOrderByMembershipStartDate(2L);

        // Then
        assertThat(workPlace1Rules).hasSize(2);
        assertThat(workPlace1Rules)
                .extracting(WorkRuleJpaEntity::getWorkPlaceId)
                .containsOnly(1L);
        
        assertThat(workPlace2Rules).hasSize(1);
        assertThat(workPlace2Rules.get(0).getWorkPlaceId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("カスタムクエリ_有効日による検索")
    void findByUserIdAndEffectiveDate_正常ケース_有効な勤怠ルールが取得される() {
        // Given
        workRuleJpaRepository.save(workRule1); // 2024/01/01-06/30
        workRuleJpaRepository.save(workRule2); // 2024/07/01-12/31

        // When
        Optional<WorkRuleJpaEntity> ruleIn1stHalf = workRuleJpaRepository
                .findByUserIdAndEffectiveDate("user1", LocalDate.of(2024, 3, 15));
        Optional<WorkRuleJpaEntity> ruleIn2ndHalf = workRuleJpaRepository
                .findByUserIdAndEffectiveDate("user1", LocalDate.of(2024, 9, 15));
        Optional<WorkRuleJpaEntity> ruleOutOfRange = workRuleJpaRepository
                .findByUserIdAndEffectiveDate("user1", LocalDate.of(2025, 1, 15));

        // Then
        assertThat(ruleIn1stHalf).isPresent();
        assertThat(ruleIn1stHalf.get().getWorkPlaceId()).isEqualTo(1L);
        
        assertThat(ruleIn2ndHalf).isPresent();
        assertThat(ruleIn2ndHalf.get().getWorkPlaceId()).isEqualTo(2L);
        
        assertThat(ruleOutOfRange).isEmpty();
    }

    @Test
    @DisplayName("カスタムクエリ_重複期間検索")
    void findOverlappingRules_正常ケース_重複する勤怠ルールが検出される() {
        // Given
        workRuleJpaRepository.save(workRule1); // 2024/01/01-06/30
        workRuleJpaRepository.save(workRule2); // 2024/07/01-12/31

        // When
        // 既存期間と重複するケース
        List<WorkRuleJpaEntity> overlapping1 = workRuleJpaRepository
                .findOverlappingRules("user1", LocalDate.of(2024, 5, 1), LocalDate.of(2024, 8, 31));
        
        // 既存期間と重複しないケース
        List<WorkRuleJpaEntity> overlapping2 = workRuleJpaRepository
                .findOverlappingRules("user1", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30));
        
        // 一部重複ケース
        List<WorkRuleJpaEntity> overlapping3 = workRuleJpaRepository
                .findOverlappingRules("user1", LocalDate.of(2024, 6, 15), LocalDate.of(2024, 7, 15));

        // Then
        assertThat(overlapping1).hasSize(2); // 両方と重複
        assertThat(overlapping2).hasSize(0); // 重複なし
        assertThat(overlapping3).hasSize(2); // 両方と重複
    }

    @Test
    @DisplayName("カスタムクエリ_重複期間検索_ID除外")
    void findOverlappingRulesExcludingId_正常ケース_自分以外の重複ルールが検出される() {
        // Given
        WorkRuleJpaEntity savedRule1 = workRuleJpaRepository.save(workRule1); // 2024/01/01-06/30
        workRuleJpaRepository.save(workRule2); // 2024/07/01-12/31

        // When
        // 自分自身を除外して重複チェック
        List<WorkRuleJpaEntity> overlapping = workRuleJpaRepository
                .findOverlappingRulesExcludingId(
                        "user1", 
                        LocalDate.of(2024, 5, 1), 
                        LocalDate.of(2024, 8, 31), 
                        savedRule1.getId()
                );

        // Then
        assertThat(overlapping).hasSize(1); // rule2のみ（rule1は除外）
        assertThat(overlapping.get(0).getWorkPlaceId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("カスタムクエリ_期間内有効ルール検索")
    void findActiveRulesInPeriod_正常ケース_期間内有効ルールが取得される() {
        // Given
        workRuleJpaRepository.save(workRule1); // 2024/01/01-06/30
        workRuleJpaRepository.save(workRule2); // 2024/07/01-12/31
        workRuleJpaRepository.save(workRule3); // 2024/01/01-12/31

        // When
        List<WorkRuleJpaEntity> activeRules = workRuleJpaRepository
                .findActiveRulesInPeriod(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 9, 30));

        // Then
        assertThat(activeRules).hasSize(3); // 全て該当期間に有効
        assertThat(activeRules)
                .extracting(WorkRuleJpaEntity::getUserId)
                .containsExactlyInAnyOrder("user1", "user1", "user2");
    }

    @Test
    @DisplayName("カスタムクエリ_期間内有効ルール検索_一部該当")
    void findActiveRulesInPeriod_一部該当ケース_該当ルールのみ取得される() {
        // Given
        workRuleJpaRepository.save(workRule1); // 2024/01/01-06/30
        workRuleJpaRepository.save(workRule2); // 2024/07/01-12/31
        workRuleJpaRepository.save(workRule3); // 2024/01/01-12/31

        // When
        // 上半期のみの検索
        List<WorkRuleJpaEntity> activeRules = workRuleJpaRepository
                .findActiveRulesInPeriod(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 5, 31));

        // Then
        assertThat(activeRules).hasSize(2); // rule1とrule3のみ該当
        assertThat(activeRules)
                .extracting(WorkRuleJpaEntity::getWorkPlaceId)
                .containsOnly(1L);
    }

    @Test
    @DisplayName("GPS座標値検証_有効な範囲")
    void save_有効なGPS座標_正常に保存される() {
        // Given
        WorkRuleJpaEntity validRule = new WorkRuleJpaEntity(
                1L,
                -90.0, // 最小値
                -180.0, // 最小値
                "validUser",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                null,
                null,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        // When
        WorkRuleJpaEntity savedRule = workRuleJpaRepository.save(validRule);

        // Then
        assertThat(savedRule.getLatitude()).isEqualTo(-90.0);
        assertThat(savedRule.getLongitude()).isEqualTo(-180.0);
    }

    @Test
    @DisplayName("GPS座標値検証_境界値")
    void save_境界値GPS座標_正常に保存される() {
        // Given
        WorkRuleJpaEntity boundaryRule1 = new WorkRuleJpaEntity(
                1L,
                90.0, // 最大値
                180.0, // 最大値
                "boundaryUser1",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                null,
                null,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
        
        WorkRuleJpaEntity boundaryRule2 = new WorkRuleJpaEntity(
                1L,
                0.0, // 中央値
                0.0, // 中央値
                "boundaryUser2",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                null,
                null,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        // When
        WorkRuleJpaEntity saved1 = workRuleJpaRepository.save(boundaryRule1);
        WorkRuleJpaEntity saved2 = workRuleJpaRepository.save(boundaryRule2);

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
        WorkRuleJpaEntity noBreakRule = new WorkRuleJpaEntity(
                1L,
                35.6762,
                139.7649,
                "noBreakUser",
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                null, // 休憩開始時刻なし
                null, // 休憩終了時刻なし
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        // When
        WorkRuleJpaEntity savedRule = workRuleJpaRepository.save(noBreakRule);

        // Then
        assertThat(savedRule.getBreakStartTime()).isNull();
        assertThat(savedRule.getBreakEndTime()).isNull();
        assertThat(savedRule.getStandardStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(savedRule.getStandardEndTime()).isEqualTo(LocalTime.of(17, 0));
    }
}