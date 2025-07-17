package com.github.okanikani.kairos.commons.controllers;

import com.github.okanikani.kairos.commons.dto.ErrorResponse;
import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.BusinessRuleViolationException;
import com.github.okanikani.kairos.commons.exceptions.DuplicateResourceException;
import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.commons.monitoring.ErrorMetricsService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

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
    
    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    
    private final ErrorMetricsService errorMetricsService;
    
    public GlobalExceptionHandler() {
        this.errorMetricsService = null; // テスト環境では無効化
    }
    
    public GlobalExceptionHandler(ErrorMetricsService errorMetricsService) {
        this.errorMetricsService = errorMetricsService;
    }
    
    /**
     * リソース不存在例外のハンドリング
     * 
     * @param ex ResourceNotFoundException
     * @return 404 Not Found レスポンス
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        String errorCode = "RESOURCE_NOT_FOUND";
    if (logger.isWarnEnabled()) {
            logger.warn("リソースが見つかりません: {} [errorCode={}]", ex.getMessage(), errorCode);
        }
        
        // エラーメトリクス記録（本番環境のみ）
        if (errorMetricsService != null) {
            errorMetricsService.recordError(errorCode, request.getRequestURI(), HttpStatus.NOT_FOUND.value());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(errorCode, ex.getMessage());
        
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
        if (logger.isWarnEnabled()) {
            logger.warn("権限エラーが発生しました: {}", ex.getMessage());
        }
        
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
        if (logger.isWarnEnabled()) {
            logger.warn("バリデーションエラーが発生しました: {}", ex.getMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            VALIDATION_ERROR,
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
        if (logger.isWarnEnabled()) {
            logger.warn("必須パラメータエラーが発生しました: {}", ex.getMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            VALIDATION_ERROR,
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
        if (logger.isWarnEnabled()) {
            logger.warn("リソース重複エラーが発生しました: {}", ex.getMessage());
        }
        
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
        if (logger.isWarnEnabled()) {
            logger.warn("業務ルール違反エラーが発生しました: {}", ex.getMessage());
        }
        
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
        if (logger.isWarnEnabled()) {
            logger.warn("バリデーションエラーが発生しました: {}", ex.getMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            VALIDATION_ERROR,
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
        if (logger.isWarnEnabled()) {
            logger.warn("日時パースエラーが発生しました: {}", ex.getMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            VALIDATION_ERROR,
            "日時形式が正しくありません。ISO-8601形式（YYYY-MM-DDTHH:mm:ss）で入力してください。"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 不正な状態例外のハンドリング
     * 
     * システム内部状態の不整合で発生します。
     * 
     * @param ex IllegalStateException
     * @return 500 Internal Server Error レスポンス
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        if (logger.isErrorEnabled()) {
            logger.error("システム内部状態エラーが発生しました: {}", ex.getMessage(), ex);
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "ILLEGAL_STATE_ERROR",
            "システム内部でエラーが発生しました。管理者にお問い合わせください。"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * JWT関連例外のハンドリング
     * 
     * JWTトークンの不正、期限切れ、署名無効などで発生します。
     * 
     * @param ex JwtException
     * @return 401 Unauthorized レスポンス
     */
    @ExceptionHandler({
        JwtException.class,
        MalformedJwtException.class,
        ExpiredJwtException.class,
        SignatureException.class
    })
    public ResponseEntity<ErrorResponse> handleJwtException(Exception ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("JWT認証エラーが発生しました: {}", ex.getMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "JWT_AUTHENTICATION_ERROR",
            "認証に失敗しました。再度ログインしてください。"
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Spring MVCバリデーション例外のハンドリング
     * 
     * @Valid アノテーションによるバリデーション失敗で発生します。
     * 
     * @param ex MethodArgumentNotValidException
     * @return 400 Bad Request レスポンス
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("リクエストバリデーションエラーが発生しました: {}", ex.getMessage());
        }
        
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "REQUEST_VALIDATION_ERROR",
            "リクエストパラメータが不正です: " + errorMessage
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * HTTPメッセージ読み取り例外のハンドリング
     * 
     * JSONパース失敗などで発生します。
     * 
     * @param ex HttpMessageNotReadableException
     * @return 400 Bad Request レスポンス
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("リクエストボディパースエラーが発生しました: {}", ex.getMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "REQUEST_PARSE_ERROR",
            "リクエストボディの形式が正しくありません。JSON形式で送信してください。"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * HTTPメソッド不正例外のハンドリング
     * 
     * @param ex HttpRequestMethodNotSupportedException
     * @return 405 Method Not Allowed レスポンス
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("サポートされていないHTTPメソッドが指定されました: {}", ex.getMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "METHOD_NOT_ALLOWED",
            "指定されたHTTPメソッドはサポートされていません。"
        );
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }
    
    /**
     * 必須パラメータ不足例外のハンドリング
     * 
     * @param ex MissingServletRequestParameterException
     * @return 400 Bad Request レスポンス
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("必須パラメータが不足しています: {}", ex.getMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "MISSING_PARAMETER",
            "必須パラメータが不足しています: " + ex.getParameterName()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * データ整合性違反例外のハンドリング
     * 
     * データベース制約違反で発生します。
     * 
     * @param ex DataIntegrityViolationException
     * @return 409 Conflict レスポンス
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("データ整合性違反が発生しました: {}", ex.getMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "DATA_INTEGRITY_VIOLATION",
            "データの整合性制約に違反しています。データを確認してください。"
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Spring Security アクセス拒否例外のハンドリング
     * 
     * @param ex AccessDeniedException
     * @return 403 Forbidden レスポンス
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("アクセス拒否が発生しました: {}", ex.getMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "ACCESS_DENIED",
            "このリソースにアクセスする権限がありません。"
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
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
        if (logger.isErrorEnabled()) {
            logger.error("予期しないエラーが発生しました", ex);
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "INTERNAL_ERROR",
            "システムエラーが発生しました。しばらく時間をおいて再度お試しください。"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}