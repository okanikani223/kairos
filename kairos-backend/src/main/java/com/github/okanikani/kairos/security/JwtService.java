package com.github.okanikani.kairos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    
    // JWTシークレットキー: 本番環境では必ず環境変数で設定すること
    // デフォルト値は開発・テスト用のみ、セキュリティ上必ず32文字以上で設定
    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String secretKey;
    
    // JWTトークン有効期限: 86400000ms = 24時間
    // 業務要件では1日以内のセッションで再ログインが必要
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    /**
     * JWTトークンからユーザー名を取得
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * JWTトークンから特定のクレームを取得
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * ユーザー名からJWTトークンを生成
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }
    
    /**
     * JWTトークンが有効かチェック
     */
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }
    
    /**
     * JWTトークンが期限切れかチェック
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * JWTトークンから有効期限を取得
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * JWTトークンから全てのクレームを取得
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 署名キーを取得
     */
    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}