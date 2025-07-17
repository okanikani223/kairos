package com.github.okanikani.kairos.commons.exceptions;

/**
 * Kairosシステム共通の基底例外クラス
 * 
 * すべてのカスタム例外はこのクラスを継承する必要があります。
 * RuntimeExceptionを継承しているため、呼び出し元でのcatch宣言は不要です。
 */
public abstract class KairosException extends RuntimeException {
    
    private static final long serialVersionUID = 2743581293847561023L;
    
    /**
     * エラーメッセージを指定してKairosExceptionを作成します
     * 
     * @param message エラーメッセージ
     */
    public KairosException(String message) {
        super(message);
    }
    
    /**
     * エラーメッセージと原因例外を指定してKairosExceptionを作成します
     * 
     * @param message エラーメッセージ
     * @param cause 原因例外
     */
    public KairosException(String message, Throwable cause) {
        super(message, cause);
    }
}