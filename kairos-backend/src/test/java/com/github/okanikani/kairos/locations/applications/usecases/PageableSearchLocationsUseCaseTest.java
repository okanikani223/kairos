package com.github.okanikani.kairos.locations.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.applications.usecases.dto.PageableSearchLocationsRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.PagedLocationResponse;
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PageableSearchLocationsUseCaseTest {

    @Autowired
    private PageableSearchLocationsUseCase pageableSearchLocationsUseCase;

    @MockitoBean
    private LocationRepository locationRepository;

    private User testUser;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser");
        startDateTime = LocalDateTime.of(2024, 1, 1, 9, 0, 0);
        endDateTime = LocalDateTime.of(2024, 1, 1, 18, 0, 0);
    }

    @Test
    void execute_正常ケース_ページネーション付きで位置情報が取得される() {
        // Arrange
        PageableSearchLocationsRequest request = new PageableSearchLocationsRequest(
            startDateTime,
            endDateTime,
            0, // page
            10 // size
        );

        List<Location> locationList = Arrays.asList(
            new Location(1L, 35.6812, 139.7671, startDateTime.plusHours(1), testUser),
            new Location(2L, 35.6813, 139.7672, startDateTime.plusHours(2), testUser),
            new Location(3L, 35.6814, 139.7673, startDateTime.plusHours(3), testUser)
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<Location> locationPage = new PageImpl<>(locationList, pageable, 3);

        when(locationRepository.findByUserAndDateTimeRange(
            eq(testUser),
            eq(startDateTime),
            eq(endDateTime),
            any(Pageable.class)
        )).thenReturn(locationPage);

        // Act
        PagedLocationResponse response = pageableSearchLocationsUseCase.execute(request, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(3, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.first());
        assertTrue(response.last());
        assertFalse(response.hasNext());
        assertFalse(response.hasPrevious());

        List<LocationResponse> content = response.content();
        assertEquals(3, content.size());
        assertEquals(1L, content.get(0).id());
        assertEquals(35.6812, content.get(0).latitude());
        assertEquals(139.7671, content.get(0).longitude());

        verify(locationRepository).findByUserAndDateTimeRange(
            eq(testUser),
            eq(startDateTime),
            eq(endDateTime),
            any(Pageable.class)
        );
    }

    @Test
    void execute_第2ページの取得_正しいページング情報が設定される() {
        // Arrange
        PageableSearchLocationsRequest request = new PageableSearchLocationsRequest(
            startDateTime,
            endDateTime,
            1, // page
            2  // size
        );

        List<Location> locationList = Arrays.asList(
            new Location(3L, 35.6814, 139.7673, startDateTime.plusHours(3), testUser),
            new Location(4L, 35.6815, 139.7674, startDateTime.plusHours(4), testUser)
        );

        Pageable pageable = PageRequest.of(1, 2);
        Page<Location> locationPage = new PageImpl<>(locationList, pageable, 5); // 全5件

        when(locationRepository.findByUserAndDateTimeRange(
            eq(testUser),
            eq(startDateTime),
            eq(endDateTime),
            any(Pageable.class)
        )).thenReturn(locationPage);

        // Act
        PagedLocationResponse response = pageableSearchLocationsUseCase.execute(request, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.page());
        assertEquals(2, response.size());
        assertEquals(5, response.totalElements());
        assertEquals(3, response.totalPages()); // (5 + 2 - 1) / 2 = 3
        assertFalse(response.first());
        assertFalse(response.last());
        assertTrue(response.hasNext());
        assertTrue(response.hasPrevious());

        assertEquals(2, response.content().size());
    }

    @Test
    void execute_該当データなし_空のページが返される() {
        // Arrange
        PageableSearchLocationsRequest request = new PageableSearchLocationsRequest(
            startDateTime,
            endDateTime,
            0,
            10
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<Location> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(locationRepository.findByUserAndDateTimeRange(
            eq(testUser),
            eq(startDateTime),
            eq(endDateTime),
            any(Pageable.class)
        )).thenReturn(emptyPage);

        // Act
        PagedLocationResponse response = pageableSearchLocationsUseCase.execute(request, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
        assertTrue(response.first());
        assertTrue(response.last());
        assertFalse(response.hasNext());
        assertFalse(response.hasPrevious());
        assertTrue(response.content().isEmpty());
    }

    @Test
    void execute_リクエストがnull_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> pageableSearchLocationsUseCase.execute(null, "testuser")
        );

        assertEquals("requestは必須です", exception.getMessage());
        verify(locationRepository, never()).findByUserAndDateTimeRange(any(), any(), any(), any());
    }

    @Test
    void execute_ユーザーIDがnull_例外が発生する() {
        // Arrange
        PageableSearchLocationsRequest request = new PageableSearchLocationsRequest(
            startDateTime,
            endDateTime,
            0,
            10
        );

        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> pageableSearchLocationsUseCase.execute(request, null)
        );

        assertEquals("userIdは必須です", exception.getMessage());
        verify(locationRepository, never()).findByUserAndDateTimeRange(any(), any(), any(), any());
    }

    @Test
    void execute_開始日時が終了日時より後_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new PageableSearchLocationsRequest(
                endDateTime,   // 終了日時
                startDateTime, // 開始日時（逆転）
                0,
                10
            )
        );

        assertEquals("開始日時は終了日時より前である必要があります", exception.getMessage());
    }

    @Test
    void execute_無効なページ番号_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new PageableSearchLocationsRequest(
                startDateTime,
                endDateTime,
                -1, // 無効なページ番号
                10
            )
        );

        assertEquals("ページ番号は0以上である必要があります", exception.getMessage());
    }

    @Test
    void execute_無効なページサイズ_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new PageableSearchLocationsRequest(
                startDateTime,
                endDateTime,
                0,
                0 // 無効なページサイズ
            )
        );

        assertEquals("ページサイズは1以上である必要があります", exception.getMessage());
    }

    @Test
    void execute_ページサイズが上限超過_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new PageableSearchLocationsRequest(
                startDateTime,
                endDateTime,
                0,
                101 // 上限を超過したページサイズ
            )
        );

        assertEquals("ページサイズは100以下である必要があります", exception.getMessage());
    }
}