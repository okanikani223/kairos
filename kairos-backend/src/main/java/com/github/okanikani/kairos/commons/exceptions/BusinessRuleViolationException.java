package com.github.okanikani.kairos.commons.exceptions;

/**
 * 業務ルール違反例外
 * 
 * アプリケーションの業務ルールに違反した操作を行おうとした場合に発生します。
 * HTTPステータス 422 Unprocessable Entity として処理されます。
 * 
 * 使用例:
 * - WorkRuleの期間重複（同一ユーザーで重複する期間の勤務ルール登録）
 * - 開始時刻が終了時刻より後の時間設定
 * - 有効期限切れのデータに対する操作
 * - 業務フローに反する状態遷移
 */
public class BusinessRuleViolationException extends KairosException {
    
    private static final long serialVersionUID = 4571829304865120374L;
    
    /**
     * メッセージを指定してBusinessRuleViolationExceptionを生成します。
     * 
     * @param message エラーメッセージ
     */
    public BusinessRuleViolationException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因を指定してBusinessRuleViolationExceptionを生成します。
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}