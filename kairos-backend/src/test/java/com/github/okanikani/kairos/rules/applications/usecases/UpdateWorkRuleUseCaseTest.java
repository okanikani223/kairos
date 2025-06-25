package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.applications.usecases.dto.UpdateWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UpdateWorkRuleUseCaseTest {

    private UpdateWorkRuleUseCase updateWorkRuleUseCase;

    @Mock
    private WorkRuleRepository workRuleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        updateWorkRuleUseCase = new UpdateWorkRuleUseCase(workRuleRepository);
    }

    @Test
    void execute_正常ケース_勤務ルールが更新される() {
        // Arrange
        Long workRuleId = 1L;
        String userId = "testuser";
        User user = new User(userId);
        UserDto userDto = new UserDto(userId);
        
        WorkRule existingWorkRule = new WorkRule(workRuleId, 100L, 35.6812, 139.7671, user,
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        UpdateWorkRuleRequest request = new UpdateWorkRuleRequest(
            200L, 35.6900, 139.7800, userDto,
            LocalTime.of(10, 0), LocalTime.of(19, 0),
            LocalTime.of(12, 30), LocalTime.of(13, 30),
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30)
        );

        when(workRuleRepository.findById(eq(workRuleId))).thenReturn(existingWorkRule);

        // Act
        WorkRuleResponse response = updateWorkRuleUseCase.execute(workRuleId, request, userId);

        // Assert
        assertNotNull(response);
        assertEquals(workRuleId, response.id());
        assertEquals(200L, response.workPlaceId());
        assertEquals(35.6900, response.latitude());
        assertEquals(139.7800, response.longitude());
        assertEquals(userId, response.user().userId());
        assertEquals(LocalTime.of(10, 0), response.standardStartTime());
        assertEquals(LocalTime.of(19, 0), response.standardEndTime());
        assertEquals(LocalTime.of(12, 30), response.breakStartTime());
        assertEquals(LocalTime.of(13, 30), response.breakEndTime());
        assertEquals(LocalDate.of(2024, 2, 1), response.membershipStartDate());
        assertEquals(LocalDate.of(2024, 11, 30), response.membershipEndDate());
        
        verify(workRuleRepository, times(1)).findById(eq(workRuleId));
        verify(workRuleRepository, times(1)).save(any(WorkRule.class));
    }

    @Test
    void execute_勤務ルールが存在しない場合_例外が発生する() {
        // Arrange
        Long workRuleId = 999L;
        String userId = "testuser";
        UserDto userDto = new UserDto(userId);
        
        UpdateWorkRuleRequest request = new UpdateWorkRuleRequest(
            200L, 35.6900, 139.7800, userDto,
            LocalTime.of(10, 0), LocalTime.of(19, 0),
            null, null,
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30)
        );
        
        when(workRuleRepository.findById(eq(workRuleId))).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> updateWorkRuleUseCase.execute(workRuleId, request, userId)
        );
        assertEquals("指定された勤務ルールが存在しません", exception.getMessage());
        verify(workRuleRepository, times(1)).findById(eq(workRuleId));
        verify(workRuleRepository, never()).save(any());
    }

    @Test
    void execute_権限がない場合_例外が発生する() {
        // Arrange
        Long workRuleId = 1L;
        String userId = "testuser";
        String otherUserId = "otheruser";
        User otherUser = new User(otherUserId);
        UserDto userDto = new UserDto(userId);
        
        WorkRule existingWorkRule = new WorkRule(workRuleId, 100L, 35.6812, 139.7671, otherUser,
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        UpdateWorkRuleRequest request = new UpdateWorkRuleRequest(
            200L, 35.6900, 139.7800, userDto,
            LocalTime.of(10, 0), LocalTime.of(19, 0),
            null, null,
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30)
        );

        when(workRuleRepository.findById(eq(workRuleId))).thenReturn(existingWorkRule);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> updateWorkRuleUseCase.execute(workRuleId, request, userId)
        );
        assertEquals("この勤務ルールを更新する権限がありません", exception.getMessage());
        verify(workRuleRepository, times(1)).findById(eq(workRuleId));
        verify(workRuleRepository, never()).save(any());
    }

    @Test
    void execute_nullWorkRuleId_例外が発生する() {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        UpdateWorkRuleRequest request = new UpdateWorkRuleRequest(
            200L, 35.6900, 139.7800, userDto,
            LocalTime.of(10, 0), LocalTime.of(19, 0),
            null, null,
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30)
        );

        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> updateWorkRuleUseCase.execute(null, request, "testuser")
        );
        assertEquals("workRuleIdは必須です", exception.getMessage());
        verify(workRuleRepository, never()).findById(any());
        verify(workRuleRepository, never()).save(any());
    }

    @Test
    void execute_nullRequest_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> updateWorkRuleUseCase.execute(1L, null, "testuser")
        );
        assertEquals("requestは必須です", exception.getMessage());
        verify(workRuleRepository, never()).findById(any());
        verify(workRuleRepository, never()).save(any());
    }

    @Test
    void execute_nullUserId_例外が発生する() {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        UpdateWorkRuleRequest request = new UpdateWorkRuleRequest(
            200L, 35.6900, 139.7800, userDto,
            LocalTime.of(10, 0), LocalTime.of(19, 0),
            null, null,
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30)
        );

        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> updateWorkRuleUseCase.execute(1L, request, null)
        );
        assertEquals("userIdは必須です", exception.getMessage());
        verify(workRuleRepository, never()).findById(any());
        verify(workRuleRepository, never()).save(any());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new UpdateWorkRuleUseCase(null)
        );
        assertEquals("workRuleRepositoryは必須です", exception.getMessage());
    }
}