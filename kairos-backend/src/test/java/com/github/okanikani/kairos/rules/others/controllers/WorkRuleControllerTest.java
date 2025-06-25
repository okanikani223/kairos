package com.github.okanikani.kairos.rules.others.controllers;

import com.github.okanikani.kairos.rules.applications.usecases.DeleteWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.FindAllWorkRulesUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.FindWorkRuleByIdUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.RegisterWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.UpdateWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WorkRuleControllerTest {

    private WorkRuleController workRuleController;

    @Mock
    private RegisterWorkRuleUseCase registerWorkRuleUseCase;
    
    @Mock
    private FindAllWorkRulesUseCase findAllWorkRulesUseCase;
    
    @Mock
    private FindWorkRuleByIdUseCase findWorkRuleByIdUseCase;
    
    @Mock
    private UpdateWorkRuleUseCase updateWorkRuleUseCase;
    
    @Mock
    private DeleteWorkRuleUseCase deleteWorkRuleUseCase;
    
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workRuleController = new WorkRuleController(registerWorkRuleUseCase, findAllWorkRulesUseCase, findWorkRuleByIdUseCase, updateWorkRuleUseCase, deleteWorkRuleUseCase);
    }

    @Test
    void findAllWorkRules_正常ケース_200ステータスと勤務ルール一覧を返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
        List<WorkRuleResponse> expectedWorkRules = Arrays.asList(
            new WorkRuleResponse(1L, 100L, 35.6812, 139.7671, new UserDto("testuser"),
                LocalTime.of(9, 0), LocalTime.of(18, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0),
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
            new WorkRuleResponse(2L, 200L, 35.6813, 139.7672, new UserDto("testuser"),
                LocalTime.of(10, 0), LocalTime.of(19, 0),
                null, null,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
        );
        
        when(findAllWorkRulesUseCase.execute(anyString())).thenReturn(expectedWorkRules);

        // Act
        ResponseEntity<List<WorkRuleResponse>> response = workRuleController.findAllWorkRules(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).id());
        assertEquals(100L, response.getBody().get(0).workPlaceId());
        verify(findAllWorkRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findAllWorkRules_勤務ルールが存在しない場合_200ステータスと空リストを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findAllWorkRulesUseCase.execute(anyString())).thenReturn(List.of());

        // Act
        ResponseEntity<List<WorkRuleResponse>> response = workRuleController.findAllWorkRules(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(findAllWorkRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findAllWorkRules_ユースケースで例外発生_500ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findAllWorkRulesUseCase.execute(anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<List<WorkRuleResponse>> response = workRuleController.findAllWorkRules(authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(findAllWorkRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findWorkRuleById_正常ケース_200ステータスと勤務ルールを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        
        WorkRuleResponse expectedWorkRule = new WorkRuleResponse(workRuleId, 100L, 35.6812, 139.7671, new UserDto("testuser"),
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        
        when(findWorkRuleByIdUseCase.execute(eq(workRuleId), anyString())).thenReturn(expectedWorkRule);

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.findWorkRuleById(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(workRuleId, response.getBody().id());
        assertEquals(100L, response.getBody().workPlaceId());
        verify(findWorkRuleByIdUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void findWorkRuleById_勤務ルールが存在しない場合_404ステータスを返す() {
        // Arrange
        Long workRuleId = 999L;
        when(authentication.getName()).thenReturn("testuser");
        when(findWorkRuleByIdUseCase.execute(eq(workRuleId), anyString()))
            .thenThrow(new IllegalArgumentException("指定された勤務ルールが存在しません"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.findWorkRuleById(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(findWorkRuleByIdUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void findWorkRuleById_権限がない場合_403ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        when(findWorkRuleByIdUseCase.execute(eq(workRuleId), anyString()))
            .thenThrow(new IllegalArgumentException("この勤務ルールにアクセスする権限がありません"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.findWorkRuleById(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(findWorkRuleByIdUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void findWorkRuleById_予期しない例外発生_500ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        when(findWorkRuleByIdUseCase.execute(eq(workRuleId), anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.findWorkRuleById(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(findWorkRuleByIdUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void updateWorkRule_正常ケース_200ステータスと更新された勤務ルールを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        
        WorkRuleController.UpdateWorkRuleRequestBody requestBody = new WorkRuleController.UpdateWorkRuleRequestBody(
            200L, 35.6900, 139.7800,
            LocalTime.of(10, 0), LocalTime.of(19, 0),
            LocalTime.of(12, 30), LocalTime.of(13, 30),
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30)
        );
        
        WorkRuleResponse expectedResponse = new WorkRuleResponse(workRuleId, 200L, 35.6900, 139.7800, new UserDto("testuser"),
            LocalTime.of(10, 0), LocalTime.of(19, 0),
            LocalTime.of(12, 30), LocalTime.of(13, 30),
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30));
        
        when(updateWorkRuleUseCase.execute(eq(workRuleId), any(), anyString())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.updateWorkRule(workRuleId, requestBody, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(workRuleId, response.getBody().id());
        assertEquals(200L, response.getBody().workPlaceId());
        assertEquals(35.6900, response.getBody().latitude());
        verify(updateWorkRuleUseCase, times(1)).execute(eq(workRuleId), any(), eq("testuser"));
    }

    @Test
    void updateWorkRule_勤務ルールが存在しない場合_404ステータスを返す() {
        // Arrange
        Long workRuleId = 999L;
        when(authentication.getName()).thenReturn("testuser");
        
        WorkRuleController.UpdateWorkRuleRequestBody requestBody = new WorkRuleController.UpdateWorkRuleRequestBody(
            200L, 35.6900, 139.7800,
            LocalTime.of(10, 0), LocalTime.of(19, 0),
            null, null,
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30)
        );
        
        when(updateWorkRuleUseCase.execute(eq(workRuleId), any(), anyString()))
            .thenThrow(new IllegalArgumentException("指定された勤務ルールが存在しません"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.updateWorkRule(workRuleId, requestBody, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(updateWorkRuleUseCase, times(1)).execute(eq(workRuleId), any(), eq("testuser"));
    }

    @Test
    void updateWorkRule_権限がない場合_403ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        
        WorkRuleController.UpdateWorkRuleRequestBody requestBody = new WorkRuleController.UpdateWorkRuleRequestBody(
            200L, 35.6900, 139.7800,
            LocalTime.of(10, 0), LocalTime.of(19, 0),
            null, null,
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30)
        );
        
        when(updateWorkRuleUseCase.execute(eq(workRuleId), any(), anyString()))
            .thenThrow(new IllegalArgumentException("この勤務ルールを更新する権限がありません"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.updateWorkRule(workRuleId, requestBody, authentication);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(updateWorkRuleUseCase, times(1)).execute(eq(workRuleId), any(), eq("testuser"));
    }

    @Test
    void updateWorkRule_予期しない例外発生_500ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        
        WorkRuleController.UpdateWorkRuleRequestBody requestBody = new WorkRuleController.UpdateWorkRuleRequestBody(
            200L, 35.6900, 139.7800,
            LocalTime.of(10, 0), LocalTime.of(19, 0),
            null, null,
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30)
        );
        
        when(updateWorkRuleUseCase.execute(eq(workRuleId), any(), anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.updateWorkRule(workRuleId, requestBody, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(updateWorkRuleUseCase, times(1)).execute(eq(workRuleId), any(), eq("testuser"));
    }

    @Test
    void deleteWorkRule_正常ケース_204ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        doNothing().when(deleteWorkRuleUseCase).execute(eq(workRuleId), anyString());

        // Act
        ResponseEntity<Void> response = workRuleController.deleteWorkRule(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deleteWorkRuleUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void deleteWorkRule_勤務ルールが存在しない場合_404ステータスを返す() {
        // Arrange
        Long workRuleId = 999L;
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new IllegalArgumentException("指定された勤務ルールが存在しません"))
            .when(deleteWorkRuleUseCase).execute(eq(workRuleId), anyString());

        // Act
        ResponseEntity<Void> response = workRuleController.deleteWorkRule(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(deleteWorkRuleUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void deleteWorkRule_権限がない場合_403ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new IllegalArgumentException("この勤務ルールを削除する権限がありません"))
            .when(deleteWorkRuleUseCase).execute(eq(workRuleId), anyString());

        // Act
        ResponseEntity<Void> response = workRuleController.deleteWorkRule(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(deleteWorkRuleUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void deleteWorkRule_予期しない例外発生_500ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new RuntimeException("データベースエラー"))
            .when(deleteWorkRuleUseCase).execute(eq(workRuleId), anyString());

        // Act
        ResponseEntity<Void> response = workRuleController.deleteWorkRule(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(deleteWorkRuleUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void constructor_nullRegisterUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRuleController(null, findAllWorkRulesUseCase, findWorkRuleByIdUseCase, updateWorkRuleUseCase, deleteWorkRuleUseCase)
        );
        assertEquals("registerWorkRuleUseCaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullFindAllUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRuleController(registerWorkRuleUseCase, null, findWorkRuleByIdUseCase, updateWorkRuleUseCase, deleteWorkRuleUseCase)
        );
        assertEquals("findAllWorkRulesUseCaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullFindByIdUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRuleController(registerWorkRuleUseCase, findAllWorkRulesUseCase, null, updateWorkRuleUseCase, deleteWorkRuleUseCase)
        );
        assertEquals("findWorkRuleByIdUseCaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullUpdateUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRuleController(registerWorkRuleUseCase, findAllWorkRulesUseCase, findWorkRuleByIdUseCase, null, deleteWorkRuleUseCase)
        );
        assertEquals("updateWorkRuleUseCaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullDeleteUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRuleController(registerWorkRuleUseCase, findAllWorkRulesUseCase, findWorkRuleByIdUseCase, updateWorkRuleUseCase, null)
        );
        assertEquals("deleteWorkRuleUseCaseは必須です", exception.getMessage());
    }

    @Test
    void registerWorkRule_正常ケース_201ステータスと勤務ルールを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");

        WorkRuleController.RegisterWorkRuleRequestBody requestBody = new WorkRuleController.RegisterWorkRuleRequestBody(
            100L, 35.6812, 139.7671,
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );

        WorkRuleResponse expectedResponse = new WorkRuleResponse(
            1L, 100L, 35.6812, 139.7671, new UserDto("testuser"),
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );

        when(registerWorkRuleUseCase.execute(any())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.registerWorkRule(requestBody, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().user().userId());
        assertEquals(100L, response.getBody().workPlaceId());

        verify(registerWorkRuleUseCase, times(1)).execute(any());
    }

    @Test
    void registerWorkRule_バリデーションエラー_例外が発生する() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");

        WorkRuleController.RegisterWorkRuleRequestBody requestBody = new WorkRuleController.RegisterWorkRuleRequestBody(
            100L, 91.0, 139.7671, // 無効な緯度（範囲外）
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );

        when(registerWorkRuleUseCase.execute(any()))
            .thenThrow(new IllegalArgumentException("緯度は-90.0から90.0の範囲で指定してください"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            workRuleController.registerWorkRule(requestBody, authentication);
        });
        verify(registerWorkRuleUseCase, times(1)).execute(any());
    }

    @Test
    void registerWorkRule_時間順序違反_例外が発生する() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");

        WorkRuleController.RegisterWorkRuleRequestBody requestBody = new WorkRuleController.RegisterWorkRuleRequestBody(
            100L, 35.6812, 139.7671,
            LocalTime.of(18, 0), LocalTime.of(9, 0), // 開始時刻が終了時刻より後
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );

        when(registerWorkRuleUseCase.execute(any()))
            .thenThrow(new IllegalArgumentException("標準開始時刻は標準終了時刻より前である必要があります"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            workRuleController.registerWorkRule(requestBody, authentication);
        });
        verify(registerWorkRuleUseCase, times(1)).execute(any());
    }

    @Test
    void registerWorkRule_期間重複エラー_例外が発生する() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");

        WorkRuleController.RegisterWorkRuleRequestBody requestBody = new WorkRuleController.RegisterWorkRuleRequestBody(
            100L, 35.6812, 139.7671,
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );

        when(registerWorkRuleUseCase.execute(any()))
            .thenThrow(new IllegalArgumentException("同一ユーザーの勤務ルール期間が重複しています"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            workRuleController.registerWorkRule(requestBody, authentication);
        });
        verify(registerWorkRuleUseCase, times(1)).execute(any());
    }

    @Test
    void registerWorkRule_予期しない例外発生_例外が発生する() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");

        WorkRuleController.RegisterWorkRuleRequestBody requestBody = new WorkRuleController.RegisterWorkRuleRequestBody(
            100L, 35.6812, 139.7671,
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );

        when(registerWorkRuleUseCase.execute(any()))
            .thenThrow(new RuntimeException("データベース接続エラー"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            workRuleController.registerWorkRule(requestBody, authentication);
        });
        verify(registerWorkRuleUseCase, times(1)).execute(any());
    }

    @Test
    void registerWorkRule_休憩時間なし_正常登録される() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");

        WorkRuleController.RegisterWorkRuleRequestBody requestBody = new WorkRuleController.RegisterWorkRuleRequestBody(
            100L, 35.6812, 139.7671,
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            null, null, // 休憩時間なし
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );

        WorkRuleResponse expectedResponse = new WorkRuleResponse(
            1L, 100L, 35.6812, 139.7671, new UserDto("testuser"),
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            null, null, // 休憩時間なし
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );

        when(registerWorkRuleUseCase.execute(any())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.registerWorkRule(requestBody, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().breakStartTime());
        assertNull(response.getBody().breakEndTime());

        verify(registerWorkRuleUseCase, times(1)).execute(any());
    }
}