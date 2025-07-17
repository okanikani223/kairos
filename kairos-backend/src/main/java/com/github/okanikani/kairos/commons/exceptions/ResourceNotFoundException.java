package com.github.okanikani.kairos.commons.exceptions;

/**
 * リソースが見つからない場合の例外クラス
 * 
 * 以下のケースで使用されます：
 * - 勤怠表が見つからない
 * - 勤務ルールが見つからない
 * - 位置情報が見つからない
 * - その他のエンティティが見つからない
 * 
 * HTTPステータス: 404 Not Found
 */
public class ResourceNotFoundException extends KairosException {
    
    private static final long serialVersionUID = 639174285360471829L;
    
    /**
     * エラーメッセージを指定してResourceNotFoundExceptionを作成します
     * 
     * @param message エラーメッセージ
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    /**
     * エラーメッセージと原因例外を指定してResourceNotFoundExceptionを作成します
     * 
     * @param message エラーメッセージ
     * @param cause 原因例外
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}