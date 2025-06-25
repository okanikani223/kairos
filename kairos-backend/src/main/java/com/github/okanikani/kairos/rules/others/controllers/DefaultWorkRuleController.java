package com.github.okanikani.kairos.rules.others.controllers;

import com.github.okanikani.kairos.rules.applications.usecases.FindAllDefaultWorkRulesUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.RegisterDefaultWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterDefaultWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * デフォルト勤怠ルール管理のREST APIコントローラー
 */
@RestController
@RequestMapping("/api/default-work-rules")
public class DefaultWorkRuleController {
    
    private final RegisterDefaultWorkRuleUseCase registerDefaultWorkRuleUseCase;
    private final FindAllDefaultWorkRulesUseCase findAllDefaultWorkRulesUseCase;
    
    /**
     * コンストラクタ
     * @param registerDefaultWorkRuleUseCase デフォルト勤怠ルール登録ユースケース
     * @param findAllDefaultWorkRulesUseCase 全デフォルト勤務ルール取得ユースケース
     */
    public DefaultWorkRuleController(RegisterDefaultWorkRuleUseCase registerDefaultWorkRuleUseCase, FindAllDefaultWorkRulesUseCase findAllDefaultWorkRulesUseCase) {
        this.registerDefaultWorkRuleUseCase = java.util.Objects.requireNonNull(registerDefaultWorkRuleUseCase, "registerDefaultWorkRuleUseCaseは必須です");
        this.findAllDefaultWorkRulesUseCase = java.util.Objects.requireNonNull(findAllDefaultWorkRulesUseCase, "findAllDefaultWorkRulesUseCaseは必須です");
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
        DefaultWorkRuleResponse response = registerDefaultWorkRuleUseCase.execute(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * デフォルト勤務ルール一覧取得
     */
    @GetMapping
    public ResponseEntity<List<DefaultWorkRuleResponse>> findAllDefaultWorkRules(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<DefaultWorkRuleResponse> response = findAllDefaultWorkRulesUseCase.execute(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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