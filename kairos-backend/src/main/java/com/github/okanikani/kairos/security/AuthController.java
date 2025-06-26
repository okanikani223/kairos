package com.github.okanikani.kairos.security;

import com.github.okanikani.kairos.users.applications.usecases.AuthenticationUseCase;
import com.github.okanikani.kairos.users.applications.usecases.RegisterUserUseCase;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.LoginResponse;
import com.github.okanikani.kairos.users.applications.usecases.dto.RegisterRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認証用コントローラー
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationUseCase authenticationUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    
    public AuthController(AuthenticationUseCase authenticationUseCase, RegisterUserUseCase registerUserUseCase) {
        this.authenticationUseCase = authenticationUseCase;
        this.registerUserUseCase = registerUserUseCase;
    }
    
    /**
     * ログイン認証
     * 
     * @param request ログインリクエスト
     * @return ログインレスポンス（JWTトークン含む）
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationUseCase.authenticate(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * ユーザー登録
     * 
     * @param request 登録リクエスト
     * @return 登録されたユーザー情報
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = registerUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}