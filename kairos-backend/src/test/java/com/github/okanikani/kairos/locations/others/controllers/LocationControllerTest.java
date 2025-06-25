package com.github.okanikani.kairos.locations.others.controllers;

import com.github.okanikani.kairos.locations.applications.usecases.DeleteLocationUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.FindAllLocationsUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.FindLocationByIdUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.RegisterLocationUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.SearchLocationsUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.dto.RegisterLocationRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.security.core.Authentication;

class LocationControllerTest {

    private LocationController locationController;

    @Mock
    private RegisterLocationUseCase registerLocationUseCase;
    
    @Mock
    private FindAllLocationsUseCase findAllLocationsUseCase;
    
    @Mock
    private FindLocationByIdUseCase findLocationByIdUseCase;
    
    @Mock
    private DeleteLocationUseCase deleteLocationUseCase;
    
    @Mock
    private SearchLocationsUseCase searchLocationsUseCase;
    
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        locationController = new LocationController(registerLocationUseCase, findAllLocationsUseCase, findLocationByIdUseCase, deleteLocationUseCase, searchLocationsUseCase);
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

        when(registerLocationUseCase.execute(any(RegisterLocationRequest.class), anyString())).thenReturn(expectedResponse);
        when(authentication.getName()).thenReturn("testuser");

        // Act
        ResponseEntity<LocationResponse> response = locationController.registerLocation(request, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals(35.6812, response.getBody().latitude());
        assertEquals(139.7671, response.getBody().longitude());
        assertEquals(recordedAt, response.getBody().recordedAt());
        verify(registerLocationUseCase, times(1)).execute(any(RegisterLocationRequest.class), eq("testuser"));
    }

    @Test
    void registerLocation_ユースケースで例外発生_400ステータスを返す() {
        // Arrange
        RegisterLocationRequest request = new RegisterLocationRequest(
            91.0,  // 無効な緯度
            139.7671,
            LocalDateTime.now()
        );

        when(registerLocationUseCase.execute(any(RegisterLocationRequest.class), anyString()))
            .thenThrow(new IllegalArgumentException("緯度は-90.0～90.0の範囲で指定してください"));
        when(authentication.getName()).thenReturn("testuser");

        // Act
        ResponseEntity<LocationResponse> response = locationController.registerLocation(request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(registerLocationUseCase, times(1)).execute(any(RegisterLocationRequest.class), eq("testuser"));
    }

    @Test
    void registerLocation_予期しない例外発生_500ステータスを返す() {
        // Arrange
        RegisterLocationRequest request = new RegisterLocationRequest(
            35.6812,
            139.7671,
            LocalDateTime.now()
        );

        when(registerLocationUseCase.execute(any(RegisterLocationRequest.class), anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));
        when(authentication.getName()).thenReturn("testuser");

        // Act
        ResponseEntity<LocationResponse> response = locationController.registerLocation(request, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(registerLocationUseCase, times(1)).execute(any(RegisterLocationRequest.class), eq("testuser"));
    }

    @Test
    void constructor_nullRegisterUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new LocationController(null, findAllLocationsUseCase, findLocationByIdUseCase, deleteLocationUseCase, searchLocationsUseCase)
        );
        assertEquals("registerLocationUseCaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullFindAllUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new LocationController(registerLocationUseCase, null, findLocationByIdUseCase, deleteLocationUseCase, searchLocationsUseCase)
        );
        assertEquals("findAllLocationsUseCaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullFindByIdUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new LocationController(registerLocationUseCase, findAllLocationsUseCase, null, deleteLocationUseCase, searchLocationsUseCase)
        );
        assertEquals("findLocationByIdUseCaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullDeleteUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new LocationController(registerLocationUseCase, findAllLocationsUseCase, findLocationByIdUseCase, null, searchLocationsUseCase)
        );
        assertEquals("deleteLocationUseCaseは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullSearchUseCase_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new LocationController(registerLocationUseCase, findAllLocationsUseCase, findLocationByIdUseCase, deleteLocationUseCase, null)
        );
        assertEquals("searchLocationsUseCaseは必須です", exception.getMessage());
    }
    
    @Test
    void findAllLocations_正常ケース_200ステータスと位置情報一覧を返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        
        List<LocationResponse> expectedLocations = Arrays.asList(
            new LocationResponse(1L, 35.6812, 139.7671, LocalDateTime.of(2024, 1, 1, 9, 0)),
            new LocationResponse(2L, 35.6813, 139.7672, LocalDateTime.of(2024, 1, 1, 12, 0)),
            new LocationResponse(3L, 35.6814, 139.7673, LocalDateTime.of(2024, 1, 1, 18, 0))
        );
        
        when(findAllLocationsUseCase.execute(anyString())).thenReturn(expectedLocations);

