package com.github.okanikani.kairos.reportcreationrules.domains.models.entities;

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
    
    /**
     * コンストラクタ
     * 各フィールドのバリデーションを実行
     */
    public ReportCreationRule {
        // 必須フィールドのnullチェック
        Objects.requireNonNull(user, "ユーザーは必須です");
        
        // 勤怠締め日の範囲チェック
        if (closingDay < 1 || closingDay > 31) {
            throw new IllegalArgumentException("勤怠締め日は1日から31日までの範囲で指定してください");
        }
        
        // 勤怠時間計算単位の範囲チェック
        if (timeCalculationUnitMinutes < 1) {
            throw new IllegalArgumentException("勤怠時間計算単位は1分以上である必要があります");
        }
        if (timeCalculationUnitMinutes > 60) {
            throw new IllegalArgumentException("勤怠時間計算単位は60分以下である必要があります");
        }
    }
}