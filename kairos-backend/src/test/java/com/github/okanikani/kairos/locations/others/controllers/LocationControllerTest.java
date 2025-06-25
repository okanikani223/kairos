package com.github.okanikani.kairos.locations.others.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.okanikani.kairos.commons.dto.ErrorResponse;
import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.locations.applications.usecases.DeleteLocationUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.FindAllLocationsUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.FindLocationByIdUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.RegisterLocationUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.SearchLocationsUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.dto.RegisterLocationRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.security.JwtService;
import com.github.okanikani.kairos.commons.controllers.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
@Import(GlobalExceptionHandler.class)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterLocationUseCase registerLocationUseCase;
    
    @MockitoBean
    private FindAllLocationsUseCase findAllLocationsUseCase;
    
    @MockitoBean
    private FindLocationByIdUseCase findLocationByIdUseCase;
    
    @MockitoBean
    private DeleteLocationUseCase deleteLocationUseCase;
    
    @MockitoBean
    private SearchLocationsUseCase searchLocationsUseCase;
    
    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "testuser")
    void registerLocation_正常ケース_201ステータスとレスポンスを返す() throws Exception {
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

        // Act & Assert
        mockMvc.perform(post("/api/locations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.latitude").value(35.6812))
                .andExpect(jsonPath("$.longitude").value(139.7671))
                .andExpect(jsonPath("$.recordedAt").value("2024-01-01T12:00:00"));

        verify(registerLocationUseCase, times(1)).execute(any(RegisterLocationRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void registerLocation_ユースケースで例外発生_400ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        RegisterLocationRequest request = new RegisterLocationRequest(
            91.0,  // 無効な緯度
            139.7671,
            LocalDateTime.now()
        );

        when(registerLocationUseCase.execute(any(RegisterLocationRequest.class), anyString()))
            .thenThrow(new IllegalArgumentException("緯度は-90.0～90.0の範囲で指定してください"));

        // Act & Assert
        mockMvc.perform(post("/api/locations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("緯度は-90.0～90.0の範囲で指定してください"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(registerLocationUseCase, times(1)).execute(any(RegisterLocationRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void registerLocation_予期しない例外発生_500ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        RegisterLocationRequest request = new RegisterLocationRequest(
            35.6812,
            139.7671,
            LocalDateTime.now()
        );

        when(registerLocationUseCase.execute(any(RegisterLocationRequest.class), anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act & Assert
        mockMvc.perform(post("/api/locations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(registerLocationUseCase, times(1)).execute(any(RegisterLocationRequest.class), eq("testuser"));
    }

    
    @Test
    @WithMockUser(username = "testuser")
    void findAllLocations_正常ケース_200ステータスと位置情報一覧を返す() throws Exception {
        // Arrange
        List<LocationResponse> expectedLocations = Arrays.asList(
            new LocationResponse(1L, 35.6812, 139.7671, LocalDateTime.of(2024, 1, 1, 9, 0)),
            new LocationResponse(2L, 35.6813, 139.7672, LocalDateTime.of(2024, 1, 1, 12, 0)),
            new LocationResponse(3L, 35.6814, 139.7673, LocalDateTime.of(2024, 1, 1, 18, 0))
        );
        
        when(findAllLocationsUseCase.execute(anyString())).thenReturn(expectedLocations);

        // Act & Assert
        mockMvc.perform(get("/api/locations")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].latitude").value(35.6812))
                .andExpect(jsonPath("$[0].longitude").value(139.7671));

        verify(findAllLocationsUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findAllLocations_位置情報が存在しない場合_200ステータスと空リストを返す() throws Exception {
        // Arrange
        when(findAllLocationsUseCase.execute(anyString())).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/locations")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(findAllLocationsUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findAllLocations_ユースケースで例外発生_500ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        when(findAllLocationsUseCase.execute(anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act & Assert
        mockMvc.perform(get("/api/locations")
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(findAllLocationsUseCase, times(1)).execute(eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findLocationById_正常ケース_200ステータスと位置情報を返す() throws Exception {
        // Arrange
        Long locationId = 1L;
        LocationResponse expectedLocation = new LocationResponse(
            locationId, 35.6812, 139.7671, LocalDateTime.of(2024, 1, 1, 9, 0)
        );
        
        when(findLocationByIdUseCase.execute(eq(locationId), anyString())).thenReturn(expectedLocation);

        // Act & Assert
        mockMvc.perform(get("/api/locations/{id}", locationId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(locationId))
                .andExpect(jsonPath("$.latitude").value(35.6812))
                .andExpect(jsonPath("$.longitude").value(139.7671));

        verify(findLocationByIdUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findLocationById_位置情報が存在しない場合_404ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        Long locationId = 999L;
        when(findLocationByIdUseCase.execute(eq(locationId), anyString()))
            .thenThrow(new ResourceNotFoundException("指定された位置情報が存在しません"));

        // Act & Assert
        mockMvc.perform(get("/api/locations/{id}", locationId)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("指定された位置情報が存在しません"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(findLocationByIdUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findLocationById_権限がない場合_403ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        Long locationId = 1L;
        when(findLocationByIdUseCase.execute(eq(locationId), anyString()))
            .thenThrow(new AuthorizationException("この位置情報にアクセスする権限がありません"));

        // Act & Assert
        mockMvc.perform(get("/api/locations/{id}", locationId)
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("AUTHORIZATION_ERROR"))
                .andExpect(jsonPath("$.message").value("この位置情報にアクセスする権限がありません"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(findLocationByIdUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void findLocationById_予期しない例外発生_500ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        Long locationId = 1L;
        when(findLocationByIdUseCase.execute(eq(locationId), anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act & Assert
        mockMvc.perform(get("/api/locations/{id}", locationId)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(findLocationByIdUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteLocation_正常ケース_204ステータスを返す() throws Exception {
        // Arrange
        Long locationId = 1L;
        doNothing().when(deleteLocationUseCase).execute(eq(locationId), anyString());

        // Act & Assert
        mockMvc.perform(delete("/api/locations/{id}", locationId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(deleteLocationUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteLocation_位置情報が存在しない場合_404ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        Long locationId = 999L;
        doThrow(new ResourceNotFoundException("指定された位置情報が存在しません"))
            .when(deleteLocationUseCase).execute(eq(locationId), anyString());

        // Act & Assert
        mockMvc.perform(delete("/api/locations/{id}", locationId)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("指定された位置情報が存在しません"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(deleteLocationUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteLocation_権限がない場合_403ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        Long locationId = 1L;
        doThrow(new AuthorizationException("この位置情報を削除する権限がありません"))
            .when(deleteLocationUseCase).execute(eq(locationId), anyString());

        // Act & Assert
        mockMvc.perform(delete("/api/locations/{id}", locationId)
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("AUTHORIZATION_ERROR"))
                .andExpect(jsonPath("$.message").value("この位置情報を削除する権限がありません"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(deleteLocationUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteLocation_予期しない例外発生_500ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        Long locationId = 1L;
        doThrow(new RuntimeException("データベースエラー"))
            .when(deleteLocationUseCase).execute(eq(locationId), anyString());

        // Act & Assert
        mockMvc.perform(delete("/api/locations/{id}", locationId)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(deleteLocationUseCase, times(1)).execute(eq(locationId), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchLocations_正常ケース_200ステータスと検索結果を返す() throws Exception {
        // Arrange
        String startDateTime = "2024-01-01T09:00:00";
        String endDateTime = "2024-01-01T18:00:00";
        
        List<LocationResponse> expectedLocations = Arrays.asList(
            new LocationResponse(1L, 35.6812, 139.7671, LocalDateTime.of(2024, 1, 1, 9, 30)),
            new LocationResponse(2L, 35.6813, 139.7672, LocalDateTime.of(2024, 1, 1, 12, 0))
        );
        
        when(searchLocationsUseCase.execute(any(), anyString())).thenReturn(expectedLocations);

        // Act & Assert
        mockMvc.perform(get("/api/locations/search")
                .param("startDateTime", startDateTime)
                .param("endDateTime", endDateTime)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].latitude").value(35.6812));

        verify(searchLocationsUseCase, times(1)).execute(any(), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchLocations_検索結果が存在しない場合_200ステータスと空リストを返す() throws Exception {
        // Arrange
        String startDateTime = "2024-01-01T09:00:00";
        String endDateTime = "2024-01-01T18:00:00";
        
        when(searchLocationsUseCase.execute(any(), anyString())).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/locations/search")
                .param("startDateTime", startDateTime)
                .param("endDateTime", endDateTime)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(searchLocationsUseCase, times(1)).execute(any(), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchLocations_不正な日時形式_400ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        String invalidStartDateTime = "invalid-datetime";
        String endDateTime = "2024-01-01T18:00:00";

        // Act & Assert
        mockMvc.perform(get("/api/locations/search")
                .param("startDateTime", invalidStartDateTime)
                .param("endDateTime", endDateTime)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("日時形式が正しくありません。ISO-8601形式（YYYY-MM-DDTHH:mm:ss）で入力してください。"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(searchLocationsUseCase, never()).execute(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchLocations_開始日時が終了日時より後_400ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        String startDateTime = "2024-01-01T18:00:00";  // 終了日時
        String endDateTime = "2024-01-01T09:00:00";    // 開始日時（逆転）

        // Act & Assert
        // SearchLocationsRequestコンストラクタで例外が発生するため、usecaseは呼び出されない
        mockMvc.perform(get("/api/locations/search")
                .param("startDateTime", startDateTime)
                .param("endDateTime", endDateTime)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(searchLocationsUseCase, never()).execute(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchLocations_予期しない例外発生_500ステータスとエラーレスポンスを返す() throws Exception {
        // Arrange
        String startDateTime = "2024-01-01T09:00:00";
        String endDateTime = "2024-01-01T18:00:00";
        
        when(searchLocationsUseCase.execute(any(), anyString()))
            .thenThrow(new RuntimeException("データベースエラー"));

        // Act & Assert
        mockMvc.perform(get("/api/locations/search")
                .param("startDateTime", startDateTime)
                .param("endDateTime", endDateTime)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("システムエラーが発生しました。しばらく時間をおいて再度お試しください。"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(searchLocationsUseCase, times(1)).execute(any(), eq("testuser"));
    }
}