        // Act
        ResponseEntity<List<LocationResponse>> response = locationController.findAllLocations(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).id());
        assertEquals(35.6812, response.getBody().get(0).latitude());
        verify(findAllLocationsUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findAllLocations_位置情報が存在しない場合_200ステータスと空リストを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findAllLocationsUseCase.execute(anyString())).thenReturn(List.of());

        // Act
        ResponseEntity<List<LocationResponse>> response = locationController.findAllLocations(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(findAllLocationsUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findAllLocations_ユースケースで例外発生_500ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(findAllLocationsUseCase.execute(anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<List<LocationResponse>> response = locationController.findAllLocations(authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(findAllLocationsUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    void findLocationById_正常ケース_200ステータスと位置情報を返す() {
        // Arrange
        Long locationId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        
        LocationResponse expectedLocation = new LocationResponse(
            locationId, 35.6812, 139.7671, LocalDateTime.of(2024, 1, 1, 9, 0)
        );
        
        when(findLocationByIdUseCase.execute(eq(locationId), anyString())).thenReturn(expectedLocation);

        // Act
        ResponseEntity<LocationResponse> response = locationController.findLocationById(locationId, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(locationId, response.getBody().id());
        assertEquals(35.6812, response.getBody().latitude());
        verify(findLocationByIdUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    void findLocationById_位置情報が存在しない場合_404ステータスを返す() {
        // Arrange
        Long locationId = 999L;
        when(authentication.getName()).thenReturn("testuser");
        when(findLocationByIdUseCase.execute(eq(locationId), anyString()))
            .thenThrow(new IllegalArgumentException("指定された位置情報が存在しません"));

        // Act
        ResponseEntity<LocationResponse> response = locationController.findLocationById(locationId, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(findLocationByIdUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    void findLocationById_権限がない場合_403ステータスを返す() {
        // Arrange
        Long locationId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        when(findLocationByIdUseCase.execute(eq(locationId), anyString()))
            .thenThrow(new IllegalArgumentException("この位置情報にアクセスする権限がありません"));

        // Act
        ResponseEntity<LocationResponse> response = locationController.findLocationById(locationId, authentication);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(findLocationByIdUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    void findLocationById_予期しない例外発生_500ステータスを返す() {
        // Arrange
        Long locationId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        when(findLocationByIdUseCase.execute(eq(locationId), anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<LocationResponse> response = locationController.findLocationById(locationId, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(findLocationByIdUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    void deleteLocation_正常ケース_204ステータスを返す() {
        // Arrange
        Long locationId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        doNothing().when(deleteLocationUseCase).execute(eq(locationId), anyString());

        // Act
        ResponseEntity<Void> response = locationController.deleteLocation(locationId, authentication);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deleteLocationUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    void deleteLocation_位置情報が存在しない場合_404ステータスを返す() {
        // Arrange
        Long locationId = 999L;
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new IllegalArgumentException("指定された位置情報が存在しません"))
            .when(deleteLocationUseCase).execute(eq(locationId), anyString());

        // Act
        ResponseEntity<Void> response = locationController.deleteLocation(locationId, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(deleteLocationUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    void deleteLocation_権限がない場合_403ステータスを返す() {
        // Arrange
        Long locationId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new IllegalArgumentException("この位置情報を削除する権限がありません"))
            .when(deleteLocationUseCase).execute(eq(locationId), anyString());

        // Act
        ResponseEntity<Void> response = locationController.deleteLocation(locationId, authentication);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(deleteLocationUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    void deleteLocation_予期しない例外発生_500ステータスを返す() {
        // Arrange
        Long locationId = 1L;
        when(authentication.getName()).thenReturn("testuser");
        doThrow(new RuntimeException("データベースエラー"))
            .when(deleteLocationUseCase).execute(eq(locationId), anyString());

        // Act
        ResponseEntity<Void> response = locationController.deleteLocation(locationId, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(deleteLocationUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    void searchLocations_正常ケース_200ステータスと検索結果を返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        String startDateTime = "2024-01-01T09:00:00";
        String endDateTime = "2024-01-01T18:00:00";
        
        List<LocationResponse> expectedLocations = Arrays.asList(
            new LocationResponse(1L, 35.6812, 139.7671, LocalDateTime.of(2024, 1, 1, 9, 30)),
            new LocationResponse(2L, 35.6813, 139.7672, LocalDateTime.of(2024, 1, 1, 12, 0))
        );
        
        when(searchLocationsUseCase.execute(any(), anyString())).thenReturn(expectedLocations);

        // Act
        ResponseEntity<List<LocationResponse>> response = locationController.searchLocations(
            startDateTime, endDateTime, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).id());
        verify(searchLocationsUseCase, times(1)).execute(any(), eq("testuser"));
    }

    @Test
    void searchLocations_検索結果が存在しない場合_200ステータスと空リストを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        String startDateTime = "2024-01-01T09:00:00";
        String endDateTime = "2024-01-01T18:00:00";
        
        when(searchLocationsUseCase.execute(any(), anyString())).thenReturn(List.of());

        // Act
        ResponseEntity<List<LocationResponse>> response = locationController.searchLocations(
            startDateTime, endDateTime, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(searchLocationsUseCase, times(1)).execute(any(), eq("testuser"));
    }

    @Test
    void searchLocations_不正な日時形式_400ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        String invalidStartDateTime = "invalid-datetime";
        String endDateTime = "2024-01-01T18:00:00";

        // Act
        ResponseEntity<List<LocationResponse>> response = locationController.searchLocations(
            invalidStartDateTime, endDateTime, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(searchLocationsUseCase, never()).execute(any(), any());
    }

    @Test
    void searchLocations_開始日時が終了日時より後_400ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        String startDateTime = "2024-01-01T18:00:00";  // 終了日時
        String endDateTime = "2024-01-01T09:00:00";    // 開始日時（逆転）

        // Act
        ResponseEntity<List<LocationResponse>> response = locationController.searchLocations(
            startDateTime, endDateTime, authentication);

        // Assert
        // SearchLocationsRequestコンストラクタで例外が発生するため、usecaseは呼び出されない
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(searchLocationsUseCase, never()).execute(any(), any());
    }

    @Test
    void searchLocations_予期しない例外発生_500ステータスを返す() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        String startDateTime = "2024-01-01T09:00:00";
        String endDateTime = "2024-01-01T18:00:00";
        
        when(searchLocationsUseCase.execute(any(), anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act
        ResponseEntity<List<LocationResponse>> response = locationController.searchLocations(
            startDateTime, endDateTime, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(searchLocationsUseCase, times(1)).execute(any(), eq("testuser"));
    }
}