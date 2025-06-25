package com.github.okanikani.kairos.rules.others.controllers;

import com.github.okanikani.kairos.rules.applications.usecases.DeleteWorkRuleUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.FindAllWorkRulesUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.FindWorkRuleByIdUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.RegisterWorkRuleUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.UpdateWorkRuleUsecase;
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
    private RegisterWorkRuleUsecase registerWorkRuleUsecase;
    
    @Mock
    private FindAllWorkRulesUsecase findAllWorkRulesUsecase;
    
    @Mock
    private FindWorkRuleByIdUsecase findWorkRuleByIdUsecase;
    
    @Mock
    private UpdateWorkRuleUsecase updateWorkRuleUsecase;
    
    @Mock
    private DeleteWorkRuleUsecase deleteWorkRuleUsecase;
    
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workRuleController = new WorkRuleController(registerWorkRuleUsecase, findAllWorkRulesUsecase, findWorkRuleByIdUsecase, updateWorkRuleUsecase, deleteWorkRuleUsecase);
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
        
        when(findAllWorkRulesUsecase.execute(anyString())).thenReturn(expectedWorkRules);

        // Act
        ResponseEntity<List<WorkRuleResponse>> response = workRuleController.findAllWorkRules(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).id());
        assertEquals(100L, response.getBody().get(0).workPlaceId());
        verify(findAllWorkRulesUsecase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findAllWorkRules_勤務ルールが存在しない場合_200ステータスと空リストを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findAllWorkRulesUsecase.execute(anyString())).thenReturn(List.of());

        // Act
        ResponseEntity<List<WorkRuleResponse>> response = workRuleController.findAllWorkRules(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(findAllWorkRulesUsecase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findAllWorkRules_ユースケースで例外発生_500ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findAllWorkRulesUsecase.execute(anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<List<WorkRuleResponse>> response = workRuleController.findAllWorkRules(authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(findAllWorkRulesUsecase, times(1)).execute(eq("testuser"));
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
        
        when(findWorkRuleByIdUsecase.execute(eq(workRuleId), anyString())).thenReturn(expectedWorkRule);

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.findWorkRuleById(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(workRuleId, response.getBody().id());
        assertEquals(100L, response.getBody().workPlaceId());
        verify(findWorkRuleByIdUsecase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void findWorkRuleById_勤務ルールが存在しない場合_404ステータスを返す() {
        // Arrange
        Long workRuleId = 999L;
        when(authentication.getName()).thenReturn("testuser");
        when(findWorkRuleByIdUsecase.execute(eq(workRuleId), anyString()))
            .thenThrow(new IllegalArgumentException("指定された勤務ルールが存在しません"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.findWorkRuleById(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(findWorkRuleByIdUsecase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void findWorkRuleById_権限がない場合_403ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        when(findWorkRuleByIdUsecase.execute(eq(workRuleId), anyString()))
            .thenThrow(new IllegalArgumentException("この勤務ルールにアクセスする権限がありません"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.findWorkRuleById(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(findWorkRuleByIdUsecase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void findWorkRuleById_予期しない例外発生_500ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        when(findWorkRuleByIdUsecase.execute(eq(workRuleId), anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.findWorkRuleById(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(findWorkRuleByIdUsecase, times(1)).execute(eq(workRuleId), eq("testuser"));
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
        
        when(updateWorkRuleUsecase.execute(eq(workRuleId), any(), anyString())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.updateWorkRule(workRuleId, requestBody, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(workRuleId, response.getBody().id());
        assertEquals(200L, response.getBody().workPlaceId());
        assertEquals(35.6900, response.getBody().latitude());
        verify(updateWorkRuleUsecase, times(1)).execute(eq(workRuleId), any(), eq("testuser"));
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
        
        when(updateWorkRuleUsecase.execute(eq(workRuleId), any(), anyString()))
            .thenThrow(new IllegalArgumentException("指定された勤務ルールが存在しません"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.updateWorkRule(workRuleId, requestBody, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(updateWorkRuleUsecase, times(1)).execute(eq(workRuleId), any(), eq("testuser"));
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
        
        when(updateWorkRuleUsecase.execute(eq(workRuleId), any(), anyString()))
            .thenThrow(new IllegalArgumentException("この勤務ルールを更新する権限がありません"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.updateWorkRule(workRuleId, requestBody, authentication);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(updateWorkRuleUsecase, times(1)).execute(eq(workRuleId), any(), eq("testuser"));
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
        
        when(updateWorkRuleUsecase.execute(eq(workRuleId), any(), anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<WorkRuleResponse> response = workRuleController.updateWorkRule(workRuleId, requestBody, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(updateWorkRuleUsecase, times(1)).execute(eq(workRuleId), any(), eq("testuser"));
    }

    @Test
    void deleteWorkRule_正常ケース_204ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        doNothing().when(deleteWorkRuleUsecase).execute(eq(workRuleId), anyString());

        // Act
        ResponseEntity<Void> response = workRuleController.deleteWorkRule(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deleteWorkRuleUsecase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void deleteWorkRule_勤務ルールが存在しない場合_404ステータスを返す() {
        // Arrange
        Long workRuleId = 999L;
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new IllegalArgumentException("指定された勤務ルールが存在しません"))
            .when(deleteWorkRuleUsecase).execute(eq(workRuleId), anyString());

        // Act
        ResponseEntity<Void> response = workRuleController.deleteWorkRule(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(deleteWorkRuleUsecase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void deleteWorkRule_権限がない場合_403ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new IllegalArgumentException("この勤務ルールを削除する権限がありません"))
            .when(deleteWorkRuleUsecase).execute(eq(workRuleId), anyString());

        // Act
        ResponseEntity<Void> response = workRuleController.deleteWorkRule(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(deleteWorkRuleUsecase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void deleteWorkRule_予期しない例外発生_500ステータスを返す() {
        // Arrange
        Long workRuleId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new RuntimeException("データベースエラー"))
            .when(deleteWorkRuleUsecase).execute(eq(workRuleId), anyString());

        // Act
        ResponseEntity<Void> response = workRuleController.deleteWorkRule(workRuleId, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(deleteWorkRuleUsecase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    void constructor_nullRegisterUsecase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRuleController(null, findAllWorkRulesUsecase, findWorkRuleByIdUsecase, updateWorkRuleUsecase, deleteWorkRuleUsecase)
        );
        assertEquals("registerWorkRuleUsecaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullFindAllUsecase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRuleController(registerWorkRuleUsecase, null, findWorkRuleByIdUsecase, updateWorkRuleUsecase, deleteWorkRuleUsecase)
        );
        assertEquals("findAllWorkRulesUsecaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullFindByIdUsecase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRuleController(registerWorkRuleUsecase, findAllWorkRulesUsecase, null, updateWorkRuleUsecase, deleteWorkRuleUsecase)
        );
        assertEquals("findWorkRuleByIdUsecaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullUpdateUsecase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRuleController(registerWorkRuleUsecase, findAllWorkRulesUsecase, findWorkRuleByIdUsecase, null, deleteWorkRuleUsecase)
        );
        assertEquals("updateWorkRuleUsecaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullDeleteUsecase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRuleController(registerWorkRuleUsecase, findAllWorkRulesUsecase, findWorkRuleByIdUsecase, updateWorkRuleUsecase, null)
        );
        assertEquals("deleteWorkRuleUsecaseは必須です", exception.getMessage());
    }
}