package com.github.okanikani.kairos.commons.exceptions;

/**
 * バリデーション例外
 * 
 * 入力データのバリデーションエラーが発生した場合に使用します。
 * HTTPステータス 400 Bad Request として処理されます。
 * 
 * 使用例:
 * - 必須パラメータの未設定
 * - 無効な値の範囲チェック（緯度経度、時刻、日付など）
 * - データ形式エラー
 * - 文字列長の制限チェック
 * - 数値の範囲チェック
 */
public class ValidationException extends KairosException {
    
    /**
     * メッセージを指定してValidationExceptionを生成します。
     * 
     * @param message エラーメッセージ
     */
    public ValidationException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因を指定してValidationExceptionを生成します。
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}