package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import com.github.okanikani.kairos.rules.domains.service.WorkRuleDomainService;
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
import static org.mockito.Mockito.*;

class RegisterWorkRuleUseCaseTest {

    private RegisterWorkRuleUseCase registerWorkRuleUseCase;

    @Mock
    private WorkRuleRepository workRuleRepository;

    @Mock
    private WorkRuleDomainService workRuleDomainService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registerWorkRuleUseCase = new RegisterWorkRuleUseCase(workRuleRepository, workRuleDomainService);
    }

    @Test
    void execute_正常ケース_勤怠ルールが登録される() {
        // Arrange
        RegisterWorkRuleRequest request = new RegisterWorkRuleRequest(
            1L,                                    // 勤怠先ID
            35.6762,                              // 緯度
            139.6503,                             // 経度
            new UserDto("testuser"),              // ユーザー
            LocalTime.of(9, 0),                   // 規定勤怠開始時刻
            LocalTime.of(17, 30),                 // 規定勤怠終了時刻
            LocalTime.of(12, 0),                  // 規定休憩開始時刻
            LocalTime.of(13, 0),                  // 規定休憩終了時刻
            LocalDate.of(2024, 1, 1),             // 所属開始日
            LocalDate.of(2024, 12, 31)            // 所属終了日
        );

        when(workRuleRepository.findByUser(any(User.class))).thenReturn(Arrays.asList());
        when(workRuleDomainService.hasOverlappingPeriod(any(), any(), any())).thenReturn(false);
        doNothing().when(workRuleRepository).save(any(WorkRule.class));

        // Act
        WorkRuleResponse response = registerWorkRuleUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(request.workPlaceId(), response.workPlaceId());
        assertEquals(request.latitude(), response.latitude());
        assertEquals(request.longitude(), response.longitude());
        assertEquals(request.user(), response.user());
        assertEquals(request.standardStartTime(), response.standardStartTime());
        assertEquals(request.standardEndTime(), response.standardEndTime());
        assertEquals(request.breakStartTime(), response.breakStartTime());
        assertEquals(request.breakEndTime(), response.breakEndTime());
        assertEquals(request.membershipStartDate(), response.membershipStartDate());
        assertEquals(request.membershipEndDate(), response.membershipEndDate());

        verify(workRuleRepository, times(1)).save(any(WorkRule.class));
    }

    @Test
    void execute_異常ケース_nullリクエストで例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> registerWorkRuleUseCase.execute(null)
        );
        assertEquals("requestは必須です", exception.getMessage());

        verify(workRuleRepository, never()).save(any());
    }

    @Test
    void execute_休憩時間なしの場合_正常に登録される() {
        // Arrange
        RegisterWorkRuleRequest request = new RegisterWorkRuleRequest(
            1L,                                    
            35.6762,                              
            139.6503,                             
            new UserDto("testuser"),              
            LocalTime.of(9, 0),                   
            LocalTime.of(17, 30),                 
            null,                                 // 休憩開始時刻なし
            null,                                 // 休憩終了時刻なし
            LocalDate.of(2024, 1, 1),             
            LocalDate.of(2024, 12, 31)            
        );

        when(workRuleRepository.findByUser(any(User.class))).thenReturn(Arrays.asList());
        when(workRuleDomainService.hasOverlappingPeriod(any(), any(), any())).thenReturn(false);
        doNothing().when(workRuleRepository).save(any(WorkRule.class));

        // Act
        WorkRuleResponse response = registerWorkRuleUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertNull(response.breakStartTime());
        assertNull(response.breakEndTime());

        verify(workRuleRepository, times(1)).save(any(WorkRule.class));
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new RegisterWorkRuleUseCase(null, workRuleDomainService)
        );
        assertEquals("workRuleRepositoryは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullDomainService_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new RegisterWorkRuleUseCase(workRuleRepository, null)
        );
        assertEquals("workRuleDomainServiceは必須です", exception.getMessage());
    }

    @Test
    void execute_所属期間重複_例外が発生する() {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        RegisterWorkRuleRequest request = new RegisterWorkRuleRequest(
            1L,
            35.6762,
            139.6503,
            userDto,
            LocalTime.of(9, 0),
            LocalTime.of(17, 30),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0),
            LocalDate.of(2024, 6, 1),     // 重複する期間
            LocalDate.of(2024, 8, 31)
        );

        // 既存の勤怠ルール（重複する期間）
        User user = new User("testuser");
        List<WorkRule> existingRules = Arrays.asList(
            new WorkRule(
                1L,
                1L,
                35.6762,
                139.6503,
                user,
                LocalTime.of(9, 0),
                LocalTime.of(17, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2024, 5, 1),     // 既存期間: 2024/5/1 - 2024/7/31
                LocalDate.of(2024, 7, 31)
            )
        );

        when(workRuleRepository.findByUser(user)).thenReturn(existingRules);
        when(workRuleDomainService.hasOverlappingPeriod(any(), any(), any())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> registerWorkRuleUseCase.execute(request)
        );
        assertEquals("指定された所属期間は既存の勤怠ルールと重複しています", exception.getMessage());

        verify(workRuleRepository, never()).save(any());
    }

    @Test
    void execute_所属期間重複なし_正常に登録される() {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        RegisterWorkRuleRequest request = new RegisterWorkRuleRequest(
            1L,
            35.6762,
            139.6503,
            userDto,
            LocalTime.of(9, 0),
            LocalTime.of(17, 30),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0),
            LocalDate.of(2024, 8, 1),     // 重複しない期間
            LocalDate.of(2024, 10, 31)
        );

        // 既存の勤怠ルール（重複しない期間）
        User user = new User("testuser");
        List<WorkRule> existingRules = Arrays.asList(
            new WorkRule(
                1L,
                1L,
                35.6762,
                139.6503,
                user,
                LocalTime.of(9, 0),
                LocalTime.of(17, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2024, 5, 1),     // 既存期間: 2024/5/1 - 2024/7/31
                LocalDate.of(2024, 7, 31)
            )
        );

        when(workRuleRepository.findByUser(user)).thenReturn(existingRules);
        when(workRuleDomainService.hasOverlappingPeriod(any(), any(), any())).thenReturn(false);
        doNothing().when(workRuleRepository).save(any(WorkRule.class));

        // Act
        WorkRuleResponse response = registerWorkRuleUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(request.workPlaceId(), response.workPlaceId());

        verify(workRuleRepository, times(1)).findByUser(user);
        verify(workRuleRepository, times(1)).save(any(WorkRule.class));
    }

    @Test
    void execute_異なるユーザーで所属期間重複_正常に登録される() {
        // Arrange
        UserDto userDto = new UserDto("testuser2");
        RegisterWorkRuleRequest request = new RegisterWorkRuleRequest(
            1L,
            35.6762,
            139.6503,
            userDto,
            LocalTime.of(9, 0),
            LocalTime.of(17, 30),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0),
            LocalDate.of(2024, 6, 1),     // testuser1と同じ期間だが、ユーザーが異なる
            LocalDate.of(2024, 8, 31)
        );

        // 対象ユーザーには既存ルールなし
        User user = new User("testuser2");
        when(workRuleRepository.findByUser(user)).thenReturn(Arrays.asList());
        when(workRuleDomainService.hasOverlappingPeriod(any(), any(), any())).thenReturn(false);
        doNothing().when(workRuleRepository).save(any(WorkRule.class));

        // Act
        WorkRuleResponse response = registerWorkRuleUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(request.workPlaceId(), response.workPlaceId());

        verify(workRuleRepository, times(1)).findByUser(user);
        verify(workRuleRepository, times(1)).save(any(WorkRule.class));
    }
}