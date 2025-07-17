package com.github.okanikani.kairos.reportcreationrules.domains.models.entities;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User;

import java.util.Objects;

/**
 * 勤怠作成ルールエンティティ
 * 勤怠表作成時のルールを表現するドメインモデル
 * 
 * 勤怠締め日と勤怠時間計算単位を管理し、
 * 勤怠表の作成・集計処理における基準を提供する
 */
public record ReportCreationRule(
        Long id,                           // 一意識別子（新規作成時はnull）
        User user,                         // ユーザー
        int closingDay,                    // 勤怠締め日（月の日：1-31）
        int timeCalculationUnitMinutes     // 勤怠時間計算単位（分：1-60）
) {
    
    // 勤怠締め日の範囲定数
    private static final int MIN_CLOSING_DAY = 1;
    private static final int MAX_CLOSING_DAY = 31;
    
    // 勤怠時間計算単位の範囲定数
    private static final int MIN_TIME_CALCULATION_UNIT_MINUTES = 1;
    private static final int MAX_TIME_CALCULATION_UNIT_MINUTES = 60;
    
    /**
     * コンストラクタ
     * 各フィールドのバリデーションを実行
     */
    public ReportCreationRule {
        // 必須フィールドのnullチェック
        Objects.requireNonNull(user, "ユーザーは必須です");
        
        // 勤怠締め日の範囲チェック
        if (closingDay < MIN_CLOSING_DAY || closingDay > MAX_CLOSING_DAY) {
            throw new ValidationException("勤怠締め日は" + MIN_CLOSING_DAY + "日から" + MAX_CLOSING_DAY + "日までの範囲で指定してください");
        }
        
        // 勤怠時間計算単位の範囲チェック
        if (timeCalculationUnitMinutes < MIN_TIME_CALCULATION_UNIT_MINUTES) {
            throw new ValidationException("勤怠時間計算単位は" + MIN_TIME_CALCULATION_UNIT_MINUTES + "分以上である必要があります");
        }
        if (timeCalculationUnitMinutes > MAX_TIME_CALCULATION_UNIT_MINUTES) {
            throw new ValidationException("勤怠時間計算単位は" + MAX_TIME_CALCULATION_UNIT_MINUTES + "分以下である必要があります");
        }
    }
}