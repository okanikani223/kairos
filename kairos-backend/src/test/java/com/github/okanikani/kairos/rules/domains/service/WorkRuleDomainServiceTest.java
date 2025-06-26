package com.github.okanikani.kairos.rules.domains.service;

import com.github.okanikani.kairos.commons.testhelper.builders.WorkRuleBuilder;
import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * WorkRuleDomainServiceのテスト
 */
@DisplayName("WorkRuleDomainServiceのテスト")
class WorkRuleDomainServiceTest {

    private WorkRuleDomainService workRuleDomainService;

    @BeforeEach
    void setUp() {
        workRuleDomainService = new WorkRuleDomainService();
    }

    @Nested
    @DisplayName("hasOverlappingPeriod メソッドのテスト")
    class HasOverlappingPeriodTest {

        @Test
        @DisplayName("正常系_既存ルールが空リストの場合_falseを返す")
        void 正常系_既存ルールが空リストの場合_falseを返す() {
            // Arrange
            List<WorkRule> emptyRules = Collections.emptyList();
            LocalDate newStartDate = LocalDate.of(2025, 1, 1);
            LocalDate newEndDate = LocalDate.of(2025, 12, 31);

            // Act
            boolean result = workRuleDomainService.hasOverlappingPeriod(emptyRules, newStartDate, newEndDate);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("正常系_期間が重複していない場合_falseを返す")
        void 正常系_期間が重複していない場合_falseを返す() {
            // Arrange
            WorkRule existingRule = WorkRuleBuilder.create()
                    .withMembershipPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
                    .build();
            List<WorkRule> existingRules = Collections.singletonList(existingRule);
            
            LocalDate newStartDate = LocalDate.of(2025, 1, 1);
            LocalDate newEndDate = LocalDate.of(2025, 12, 31);

            // Act
            boolean result = workRuleDomainService.hasOverlappingPeriod(existingRules, newStartDate, newEndDate);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("正常系_期間が完全に重複している場合_trueを返す")
        void 正常系_期間が完全に重複している場合_trueを返す() {
            // Arrange
            WorkRule existingRule = WorkRuleBuilder.create()
                    .withMembershipPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31))
                    .build();
            List<WorkRule> existingRules = Collections.singletonList(existingRule);
            
            LocalDate newStartDate = LocalDate.of(2025, 1, 1);
            LocalDate newEndDate = LocalDate.of(2025, 12, 31);

            // Act
            boolean result = workRuleDomainService.hasOverlappingPeriod(existingRules, newStartDate, newEndDate);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("正常系_新規期間が既存期間を包含している場合_trueを返す")
        void 正常系_新規期間が既存期間を包含している場合_trueを返す() {
            // Arrange
            WorkRule existingRule = WorkRuleBuilder.create()
                    .withMembershipPeriod(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 9, 30))
                    .build();
            List<WorkRule> existingRules = Collections.singletonList(existingRule);
            
            LocalDate newStartDate = LocalDate.of(2025, 1, 1);
            LocalDate newEndDate = LocalDate.of(2025, 12, 31);

            // Act
            boolean result = workRuleDomainService.hasOverlappingPeriod(existingRules, newStartDate, newEndDate);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("正常系_新規期間が既存期間に包含されている場合_trueを返す")
        void 正常系_新規期間が既存期間に包含されている場合_trueを返す() {
            // Arrange
            WorkRule existingRule = WorkRuleBuilder.create()
                    .withMembershipPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31))
                    .build();
            List<WorkRule> existingRules = Collections.singletonList(existingRule);
            
            LocalDate newStartDate = LocalDate.of(2025, 3, 1);
            LocalDate newEndDate = LocalDate.of(2025, 9, 30);

            // Act
            boolean result = workRuleDomainService.hasOverlappingPeriod(existingRules, newStartDate, newEndDate);

            // Assert
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @MethodSource("期間重複テストケース")
        @DisplayName("正常系_様々な重複パターンのテスト")
        void 正常系_様々な重複パターンのテスト(
                LocalDate existingStart, LocalDate existingEnd,
                LocalDate newStart, LocalDate newEnd,
                boolean expectedOverlap,
                String description) {
            // Arrange
            WorkRule existingRule = WorkRuleBuilder.create()
                    .withMembershipPeriod(existingStart, existingEnd)
                    .build();
            List<WorkRule> existingRules = Collections.singletonList(existingRule);

            // Act
            boolean result = workRuleDomainService.hasOverlappingPeriod(existingRules, newStart, newEnd);

            // Assert
            assertThat(result).as(description).isEqualTo(expectedOverlap);
        }

