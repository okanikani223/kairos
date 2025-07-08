package com.github.okanikani.kairos.commons.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 位置情報フィルタリング機能の設定プロパティ
 * 
 * application.ymlの kairos.location.filtering セクションから設定値を読み込む
 * 勤怠表生成時の作業場所範囲フィルタリングの動作を制御する
 * 
 * 設定例:
 * kairos:
 *   location:
 *     filtering:
 *       enabled: true
 *       default-tolerance-meters: 100
 *       strict-mode: false
 */
@ConfigurationProperties(prefix = "kairos.location.filtering")
public record LocationFilteringProperties(
    
    /**
     * 位置情報フィルタリング機能の有効/無効
     * 
     * true: 作業場所からの距離に基づいて位置情報をフィルタリング
     * false: 従来通り全ての位置情報を勤怠表生成対象とする（デフォルト）
     * 
     * 後方互換性のため、デフォルトは false に設定
     */
    boolean enabled,
    
    /**
     * 作業場所からの許容距離（メートル単位）
     * 
     * この距離以内にある位置情報のみを勤務時間として計算する
     * デフォルト値: 100メートル
     * 
     * 一般的な設定例:
     * - オフィス建物内: 50-100メートル
     * - 建設現場など広い作業場所: 200-500メートル
     * - 移動を伴う営業活動: 1000メートル以上
     */
    int defaultToleranceMeters,
    
    /**
     * 厳密モードの設定
     * 
     * true: 作業場所情報が取得できない場合はエラーとして処理
     * false: 作業場所情報が取得できない場合は警告ログを出力し、全位置情報を対象とする（デフォルト）
     * 
     * 段階的導入時は false を推奨
     */
    boolean strictMode
    
) {
    
    private static final int MAX_TOLERANCE_METERS = 10000;
    
    /**
     * デフォルト設定でのインスタンス生成
     * テスト用途や設定が存在しない場合の fallback として使用
     * 
     * @return デフォルト設定のLocationFilteringPropertiesインスタンス
     */
    public static LocationFilteringProperties defaultSettings() {
        return new LocationFilteringProperties(
            false,  // disabled by default for backward compatibility
            100,    // 100 meters default tolerance
            false   // lenient mode by default
        );
    }
    
    /**
     * 有効な設定値かどうかの検証
     * Spring Boot の ConfigurationProperties バリデーションの補完として使用
     * 
     * @throws IllegalArgumentException 設定値が無効な場合
     */
    public void validate() {
        if (defaultToleranceMeters < 0) {
            throw new IllegalArgumentException(
                "defaultToleranceMetersは0以上の値を指定してください。指定値: " + defaultToleranceMeters);
        }
        
        if (defaultToleranceMeters > MAX_TOLERANCE_METERS) {
            throw new IllegalArgumentException(
                "defaultToleranceMetersは" + MAX_TOLERANCE_METERS + "メートル以下の値を指定してください。指定値: " + defaultToleranceMeters);
        }
    }
}