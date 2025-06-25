package com.github.okanikani.kairos.commons.exceptions;

/**
 * 認可（権限）エラーの例外クラス
 * 
 * 以下のケースで使用されます：
 * - 他ユーザーの勤怠表へのアクセス試行
 * - 他ユーザーの勤務ルールへのアクセス試行
 * - JWT認証ユーザーとリクエストユーザーの不一致
 * - その他の権限不足によるアクセス拒否
 * 
 * HTTPステータス: 403 Forbidden
 */
public class AuthorizationException extends KairosException {
    
    /**
     * エラーメッセージを指定してAuthorizationExceptionを作成します
     * 
     * @param message エラーメッセージ
     */
    public AuthorizationException(String message) {
        super(message);
    }
    
    /**
     * エラーメッセージと原因例外を指定してAuthorizationExceptionを作成します
     * 
     * @param message エラーメッセージ
     * @param cause 原因例外
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}