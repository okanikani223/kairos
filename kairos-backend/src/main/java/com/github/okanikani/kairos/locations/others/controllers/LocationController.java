package com.github.okanikani.kairos.locations.others.controllers;

import com.github.okanikani.kairos.locations.applications.usecases.DeleteLocationUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.FindAllLocationsUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.FindLocationByIdUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.RegisterLocationUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.SearchLocationsUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.dto.RegisterLocationRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.applications.usecases.dto.SearchLocationsRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    
    private final RegisterLocationUseCase registerLocationUseCase;
    private final FindAllLocationsUseCase findAllLocationsUseCase;
    private final FindLocationByIdUseCase findLocationByIdUseCase;
    private final DeleteLocationUseCase deleteLocationUseCase;
    private final SearchLocationsUseCase searchLocationsUseCase;
    
    public LocationController(RegisterLocationUseCase registerLocationUseCase, FindAllLocationsUseCase findAllLocationsUseCase, FindLocationByIdUseCase findLocationByIdUseCase, DeleteLocationUseCase deleteLocationUseCase, SearchLocationsUseCase searchLocationsUseCase) {
        this.registerLocationUseCase = Objects.requireNonNull(registerLocationUseCase, "registerLocationUseCaseは必須です");
        this.findAllLocationsUseCase = Objects.requireNonNull(findAllLocationsUseCase, "findAllLocationsUseCaseは必須です");
        this.findLocationByIdUseCase = Objects.requireNonNull(findLocationByIdUseCase, "findLocationByIdUseCaseは必須です");
        this.deleteLocationUseCase = Objects.requireNonNull(deleteLocationUseCase, "deleteLocationUseCaseは必須です");
        this.searchLocationsUseCase = Objects.requireNonNull(searchLocationsUseCase, "searchLocationsUseCaseは必須です");
    }
    
    @PostMapping
    public ResponseEntity<LocationResponse> registerLocation(@RequestBody RegisterLocationRequest request, Authentication authentication) {
        String userId = authentication.getName();
        LocationResponse response = registerLocationUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<LocationResponse>> findAllLocations(Authentication authentication) {
        String userId = authentication.getName();
        List<LocationResponse> response = findAllLocationsUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> findLocationById(@PathVariable(name = "id") Long id, Authentication authentication) {
        String userId = authentication.getName();
        LocationResponse response = findLocationByIdUseCase.execute(id, userId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable(name = "id") Long id, Authentication authentication) {
        String userId = authentication.getName();
        deleteLocationUseCase.execute(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<LocationResponse>> searchLocations(
            @RequestParam("startDateTime") String startDateTimeStr,
            @RequestParam("endDateTime") String endDateTimeStr,
            Authentication authentication) {
        // 日時文字列のパース処理
        // フォーマット: "2024-01-01T09:00:00"
        LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        String userId = authentication.getName();
        SearchLocationsRequest request = new SearchLocationsRequest(startDateTime, endDateTime);
        List<LocationResponse> response = searchLocationsUseCase.execute(request, userId);
        return ResponseEntity.ok(response);
    }
}