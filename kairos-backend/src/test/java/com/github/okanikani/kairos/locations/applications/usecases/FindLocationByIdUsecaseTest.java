package com.github.okanikani.kairos.locations.applications.usecases;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FindLocationByIdUsecaseTest {

    private FindLocationByIdUsecase findLocationByIdUsecase;

    @Mock
    private LocationRepository locationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        findLocationByIdUsecase = new FindLocationByIdUsecase(locationRepository);
    }

    @Test
    void execute_正常ケース_位置情報が取得される() {
        // Arrange
        Long locationId = 1L;
        String userId = "testuser";
        User user = new User(userId);
        LocalDateTime recordedAt = LocalDateTime.of(2024, 1, 1, 9, 0);
        
        Location location = new Location(locationId, 35.6812, 139.7671, recordedAt, user);
        when(locationRepository.findById(eq(locationId))).thenReturn(location);

        // Act
        LocationResponse response = findLocationByIdUsecase.execute(locationId, userId);

        // Assert
        assertNotNull(response);
        assertEquals(locationId, response.id());
        assertEquals(35.6812, response.latitude());
        assertEquals(139.7671, response.longitude());
        assertEquals(recordedAt, response.recordedAt());
        
        verify(locationRepository, times(1)).findById(eq(locationId));
    }

    @Test
    void execute_位置情報が存在しない場合_例外が発生する() {
        // Arrange
        Long locationId = 999L;
        String userId = "testuser";
        when(locationRepository.findById(eq(locationId))).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> findLocationByIdUsecase.execute(locationId, userId)
        );
        assertEquals("指定された位置情報が存在しません", exception.getMessage());
        verify(locationRepository, times(1)).findById(eq(locationId));
    }

    @Test
    void execute_他のユーザーの位置情報_例外が発生する() {
        // Arrange
        Long locationId = 1L;
        String requestUserId = "testuser";
        String ownerUserId = "anotheruser";
        User ownerUser = new User(ownerUserId);
        LocalDateTime recordedAt = LocalDateTime.of(2024, 1, 1, 9, 0);
        
        Location location = new Location(locationId, 35.6812, 139.7671, recordedAt, ownerUser);
        when(locationRepository.findById(eq(locationId))).thenReturn(location);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> findLocationByIdUsecase.execute(locationId, requestUserId)
        );
        assertEquals("この位置情報にアクセスする権限がありません", exception.getMessage());
        verify(locationRepository, times(1)).findById(eq(locationId));
    }

    @Test
    void execute_nullLocationId_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> findLocationByIdUsecase.execute(null, "testuser")
        );
        assertEquals("locationIdは必須です", exception.getMessage());
        verify(locationRepository, never()).findById(any());
    }

    @Test
    void execute_nullUserId_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> findLocationByIdUsecase.execute(1L, null)
        );
        assertEquals("userIdは必須です", exception.getMessage());
        verify(locationRepository, never()).findById(any());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new FindLocationByIdUsecase(null)
        );
        assertEquals("locationRepositoryは必須です", exception.getMessage());
    }
}