package com.github.okanikani.kairos.commons.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * エラーメトリクス追跡サービス
 * 
 * アプリケーション内で発生するエラーの統計情報を収集・追跡します。
 * 本番環境でのエラー監視とデバッグ支援を目的とします。
 */
@Service
public class ErrorMetricsService {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorMetricsService.class);
    
    // エラーコード別カウンター
    private final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    
    // エラーパス別カウンター
    private final ConcurrentHashMap<String, AtomicLong> errorPathCounts = new ConcurrentHashMap<>();
    
    /**
     * エラー発生を記録
     * 
     * @param errorCode エラーコード
     * @param requestPath リクエストパス
     * @param httpStatus HTTPステータスコード
     */
    public void recordError(String errorCode, String requestPath, int httpStatus) {
        // エラーコード別カウント
        errorCounts.computeIfAbsent(errorCode, k -> new AtomicLong(0)).incrementAndGet();
        
        // エラーパス別カウント
        String pathKey = String.format("%s:%d", requestPath, httpStatus);
        errorPathCounts.computeIfAbsent(pathKey, k -> new AtomicLong(0)).incrementAndGet();
        
        // 重要度が高いエラーの場合は追加ログ出力
        if (httpStatus >= 500) {
            logger.error("重要エラー発生 [errorCode={}, requestPath={}, httpStatus={}]", 
                errorCode, requestPath, httpStatus);
        }
        
        // メトリクス定期出力（100回ごと）
        long totalErrors = errorCounts.values().stream().mapToLong(AtomicLong::get).sum();
        if (totalErrors % 100 == 0) {
            logMetricsSummary();
        }
    }
    
    /**
     * エラーメトリクス概要をログ出力
     */
    public void logMetricsSummary() {
        logger.info("エラーメトリクス概要:");
        logger.info("- エラーコード別: {}", errorCounts);
        logger.info("- パス別エラー: {}", errorPathCounts);
    }
    
    /**
     * 特定のエラーコードの発生回数を取得
     * 
     * @param errorCode エラーコード
     * @return 発生回数
     */
    public long getErrorCount(String errorCode) {
        return errorCounts.getOrDefault(errorCode, new AtomicLong(0)).get();
    }
    
    /**
     * エラーメトリクスをリセット（主にテスト用）
     */
    public void resetMetrics() {
        errorCounts.clear();
        errorPathCounts.clear();
        logger.info("エラーメトリクスをリセットしました");
    }
}