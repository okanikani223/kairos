package com.github.okanikani.kairos.rules.others.jpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalTime;
import java.util.Objects;

/**
 * デフォルト勤怠ルールのJPAエンティティ
 * 
 * 業務要件: 所属期間の制約がないデフォルトの勤怠ルール設定を管理
 */
@Entity
@Table(name = "default_work_rules")
public class DefaultWorkRuleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "勤怠先IDは必須です")
    @Column(name = "work_place_id", nullable = false)
    private Long workPlaceId;

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

    @NotBlank(message = "ユーザーIDは必須です")
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @NotNull(message = "規定勤怠開始時刻は必須です")
    @Column(name = "standard_start_time", nullable = false)
    private LocalTime standardStartTime;

    @NotNull(message = "規定勤怠終了時刻は必須です")
    @Column(name = "standard_end_time", nullable = false)
    private LocalTime standardEndTime;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    protected DefaultWorkRuleJpaEntity() {
        // JPAのため
    }

    public DefaultWorkRuleJpaEntity(Long workPlaceId, Double latitude, Double longitude, String userId,
                                   LocalTime standardStartTime, LocalTime standardEndTime,
                                   LocalTime breakStartTime, LocalTime breakEndTime) {
        this.workPlaceId = Objects.requireNonNull(workPlaceId, "勤怠先IDは必須です");
        this.latitude = Objects.requireNonNull(latitude, "緯度は必須です");
        this.longitude = Objects.requireNonNull(longitude, "経度は必須です");
        this.userId = Objects.requireNonNull(userId, "ユーザーIDは必須です");
        this.standardStartTime = Objects.requireNonNull(standardStartTime, "規定勤怠開始時刻は必須です");
        this.standardEndTime = Objects.requireNonNull(standardEndTime, "規定勤怠終了時刻は必須です");
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
    }

    // ビジネスルールのバリデーション
    @PrePersist
    @PreUpdate
    @SuppressWarnings("PMD.UnusedPrivateMethod") // JPAによって自動的に呼び出される
    private void validateBusinessRules() {
        // 勤怠時刻のチェック
        if (standardStartTime.isAfter(standardEndTime)) {
            throw new IllegalStateException("規定勤怠開始時刻は規定勤怠終了時刻より前である必要があります");
        }
        
        // 休憩時刻のチェック
        if ((breakStartTime == null) != (breakEndTime == null)) {
            throw new IllegalStateException("休憩時刻は開始時刻と終了時刻の両方を設定するか、両方ともnullにしてください");
        }
        
        if (breakStartTime != null && breakEndTime != null && breakStartTime.isAfter(breakEndTime)) {
            throw new IllegalStateException("規定休憩開始時刻は規定休憩終了時刻より前である必要があります");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getWorkPlaceId() {
        return workPlaceId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getUserId() {
        return userId;
    }

    public LocalTime getStandardStartTime() {
        return standardStartTime;
    }

    public LocalTime getStandardEndTime() {
        return standardEndTime;
    }

    public LocalTime getBreakStartTime() {
        return breakStartTime;
    }

    public LocalTime getBreakEndTime() {
        return breakEndTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultWorkRuleJpaEntity that = (DefaultWorkRuleJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}