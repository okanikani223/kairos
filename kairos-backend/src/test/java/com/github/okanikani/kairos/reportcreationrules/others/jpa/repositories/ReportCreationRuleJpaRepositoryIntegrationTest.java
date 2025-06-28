package com.github.okanikani.kairos.reportcreationrules.others.jpa.repositories;

import com.github.okanikani.kairos.reportcreationrules.others.jpa.entities.ReportCreationRuleJpaEntity;
import jakarta.persistence.EntityManager;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReportCreationRuleJpaRepositoryの統合テスト
 * ユーザーIDの一意制約と勤怠作成ルールのバリデーションテストを含む
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ReportCreationRuleJpaRepository統合テスト")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude="
})
class ReportCreationRuleJpaRepositoryIntegrationTest {

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
    private ReportCreationRuleJpaRepository reportCreationRuleJpaRepository;
    
    @Autowired
    private EntityManager entityManager;

    private ReportCreationRuleJpaEntity rule1;
    private ReportCreationRuleJpaEntity rule2;
    private ReportCreationRuleJpaEntity rule3;

    @BeforeEach
    void setUp() {
        reportCreationRuleJpaRepository.deleteAll();
        
        // 勤怠作成ルール1: user1, 月末締め, 15分単位
        rule1 = new ReportCreationRuleJpaEntity(
                "user1",     // userId
                31,          // closingDay (月末締め)
                15           // timeCalculationUnitMinutes (15分単位)
        );
        
        // 勤怠作成ルール2: user2, 25日締め, 30分単位
        rule2 = new ReportCreationRuleJpaEntity(
                "user2",     // userId
                25,          // closingDay (25日締め)
                30           // timeCalculationUnitMinutes (30分単位)
        );
        
        // 勤怠作成ルール3: user3, 15日締め, 1分単位
        rule3 = new ReportCreationRuleJpaEntity(
                "user3",     // userId
                15,          // closingDay (15日締め)
                1            // timeCalculationUnitMinutes (1分単位)
        );
    }

    @Test
    @DisplayName("基本CRUD操作_勤怠作成ルール作成")
    void save_正常ケース_勤怠作成ルールが作成される() {
        // When
        ReportCreationRuleJpaEntity savedRule = reportCreationRuleJpaRepository.save(rule1);

        // Then
        assertThat(savedRule.getId()).isNotNull();
        assertThat(savedRule.getUserId()).isEqualTo("user1");
        assertThat(savedRule.getClosingDay()).isEqualTo(31);
        assertThat(savedRule.getTimeCalculationUnitMinutes()).isEqualTo(15);
    }

