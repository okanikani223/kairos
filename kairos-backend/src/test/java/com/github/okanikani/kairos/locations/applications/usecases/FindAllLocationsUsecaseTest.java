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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FindAllLocationsUsecaseTest {

    private FindAllLocationsUsecase findAllLocationsUsecase;

    @Mock
    private LocationRepository locationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        findAllLocationsUsecase = new FindAllLocationsUsecase(locationRepository);
    }

    @Test
    void execute_正常ケース_ユーザーの位置情報一覧が取得される() {
        // Arrange
        String userId = "testuser";
        User user = new User(userId);
        
        List<Location> locations = Arrays.asList(
            new Location(1L, 35.6812, 139.7671, LocalDateTime.of(2024, 1, 1, 9, 0), user),
            new Location(2L, 35.6813, 139.7672, LocalDateTime.of(2024, 1, 1, 12, 0), user),
            new Location(3L, 35.6814, 139.7673, LocalDateTime.of(2024, 1, 1, 18, 0), user)
        );

        when(locationRepository.findByUser(any(User.class))).thenReturn(locations);

        // Act
        List<LocationResponse> response = findAllLocationsUsecase.execute(userId);

        // Assert
        assertNotNull(response);
        assertEquals(3, response.size());
        
        LocationResponse first = response.get(0);
        assertEquals(1L, first.id());
        assertEquals(35.6812, first.latitude());
        assertEquals(139.7671, first.longitude());
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 0), first.recordedAt());
        
        verify(locationRepository, times(1)).findByUser(any(User.class));
    }

    @Test
    void execute_位置情報が存在しない場合_空のリストが返される() {
        // Arrange
        String userId = "testuser";
        when(locationRepository.findByUser(any(User.class))).thenReturn(List.of());

        // Act
        List<LocationResponse> response = findAllLocationsUsecase.execute(userId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(locationRepository, times(1)).findByUser(any(User.class));
    }

    @Test
    void execute_nullユーザーID_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> findAllLocationsUsecase.execute(null)
        );
        assertEquals("userIdは必須です", exception.getMessage());
        verify(locationRepository, never()).findByUser(any());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new FindAllLocationsUsecase(null)
        );
        assertEquals("locationRepositoryは必須です", exception.getMessage());
    }
}