package com.github.okanikani.kairos.rules.others.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.okanikani.kairos.commons.controllers.GlobalExceptionHandler;
import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.rules.applications.usecases.DeleteWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.FindAllWorkRulesUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.FindWorkRuleByIdUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.RegisterWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.UpdateWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import com.github.okanikani.kairos.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkRuleController.class)
@Import(GlobalExceptionHandler.class)
class WorkRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterWorkRuleUseCase registerWorkRuleUseCase;
    
    @MockitoBean
    private FindAllWorkRulesUseCase findAllWorkRulesUseCase;
    
    @MockitoBean
    private FindWorkRuleByIdUseCase findWorkRuleByIdUseCase;
    
    @MockitoBean
    private UpdateWorkRuleUseCase updateWorkRuleUseCase;
    
    @MockitoBean
    private DeleteWorkRuleUseCase deleteWorkRuleUseCase;
    
    @MockitoBean
    private JwtService jwtService;
    
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser(username = "testuser")
    void findAllWorkRules_正常ケース_200ステータスと勤務ルール一覧を返す() throws Exception {
        // Arrange
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
        
        when(findAllWorkRulesUseCase.execute(eq("testuser"))).thenReturn(expectedWorkRules);

        // Act & Assert
        mockMvc.perform(get("/api/work-rules")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].workPlaceId").value(100))
                .andExpect(jsonPath("$[0].user.userId").value("testuser"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].workPlaceId").value(200));
        
        verify(findAllWorkRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findAllWorkRules_勤務ルールが存在しない場合_200ステータスと空リストを返す() throws Exception {
        // Arrange
        when(findAllWorkRulesUseCase.execute(eq("testuser"))).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/work-rules")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        
        verify(findAllWorkRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findAllWorkRules_ユースケースで例外発生_500ステータスを返す() throws Exception {
        // Arrange
        when(findAllWorkRulesUseCase.execute(eq("testuser")))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act & Assert
        mockMvc.perform(get("/api/work-rules")
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"));
        
        verify(findAllWorkRulesUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findWorkRuleById_正常ケース_200ステータスと勤務ルールを返す() throws Exception {
        // Arrange
        Long workRuleId = 1L;
        
        WorkRuleResponse expectedWorkRule = new WorkRuleResponse(workRuleId, 100L, 35.6812, 139.7671, new UserDto("testuser"),
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        
        when(findWorkRuleByIdUseCase.execute(eq(workRuleId), eq("testuser"))).thenReturn(expectedWorkRule);

        // Act & Assert
        mockMvc.perform(get("/api/work-rules/{id}", workRuleId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workRuleId))
                .andExpect(jsonPath("$.workPlaceId").value(100))
                .andExpect(jsonPath("$.user.userId").value("testuser"));
        
        verify(findWorkRuleByIdUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findWorkRuleById_勤務ルールが存在しない場合_404ステータスを返す() throws Exception {
        // Arrange
        Long workRuleId = 999L;
        when(findWorkRuleByIdUseCase.execute(eq(workRuleId), eq("testuser")))
            .thenThrow(new ResourceNotFoundException("指定された勤務ルールが存在しません"));

        // Act & Assert
        mockMvc.perform(get("/api/work-rules/{id}", workRuleId)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("指定された勤務ルールが存在しません"));
        
        verify(findWorkRuleByIdUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findWorkRuleById_権限がない場合_403ステータスを返す() throws Exception {
        // Arrange
        Long workRuleId = 1L;
        when(findWorkRuleByIdUseCase.execute(eq(workRuleId), eq("testuser")))
            .thenThrow(new AuthorizationException("この勤務ルールにアクセスする権限がありません"));

        // Act & Assert
        mockMvc.perform(get("/api/work-rules/{id}", workRuleId)
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("AUTHORIZATION_ERROR"))
                .andExpect(jsonPath("$.message").value("この勤務ルールにアクセスする権限がありません"));
        
        verify(findWorkRuleByIdUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findWorkRuleById_予期しない例外発生_500ステータスを返す() throws Exception {
        // Arrange
        Long workRuleId = 1L;
        when(findWorkRuleByIdUseCase.execute(eq(workRuleId), eq("testuser")))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act & Assert
        mockMvc.perform(get("/api/work-rules/{id}", workRuleId)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"));
        
        verify(findWorkRuleByIdUseCase, times(1)).execute(eq(workRuleId), eq("testuser"));
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
}