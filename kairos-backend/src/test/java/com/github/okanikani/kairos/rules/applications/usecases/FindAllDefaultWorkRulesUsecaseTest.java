package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.DefaultWorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FindAllDefaultWorkRulesUsecaseTest {

    private FindAllDefaultWorkRulesUsecase findAllDefaultWorkRulesUsecase;

    @Mock
    private DefaultWorkRuleRepository defaultWorkRuleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        findAllDefaultWorkRulesUsecase = new FindAllDefaultWorkRulesUsecase(defaultWorkRuleRepository);
    }

    @Test
    void execute_正常ケース_ユーザーの全デフォルト勤務ルールが取得される() {
        // Arrange
        String userId = "testuser";
        User user = new User(userId);
        
        List<DefaultWorkRule> defaultWorkRules = Arrays.asList(
            new DefaultWorkRule(1L, 100L, 35.6812, 139.7671, user,
                LocalTime.of(9, 0), LocalTime.of(18, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0)),
            new DefaultWorkRule(2L, 200L, 35.6813, 139.7672, user,
                LocalTime.of(10, 0), LocalTime.of(19, 0),
                null, null)
        );

        when(defaultWorkRuleRepository.findByUser(eq(user))).thenReturn(defaultWorkRules);

        // Act
        List<DefaultWorkRuleResponse> response = findAllDefaultWorkRulesUsecase.execute(userId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        
        DefaultWorkRuleResponse first = response.get(0);
        assertEquals(1L, first.id());
        assertEquals(100L, first.workPlaceId());
        assertEquals(35.6812, first.latitude());
        assertEquals(139.7671, first.longitude());
        assertEquals(userId, first.user().userId());
        assertEquals(LocalTime.of(9, 0), first.standardStartTime());
        assertEquals(LocalTime.of(18, 0), first.standardEndTime());
        assertEquals(LocalTime.of(12, 0), first.breakStartTime());
        assertEquals(LocalTime.of(13, 0), first.breakEndTime());
        
        verify(defaultWorkRuleRepository, times(1)).findByUser(eq(user));
    }

    @Test
    void execute_デフォルト勤務ルールが存在しない場合_空のリストが返される() {
        // Arrange
        String userId = "testuser";
        User user = new User(userId);
        
        when(defaultWorkRuleRepository.findByUser(eq(user))).thenReturn(List.of());

        // Act
        List<DefaultWorkRuleResponse> response = findAllDefaultWorkRulesUsecase.execute(userId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(defaultWorkRuleRepository, times(1)).findByUser(eq(user));
    }

    @Test
    void execute_nullUserId_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> findAllDefaultWorkRulesUsecase.execute(null)
        );
        assertEquals("userIdは必須です", exception.getMessage());
        verify(defaultWorkRuleRepository, never()).findByUser(any());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new FindAllDefaultWorkRulesUsecase(null)
        );
        assertEquals("defaultWorkRuleRepositoryは必須です", exception.getMessage());
    }
}