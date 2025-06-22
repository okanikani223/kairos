package com.github.okanikani223.kairos.reports.domains.models.vos;

import com.github.okanikani223.kairos.reports.domains.roundings.RoundingSetting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 勤怠日時(開始、終了)を表わすクラス
 * @param value ベースになる日時情報
 */
public record WorkTime(LocalDateTime value) {

    public WorkTime {
        Objects.requireNonNull(value, "勤務時刻は必須です");
        // 丸め処理なしのバージョン。外部から渡された値をそのまま保持。
    }

    /**
     * 丸め設定付きのファクトリメソッド。
     */
    public static WorkTime of(LocalDateTime value, RoundingSetting roundingSetting) {
        Objects.requireNonNull(value, "勤務時刻は必須です");
        if (roundingSetting != null) {
            return new WorkTime(roundingSetting.round(value));
        }
        return new WorkTime(value);
    }

    /**
     * 勤務日（LocalDate部のみ）を返す。
     */
    public LocalDate date() {
        return value.toLocalDate();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

