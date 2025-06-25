package com.github.okanikani.kairos.commons.dto;

import java.time.LocalDateTime;

/**
 * エラーレスポンス統一DTO
 * 
 * API エラー時に返却される統一形式のレスポンスボディです。
 * クライアントがエラー内容を適切に処理できるよう、エラーコードとメッセージを含みます。
 */
public record ErrorResponse(
    /**
     * エラーコード
     * 
     * エラーの種類を識別するための固定文字列です。
     * クライアント側でのエラー処理分岐に使用されます。
     * 
     * 例: "RESOURCE_NOT_FOUND", "AUTHORIZATION_ERROR", "VALIDATION_ERROR"
     */
    String errorCode,
    
    /**
     * エラーメッセージ
     * 
     * ユーザーに表示する具体的なエラー内容です。
     * 業務文脈を含んだ日本語メッセージが設定されます。
     * 
     * 例: "指定された勤怠表が存在しません", "この操作を実行する権限がありません"
     */
    String message,
    
    /**
     * エラー発生日時
     * 
     * エラーが発生した日時のタイムスタンプです。
     * ログ調査やデバッグ時の時系列特定に使用されます。
     */
    LocalDateTime timestamp
) {
    
    /**
     * エラーコードとメッセージを指定してErrorResponseを作成します
     * タイムスタンプは自動的に現在時刻が設定されます
     * 
     * @param errorCode エラーコード
     * @param message エラーメッセージ
     * @return ErrorResponse インスタンス
     */
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message, LocalDateTime.now());
    }
}