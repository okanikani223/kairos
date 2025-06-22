package com.github.okanikani.kairos.locations.others.controllers;

import com.github.okanikani.kairos.locations.applications.usecases.RegisterLocationUsecase;
import com.github.okanikani.kairos.locations.applications.usecases.dto.RegisterLocationRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    
    private final RegisterLocationUsecase registerLocationUsecase;
    
    public LocationController(RegisterLocationUsecase registerLocationUsecase) {
        this.registerLocationUsecase = Objects.requireNonNull(registerLocationUsecase, "registerLocationUsecaseは必須です");
    }
    
    @PostMapping
    public ResponseEntity<LocationResponse> registerLocation(@RequestBody RegisterLocationRequest request) {
        try {
            LocationResponse response = registerLocationUsecase.execute(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}