package com.github.okanikani.kairos.reports.domains.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * 勤怠表の計算期間を算出するユーティリティクラス
 * 勤怠締め日を考慮した正確な期間計算を提供する
 */
public class ReportPeriodCalculator {
    
    /**
     * 勤怠年月と勤怠締め日から実際の勤怠計算期間を算出
     * 
     * 計算ロジック：
     * - 期間開始日：前月の勤怠締め日の翌日
     * - 期間終了日：当月の勤怠締め日
     * 
     * 例：勤怠年月=2025/06、勤怠締め日=15の場合
     * → 期間：2025/05/16〜2025/06/15
     * 
     * @param reportYearMonth 勤怠年月（例：2025/06）
     * @param closingDay 勤怠締め日（例：15）
     * @return 勤怠計算期間
     */
    public static ReportPeriod calculatePeriod(YearMonth reportYearMonth, int closingDay) {
        // バリデーション
        if (closingDay < 1 || closingDay > 31) {
            throw new IllegalArgumentException("勤怠締め日は1-31の範囲で指定してください");
        }
        
        // 勤怠年月の前月の勤怠締め日+1が期間開始
        LocalDate periodStart = reportYearMonth.minusMonths(1).atDay(closingDay).plusDays(1);
        
        // 勤怠年月の勤怠締め日が期間終了（月末調整考慮）
        LocalDate periodEnd;
        if (closingDay > reportYearMonth.lengthOfMonth()) {
            // 勤怠締め日が月の日数を超える場合は月末に調整
            periodEnd = reportYearMonth.atEndOfMonth();
        } else {
            periodEnd = reportYearMonth.atDay(closingDay);
        }
        
        return new ReportPeriod(periodStart, periodEnd);
    }
    
    /**
     * 勤怠計算期間を表すレコード
     * 開始日と終了日を保持し、時刻変換メソッドを提供
     */
    public record ReportPeriod(LocalDate startDate, LocalDate endDate) {
        
        public ReportPeriod {
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("開始日と終了日は必須です");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("開始日は終了日以前である必要があります");
            }
        }
        
        /**
         * 期間開始の日時（00:00:00）を取得
         * @return 期間開始日時
         */
        public LocalDateTime startDateTime() {
            return startDate.atStartOfDay();
        }
        
        /**
         * 期間終了の日時（23:59:59）を取得
         * @return 期間終了日時
         */
        public LocalDateTime endDateTime() {
            return endDate.atTime(23, 59, 59);
        }
        
        /**
         * 期間の日数を取得
         * @return 期間の日数（開始日と終了日を含む）
         */
        public long getDays() {
            return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
    }
}