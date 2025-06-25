package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DeleteWorkRuleUseCaseTest {

    private DeleteWorkRuleUseCase deleteWorkRuleUseCase;

    @Mock
    private WorkRuleRepository workRuleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deleteWorkRuleUseCase = new DeleteWorkRuleUseCase(workRuleRepository);
    }

    @Test
    void execute_正常ケース_勤務ルールが削除される() {
        // Arrange
        Long workRuleId = 1L;
        String userId = "testuser";
        User user = new User(userId);
        
        WorkRule workRule = new WorkRule(workRuleId, 100L, 35.6812, 139.7671, user,
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        when(workRuleRepository.findById(eq(workRuleId))).thenReturn(workRule);
        doNothing().when(workRuleRepository).deleteById(eq(workRuleId));

        // Act
        deleteWorkRuleUseCase.execute(workRuleId, userId);

        // Assert
        verify(workRuleRepository, times(1)).findById(eq(workRuleId));
        verify(workRuleRepository, times(1)).deleteById(eq(workRuleId));
    }

    @Test
    void execute_勤務ルールが存在しない場合_例外が発生する() {
        // Arrange
        Long workRuleId = 999L;
        String userId = "testuser";
        
        when(workRuleRepository.findById(eq(workRuleId))).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> deleteWorkRuleUseCase.execute(workRuleId, userId)
        );
        assertEquals("指定された勤務ルールが存在しません", exception.getMessage());
        verify(workRuleRepository, times(1)).findById(eq(workRuleId));
        verify(workRuleRepository, never()).deleteById(any());
    }

    @Test
    void execute_権限がない場合_例外が発生する() {
        // Arrange
        Long workRuleId = 1L;
        String userId = "testuser";
        String otherUserId = "otheruser";
        User otherUser = new User(otherUserId);
        
        WorkRule workRule = new WorkRule(workRuleId, 100L, 35.6812, 139.7671, otherUser,
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        when(workRuleRepository.findById(eq(workRuleId))).thenReturn(workRule);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> deleteWorkRuleUseCase.execute(workRuleId, userId)
        );
        assertEquals("この勤務ルールを削除する権限がありません", exception.getMessage());
        verify(workRuleRepository, times(1)).findById(eq(workRuleId));
        verify(workRuleRepository, never()).deleteById(any());
    }

    @Test
    void execute_nullWorkRuleId_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> deleteWorkRuleUseCase.execute(null, "testuser")
        );
        assertEquals("workRuleIdは必須です", exception.getMessage());
        verify(workRuleRepository, never()).findById(any());
        verify(workRuleRepository, never()).deleteById(any());
    }

    @Test
    void execute_nullUserId_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> deleteWorkRuleUseCase.execute(1L, null)
        );
        assertEquals("userIdは必須です", exception.getMessage());
        verify(workRuleRepository, never()).findById(any());
        verify(workRuleRepository, never()).deleteById(any());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new DeleteWorkRuleUseCase(null)
        );
        assertEquals("workRuleRepositoryは必須です", exception.getMessage());
    }
}