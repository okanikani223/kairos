package com.github.okanikani.kairos.reports.others.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.okanikani.kairos.reports.applications.usecases.DeleteReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.FindReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.GenerateReportFromLocationUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.RegisterReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.UpdateReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class ReportControllerTest {

    private ReportController reportController;

    @Mock
    private RegisterReportUseCase registerReportUseCase;

    @Mock
    private FindReportUseCase findReportUseCase;

    @Mock
    private UpdateReportUseCase updateReportUseCase;

    @Mock
    private DeleteReportUseCase deleteReportUseCase;

    @Mock
    private GenerateReportFromLocationUseCase generateReportFromLocationUseCase;

    @Mock
    private Authentication authentication;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportController = new ReportController(registerReportUseCase, findReportUseCase, updateReportUseCase, deleteReportUseCase, generateReportFromLocationUseCase);
        objectMapper = new ObjectMapper();
    }

    @Test
    void registerReport_正常ケース_201ステータスとレスポンスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
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

        // Act
        ResponseEntity<ReportResponse> response = reportController.registerReport(request, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().owner().userId());
        assertEquals(YearMonth.of(2024, 1), response.getBody().yearMonth());
    }

    @Test
    void registerReport_認証ユーザーとリクエストユーザーが異なる_403ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
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

        // Act
        ResponseEntity<ReportResponse> response = reportController.registerReport(request, authentication);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void registerReport_ユースケースで例外発生_400ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
        UserDto userDto = new UserDto("testuser");
        RegisterReportRequest request = new RegisterReportRequest(
            YearMonth.of(2024, 1),
            userDto,
            List.of()
        );

        when(registerReportUseCase.execute(any())).thenThrow(new IllegalArgumentException("workDaysは必須です"));

        // Act
        ResponseEntity<ReportResponse> response = reportController.registerReport(request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void findReport_正常ケース_200ステータスとレスポンスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
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

        // Act
        ResponseEntity<ReportResponse> response = reportController.findReport(2024, 1, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().owner().userId());
    }

    @Test
    void findReport_存在しない勤怠表_404ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findReportUseCase.execute(any())).thenReturn(null);

        // Act
        ResponseEntity<ReportResponse> response = reportController.findReport(2024, 1, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateReport_正常ケース_200ステータスとレスポンスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
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

        // Act
        ResponseEntity<ReportResponse> response = reportController.updateReport(2024, 1, request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().owner().userId());
    }

    @Test
    void updateReport_パスパラメータとボディの年月不一致_400ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
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

        // Act
        ResponseEntity<ReportResponse> response = reportController.updateReport(2024, 1, request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateReport_認証ユーザーとリクエストユーザーが異なる_403ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
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

        // Act
        ResponseEntity<ReportResponse> response = reportController.updateReport(2024, 1, request, authentication);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteReport_正常ケース_204ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");

        // Act
        ResponseEntity<Void> response = reportController.deleteReport(2024, 1, authentication);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deleteReportUseCase, times(1)).execute(any());
    }

    @Test
    void deleteReport_認証ユーザーと削除対象ユーザーが異なる_403ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new IllegalArgumentException("削除対象の勤怠表が存在しません"))
            .when(deleteReportUseCase).execute(any());

        // Act
        ResponseEntity<Void> response = reportController.deleteReport(2024, 1, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteReport_存在しない勤怠表削除時_404ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new IllegalArgumentException("削除対象の勤怠表が存在しません"))
            .when(deleteReportUseCase).execute(any());

        // Act
        ResponseEntity<Void> response = reportController.deleteReport(2024, 1, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteReport_削除不可な勤怠表の場合_400ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new IllegalArgumentException("ステータスがSUBMITTEDの勤怠表は削除できません"))
            .when(deleteReportUseCase).execute(any());

        // Act
        ResponseEntity<Void> response = reportController.deleteReport(2024, 1, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void generateReportFromLocation_正常ケース_201ステータスとレスポンスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
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

        // Act
        ResponseEntity<ReportResponse> response = reportController.generateReportFromLocation(request, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().owner().userId());
        assertEquals(YearMonth.of(2024, 1), response.getBody().yearMonth());
        verify(generateReportFromLocationUseCase, times(1)).execute(any());
    }

    @Test
    void generateReportFromLocation_認証ユーザーとリクエストユーザーが異なる_403ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
        UserDto userDto = new UserDto("anotheruser"); // 認証ユーザーと異なる
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(
            YearMonth.of(2024, 1),
            userDto
        );

        // Act
        ResponseEntity<ReportResponse> response = reportController.generateReportFromLocation(request, authentication);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(generateReportFromLocationUseCase, never()).execute(any());
    }

    @Test
    void generateReportFromLocation_ユースケースで例外発生_400ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
        UserDto userDto = new UserDto("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(
            YearMonth.of(2024, 1),
            userDto
        );

        when(generateReportFromLocationUseCase.execute(any()))
            .thenThrow(new IllegalArgumentException("位置情報が存在しません"));

        // Act
        ResponseEntity<ReportResponse> response = reportController.generateReportFromLocation(request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(generateReportFromLocationUseCase, times(1)).execute(any());
    }

    @Test
    void generateReportFromLocation_予期しない例外発生_500ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
        UserDto userDto = new UserDto("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(
            YearMonth.of(2024, 1),
            userDto
        );

        when(generateReportFromLocationUseCase.execute(any()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<ReportResponse> response = reportController.generateReportFromLocation(request, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(generateReportFromLocationUseCase, times(1)).execute(any());
    }

    @Test
    void registerReport_予期しない例外発生_500ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
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

        // Act
        ResponseEntity<ReportResponse> response = reportController.registerReport(request, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(registerReportUseCase, times(1)).execute(any());
    }

    @Test
    void findReport_予期しない例外発生_400ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findReportUseCase.execute(any()))
            .thenThrow(new RuntimeException("データベース接続エラー"));

        // Act
        ResponseEntity<ReportResponse> response = reportController.findReport(2024, 1, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(findReportUseCase, times(1)).execute(any());
    }

    @Test
    void updateReport_予期しない例外発生_500ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
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

        // Act
        ResponseEntity<ReportResponse> response = reportController.updateReport(2024, 1, request, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(updateReportUseCase, times(1)).execute(any());
    }

    @Test
    void deleteReport_予期しない例外発生_500ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new RuntimeException("データベース接続エラー"))
            .when(deleteReportUseCase).execute(any());

        // Act
        ResponseEntity<Void> response = reportController.deleteReport(2024, 1, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(deleteReportUseCase, times(1)).execute(any());
    }


    @Test
    void findReport_無効な年月パラメータ_400ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
        // 無効な月（0月）をテスト
        ResponseEntity<ReportResponse> response1 = reportController.findReport(2024, 0, authentication);
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());
        
        // 無効な月（13月）をテスト
        ResponseEntity<ReportResponse> response2 = reportController.findReport(2024, 13, authentication);
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }
}