package com.github.okanikani.kairos.rules.others.repositories;

import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import com.github.okanikani.kairos.rules.others.jpa.entities.WorkRuleJpaEntity;
import com.github.okanikani.kairos.rules.others.jpa.repositories.WorkRuleJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JpaWorkRuleRepositoryのUnit Test
 * 
 * テスト対象: ドメインモデルとJPAエンティティ間の変換とCRUD操作
 */
@ExtendWith(MockitoExtension.class)
class JpaWorkRuleRepositoryTest {

    @Mock
    private WorkRuleJpaRepository workRuleJpaRepository;

    @InjectMocks
    private JpaWorkRuleRepository jpaWorkRuleRepository;

    private WorkRule testWorkRule;
    private WorkRuleJpaEntity testJpaEntity;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test-user-001");
        
        testWorkRule = new WorkRule(
                1L,                                      // id
                100L,                                    // workPlaceId
                35.6762,                                 // latitude (東京駅)
                139.6503,                                // longitude (東京駅)
                testUser,                                // user
                LocalTime.of(9, 0),                      // standardStartTime
                LocalTime.of(18, 0),                     // standardEndTime
                LocalTime.of(12, 0),                     // breakStartTime
                LocalTime.of(13, 0),                     // breakEndTime
                LocalDate.of(2025, 1, 1),                // membershipStartDate
                LocalDate.of(2025, 12, 31)               // membershipEndDate
        );

