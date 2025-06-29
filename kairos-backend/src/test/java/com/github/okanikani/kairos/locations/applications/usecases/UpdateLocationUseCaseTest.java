package com.github.okanikani.kairos.locations.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.applications.usecases.dto.UpdateLocationRequest;
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UpdateLocationUseCaseTest {

    @Autowired
    private UpdateLocationUseCase updateLocationUseCase;

    @MockitoBean
    private LocationRepository locationRepository;

    private User testUser;
    private Location existingLocation;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser");
        now = LocalDateTime.now();
        existingLocation = new Location(1L, 35.6762, 139.6503, now, testUser);
    }

    @Test
    void execute_正常ケース_位置情報が更新される() {
        // Arrange
        UpdateLocationRequest request = new UpdateLocationRequest(
            35.6892,  // 新しい緯度
            139.6917, // 新しい経度
            now.plusHours(1) // 新しい記録日時
        );
        
        Location updatedLocation = new Location(
            1L,
            request.latitude(),
            request.longitude(),
            request.recordedAt(),
            testUser
        );
        
        when(locationRepository.findById(1L)).thenReturn(existingLocation);
        when(locationRepository.save(any(Location.class))).thenReturn(updatedLocation);

        // Act
        LocationResponse response = updateLocationUseCase.execute(1L, request, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(35.6892, response.latitude());
        assertEquals(139.6917, response.longitude());
        assertEquals(now.plusHours(1), response.recordedAt());
        
        verify(locationRepository).findById(1L);
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void execute_位置情報が存在しない場合_例外が発生する() {
        // Arrange
        UpdateLocationRequest request = new UpdateLocationRequest(
            35.6892,
            139.6917,
            now.plusHours(1)
        );
        
        when(locationRepository.findById(999L)).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> updateLocationUseCase.execute(999L, request, "testuser")
        );
        
        assertEquals("指定された位置情報が見つかりません", exception.getMessage());
        verify(locationRepository).findById(999L);
        verify(locationRepository, never()).save(any());
    }

    @Test
    void execute_他ユーザーの位置情報を更新しようとした場合_例外が発生する() {
        // Arrange
        UpdateLocationRequest request = new UpdateLocationRequest(
            35.6892,
            139.6917,
            now.plusHours(1)
        );
        
        when(locationRepository.findById(1L)).thenReturn(existingLocation);

        // Act & Assert
        AuthorizationException exception = assertThrows(
            AuthorizationException.class,
            () -> updateLocationUseCase.execute(1L, request, "otheruser")
        );
        
        assertEquals("他のユーザーの位置情報は更新できません", exception.getMessage());
        verify(locationRepository).findById(1L);
        verify(locationRepository, never()).save(any());
    }

    @Test
    void execute_無効な緯度_例外が発生する() {
        // Arrange
        UpdateLocationRequest request = new UpdateLocationRequest(
            91.0, // 無効な緯度
            139.6917,
            now.plusHours(1)
        );
        
        when(locationRepository.findById(1L)).thenReturn(existingLocation);

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> updateLocationUseCase.execute(1L, request, "testuser")
        );
        
        assertTrue(exception.getMessage().contains("緯度は-90.0～90.0の範囲で指定してください"));
        verify(locationRepository).findById(1L);
        verify(locationRepository, never()).save(any());
    }

    @Test
    void execute_無効な経度_例外が発生する() {
        // Arrange
        UpdateLocationRequest request = new UpdateLocationRequest(
            35.6892,
            181.0, // 無効な経度
            now.plusHours(1)
        );
        
        when(locationRepository.findById(1L)).thenReturn(existingLocation);

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> updateLocationUseCase.execute(1L, request, "testuser")
        );
        
        assertTrue(exception.getMessage().contains("経度は-180.0～180.0の範囲で指定してください"));
        verify(locationRepository).findById(1L);
        verify(locationRepository, never()).save(any());
    }

    @Test
    void execute_記録日時がnull_例外が発生する() {
        // Arrange & Act & Assert
        // UpdateLocationRequestのコンストラクタでNullPointerExceptionが発生する
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new UpdateLocationRequest(
                35.6892,
                139.6917,
                null // null記録日時
            )
        );
        
        assertEquals("記録日時は必須です", exception.getMessage());
        
        // リポジトリメソッドは呼ばれない
        verify(locationRepository, never()).findById(any());
        verify(locationRepository, never()).save(any());
    }
}