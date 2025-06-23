package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterWorkRuleUsecaseTest {

    private RegisterWorkRuleUsecase registerWorkRuleUsecase;

    @Mock
    private WorkRuleRepository workRuleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registerWorkRuleUsecase = new RegisterWorkRuleUsecase(workRuleRepository);
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

        doNothing().when(workRuleRepository).save(any(WorkRule.class));

        // Act
        WorkRuleResponse response = registerWorkRuleUsecase.execute(request);

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
            () -> registerWorkRuleUsecase.execute(null)
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

        doNothing().when(workRuleRepository).save(any(WorkRule.class));

        // Act
        WorkRuleResponse response = registerWorkRuleUsecase.execute(request);

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
            () -> new RegisterWorkRuleUsecase(null)
        );
        assertEquals("workRuleRepositoryは必須です", exception.getMessage());
    }
}