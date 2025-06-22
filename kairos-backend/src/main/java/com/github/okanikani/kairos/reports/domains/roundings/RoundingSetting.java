package com.github.okanikani223.kairos.reports.domains.roundings;

import java.time.LocalDateTime;

/**
 * 勤務時刻に対する丸め処理を定義するインターフェース。
 */
@FunctionalInterface
public interface RoundingSetting {
    /**
     * 勤務時刻を丸めた結果を返す。
     * @param dateTime 元の時刻
     * @return 丸め後の時刻
     */
    LocalDateTime round(LocalDateTime dateTime);
}

