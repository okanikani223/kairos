package com.github.okanikani.kairos.locations.applications.usecases.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public record RegisterLocationRequest(
        double latitude,
        double longitude,
        LocalDateTime recordedAt
) {
    public RegisterLocationRequest {
        Objects.requireNonNull(recordedAt, "記録日時は必須です");
    }
}