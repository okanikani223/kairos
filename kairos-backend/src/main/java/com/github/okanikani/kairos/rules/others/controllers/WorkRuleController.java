package com.github.okanikani.kairos.rules.others.controllers;

import com.github.okanikani.kairos.rules.applications.usecases.DeleteWorkRuleUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.FindAllWorkRulesUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.FindWorkRuleByIdUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.RegisterWorkRuleUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.UpdateWorkRuleUsecase;
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
    
    private final RegisterWorkRuleUsecase registerWorkRuleUsecase;
    private final FindAllWorkRulesUsecase findAllWorkRulesUsecase;
    private final FindWorkRuleByIdUsecase findWorkRuleByIdUsecase;
    private final UpdateWorkRuleUsecase updateWorkRuleUsecase;
    private final DeleteWorkRuleUsecase deleteWorkRuleUsecase;
    
    public WorkRuleController(RegisterWorkRuleUsecase registerWorkRuleUsecase, FindAllWorkRulesUsecase findAllWorkRulesUsecase, FindWorkRuleByIdUsecase findWorkRuleByIdUsecase, UpdateWorkRuleUsecase updateWorkRuleUsecase, DeleteWorkRuleUsecase deleteWorkRuleUsecase) {
        this.registerWorkRuleUsecase = java.util.Objects.requireNonNull(registerWorkRuleUsecase, "registerWorkRuleUsecaseは必須です");
        this.findAllWorkRulesUsecase = java.util.Objects.requireNonNull(findAllWorkRulesUsecase, "findAllWorkRulesUsecaseは必須です");
        this.findWorkRuleByIdUsecase = java.util.Objects.requireNonNull(findWorkRuleByIdUsecase, "findWorkRuleByIdUsecaseは必須です");
        this.updateWorkRuleUsecase = java.util.Objects.requireNonNull(updateWorkRuleUsecase, "updateWorkRuleUsecaseは必須です");
        this.deleteWorkRuleUsecase = java.util.Objects.requireNonNull(deleteWorkRuleUsecase, "deleteWorkRuleUsecaseは必須です");
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
        
        WorkRuleResponse response = registerWorkRuleUsecase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 勤務ルール一覧取得
     */
    @GetMapping
    public ResponseEntity<List<WorkRuleResponse>> findAllWorkRules(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<WorkRuleResponse> response = findAllWorkRulesUsecase.execute(userId);
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
            WorkRuleResponse response = findWorkRuleByIdUsecase.execute(id, userId);
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
            
            WorkRuleResponse response = updateWorkRuleUsecase.execute(id, request, userId);
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
            deleteWorkRuleUsecase.execute(id, userId);
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