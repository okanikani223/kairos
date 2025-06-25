package com.github.okanikani.kairos.reportcreationrules.others.controllers;

import com.github.okanikani.kairos.reportcreationrules.applications.usecases.FindAllReportCreationRulesUseCase;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.RegisterReportCreationRuleUseCase;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.RegisterReportCreationRuleRequest;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.ReportCreationRuleResponse;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 勤怠作成ルール管理のREST APIコントローラー
 */
@RestController
@RequestMapping("/api/report-creation-rules")
public class ReportCreationRuleController {
    
    private final RegisterReportCreationRuleUseCase registerReportCreationRuleUseCase;
    private final FindAllReportCreationRulesUseCase findAllReportCreationRulesUseCase;
    
    /**
     * コンストラクタ
     * @param registerReportCreationRuleUseCase 勤怠作成ルール登録ユースケース
     * @param findAllReportCreationRulesUseCase レポート作成ルール取得ユースケース
     */
    public ReportCreationRuleController(RegisterReportCreationRuleUseCase registerReportCreationRuleUseCase, FindAllReportCreationRulesUseCase findAllReportCreationRulesUseCase) {
        this.registerReportCreationRuleUseCase = java.util.Objects.requireNonNull(registerReportCreationRuleUseCase, "registerReportCreationRuleUseCaseは必須です");
        this.findAllReportCreationRulesUseCase = java.util.Objects.requireNonNull(findAllReportCreationRulesUseCase, "findAllReportCreationRulesUseCaseは必須です");
    }
    
    /**
     * 勤怠作成ルールを登録する
     * @param requestBody リクエストボディ（ユーザー情報を除く）
     * @param authentication 認証情報（JWT トークンからユーザーIDを取得）
     * @return 登録された勤怠作成ルール情報
     */
    @PostMapping
    public ResponseEntity<ReportCreationRuleResponse> registerReportCreationRule(
            @RequestBody RegisterReportCreationRuleRequestBody requestBody,
            Authentication authentication) {
        
        // JWTからユーザーIDを取得
        String userId = authentication.getName();
        UserDto userDto = new UserDto(userId);
        
        // リクエストを構築
        RegisterReportCreationRuleRequest request = new RegisterReportCreationRuleRequest(
            userDto,
            requestBody.closingDay(),
            requestBody.timeCalculationUnitMinutes()
        );
        
        // ユースケースを実行
        ReportCreationRuleResponse response = registerReportCreationRuleUseCase.execute(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * レポート作成ルール取得
     */
    @GetMapping
    public ResponseEntity<ReportCreationRuleResponse> findReportCreationRule(Authentication authentication) {
        String userId = authentication.getName();
        ReportCreationRuleResponse response = findAllReportCreationRulesUseCase.execute(userId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 勤怠作成ルール登録用のリクエストボディ
     * ユーザー情報はJWTから取得するため、リクエストボディには含めない
     */
    public record RegisterReportCreationRuleRequestBody(
            int closingDay,                       // 勤怠締め日（月の日：1-31）
            int timeCalculationUnitMinutes        // 勤怠時間計算単位（分：1-60）
    ) {}
}