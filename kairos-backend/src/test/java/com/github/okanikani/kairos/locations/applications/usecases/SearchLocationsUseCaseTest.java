package com.github.okanikani.kairos.locations.applications.usecases;

import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.applications.usecases.dto.SearchLocationsRequest;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SearchLocationsUseCaseTest {

    private SearchLocationsUseCase searchLocationsUseCase;

    @Mock
    private LocationRepository locationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        searchLocationsUseCase = new SearchLocationsUseCase(locationRepository);
    }

    @Test
    void execute_正常ケース_期間内の位置情報が取得される() {
        // Arrange
        String userId = "testuser";
        User user = new User(userId);
        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2024, 1, 1, 18, 0);
        SearchLocationsRequest request = new SearchLocationsRequest(startDateTime, endDateTime);
        
        List<Location> locations = Arrays.asList(
            new Location(1L, 35.6812, 139.7671, LocalDateTime.of(2024, 1, 1, 9, 30), user),
            new Location(2L, 35.6813, 139.7672, LocalDateTime.of(2024, 1, 1, 12, 0), user),
            new Location(3L, 35.6814, 139.7673, LocalDateTime.of(2024, 1, 1, 17, 30), user)
        );

        when(locationRepository.findByUserAndDateTimeRange(eq(user), eq(startDateTime), eq(endDateTime)))
            .thenReturn(locations);

        // Act
        List<LocationResponse> response = searchLocationsUseCase.execute(request, userId);

        // Assert
        assertNotNull(response);
        assertEquals(3, response.size());
        
        LocationResponse first = response.get(0);
        assertEquals(1L, first.id());
        assertEquals(35.6812, first.latitude());
        assertEquals(139.7671, first.longitude());
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 30), first.recordedAt());
        
        verify(locationRepository, times(1)).findByUserAndDateTimeRange(eq(user), eq(startDateTime), eq(endDateTime));
    }

    @Test
    void execute_検索結果が存在しない場合_空のリストが返される() {
        // Arrange
        String userId = "testuser";
        User user = new User(userId);
        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2024, 1, 1, 18, 0);
        SearchLocationsRequest request = new SearchLocationsRequest(startDateTime, endDateTime);
        
        when(locationRepository.findByUserAndDateTimeRange(eq(user), eq(startDateTime), eq(endDateTime)))
            .thenReturn(List.of());

        // Act
        List<LocationResponse> response = searchLocationsUseCase.execute(request, userId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(locationRepository, times(1)).findByUserAndDateTimeRange(eq(user), eq(startDateTime), eq(endDateTime));
    }

    @Test
    void execute_nullRequest_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> searchLocationsUseCase.execute(null, "testuser")
        );
        assertEquals("requestは必須です", exception.getMessage());
        verify(locationRepository, never()).findByUserAndDateTimeRange(any(), any(), any());
    }

    @Test
    void execute_nullUserId_例外が発生する() {
        // Arrange
        SearchLocationsRequest request = new SearchLocationsRequest(
            LocalDateTime.of(2024, 1, 1, 9, 0),
            LocalDateTime.of(2024, 1, 1, 18, 0)
        );

        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> searchLocationsUseCase.execute(request, null)
        );
        assertEquals("userIdは必須です", exception.getMessage());
        verify(locationRepository, never()).findByUserAndDateTimeRange(any(), any(), any());
    }

    @Test
    void execute_nullStartDateTime_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new SearchLocationsRequest(null, LocalDateTime.of(2024, 1, 1, 18, 0))
        );
        assertEquals("startDateTimeは必須です", exception.getMessage());
    }

    @Test
    void execute_nullEndDateTime_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new SearchLocationsRequest(LocalDateTime.of(2024, 1, 1, 9, 0), null)
        );
        assertEquals("endDateTimeは必須です", exception.getMessage());
    }

    @Test
    void execute_startDateTimeがendDateTimeより後_例外が発生する() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new SearchLocationsRequest(
                LocalDateTime.of(2024, 1, 1, 18, 0),  // 終了日時
                LocalDateTime.of(2024, 1, 1, 9, 0)    // 開始日時（逆転）
            )
        );
        assertEquals("開始日時は終了日時より前である必要があります", exception.getMessage());
    }

    @Test
    void constructor_nullRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new SearchLocationsUseCase(null)
        );
        assertEquals("locationRepositoryは必須です", exception.getMessage());
    }
}