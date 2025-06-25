package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FindAllWorkRulesUsecaseTest {

    private FindAllWorkRulesUsecase findAllWorkRulesUsecase;

    @Mock
    private WorkRuleRepository workRuleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        findAllWorkRulesUsecase = new FindAllWorkRulesUsecase(workRuleRepository);
    }

    @Test
    void execute_正常ケース_ユーザーの全勤務ルールが取得される() {
        // Arrange
        String userId = "testuser";
        User user = new User(userId);
        
        List<WorkRule> workRules = Arrays.asList(
            new WorkRule(1L, 100L, 35.6812, 139.7671, user,
                LocalTime.of(9, 0), LocalTime.of(18, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0),
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
            new WorkRule(2L, 200L, 35.6813, 139.7672, user,
                LocalTime.of(10, 0), LocalTime.of(19, 0),
                null, null,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
        );

        when(workRuleRepository.findByUser(eq(user))).thenReturn(workRules);

        // Act
        List<WorkRuleResponse> response = findAllWorkRulesUsecase.execute(userId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        
        WorkRuleResponse first = response.get(0);
        assertEquals(1L, first.id());
        assertEquals(100L, first.workPlaceId());
        assertEquals(35.6812, first.latitude());
        assertEquals(139.7671, first.longitude());
        assertEquals(userId, first.user().userId());
        assertEquals(LocalTime.of(9, 0), first.standardStartTime());
        assertEquals(LocalTime.of(18, 0), first.standardEndTime());
        assertEquals(LocalTime.of(12, 0), first.breakStartTime());
        assertEquals(LocalTime.of(13, 0), first.breakEndTime());
        assertEquals(LocalDate.of(2024, 1, 1), first.membershipStartDate());
        assertEquals(LocalDate.of(2024, 12, 31), first.membershipEndDate());
        
        verify(workRuleRepository, times(1)).findByUser(eq(user));
    }

    @Test
    void execute_勤務ルールが存在しない場合_空のリストが返される() {
        // Arrange
        String userId = "testuser";
        User user = new User(userId);
        
        when(workRuleRepository.findByUser(eq(user))).thenReturn(List.of());

        // Act
        List<WorkRuleResponse> response = findAllWorkRulesUsecase.execute(userId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(workRuleRepository, times(1)).findByUser(eq(user));
    }

    @Test
    void execute_nullUserId_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> findAllWorkRulesUsecase.execute(null)
        );
        assertEquals("userIdは必須です", exception.getMessage());
        verify(workRuleRepository, never()).findByUser(any());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new FindAllWorkRulesUsecase(null)
        );
        assertEquals("workRuleRepositoryは必須です", exception.getMessage());
    }
}