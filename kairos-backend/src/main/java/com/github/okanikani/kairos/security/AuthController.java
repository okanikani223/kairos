package com.github.okanikani.kairos.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認証用コントローラー
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final JwtService jwtService;
    
    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    /**
     * ログイン（簡易実装）
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 実際の実装では、ユーザー認証を行う
        // ここでは簡易的に任意のユーザーIDでトークンを生成
        String token = jwtService.generateToken(request.userId());
        return ResponseEntity.ok(new LoginResponse(token));
    }
    
    /**
     * ログインリクエスト
     */
    public record LoginRequest(String userId, String password) {}
    
    /**
     * ログインレスポンス
     */
    public record LoginResponse(String token) {}
}