    @Test
    @DisplayName("基本CRUD操作_ID検索")
    void findById_正常ケース_勤怠作成ルールが取得される() {
        // Given
        ReportCreationRuleJpaEntity savedRule = reportCreationRuleJpaRepository.save(rule1);

        // When
        Optional<ReportCreationRuleJpaEntity> found = reportCreationRuleJpaRepository.findById(savedRule.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("user1");
        assertThat(found.get().getClosingDay()).isEqualTo(31);
        assertThat(found.get().getTimeCalculationUnitMinutes()).isEqualTo(15);
    }

    @Test
    @DisplayName("基本CRUD操作_全件取得")
    void findAll_正常ケース_全勤怠作成ルールが取得される() {
        // Given
        reportCreationRuleJpaRepository.save(rule1);
        reportCreationRuleJpaRepository.save(rule2);
        reportCreationRuleJpaRepository.save(rule3);

        // When
        List<ReportCreationRuleJpaEntity> rules = reportCreationRuleJpaRepository.findAll();

        // Then
        assertThat(rules).hasSize(3);
        assertThat(rules)
                .extracting(ReportCreationRuleJpaEntity::getUserId)
                .containsExactlyInAnyOrder("user1", "user2", "user3");
    }

    @Test
    @DisplayName("基本CRUD操作_勤怠作成ルール更新")
    void save_更新ケース_勤怠作成ルールが更新される() {
        // Given
        ReportCreationRuleJpaEntity savedRule = reportCreationRuleJpaRepository.save(rule1);
        Long originalId = savedRule.getId();
        
        // 既存のルールを削除
        reportCreationRuleJpaRepository.delete(savedRule);
        // 削除操作を強制実行してデータベースに反映
        entityManager.flush();
        
        // 新しい値で勤怠作成ルール作成（同じユーザーID）
        ReportCreationRuleJpaEntity newRule = new ReportCreationRuleJpaEntity(
                "user1", // 同じユーザーID  
                20,      // 締め日を変更
                10       // 時間計算単位を変更
        );
        
        // When
        ReportCreationRuleJpaEntity updatedRule = reportCreationRuleJpaRepository.save(newRule);
        Optional<ReportCreationRuleJpaEntity> found = reportCreationRuleJpaRepository.findByUserId("user1");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("user1");
        assertThat(found.get().getClosingDay()).isEqualTo(20);
        assertThat(found.get().getTimeCalculationUnitMinutes()).isEqualTo(10);
        // 新しいIDが割り当てられていることを確認
        assertThat(found.get().getId()).isNotEqualTo(originalId);
    }

    @Test
    @DisplayName("基本CRUD操作_勤怠作成ルール削除")
    void delete_正常ケース_勤怠作成ルールが削除される() {
        // Given
        ReportCreationRuleJpaEntity savedRule = reportCreationRuleJpaRepository.save(rule1);

        // When
        reportCreationRuleJpaRepository.delete(savedRule);

        // Then
        Optional<ReportCreationRuleJpaEntity> found = reportCreationRuleJpaRepository.findById(savedRule.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("カスタムクエリ_ユーザーID検索")
    void findByUserId_正常ケース_ユーザーの勤怠作成ルールが取得される() {
        // Given
        reportCreationRuleJpaRepository.save(rule1); // user1
        reportCreationRuleJpaRepository.save(rule2); // user2
        reportCreationRuleJpaRepository.save(rule3); // user3

        // When
        Optional<ReportCreationRuleJpaEntity> user1Rule = reportCreationRuleJpaRepository.findByUserId("user1");
        Optional<ReportCreationRuleJpaEntity> user2Rule = reportCreationRuleJpaRepository.findByUserId("user2");
        Optional<ReportCreationRuleJpaEntity> user3Rule = reportCreationRuleJpaRepository.findByUserId("user3");
        Optional<ReportCreationRuleJpaEntity> nonexistentRule = reportCreationRuleJpaRepository.findByUserId("nonexistent");

        // Then
        assertThat(user1Rule).isPresent();
        assertThat(user1Rule.get().getUserId()).isEqualTo("user1");
        assertThat(user1Rule.get().getClosingDay()).isEqualTo(31);
        assertThat(user1Rule.get().getTimeCalculationUnitMinutes()).isEqualTo(15);
        
        assertThat(user2Rule).isPresent();
        assertThat(user2Rule.get().getUserId()).isEqualTo("user2");
        assertThat(user2Rule.get().getClosingDay()).isEqualTo(25);
        assertThat(user2Rule.get().getTimeCalculationUnitMinutes()).isEqualTo(30);
        
        assertThat(user3Rule).isPresent();
        assertThat(user3Rule.get().getUserId()).isEqualTo("user3");
        assertThat(user3Rule.get().getClosingDay()).isEqualTo(15);
        assertThat(user3Rule.get().getTimeCalculationUnitMinutes()).isEqualTo(1);
        
        assertThat(nonexistentRule).isEmpty();
    }

    @Test
    @DisplayName("カスタムクエリ_存在確認")
    void existsByUserId_正常ケース_存在確認ができる() {
        // Given
        reportCreationRuleJpaRepository.save(rule1); // user1

        // When & Then
        assertThat(reportCreationRuleJpaRepository.existsByUserId("user1")).isTrue();
        assertThat(reportCreationRuleJpaRepository.existsByUserId("user2")).isFalse();
        assertThat(reportCreationRuleJpaRepository.existsByUserId("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("カスタムクエリ_存在確認_ID除外")
    void existsByUserIdExcludingId_正常ケース_自分以外の存在確認ができる() {
        // Given
        ReportCreationRuleJpaEntity savedRule1 = reportCreationRuleJpaRepository.save(rule1); // user1
        reportCreationRuleJpaRepository.save(rule2); // user2

        // When & Then
        // 自分自身を除外して存在確認
        assertThat(reportCreationRuleJpaRepository
                .existsByUserIdExcludingId("user1", savedRule1.getId())).isFalse();
        assertThat(reportCreationRuleJpaRepository
                .existsByUserIdExcludingId("user2", savedRule1.getId())).isTrue();
        assertThat(reportCreationRuleJpaRepository
                .existsByUserIdExcludingId("nonexistent", savedRule1.getId())).isFalse();
    }

    @Test
    @DisplayName("データベース制約_ユーザーID一意制約確認")
    void existsByUserId_一意制約_正常に確認される() {
        // Given
        reportCreationRuleJpaRepository.save(rule1); // user1

        // When & Then
        assertThat(reportCreationRuleJpaRepository.existsByUserId("user1")).isTrue();
        assertThat(reportCreationRuleJpaRepository.existsByUserId("user2")).isFalse();
        
        // 同じユーザーIDで別のルールは存在しないことを確認
        List<ReportCreationRuleJpaEntity> allRules = reportCreationRuleJpaRepository.findAll();
        long user1Count = allRules.stream()
                .filter(rule -> "user1".equals(rule.getUserId()))
                .count();
        assertThat(user1Count).isEqualTo(1L);
    }

    @Test
    @DisplayName("境界値検証_締め日最小値")
    void save_締め日最小値_正常に保存される() {
        // Given
        ReportCreationRuleJpaEntity minClosingDayRule = new ReportCreationRuleJpaEntity(
                "minUser",
                1,    // 最小値
                15
        );

        // When
        ReportCreationRuleJpaEntity savedRule = reportCreationRuleJpaRepository.save(minClosingDayRule);

        // Then
        assertThat(savedRule.getClosingDay()).isEqualTo(1);
    }

    @Test
    @DisplayName("境界値検証_締め日最大値")
    void save_締め日最大値_正常に保存される() {
        // Given
        ReportCreationRuleJpaEntity maxClosingDayRule = new ReportCreationRuleJpaEntity(
                "maxUser",
                31,   // 最大値
                15
        );

        // When
        ReportCreationRuleJpaEntity savedRule = reportCreationRuleJpaRepository.save(maxClosingDayRule);

        // Then
        assertThat(savedRule.getClosingDay()).isEqualTo(31);
    }

    @Test
    @DisplayName("境界値検証_時間計算単位最小値")
    void save_時間計算単位最小値_正常に保存される() {
        // Given
        ReportCreationRuleJpaEntity minUnitRule = new ReportCreationRuleJpaEntity(
                "minUnitUser",
                15,
                1     // 最小値
        );

        // When
        ReportCreationRuleJpaEntity savedRule = reportCreationRuleJpaRepository.save(minUnitRule);

        // Then
        assertThat(savedRule.getTimeCalculationUnitMinutes()).isEqualTo(1);
    }

    @Test
    @DisplayName("境界値検証_時間計算単位最大値")
    void save_時間計算単位最大値_正常に保存される() {
        // Given
        ReportCreationRuleJpaEntity maxUnitRule = new ReportCreationRuleJpaEntity(
                "maxUnitUser",
                15,
                60    // 最大値
        );

        // When
        ReportCreationRuleJpaEntity savedRule = reportCreationRuleJpaRepository.save(maxUnitRule);

        // Then
        assertThat(savedRule.getTimeCalculationUnitMinutes()).isEqualTo(60);
    }

    @Test
    @DisplayName("一般的な設定パターンの確認")
    void save_一般的な設定パターン_正常に保存される() {
        // Given - 一般的な勤怠管理システムの設定パターン
        ReportCreationRuleJpaEntity monthEndRule = new ReportCreationRuleJpaEntity(
                "monthEndUser", 31, 15    // 月末締め、15分単位
        );
        ReportCreationRuleJpaEntity midMonthRule = new ReportCreationRuleJpaEntity(
                "midMonthUser", 15, 30    // 15日締め、30分単位
        );
        ReportCreationRuleJpaEntity quarterRule = new ReportCreationRuleJpaEntity(
                "quarterUser", 25, 1      // 25日締め、1分単位
        );

        // When
        ReportCreationRuleJpaEntity saved1 = reportCreationRuleJpaRepository.save(monthEndRule);
        ReportCreationRuleJpaEntity saved2 = reportCreationRuleJpaRepository.save(midMonthRule);
        ReportCreationRuleJpaEntity saved3 = reportCreationRuleJpaRepository.save(quarterRule);

        // Then
        assertThat(saved1.getClosingDay()).isEqualTo(31);
        assertThat(saved1.getTimeCalculationUnitMinutes()).isEqualTo(15);
        
        assertThat(saved2.getClosingDay()).isEqualTo(15);
        assertThat(saved2.getTimeCalculationUnitMinutes()).isEqualTo(30);
        
        assertThat(saved3.getClosingDay()).isEqualTo(25);
        assertThat(saved3.getTimeCalculationUnitMinutes()).isEqualTo(1);
    }
}