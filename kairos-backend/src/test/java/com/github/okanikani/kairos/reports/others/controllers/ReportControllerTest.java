package com.github.okanikani.kairos.reports.others.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.okanikani.kairos.commons.dto.ErrorResponse;
import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.reports.applications.usecases.DeleteReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.FindReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.GenerateReportFromLocationUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.RegisterReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.UpdateReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.dto.*;
import com.github.okanikani.kairos.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterReportUseCase registerReportUseCase;

    @MockitoBean
    private FindReportUseCase findReportUseCase;

    @MockitoBean
    private UpdateReportUseCase updateReportUseCase;

    @MockitoBean
    private DeleteReportUseCase deleteReportUseCase;

    @MockitoBean
    private GenerateReportFromLocationUseCase generateReportFromLocationUseCase;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser(username = "testuser")
    void registerReport_正常ケース_201ステータスとレスポンスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        DetailDto detailDto = new DetailDto(
            LocalDate.of(2024, 1, 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 18, 0)),
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO,
            ""
        );
        RegisterReportRequest request = new RegisterReportRequest(
            YearMonth.of(2024, 1),
            userDto,
            List.of(detailDto)
        );

        SummaryDto summaryDto = new SummaryDto(
            1.0,
            0.0,
            0.0,
            0.0,
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO
        );
        ReportResponse expectedResponse = new ReportResponse(
            YearMonth.of(2024, 1),
            userDto,
            "NOT_SUBMITTED",
            List.of(detailDto),
            summaryDto
        );

        when(registerReportUseCase.execute(any())).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/reports")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner.userId").value("testuser"))
                .andExpect(jsonPath("$.yearMonth").value("2024-01"))
                .andExpect(jsonPath("$.status").value("NOT_SUBMITTED"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void registerReport_認証ユーザーとリクエストユーザーが異なる_403ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("anotheruser"); // 認証ユーザーと異なる
        DetailDto detailDto = new DetailDto(
            LocalDate.of(2024, 1, 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 18, 0)),
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO,
            ""
        );
        RegisterReportRequest request = new RegisterReportRequest(
            YearMonth.of(2024, 1),
            userDto,
            List.of(detailDto)
        );

        // Act & Assert
        mockMvc.perform(post("/api/reports")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("AUTHORIZATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void registerReport_ユースケースで例外発生_400ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        RegisterReportRequest request = new RegisterReportRequest(
            YearMonth.of(2024, 1),
            userDto,
            List.of()
        );

        when(registerReportUseCase.execute(any())).thenThrow(new IllegalArgumentException("workDaysは必須です"));

        // Act & Assert
        mockMvc.perform(post("/api/reports")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("workDaysは必須です"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void findReport_正常ケース_200ステータスとレスポンスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        DetailDto detailDto = new DetailDto(
            LocalDate.of(2024, 1, 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 18, 0)),
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO,
            ""
        );
        SummaryDto summaryDto = new SummaryDto(
            1.0,
            0.0,
            0.0,
            0.0,
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO
        );
        ReportResponse expectedResponse = new ReportResponse(
            YearMonth.of(2024, 1),
            userDto,
            "NOT_SUBMITTED",
            List.of(detailDto),
            summaryDto
        );

        when(findReportUseCase.execute(any())).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.userId").value("testuser"))
                .andExpect(jsonPath("$.yearMonth").value("2024-01"))
                .andExpect(jsonPath("$.status").value("NOT_SUBMITTED"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findReport_存在しない勤怠表_404ステータスを返す() throws Exception {
        // Arrange
        when(findReportUseCase.execute(any())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateReport_正常ケース_200ステータスとレスポンスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        DetailDto detailDto = new DetailDto(
            LocalDate.of(2024, 1, 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 19, 0)), // 更新された終了時間
            Duration.ofHours(9), // 更新された勤務時間
            Duration.ofHours(1), // 残業時間
            Duration.ZERO,
            ""
        );
        UpdateReportRequest request = new UpdateReportRequest(
            YearMonth.of(2024, 1),
            userDto,
            List.of(detailDto)
        );

        SummaryDto summaryDto = new SummaryDto(
            1.0,
            0.0,
            0.0,
            0.0,
            Duration.ofHours(8),
            Duration.ofHours(1),
            Duration.ZERO
        );
        ReportResponse expectedResponse = new ReportResponse(
            YearMonth.of(2024, 1),
            userDto,
            "NOT_SUBMITTED",
            List.of(detailDto),
            summaryDto
        );

        when(updateReportUseCase.execute(any())).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.userId").value("testuser"))
                .andExpect(jsonPath("$.yearMonth").value("2024-01"))
                .andExpect(jsonPath("$.status").value("NOT_SUBMITTED"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateReport_パスパラメータとボディの年月不一致_400ステータスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        DetailDto detailDto = new DetailDto(
            LocalDate.of(2024, 1, 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 18, 0)),
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO,
            ""
        );
        UpdateReportRequest request = new UpdateReportRequest(
            YearMonth.of(2024, 2), // パスパラメータと異なる年月
            userDto,
            List.of(detailDto)
        );

        // Act & Assert
        mockMvc.perform(put("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateReport_認証ユーザーとリクエストユーザーが異なる_403ステータスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("anotheruser"); // 認証ユーザーと異なる
        DetailDto detailDto = new DetailDto(
            LocalDate.of(2024, 1, 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 18, 0)),
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO,
            ""
        );
        UpdateReportRequest request = new UpdateReportRequest(
            YearMonth.of(2024, 1),
            userDto,
            List.of(detailDto)
        );

        // Act & Assert
        mockMvc.perform(put("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("AUTHORIZATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteReport_正常ケース_204ステータスを返す() throws Exception {
        // Arrange
        doNothing().when(deleteReportUseCase).execute(any());

        // Act & Assert
        mockMvc.perform(delete("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(deleteReportUseCase, times(1)).execute(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteReport_認証ユーザーと削除対象ユーザーが異なる_403ステータスを返す() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("削除対象の勤怠表が存在しません"))
            .when(deleteReportUseCase).execute(any());

        // Act & Assert
        mockMvc.perform(delete("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("削除対象の勤怠表が存在しません"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteReport_存在しない勤怠表削除時_404ステータスを返す() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("指定された勤怠表が存在しません"))
            .when(deleteReportUseCase).execute(any());

        // Act & Assert
        mockMvc.perform(delete("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("指定された勤怠表が存在しません"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteReport_削除不可な勤怠表の場合_400ステータスを返す() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("ステータスがSUBMITTEDの勤怠表は削除できません"))
            .when(deleteReportUseCase).execute(any());

        // Act & Assert
        mockMvc.perform(delete("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("ステータスがSUBMITTEDの勤怠表は削除できません"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void generateReportFromLocation_正常ケース_201ステータスとレスポンスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(
            YearMonth.of(2024, 1),
            userDto
        );

        DetailDto detailDto = new DetailDto(
            LocalDate.of(2024, 1, 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 18, 0)),
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO,
            ""
        );
        SummaryDto summaryDto = new SummaryDto(
            1.0,
            0.0,
            0.0,
            0.0,
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO
        );
        ReportResponse expectedResponse = new ReportResponse(
            YearMonth.of(2024, 1),
            userDto,
            "NOT_SUBMITTED",
            List.of(detailDto),
            summaryDto
        );

        when(generateReportFromLocationUseCase.execute(any())).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/reports/generate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner.userId").value("testuser"))
                .andExpect(jsonPath("$.yearMonth").value("2024-01"))
                .andExpect(jsonPath("$.status").value("NOT_SUBMITTED"));
        
        verify(generateReportFromLocationUseCase, times(1)).execute(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void generateReportFromLocation_認証ユーザーとリクエストユーザーが異なる_403ステータスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("anotheruser"); // 認証ユーザーと異なる
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(
            YearMonth.of(2024, 1),
            userDto
        );

        // Act & Assert
        mockMvc.perform(post("/api/reports/generate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("AUTHORIZATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists());
        
        verify(generateReportFromLocationUseCase, never()).execute(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void generateReportFromLocation_ユースケースで例外発生_400ステータスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(
            YearMonth.of(2024, 1),
            userDto
        );

        when(generateReportFromLocationUseCase.execute(any()))
            .thenThrow(new IllegalArgumentException("位置情報が存在しません"));

        // Act & Assert
        mockMvc.perform(post("/api/reports/generate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("位置情報が存在しません"))
                .andExpect(jsonPath("$.timestamp").exists());
        
        verify(generateReportFromLocationUseCase, times(1)).execute(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void generateReportFromLocation_予期しない例外発生_500ステータスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(
            YearMonth.of(2024, 1),
            userDto
        );

        when(generateReportFromLocationUseCase.execute(any()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act & Assert
        mockMvc.perform(post("/api/reports/generate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"))
                .andExpect(jsonPath("$.timestamp").exists());
        
        verify(generateReportFromLocationUseCase, times(1)).execute(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void registerReport_予期しない例外発生_500ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        DetailDto detailDto = new DetailDto(
            LocalDate.of(2024, 1, 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 18, 0)),
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO,
            ""
        );
        RegisterReportRequest request = new RegisterReportRequest(
            YearMonth.of(2024, 1),
            userDto,
            List.of(detailDto)
        );

        when(registerReportUseCase.execute(any()))
            .thenThrow(new RuntimeException("データベース接続エラー"));

        // Act & Assert
        mockMvc.perform(post("/api/reports")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"))
                .andExpect(jsonPath("$.timestamp").exists());
        
        verify(registerReportUseCase, times(1)).execute(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void findReport_予期しない例外発生_500ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        when(findReportUseCase.execute(any()))
            .thenThrow(new RuntimeException("データベース接続エラー"));

        // Act & Assert
        mockMvc.perform(get("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"))
                .andExpect(jsonPath("$.timestamp").exists());
        
        verify(findReportUseCase, times(1)).execute(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateReport_予期しない例外発生_500ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        UserDto userDto = new UserDto("testuser");
        DetailDto detailDto = new DetailDto(
            LocalDate.of(2024, 1, 1),
            false,
            null,
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 9, 0)),
            new WorkTimeDto(LocalDateTime.of(2024, 1, 1, 18, 0)),
            Duration.ofHours(8),
            Duration.ZERO,
            Duration.ZERO,
            ""
        );
        UpdateReportRequest request = new UpdateReportRequest(
            YearMonth.of(2024, 1),
            userDto,
            List.of(detailDto)
        );

        when(updateReportUseCase.execute(any()))
            .thenThrow(new RuntimeException("データベース接続エラー"));

        // Act & Assert
        mockMvc.perform(put("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"))
                .andExpect(jsonPath("$.timestamp").exists());
        
        verify(updateReportUseCase, times(1)).execute(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteReport_予期しない例外発生_500ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        doThrow(new RuntimeException("データベース接続エラー"))
            .when(deleteReportUseCase).execute(any());

        // Act & Assert
        mockMvc.perform(delete("/api/reports/{year}/{month}", 2024, 1)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"))
                .andExpect(jsonPath("$.timestamp").exists());
        
        verify(deleteReportUseCase, times(1)).execute(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void findReport_無効な年月パラメータ_400ステータスを返す() throws Exception {
        // 無効な月（0月）をテスト
        mockMvc.perform(get("/api/reports/{year}/{month}", 2024, 0)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists());
        
        // 無効な月（13月）をテスト
        mockMvc.perform(get("/api/reports/{year}/{month}", 2024, 13)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}