package com.github.okanikani.kairos.rules.others.controllers;

import com.github.okanikani.kairos.rules.applications.usecases.FindAllDefaultWorkRulesUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.FindDefaultWorkRuleByIdUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.RegisterDefaultWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.UpdateDefaultWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.dto.DefaultWorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterDefaultWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UpdateDefaultWorkRuleRequest;
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
    private final FindDefaultWorkRuleByIdUseCase findDefaultWorkRuleByIdUseCase;
    private final UpdateDefaultWorkRuleUseCase updateDefaultWorkRuleUseCase;
    
    /**
     * コンストラクタ
     * @param registerDefaultWorkRuleUseCase デフォルト勤怠ルール登録ユースケース
     * @param findAllDefaultWorkRulesUseCase 全デフォルト勤務ルール取得ユースケース
     * @param findDefaultWorkRuleByIdUseCase IDによるデフォルト勤務ルール取得ユースケース
     * @param updateDefaultWorkRuleUseCase デフォルト勤務ルール更新ユースケース
     */
    public DefaultWorkRuleController(
            RegisterDefaultWorkRuleUseCase registerDefaultWorkRuleUseCase,
            FindAllDefaultWorkRulesUseCase findAllDefaultWorkRulesUseCase,
            FindDefaultWorkRuleByIdUseCase findDefaultWorkRuleByIdUseCase,
            UpdateDefaultWorkRuleUseCase updateDefaultWorkRuleUseCase) {
        this.registerDefaultWorkRuleUseCase = java.util.Objects.requireNonNull(registerDefaultWorkRuleUseCase, "registerDefaultWorkRuleUseCaseは必須です");
        this.findAllDefaultWorkRulesUseCase = java.util.Objects.requireNonNull(findAllDefaultWorkRulesUseCase, "findAllDefaultWorkRulesUseCaseは必須です");
        this.findDefaultWorkRuleByIdUseCase = java.util.Objects.requireNonNull(findDefaultWorkRuleByIdUseCase, "findDefaultWorkRuleByIdUseCaseは必須です");
        this.updateDefaultWorkRuleUseCase = java.util.Objects.requireNonNull(updateDefaultWorkRuleUseCase, "updateDefaultWorkRuleUseCaseは必須です");
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
        String userId = authentication.getName();
        List<DefaultWorkRuleResponse> response = findAllDefaultWorkRulesUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 特定のデフォルト勤務ルール取得
     * @param id デフォルト勤務ルールID
     * @param authentication 認証情報
     * @return デフォルト勤務ルール情報
     */
    @GetMapping("/{id}")
    public ResponseEntity<DefaultWorkRuleResponse> findDefaultWorkRuleById(
            @PathVariable(name = "id") Long id,
            Authentication authentication) {
        String userId = authentication.getName();
        DefaultWorkRuleResponse response = findDefaultWorkRuleByIdUseCase.execute(id, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * デフォルト勤務ルール更新
     * @param id デフォルト勤務ルールID
     * @param requestBody リクエストボディ
     * @param authentication 認証情報
     * @return 更新されたデフォルト勤務ルール情報
     */
    @PutMapping("/{id}")
    public ResponseEntity<DefaultWorkRuleResponse> updateDefaultWorkRule(
            @PathVariable(name = "id") Long id,
            @RequestBody UpdateDefaultWorkRuleRequestBody requestBody,
            Authentication authentication) {
        
        String userId = authentication.getName();
        
        // リクエストを構築
        UpdateDefaultWorkRuleRequest request = new UpdateDefaultWorkRuleRequest(
            requestBody.workPlaceId(),
            requestBody.latitude(),
            requestBody.longitude(),
            requestBody.standardStartTime(),
            requestBody.standardEndTime(),
            requestBody.breakStartTime(),
            requestBody.breakEndTime()
        );
        
        // ユースケースを実行
        DefaultWorkRuleResponse response = updateDefaultWorkRuleUseCase.execute(id, request, userId);
        
        return ResponseEntity.ok(response);
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
    
    /**
     * デフォルト勤務ルール更新用のリクエストボディ
     * ユーザー情報はJWTから取得するため、リクエストボディには含めない
     */
    public record UpdateDefaultWorkRuleRequestBody(
            Long workPlaceId,
            double latitude,
            double longitude,
            java.time.LocalTime standardStartTime,
            java.time.LocalTime standardEndTime,
            java.time.LocalTime breakStartTime,
            java.time.LocalTime breakEndTime
    ) {}
}