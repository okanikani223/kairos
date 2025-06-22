package com.github.okanikani.kairos.locations.others.controllers;

import com.github.okanikani.kairos.locations.applications.usecases.RegisterLocationUsecase;
import com.github.okanikani.kairos.locations.applications.usecases.dto.RegisterLocationRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LocationControllerTest {

    private LocationController locationController;

    @Mock
    private RegisterLocationUsecase registerLocationUsecase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        locationController = new LocationController(registerLocationUsecase);
    }

    @Test
    void registerLocation_正常ケース_201ステータスとレスポンスを返す() {
        // Arrange
        LocalDateTime recordedAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        RegisterLocationRequest request = new RegisterLocationRequest(
            35.6812,
            139.7671,
            recordedAt
        );

        LocationResponse expectedResponse = new LocationResponse(
            1L,
            35.6812,
            139.7671,
            recordedAt
        );

        when(registerLocationUsecase.execute(any(RegisterLocationRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<LocationResponse> response = locationController.registerLocation(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals(35.6812, response.getBody().latitude());
        assertEquals(139.7671, response.getBody().longitude());
        assertEquals(recordedAt, response.getBody().recordedAt());
        verify(registerLocationUsecase, times(1)).execute(any(RegisterLocationRequest.class));
    }

    @Test
    void registerLocation_ユースケースで例外発生_400ステータスを返す() {
        // Arrange
        RegisterLocationRequest request = new RegisterLocationRequest(
            91.0,  // 無効な緯度
            139.7671,
            LocalDateTime.now()
        );

        when(registerLocationUsecase.execute(any(RegisterLocationRequest.class)))
            .thenThrow(new IllegalArgumentException("緯度は-90.0～90.0の範囲で指定してください"));

        // Act
        ResponseEntity<LocationResponse> response = locationController.registerLocation(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(registerLocationUsecase, times(1)).execute(any(RegisterLocationRequest.class));
    }

    @Test
    void registerLocation_予期しない例外発生_500ステータスを返す() {
        // Arrange
        RegisterLocationRequest request = new RegisterLocationRequest(
            35.6812,
            139.7671,
            LocalDateTime.now()
        );

        when(registerLocationUsecase.execute(any(RegisterLocationRequest.class)))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<LocationResponse> response = locationController.registerLocation(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(registerLocationUsecase, times(1)).execute(any(RegisterLocationRequest.class));
    }

    @Test
    void constructor_nullUsecase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new LocationController(null)
        );
        assertEquals("registerLocationUsecaseは必須です", exception.getMessage());
    }
}