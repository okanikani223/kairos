package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.DuplicateResourceException;
import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterDefaultWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.DefaultWorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RegisterDefaultWorkRuleUseCaseのテストクラス
 */
class RegisterDefaultWorkRuleUseCaseTest {

    @Mock
    private DefaultWorkRuleRepository defaultWorkRuleRepository;

    private RegisterDefaultWorkRuleUseCase registerDefaultWorkRuleUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registerDefaultWorkRuleUseCase = new RegisterDefaultWorkRuleUseCase(defaultWorkRuleRepository);
    }

    @Test
    void execute_正常ケース_デフォルト勤怠ルールが登録される() {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        RegisterDefaultWorkRuleRequest request = new RegisterDefaultWorkRuleRequest(
            1001L,
            35.6762,
            139.6503,
            userDto,
            LocalTime.of(9, 0),
            LocalTime.of(17, 30),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0)
        );

        User user = new User("testuser");
        DefaultWorkRule savedRule = new DefaultWorkRule(
            1L, 1001L, 35.6762, 139.6503, user,
            LocalTime.of(9, 0), LocalTime.of(17, 30),
            LocalTime.of(12, 0), LocalTime.of(13, 0)
        );
        when(defaultWorkRuleRepository.findByUserAndWorkPlaceId(user, 1001L)).thenReturn(null);
        when(defaultWorkRuleRepository.save(any(DefaultWorkRule.class))).thenReturn(savedRule);

        // Act
        DefaultWorkRuleResponse response = registerDefaultWorkRuleUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(1001L, response.workPlaceId());
        assertEquals(35.6762, response.latitude());
        assertEquals(139.6503, response.longitude());
        assertEquals("testuser", response.user().userId());
        assertEquals(LocalTime.of(9, 0), response.standardStartTime());
        assertEquals(LocalTime.of(17, 30), response.standardEndTime());
        assertEquals(LocalTime.of(12, 0), response.breakStartTime());
        assertEquals(LocalTime.of(13, 0), response.breakEndTime());

        verify(defaultWorkRuleRepository, times(1)).findByUserAndWorkPlaceId(user, 1001L);
        verify(defaultWorkRuleRepository, times(1)).save(any(DefaultWorkRule.class));
    }

    @Test
    void execute_既存デフォルト勤怠ルールあり_例外が発生する() {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        RegisterDefaultWorkRuleRequest request = new RegisterDefaultWorkRuleRequest(
            1001L,
            35.6762,
            139.6503,
            userDto,
            LocalTime.of(9, 0),
            LocalTime.of(17, 30),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0)
        );

        User user = new User("testuser");
        DefaultWorkRule existingDefaultWorkRule = new DefaultWorkRule(
            1L, 1001L, 35.6762, 139.6503, user,
            LocalTime.of(8, 30), LocalTime.of(17, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0)
        );
        when(defaultWorkRuleRepository.findByUserAndWorkPlaceId(user, 1001L)).thenReturn(existingDefaultWorkRule);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> registerDefaultWorkRuleUseCase.execute(request)
        );
        assertEquals("指定されたユーザーと勤怠先の組み合わせでデフォルト勤怠ルールが既に存在します", exception.getMessage());

        verify(defaultWorkRuleRepository, times(1)).findByUserAndWorkPlaceId(user, 1001L);
        verify(defaultWorkRuleRepository, never()).save(any());
    }

    @Test
    void execute_休憩時刻なし_デフォルト勤怠ルールが登録される() {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        RegisterDefaultWorkRuleRequest request = new RegisterDefaultWorkRuleRequest(
            1001L,
            35.6762,
            139.6503,
            userDto,
            LocalTime.of(9, 0),
            LocalTime.of(17, 30),
            null,
            null
        );

        User user = new User("testuser");
        DefaultWorkRule savedRule = new DefaultWorkRule(
            1L, 1001L, 35.6762, 139.6503, user,
            LocalTime.of(9, 0), LocalTime.of(17, 30),
            null, null
        );
        when(defaultWorkRuleRepository.findByUserAndWorkPlaceId(user, 1001L)).thenReturn(null);
        when(defaultWorkRuleRepository.save(any(DefaultWorkRule.class))).thenReturn(savedRule);

        // Act
        DefaultWorkRuleResponse response = registerDefaultWorkRuleUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertNull(response.breakStartTime());
        assertNull(response.breakEndTime());

        verify(defaultWorkRuleRepository, times(1)).save(any(DefaultWorkRule.class));
    }

    @Test
    void execute_nullリクエスト_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> registerDefaultWorkRuleUseCase.execute(null)
        );
        assertEquals("リクエストは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new RegisterDefaultWorkRuleUseCase(null)
        );
        assertEquals("defaultWorkRuleRepositoryは必須です", exception.getMessage());
    }
}