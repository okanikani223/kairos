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
import java.time.format.DateTimeParseException;
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
        try {
            String userId = authentication.getName();
            LocationResponse response = registerLocationUseCase.execute(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<LocationResponse>> findAllLocations(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<LocationResponse> response = findAllLocationsUseCase.execute(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> findLocationById(@PathVariable(name = "id") Long id, Authentication authentication) {
        try {
            String userId = authentication.getName();
            LocationResponse response = findLocationByIdUseCase.execute(id, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // セキュリティエラー判定: 権限なしエラーと存在しないエラーを区別
            // 理由: 適切なHTTPステータスコードを返すため
            if (e.getMessage().contains("権限がありません")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("存在しません")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable(name = "id") Long id, Authentication authentication) {
        try {
            String userId = authentication.getName();
            deleteLocationUseCase.execute(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // セキュリティエラー判定: 権限なしエラーと存在しないエラーを区別
            // 理由: 適切なHTTPステータスコードを返すため
            if (e.getMessage().contains("権限がありません")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("存在しません")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<LocationResponse>> searchLocations(
            @RequestParam("startDateTime") String startDateTimeStr,
            @RequestParam("endDateTime") String endDateTimeStr,
            Authentication authentication) {
        try {
            // 日時文字列のパース処理
            // フォーマット: "2024-01-01T09:00:00"
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;
            
            try {
                startDateTime = LocalDateTime.parse(startDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                endDateTime = LocalDateTime.parse(endDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                // 日時フォーマットエラー: ISO-8601形式（YYYY-MM-DDTHH:mm:ss）で入力する必要がある
                // 理由: 不正な日時形式によるシステムエラーを防止するため
                return ResponseEntity.badRequest().build();
            }
            
            String userId = authentication.getName();
            SearchLocationsRequest request = new SearchLocationsRequest(startDateTime, endDateTime);
            List<LocationResponse> response = searchLocationsUseCase.execute(request, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}