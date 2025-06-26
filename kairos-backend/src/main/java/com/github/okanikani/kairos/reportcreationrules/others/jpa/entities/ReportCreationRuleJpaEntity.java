package com.github.okanikani.kairos.reportcreationrules.others.jpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Objects;

/**
 * 勤怠作成ルールのJPAエンティティ
 * 
 * 業務要件: ユーザー毎の勤怠表作成ルール（締め日、時間計算単位）を管理
 */
@Entity
@Table(name = "report_creation_rules", uniqueConstraints = {
    @UniqueConstraint(columnNames = "user_id", name = "uk_report_creation_rules_user_id")
})
public class ReportCreationRuleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "ユーザーIDは必須です")
    @Column(name = "user_id", nullable = false, length = 255, unique = true)
    private String userId;

    @NotNull(message = "勤怠締め日は必須です")
    @Min(value = 1, message = "勤怠締め日は1日以上である必要があります")
    @Max(value = 31, message = "勤怠締め日は31日以下である必要があります")
    @Column(name = "closing_day", nullable = false)
    private Integer closingDay;

    @NotNull(message = "勤怠時間計算単位は必須です")
    @Min(value = 1, message = "勤怠時間計算単位は1分以上である必要があります")
    @Max(value = 60, message = "勤怠時間計算単位は60分以下である必要があります")
    @Column(name = "time_calculation_unit_minutes", nullable = false)
    private Integer timeCalculationUnitMinutes;

    protected ReportCreationRuleJpaEntity() {
        // JPAのため
    }

    public ReportCreationRuleJpaEntity(String userId, Integer closingDay, Integer timeCalculationUnitMinutes) {
        this.userId = Objects.requireNonNull(userId, "ユーザーIDは必須です");
        this.closingDay = Objects.requireNonNull(closingDay, "勤怠締め日は必須です");
        this.timeCalculationUnitMinutes = Objects.requireNonNull(timeCalculationUnitMinutes, "勤怠時間計算単位は必須です");
    }

    // ビジネスルールのバリデーション
    @PrePersist
    @PreUpdate
    private void validateBusinessRules() {
        // 勤怠締め日の範囲チェック
        if (closingDay < 1 || closingDay > 31) {
            throw new IllegalStateException("勤怠締め日は1日から31日までの範囲で指定してください");
        }
        
        // 勤怠時間計算単位の範囲チェック
        if (timeCalculationUnitMinutes < 1 || timeCalculationUnitMinutes > 60) {
            throw new IllegalStateException("勤怠時間計算単位は1分から60分までの範囲で指定してください");
        }
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getClosingDay() {
        return closingDay;
    }

    public Integer getTimeCalculationUnitMinutes() {
        return timeCalculationUnitMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportCreationRuleJpaEntity that = (ReportCreationRuleJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}