        testJpaEntity = new WorkRuleJpaEntity(
                100L,                                    // workPlaceId
                35.6762,                                 // latitude
                139.6503,                                // longitude
                "test-user-001",                         // userId
                LocalTime.of(9, 0),                      // standardStartTime
                LocalTime.of(18, 0),                     // standardEndTime
                LocalTime.of(12, 0),                     // breakStartTime
                LocalTime.of(13, 0),                     // breakEndTime
                LocalDate.of(2025, 1, 1),                // membershipStartDate
                LocalDate.of(2025, 12, 31)               // membershipEndDate
        );
    }

    @Test
    void save_正常なWorkRule_正常に保存されドメインモデルが返される() {
        // Given
        when(workRuleJpaRepository.save(any(WorkRuleJpaEntity.class))).thenReturn(testJpaEntity);

        // When
        WorkRule result = jpaWorkRuleRepository.save(testWorkRule);

        // Then
        verify(workRuleJpaRepository).save(any(WorkRuleJpaEntity.class));
        assertThat(result).isNotNull();
        assertThat(result.workPlaceId()).isEqualTo(100L);
        assertThat(result.latitude()).isEqualTo(35.6762);
        assertThat(result.longitude()).isEqualTo(139.6503);
        assertThat(result.user().userId()).isEqualTo("test-user-001");
        assertThat(result.standardStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.standardEndTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.breakStartTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(result.breakEndTime()).isEqualTo(LocalTime.of(13, 0));
        assertThat(result.membershipStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.membershipEndDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    void findById_存在するID_対応するWorkRuleが返される() {
        // Given
        when(workRuleJpaRepository.findById(1L)).thenReturn(Optional.of(testJpaEntity));

        // When
        WorkRule result = jpaWorkRuleRepository.findById(1L);

        // Then
        verify(workRuleJpaRepository).findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.workPlaceId()).isEqualTo(100L);
        assertThat(result.user().userId()).isEqualTo("test-user-001");
    }

    @Test
    void findById_存在しないID_nullが返される() {
        // Given
        when(workRuleJpaRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        WorkRule result = jpaWorkRuleRepository.findById(999L);

        // Then
        verify(workRuleJpaRepository).findById(999L);
        assertThat(result).isNull();
    }

    @Test
    void findByUser_有効なユーザー_ユーザーのWorkRuleリストが返される() {
        // Given
        List<WorkRuleJpaEntity> jpaEntities = List.of(testJpaEntity);
        when(workRuleJpaRepository.findByUserIdOrderByMembershipStartDateDesc("test-user-001"))
                .thenReturn(jpaEntities);

        // When
        List<WorkRule> result = jpaWorkRuleRepository.findByUser(testUser);

        // Then
        verify(workRuleJpaRepository).findByUserIdOrderByMembershipStartDateDesc("test-user-001");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).user().userId()).isEqualTo("test-user-001");
        assertThat(result.get(0).workPlaceId()).isEqualTo(100L);
    }

    @Test
    void findActiveByUserAndDate_有効なルールが存在する場合_対応するWorkRuleが返される() {
        // Given
        LocalDate targetDate = LocalDate.of(2025, 6, 15);
        when(workRuleJpaRepository.findByUserIdAndEffectiveDate("test-user-001", targetDate))
                .thenReturn(Optional.of(testJpaEntity));

        // When
        List<WorkRule> result = jpaWorkRuleRepository.findActiveByUserAndDate(testUser, targetDate);

        // Then
        verify(workRuleJpaRepository).findByUserIdAndEffectiveDate("test-user-001", targetDate);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).user().userId()).isEqualTo("test-user-001");
    }

    @Test
    void findActiveByUserAndDate_有効なルールが存在しない場合_空のリストが返される() {
        // Given
        LocalDate targetDate = LocalDate.of(2024, 1, 1);
        when(workRuleJpaRepository.findByUserIdAndEffectiveDate("test-user-001", targetDate))
                .thenReturn(Optional.empty());

        // When
        List<WorkRule> result = jpaWorkRuleRepository.findActiveByUserAndDate(testUser, targetDate);

        // Then
        verify(workRuleJpaRepository).findByUserIdAndEffectiveDate("test-user-001", targetDate);
        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_存在するID_正常に削除される() {
        // When
        jpaWorkRuleRepository.deleteById(1L);

        // Then
        verify(workRuleJpaRepository).deleteById(1L);
    }

    @Test
    void findByUserAndEffectiveDate_有効な期間内の日付_対応するWorkRuleが返される() {
        // Given
        LocalDate effectiveDate = LocalDate.of(2025, 6, 15);
        when(workRuleJpaRepository.findByUserIdAndEffectiveDate("test-user-001", effectiveDate))
                .thenReturn(Optional.of(testJpaEntity));

        // When
        Optional<WorkRule> result = jpaWorkRuleRepository.findByUserAndEffectiveDate(testUser, effectiveDate);

        // Then
        verify(workRuleJpaRepository).findByUserIdAndEffectiveDate("test-user-001", effectiveDate);
        assertThat(result).isPresent();
        assertThat(result.get().user().userId()).isEqualTo("test-user-001");
    }

    @Test
    void findOverlappingRules_重複期間が存在する場合_重複するWorkRuleリストが返される() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 15);
        LocalDate endDate = LocalDate.of(2025, 6, 15);
        List<WorkRuleJpaEntity> overlappingEntities = List.of(testJpaEntity);
        when(workRuleJpaRepository.findOverlappingRules("test-user-001", startDate, endDate))
                .thenReturn(overlappingEntities);

        // When
        List<WorkRule> result = jpaWorkRuleRepository.findOverlappingRules(testUser, startDate, endDate);

        // Then
        verify(workRuleJpaRepository).findOverlappingRules("test-user-001", startDate, endDate);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).user().userId()).isEqualTo("test-user-001");
    }

    @Test
    void findOverlappingRulesExcludingId_特定IDを除外して重複期間検索_該当するWorkRuleリストが返される() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 15);
        LocalDate endDate = LocalDate.of(2025, 6, 15);
        Long excludeId = 2L;
        List<WorkRuleJpaEntity> overlappingEntities = List.of(testJpaEntity);
        when(workRuleJpaRepository.findOverlappingRulesExcludingId("test-user-001", startDate, endDate, excludeId))
                .thenReturn(overlappingEntities);

        // When
        List<WorkRule> result = jpaWorkRuleRepository.findOverlappingRulesExcludingId(testUser, startDate, endDate, excludeId);

        // Then
        verify(workRuleJpaRepository).findOverlappingRulesExcludingId("test-user-001", startDate, endDate, excludeId);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).user().userId()).isEqualTo("test-user-001");
    }

    @Test
    void existsById_存在するID_trueが返される() {
        // Given
        when(workRuleJpaRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = jpaWorkRuleRepository.existsById(1L);

        // Then
        verify(workRuleJpaRepository).existsById(1L);
        assertThat(result).isTrue();
    }

    @Test
    void existsById_存在しないID_falseが返される() {
        // Given
        when(workRuleJpaRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = jpaWorkRuleRepository.existsById(999L);

        // Then
        verify(workRuleJpaRepository).existsById(999L);
        assertThat(result).isFalse();
    }

    @Test
    void 変換メソッド_ドメインモデルからJPAエンティティ_正常に変換される() {
        // Given: 休憩時間なしのWorkRule
        WorkRule workRuleWithoutBreak = new WorkRule(
                null,                                    // id (新規作成)
                200L,                                    // workPlaceId
                35.6896,                                 // latitude (新宿駅)
                139.7006,                                // longitude (新宿駅)
                testUser,                                // user
                LocalTime.of(10, 0),                     // standardStartTime
                LocalTime.of(19, 0),                     // standardEndTime
                null,                                    // breakStartTime (休憩なし)
                null,                                    // breakEndTime (休憩なし)
                LocalDate.of(2025, 2, 1),                // membershipStartDate
                LocalDate.of(2025, 11, 30)               // membershipEndDate
        );

        WorkRuleJpaEntity expectedJpaEntity = new WorkRuleJpaEntity(
                200L,
                35.6896,
                139.7006,
                "test-user-001",
                LocalTime.of(10, 0),
                LocalTime.of(19, 0),
                null,
                null,
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 11, 30)
        );

        when(workRuleJpaRepository.save(any(WorkRuleJpaEntity.class))).thenReturn(expectedJpaEntity);

        // When
        WorkRule result = jpaWorkRuleRepository.save(workRuleWithoutBreak);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.workPlaceId()).isEqualTo(200L);
        assertThat(result.latitude()).isEqualTo(35.6896);
        assertThat(result.longitude()).isEqualTo(139.7006);
        assertThat(result.user().userId()).isEqualTo("test-user-001");
        assertThat(result.standardStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.standardEndTime()).isEqualTo(LocalTime.of(19, 0));
        assertThat(result.breakStartTime()).isNull();
        assertThat(result.breakEndTime()).isNull();
        assertThat(result.membershipStartDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(result.membershipEndDate()).isEqualTo(LocalDate.of(2025, 11, 30));
    }
}