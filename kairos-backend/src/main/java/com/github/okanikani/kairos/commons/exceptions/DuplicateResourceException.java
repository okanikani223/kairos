package com.github.okanikani.kairos.commons.exceptions;

/**
 * 重複リソース例外
 * 
 * 既に存在するリソースを重複して登録しようとした場合に発生します。
 * HTTPステータス 409 Conflict として処理されます。
 * 
 * 使用例:
 * - 同一ユーザー・同一年月の勤怠表重複登録
 * - 同一ユーザー・同一職場の勤務ルール重複登録
 * - 同一ユーザーの勤怠作成ルール重複登録
 */
public class DuplicateResourceException extends KairosException {
    
    /**
     * メッセージを指定してDuplicateResourceExceptionを生成します。
     * 
     * @param message エラーメッセージ
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因を指定してDuplicateResourceExceptionを生成します。
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}