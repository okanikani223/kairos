package com.github.okanikani.kairos.rules.others.controllers;

import com.github.okanikani.kairos.rules.applications.usecases.RegisterDefaultWorkRuleUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterDefaultWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * デフォルト勤怠ルール管理のREST APIコントローラー
 */
@RestController
@RequestMapping("/api/default-work-rules")
public class DefaultWorkRuleController {
    
    private final RegisterDefaultWorkRuleUsecase registerDefaultWorkRuleUsecase;
    
    /**
     * コンストラクタ
     * @param registerDefaultWorkRuleUsecase デフォルト勤怠ルール登録ユースケース
     */
    public DefaultWorkRuleController(RegisterDefaultWorkRuleUsecase registerDefaultWorkRuleUsecase) {
        this.registerDefaultWorkRuleUsecase = registerDefaultWorkRuleUsecase;
    }
    
    /**
     * デフォルト勤怠ルールを登録する
     * @param requestBody リクエストボディ（ユーザー情報を除く）
     * @param authentication 認証情報（JWT トークンからユーザーIDを取得）
     * @return 登録されたデフォルト勤怠ルール情報
     */
    @PostMapping
    public ResponseEntity<DefaultWorkRuleResponse> registerDefaultWorkRule(
            @RequestBody RegisterDefaultWorkRuleRequestBody requestBody,
            Authentication authentication) {
        
        // JWTからユーザーIDを取得
        String userId = authentication.getName();
        UserDto userDto = new UserDto(userId);
        
        // リクエストを構築
        RegisterDefaultWorkRuleRequest request = new RegisterDefaultWorkRuleRequest(
            requestBody.workPlaceId(),
            requestBody.latitude(),
            requestBody.longitude(),
            userDto,
            requestBody.standardStartTime(),
            requestBody.standardEndTime(),
            requestBody.breakStartTime(),
            requestBody.breakEndTime()
        );
        
        // ユースケースを実行
        DefaultWorkRuleResponse response = registerDefaultWorkRuleUsecase.execute(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * デフォルト勤怠ルール登録用のリクエストボディ
     * ユーザー情報はJWTから取得するため、リクエストボディには含めない
     */
    public record RegisterDefaultWorkRuleRequestBody(
            Long workPlaceId,
            double latitude,
            double longitude,
            java.time.LocalTime standardStartTime,
            java.time.LocalTime standardEndTime,
            java.time.LocalTime breakStartTime,
            java.time.LocalTime breakEndTime
    ) {}
}