package com.github.okanikani.kairos.locations.others.jpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 位置情報のJPAエンティティ
 * 
 * 業務要件: GPS座標と記録日時を管理し、ユーザーごとの位置情報を保存
 */
@Entity
@Table(name = "locations")
public class LocationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "緯度は必須です")
    @DecimalMin(value = "-90.0", message = "緯度は-90.0以上である必要があります")
    @DecimalMax(value = "90.0", message = "緯度は90.0以下である必要があります")
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull(message = "経度は必須です")
    @DecimalMin(value = "-180.0", message = "経度は-180.0以上である必要があります")
    @DecimalMax(value = "180.0", message = "経度は180.0以下である必要があります")
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @NotNull(message = "記録日時は必須です")
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @NotBlank(message = "ユーザーIDは必須です")
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    protected LocationJpaEntity() {
        // JPAのため
    }

    public LocationJpaEntity(Double latitude, Double longitude, LocalDateTime recordedAt, String userId) {
        this.latitude = Objects.requireNonNull(latitude, "緯度は必須です");
        this.longitude = Objects.requireNonNull(longitude, "経度は必須です");
        this.recordedAt = Objects.requireNonNull(recordedAt, "記録日時は必須です");
        this.userId = Objects.requireNonNull(userId, "ユーザーIDは必須です");
    }

    public Long getId() {
        return id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationJpaEntity that = (LocationJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}