package com.github.okanikani.kairos.commons.controllers;

import com.github.okanikani.kairos.commons.dto.ErrorResponse;
import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.BusinessRuleViolationException;
import com.github.okanikani.kairos.commons.exceptions.DuplicateResourceException;
import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * グローバル例外ハンドラー
 * 
 * アプリケーション全体で発生する例外を統一的に処理し、
 * 適切なHTTPステータスコードとエラーレスポンスを返却します。
 * 
 * これにより各Controllerでの個別例外処理が不要になり、
 * エラーレスポンスの形式が統一されます。
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * リソース不存在例外のハンドリング
     * 
     * @param ex ResourceNotFoundException
     * @return 404 Not Found レスポンス
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("リソースが見つかりません: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "RESOURCE_NOT_FOUND",
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * 権限不足例外のハンドリング
     * 
     * @param ex AuthorizationException
     * @return 403 Forbidden レスポンス
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorization(AuthorizationException ex) {
        logger.warn("権限エラーが発生しました: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "AUTHORIZATION_ERROR",
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * バリデーション例外のハンドリング
     * 
     * IllegalArgumentException は主にバリデーションエラーで使用されます。
     * 
     * @param ex IllegalArgumentException
     * @return 400 Bad Request レスポンス
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidation(IllegalArgumentException ex) {
        logger.warn("バリデーションエラーが発生しました: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "VALIDATION_ERROR",
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Null Pointer例外のハンドリング
     * 
     * 必須パラメータの未設定などで発生します。
     * 
     * @param ex NullPointerException
     * @return 400 Bad Request レスポンス
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointer(NullPointerException ex) {
        logger.warn("必須パラメータエラーが発生しました: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "VALIDATION_ERROR",
            ex.getMessage() != null ? ex.getMessage() : "必須パラメータが設定されていません"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 重複リソース例外のハンドリング
     * 
     * 既に存在するリソースを重複して登録しようとした場合の例外です。
     * 
     * @param ex DuplicateResourceException
     * @return 409 Conflict レスポンス
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        logger.warn("リソース重複エラーが発生しました: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "DUPLICATE_RESOURCE",
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * 業務ルール違反例外のハンドリング
     * 
     * アプリケーションの業務ルールに違反した操作を行おうとした場合の例外です。
     * 
     * @param ex BusinessRuleViolationException
     * @return 422 Unprocessable Entity レスポンス
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(BusinessRuleViolationException ex) {
        logger.warn("業務ルール違反エラーが発生しました: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "BUSINESS_RULE_VIOLATION",
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }
    
    /**
     * カスタムバリデーション例外のハンドリング
     * 
     * アプリケーション固有のバリデーションエラーです。
     * 
     * @param ex ValidationException
     * @return 400 Bad Request レスポンス
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        logger.warn("バリデーションエラーが発生しました: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "VALIDATION_ERROR",
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 日時パース例外のハンドリング
     * 
     * 日時形式が不正な場合の例外です。
     * 
     * @param ex DateTimeParseException
     * @return 400 Bad Request レスポンス
     */
    @ExceptionHandler(java.time.format.DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParse(java.time.format.DateTimeParseException ex) {
        logger.warn("日時パースエラーが発生しました: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "VALIDATION_ERROR",
            "日時形式が正しくありません。ISO-8601形式（YYYY-MM-DDTHH:mm:ss）で入力してください。"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * その他の予期しない例外のハンドリング
     * 
     * システムエラーなど、上記以外の例外をキャッチします。
     * セキュリティのため、詳細なエラー情報は返却しません。
     * 
     * @param ex Exception
     * @return 500 Internal Server Error レスポンス
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        logger.error("予期しないエラーが発生しました", ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "INTERNAL_ERROR",
            "システムエラーが発生しました。しばらく時間をおいて再度お試しください。"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}