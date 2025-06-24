package com.github.okanikani.kairos.locations.applications.usecases;

import com.github.okanikani.kairos.locations.applications.usecases.dto.RegisterLocationRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterLocationUsecaseTest {

    private RegisterLocationUsecase registerLocationUsecase;

    @Mock
    private LocationRepository locationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registerLocationUsecase = new RegisterLocationUsecase(locationRepository);
    }

    @Test
    void execute_正常ケース_位置情報が登録される() {
        // Arrange
        LocalDateTime recordedAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        RegisterLocationRequest request = new RegisterLocationRequest(
            35.6812,  // 東京駅の緯度
            139.7671, // 東京駅の経度
            recordedAt
        );

        Location savedLocation = new Location(
            1L,       // DB採番されたID
            35.6812,
            139.7671,
            recordedAt,
            new User("testuser")
        );

        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);

        // Act
        LocationResponse response = registerLocationUsecase.execute(request, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(35.6812, response.latitude());
        assertEquals(139.7671, response.longitude());
        assertEquals(recordedAt, response.recordedAt());

        verify(locationRepository, times(1)).save(any(Location.class));
    }

    @Test
    void execute_異常ケース_nullリクエストで例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> registerLocationUsecase.execute(null, "testuser")
        );
        assertEquals("requestは必須です", exception.getMessage());
        verify(locationRepository, never()).save(any());
    }

    @Test
    void execute_異常ケース_無効な緯度で例外が発生する() {
        // Arrange
        RegisterLocationRequest request = new RegisterLocationRequest(
            91.0,     // 無効な緯度
            139.7671,
            LocalDateTime.now()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> registerLocationUsecase.execute(request, "testuser")
        );
        assertTrue(exception.getMessage().contains("緯度は-90.0～90.0の範囲で指定してください"));
        verify(locationRepository, never()).save(any());
    }

    @Test
    void execute_異常ケース_無効な経度で例外が発生する() {
        // Arrange
        RegisterLocationRequest request = new RegisterLocationRequest(
            35.6812,
            181.0,    // 無効な経度
            LocalDateTime.now()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> registerLocationUsecase.execute(request, "testuser")
        );
        assertTrue(exception.getMessage().contains("経度は-180.0～180.0の範囲で指定してください"));
        verify(locationRepository, never()).save(any());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new RegisterLocationUsecase(null)
        );
        assertEquals("locationRepositoryは必須です", exception.getMessage());
    }
}