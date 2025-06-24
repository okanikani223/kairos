package com.github.okanikani.kairos.reports.domains.service;

import com.github.okanikani.kairos.reports.domains.models.vos.User;
import com.github.okanikani.kairos.reports.domains.roundings.RoundingSetting;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 勤務ルール解決サービス
 * 
 * 複数のルールドメイン（WorkRule、DefaultWorkRule、ReportCreationRule）から
 * 勤怠表生成に必要な情報を統合的に取得するサービス
 * 
 * Anti-Corruption Layerパターンを適用し、
 * reportsドメインが他ドメインの詳細実装に依存しないようにする
 */
public interface WorkRuleResolverService {
    
    /**
     * ユーザーの勤怠計算開始日を取得
     * ReportCreationRuleから取得し、未設定の場合はシステムデフォルト値を返す
     * 
     * @param user ユーザー
     * @return 勤怠計算開始日（1-31）
     */
    int getCalculationStartDay(User user);
    
    /**
     * ユーザーの時刻丸め設定を作成
     * ReportCreationRuleの時間計算単位を使用してRoundingSettingインスタンスを生成
     * 
     * @param user ユーザー
     * @return 丸め設定
     */
    RoundingSetting createRoundingSetting(User user);
    
    /**
     * 指定日時点で有効な勤務ルール情報を取得
     * 
     * 取得優先順位：
     * 1. WorkRule（有効期間内の勤怠ルール）- 最優先
     * 2. DefaultWorkRule（デフォルト勤怠ルール）- フォールバック
     * 3. システムデフォルト値 - 最終フォールバック
     * 
     * @param user ユーザー
     * @param workDate 勤務日
     * @return 勤務ルール情報
     */
    WorkRuleInfo resolveWorkRule(User user, LocalDate workDate);
    
    /**
     * 統合された勤務ルール情報
     * 勤怠計算に必要な情報を集約したレコード
     */
    record WorkRuleInfo(
        Duration standardWorkTime,     // 標準勤務時間
        LocalTime standardStartTime,   // 標準開始時刻
        LocalTime standardEndTime,     // 標準終了時刻
        Duration breakTime,           // 休憩時間
        boolean isValid               // ルール情報の有効性
    ) {
        
        public WorkRuleInfo {
            if (standardWorkTime != null && standardWorkTime.isNegative()) {
                throw new IllegalArgumentException("標準勤務時間は負の値にできません");
            }
            if (breakTime != null && breakTime.isNegative()) {
                throw new IllegalArgumentException("休憩時間は負の値にできません");
            }
            if (standardStartTime != null && standardEndTime != null && 
                standardStartTime.isAfter(standardEndTime)) {
                throw new IllegalArgumentException("標準開始時刻は標準終了時刻より前である必要があります");
            }
        }
        
        /**
         * システムデフォルトの勤務ルール情報を作成
         * @return デフォルト勤務ルール情報
         */
        public static WorkRuleInfo createDefault() {
            return new WorkRuleInfo(
                Duration.ofMinutes(450),  // 7.5時間（450分）
                LocalTime.of(9, 0),       // 9:00開始
                LocalTime.of(17, 30),     // 17:30終了
                Duration.ofMinutes(60),   // 1時間休憩
                true
            );
        }
        
        /**
         * 無効なルール情報を作成
         * @return 無効なルール情報
         */
        public static WorkRuleInfo createInvalid() {
            return new WorkRuleInfo(null, null, null, null, false);
        }
    }
}