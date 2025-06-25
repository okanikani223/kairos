package com.github.okanikani.kairos.rules.others.controllers;

import com.github.okanikani.kairos.rules.applications.usecases.FindAllDefaultWorkRulesUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.RegisterDefaultWorkRuleUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DefaultWorkRuleControllerTest {

    private DefaultWorkRuleController defaultWorkRuleController;

    @Mock
    private RegisterDefaultWorkRuleUsecase registerDefaultWorkRuleUsecase;
    
    @Mock
    private FindAllDefaultWorkRulesUsecase findAllDefaultWorkRulesUsecase;
    
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        defaultWorkRuleController = new DefaultWorkRuleController(registerDefaultWorkRuleUsecase, findAllDefaultWorkRulesUsecase);
    }

    @Test
    void findAllDefaultWorkRules_正常ケース_200ステータスとデフォルト勤務ルール一覧を返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
        List<DefaultWorkRuleResponse> expectedDefaultWorkRules = Arrays.asList(
            new DefaultWorkRuleResponse(1L, 100L, 35.6812, 139.7671, new UserDto("testuser"),
                LocalTime.of(9, 0), LocalTime.of(18, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0)),
            new DefaultWorkRuleResponse(2L, 200L, 35.6813, 139.7672, new UserDto("testuser"),
                LocalTime.of(10, 0), LocalTime.of(19, 0),
                null, null)
        );
        
        when(findAllDefaultWorkRulesUsecase.execute(anyString())).thenReturn(expectedDefaultWorkRules);

        // Act
        ResponseEntity<List<DefaultWorkRuleResponse>> response = defaultWorkRuleController.findAllDefaultWorkRules(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).id());
        assertEquals(100L, response.getBody().get(0).workPlaceId());
        verify(findAllDefaultWorkRulesUsecase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findAllDefaultWorkRules_デフォルト勤務ルールが存在しない場合_200ステータスと空リストを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findAllDefaultWorkRulesUsecase.execute(anyString())).thenReturn(List.of());

        // Act
        ResponseEntity<List<DefaultWorkRuleResponse>> response = defaultWorkRuleController.findAllDefaultWorkRules(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(findAllDefaultWorkRulesUsecase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findAllDefaultWorkRules_ユースケースで例外発生_500ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findAllDefaultWorkRulesUsecase.execute(anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<List<DefaultWorkRuleResponse>> response = defaultWorkRuleController.findAllDefaultWorkRules(authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(findAllDefaultWorkRulesUsecase, times(1)).execute(eq("testuser"));
    }

    @Test
    void constructor_nullRegisterUsecase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new DefaultWorkRuleController(null, findAllDefaultWorkRulesUsecase)
        );
        assertEquals("registerDefaultWorkRuleUsecaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullFindAllUsecase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new DefaultWorkRuleController(registerDefaultWorkRuleUsecase, null)
        );
        assertEquals("findAllDefaultWorkRulesUsecaseは必須です", exception.getMessage());
    }
}