package com.github.okanikani.kairos.rules.others.controllers;

import com.github.okanikani.kairos.rules.applications.usecases.DeleteWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.FindAllWorkRulesUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.FindWorkRuleByIdUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.RegisterWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.UpdateWorkRuleUseCase;
import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UpdateWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/work-rules")
public class WorkRuleController {
    
    private final RegisterWorkRuleUseCase registerWorkRuleUseCase;
    private final FindAllWorkRulesUseCase findAllWorkRulesUseCase;
    private final FindWorkRuleByIdUseCase findWorkRuleByIdUseCase;
    private final UpdateWorkRuleUseCase updateWorkRuleUseCase;
    private final DeleteWorkRuleUseCase deleteWorkRuleUseCase;
    
    public WorkRuleController(RegisterWorkRuleUseCase registerWorkRuleUseCase, FindAllWorkRulesUseCase findAllWorkRulesUseCase, FindWorkRuleByIdUseCase findWorkRuleByIdUseCase, UpdateWorkRuleUseCase updateWorkRuleUseCase, DeleteWorkRuleUseCase deleteWorkRuleUseCase) {
        this.registerWorkRuleUseCase = java.util.Objects.requireNonNull(registerWorkRuleUseCase, "registerWorkRuleUseCaseは必須です");
        this.findAllWorkRulesUseCase = java.util.Objects.requireNonNull(findAllWorkRulesUseCase, "findAllWorkRulesUseCaseは必須です");
        this.findWorkRuleByIdUseCase = java.util.Objects.requireNonNull(findWorkRuleByIdUseCase, "findWorkRuleByIdUseCaseは必須です");
        this.updateWorkRuleUseCase = java.util.Objects.requireNonNull(updateWorkRuleUseCase, "updateWorkRuleUseCaseは必須です");
        this.deleteWorkRuleUseCase = java.util.Objects.requireNonNull(deleteWorkRuleUseCase, "deleteWorkRuleUseCaseは必須です");
    }
    
    /**
     * 勤怠ルール登録
     */
    @PostMapping
    public ResponseEntity<WorkRuleResponse> registerWorkRule(
            @RequestBody RegisterWorkRuleRequestBody requestBody,
            Authentication authentication) {
        
        String userId = authentication.getName();
        UserDto userDto = new UserDto(userId);
        
        RegisterWorkRuleRequest request = new RegisterWorkRuleRequest(
            requestBody.workPlaceId(),
            requestBody.latitude(),
            requestBody.longitude(),
            userDto,
            requestBody.standardStartTime(),
            requestBody.standardEndTime(),
            requestBody.breakStartTime(),
            requestBody.breakEndTime(),
            requestBody.membershipStartDate(),
            requestBody.membershipEndDate()
        );
        
        WorkRuleResponse response = registerWorkRuleUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 勤務ルール一覧取得
     */
    @GetMapping
    public ResponseEntity<List<WorkRuleResponse>> findAllWorkRules(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<WorkRuleResponse> response = findAllWorkRulesUseCase.execute(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 特定勤務ルール取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkRuleResponse> findWorkRuleById(@PathVariable(name = "id") Long id, Authentication authentication) {
        try {
            String userId = authentication.getName();
            WorkRuleResponse response = findWorkRuleByIdUseCase.execute(id, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // セキュリティエラー判定: 権限なしエラーと存在しないエラーを区別
            // 理由: 適切なHTTPステータスコードを返すため
            if (e.getMessage().contains("権限がありません")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("存在しません")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 勤務ルール更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<WorkRuleResponse> updateWorkRule(
            @PathVariable(name = "id") Long id,
            @RequestBody UpdateWorkRuleRequestBody requestBody,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            UserDto userDto = new UserDto(userId);
            
            UpdateWorkRuleRequest request = new UpdateWorkRuleRequest(
                requestBody.workPlaceId(),
                requestBody.latitude(),
                requestBody.longitude(),
                userDto,
                requestBody.standardStartTime(),
                requestBody.standardEndTime(),
                requestBody.breakStartTime(),
                requestBody.breakEndTime(),
                requestBody.membershipStartDate(),
                requestBody.membershipEndDate()
            );
            
            WorkRuleResponse response = updateWorkRuleUseCase.execute(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // セキュリティエラー判定: 権限なしエラーと存在しないエラーを区別
            // 理由: 適切なHTTPステータスコードを返すため
            if (e.getMessage().contains("権限がありません")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("存在しません")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 勤務ルール削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkRule(@PathVariable(name = "id") Long id, Authentication authentication) {
        try {
            String userId = authentication.getName();
            deleteWorkRuleUseCase.execute(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // セキュリティエラー判定: 権限なしエラーと存在しないエラーを区別
            // 理由: 適切なHTTPステータスコードを返すため
            if (e.getMessage().contains("権限がありません")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("存在しません")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 勤怠ルール登録用リクエストボディ
     */
    public record RegisterWorkRuleRequestBody(
            Long workPlaceId,
            double latitude,
            double longitude,
            LocalTime standardStartTime,
            LocalTime standardEndTime,
            LocalTime breakStartTime,
            LocalTime breakEndTime,
            LocalDate membershipStartDate,
            LocalDate membershipEndDate
    ) {}
    
    /**
     * 勤務ルール更新用リクエストボディ
     */
    public record UpdateWorkRuleRequestBody(
            Long workPlaceId,
            double latitude,
            double longitude,
            LocalTime standardStartTime,
            LocalTime standardEndTime,
            LocalTime breakStartTime,
            LocalTime breakEndTime,
            LocalDate membershipStartDate,
            LocalDate membershipEndDate
    ) {}
}