        static Stream<Arguments> 期間重複テストケース() {
            return Stream.of(
                    // 既存: 2025/1/1 - 2025/12/31
                    Arguments.of(
                            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31),
                            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                            false, "完全に前の期間"
                    ),
                    Arguments.of(
                            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31),
                            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                            false, "完全に後の期間"
                    ),
                    Arguments.of(
                            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31),
                            LocalDate.of(2024, 7, 1), LocalDate.of(2025, 6, 30),
                            true, "前半重複"
                    ),
                    Arguments.of(
                            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31),
                            LocalDate.of(2025, 7, 1), LocalDate.of(2026, 6, 30),
                            true, "後半重複"
                    ),
                    Arguments.of(
                            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31),
                            LocalDate.of(2024, 12, 31), LocalDate.of(2025, 1, 1),
                            true, "境界日で重複（1日だけ）"
                    ),
                    Arguments.of(
                            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31),
                            LocalDate.of(2024, 12, 30), LocalDate.of(2024, 12, 31),
                            false, "境界日の前日まで"
                    ),
                    Arguments.of(
                            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31),
                            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2),
                            false, "境界日の翌日から"
                    )
            );
        }

        @Test
        @DisplayName("正常系_複数の既存ルールがあり一つでも重複している場合_trueを返す")
        void 正常系_複数の既存ルールがあり一つでも重複している場合_trueを返す() {
            // Arrange
            List<WorkRule> existingRules = Arrays.asList(
                    WorkRuleBuilder.create()
                            .withMembershipPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31))
                            .build(),
                    WorkRuleBuilder.create()
                            .withMembershipPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
                            .build(),
                    WorkRuleBuilder.create()
                            .withMembershipPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
                            .build()
            );
            
            LocalDate newStartDate = LocalDate.of(2024, 6, 1);
            LocalDate newEndDate = LocalDate.of(2025, 5, 31);

            // Act
            boolean result = workRuleDomainService.hasOverlappingPeriod(existingRules, newStartDate, newEndDate);

            // Assert
            assertThat(result).isTrue(); // 2024年のルールと重複
        }

        @Test
        @DisplayName("正常系_複数の既存ルールがありいずれとも重複していない場合_falseを返す")
        void 正常系_複数の既存ルールがありいずれとも重複していない場合_falseを返す() {
            // Arrange
            List<WorkRule> existingRules = Arrays.asList(
                    WorkRuleBuilder.create()
                            .withMembershipPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31))
                            .build(),
                    WorkRuleBuilder.create()
                            .withMembershipPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
                            .build()
            );
            
            LocalDate newStartDate = LocalDate.of(2024, 1, 1);
            LocalDate newEndDate = LocalDate.of(2025, 12, 31);

            // Act
            boolean result = workRuleDomainService.hasOverlappingPeriod(existingRules, newStartDate, newEndDate);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("境界値テスト_同一日の場合_重複とみなされる")
        void 境界値テスト_同一日の場合_重複とみなされる() {
            // Arrange
            WorkRule existingRule = WorkRuleBuilder.create()
                    .withMembershipPeriod(LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 1))
                    .build();
            List<WorkRule> existingRules = Collections.singletonList(existingRule);
            
            LocalDate newStartDate = LocalDate.of(2025, 6, 1);
            LocalDate newEndDate = LocalDate.of(2025, 6, 1);

            // Act
            boolean result = workRuleDomainService.hasOverlappingPeriod(existingRules, newStartDate, newEndDate);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("境界値テスト_隣接する期間の場合_重複しない")
        void 境界値テスト_隣接する期間の場合_重複しない() {
            // Arrange
            WorkRule existingRule = WorkRuleBuilder.create()
                    .withMembershipPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30))
                    .build();
            List<WorkRule> existingRules = Collections.singletonList(existingRule);
            
            LocalDate newStartDate = LocalDate.of(2025, 7, 1);
            LocalDate newEndDate = LocalDate.of(2025, 12, 31);

            // Act
            boolean result = workRuleDomainService.hasOverlappingPeriod(existingRules, newStartDate, newEndDate);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("ビジネスルールテスト_現実的なユースケース_年度替わりでの期間設定")
        void ビジネスルールテスト_現実的なユースケース_年度替わりでの期間設定() {
            // Arrange - 2024年度（2024/4/1-2025/3/31）のルールが存在
            WorkRule existingFiscalYearRule = WorkRuleBuilder.create()
                    .withMembershipPeriod(LocalDate.of(2024, 4, 1), LocalDate.of(2025, 3, 31))
                    .build();
            List<WorkRule> existingRules = Collections.singletonList(existingFiscalYearRule);
            
            // Act & Assert - 2025年度（2025/4/1-2026/3/31）のルールは重複しない
            boolean result2025FY = workRuleDomainService.hasOverlappingPeriod(
                    existingRules,
                    LocalDate.of(2025, 4, 1),
                    LocalDate.of(2026, 3, 31)
            );
            assertThat(result2025FY).isFalse();
            
            // Act & Assert - 2024年12月から2025年6月のルールは重複する
            boolean resultOverlapping = workRuleDomainService.hasOverlappingPeriod(
                    existingRules,
                    LocalDate.of(2024, 12, 1),
                    LocalDate.of(2025, 6, 30)
            );
            assertThat(resultOverlapping).isTrue();
        }
    }
}