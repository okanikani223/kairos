package com.github.okanikani.kairos.commons.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

/**
 * ログ設定クラス
 * 
 * 構造化ログ出力のためのMDC設定を行います。
 * 各リクエストにユニークなリクエストIDを付与し、
 * 認証済みユーザーのユーザーIDを追加します。
 */
@Configuration
public class LoggingConfiguration {
    
    /**
     * MDCフィルター
     * 
     * 各HTTPリクエストにリクエストIDとユーザーIDを設定します。
     * ログトレーサビリティとデバッグ効率の向上を目的とします。
     * 
     * @return MDCフィルター
     */
    @Bean
    public Filter mdcFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                
                try {
                    // リクエストIDの生成と設定
                    String requestId = UUID.randomUUID().toString().substring(0, 8);
                    MDC.put("requestId", requestId);
                    
                    // リクエストパスの設定
                    if (request instanceof HttpServletRequest httpRequest) {
                        MDC.put("requestPath", httpRequest.getRequestURI());
                        MDC.put("httpMethod", httpRequest.getMethod());
                    }
                    
                    // フィルターチェーンの実行
                    chain.doFilter(request, response);
                    
                    // 認証後のユーザーID設定（SecurityContextから取得）
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.isAuthenticated() 
                            && !"anonymousUser".equals(authentication.getName())) {
                        MDC.put("userId", authentication.getName());
                    }
                    
                } finally {
                    // MDCクリーンアップ（メモリリーク防止）
                    MDC.clear();
                }
            }
        };
    }
}