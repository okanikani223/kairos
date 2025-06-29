package com.github.okanikani.kairos.locations.others.controllers;

import com.github.okanikani.kairos.locations.applications.usecases.DeleteLocationUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.FindAllLocationsUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.FindLocationByIdUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.RegisterLocationUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.SearchLocationsUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.UpdateLocationUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.PageableSearchLocationsUseCase;
import com.github.okanikani.kairos.locations.applications.usecases.dto.RegisterLocationRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.UpdateLocationRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.applications.usecases.dto.SearchLocationsRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.PageableSearchLocationsRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.PagedLocationResponse;
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
    private final UpdateLocationUseCase updateLocationUseCase;
    private final PageableSearchLocationsUseCase pageableSearchLocationsUseCase;
    
    public LocationController(RegisterLocationUseCase registerLocationUseCase, FindAllLocationsUseCase findAllLocationsUseCase, FindLocationByIdUseCase findLocationByIdUseCase, DeleteLocationUseCase deleteLocationUseCase, SearchLocationsUseCase searchLocationsUseCase, UpdateLocationUseCase updateLocationUseCase, PageableSearchLocationsUseCase pageableSearchLocationsUseCase) {
        this.registerLocationUseCase = Objects.requireNonNull(registerLocationUseCase, "registerLocationUseCaseは必須です");
        this.findAllLocationsUseCase = Objects.requireNonNull(findAllLocationsUseCase, "findAllLocationsUseCaseは必須です");
        this.findLocationByIdUseCase = Objects.requireNonNull(findLocationByIdUseCase, "findLocationByIdUseCaseは必須です");
        this.deleteLocationUseCase = Objects.requireNonNull(deleteLocationUseCase, "deleteLocationUseCaseは必須です");
        this.searchLocationsUseCase = Objects.requireNonNull(searchLocationsUseCase, "searchLocationsUseCaseは必須です");
        this.updateLocationUseCase = Objects.requireNonNull(updateLocationUseCase, "updateLocationUseCaseは必須です");
        this.pageableSearchLocationsUseCase = Objects.requireNonNull(pageableSearchLocationsUseCase, "pageableSearchLocationsUseCaseは必須です");
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
    
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable(name = "id") Long id,
            @RequestBody UpdateLocationRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        LocationResponse response = updateLocationUseCase.execute(id, request, userId);
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

    @GetMapping("/search/paged")
    public ResponseEntity<PagedLocationResponse> searchLocationsWithPagination(
            @RequestParam("startDateTime") String startDateTimeStr,
            @RequestParam("endDateTime") String endDateTimeStr,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication) {
        // 日時文字列のパース処理
        // フォーマット: "2024-01-01T09:00:00"
        LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        String userId = authentication.getName();
        PageableSearchLocationsRequest request = new PageableSearchLocationsRequest(
            startDateTime, 
            endDateTime, 
            page, 
            size
        );
        PagedLocationResponse response = pageableSearchLocationsUseCase.execute(request, userId);
        return ResponseEntity.ok(response);
    }
}