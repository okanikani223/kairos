package com.github.okanikani.kairos.locations.applications.usecases.dto;

import java.time.LocalDateTime;

public record LocationResponse(
        Long id,
        double latitude,
        double longitude,
        LocalDateTime recordedAt
) {
}