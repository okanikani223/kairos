package com.github.okanikani.kairos.reportcreationrules.others.controllers;

import com.github.okanikani.kairos.reportcreationrules.applications.usecases.FindAllReportCreationRulesUsecase;
import com.github.okanikani.kairos.reportcreationrules.applications.usecases.RegisterReportCreationRuleUsecase;
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
    
    private final RegisterReportCreationRuleUsecase registerReportCreationRuleUsecase;
    private final FindAllReportCreationRulesUsecase findAllReportCreationRulesUsecase;
    
    /**
     * コンストラクタ
     * @param registerReportCreationRuleUsecase 勤怠作成ルール登録ユースケース
     * @param findAllReportCreationRulesUsecase レポート作成ルール取得ユースケース
     */
    public ReportCreationRuleController(RegisterReportCreationRuleUsecase registerReportCreationRuleUsecase, FindAllReportCreationRulesUsecase findAllReportCreationRulesUsecase) {
        this.registerReportCreationRuleUsecase = java.util.Objects.requireNonNull(registerReportCreationRuleUsecase, "registerReportCreationRuleUsecaseは必須です");
        this.findAllReportCreationRulesUsecase = java.util.Objects.requireNonNull(findAllReportCreationRulesUsecase, "findAllReportCreationRulesUsecaseは必須です");
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
            requestBody.calculationStartDay(),
            requestBody.timeCalculationUnitMinutes()
        );
        
        // ユースケースを実行
        ReportCreationRuleResponse response = registerReportCreationRuleUsecase.execute(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * レポート作成ルール取得
     */
    @GetMapping
    public ResponseEntity<ReportCreationRuleResponse> findReportCreationRule(Authentication authentication) {
        try {
            String userId = authentication.getName();
            ReportCreationRuleResponse response = findAllReportCreationRulesUsecase.execute(userId);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 勤怠作成ルール登録用のリクエストボディ
     * ユーザー情報はJWTから取得するため、リクエストボディには含めない
     */
    public record RegisterReportCreationRuleRequestBody(
            int calculationStartDay,              // 勤怠計算開始日（月の日：1-31）
            int timeCalculationUnitMinutes        // 勤怠時間計算単位（分：1-60）
    ) {}
}