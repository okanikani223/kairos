package com.github.okanikani.kairos.reports.domains.roundings;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import java.time.LocalDateTime;

/**
 * 分単位での時刻丸め処理を行うRoundingSettingの実装クラス
 * 指定された時間単位（分）で時刻を切り上げ丸めする
 */
public class MinuteBasedRoundingSetting implements RoundingSetting {
    
    // 時間計算単位の制限値
    private static final int MIN_UNIT_MINUTES = 1;
    private static final int MAX_UNIT_MINUTES = 60;
    
    private final int unitMinutes;
    
    /**
     * コンストラクタ
     * @param unitMinutes 丸め単位（分）：1-60分の範囲で指定
     */
    public MinuteBasedRoundingSetting(int unitMinutes) {
        if (unitMinutes < MIN_UNIT_MINUTES || unitMinutes > MAX_UNIT_MINUTES) {
            throw new ValidationException("時間計算単位は" + MIN_UNIT_MINUTES + "-" + MAX_UNIT_MINUTES + "分の範囲で指定してください");
        }
        this.unitMinutes = unitMinutes;
    }
    
    /**
     * 指定された時刻を分単位で切り上げ丸めする
     * @param dateTime 元の時刻
     * @return 丸め後の時刻（秒・ナノ秒は0にリセット）
     */
    @Override
    public LocalDateTime round(LocalDateTime dateTime) {
        int currentMinute = dateTime.getMinute();
        
        // 切り上げ計算：(currentMinute + unitMinutes - 1) / unitMinutes * unitMinutes
        int roundedMinute = ((currentMinute + unitMinutes - 1) / unitMinutes) * unitMinutes;
        
        // 60分を超える場合は時間を繰り上げ
        if (roundedMinute >= MAX_UNIT_MINUTES) {
            return dateTime.withMinute(0).withSecond(0).withNano(0).plusHours(1);
        }
        
        return dateTime.withMinute(roundedMinute).withSecond(0).withNano(0);
    }
    
    /**
     * 丸め単位（分）を取得
     * @return 丸め単位（分）
     */
    public int getUnitMinutes() {
        return unitMinutes;
    }
}