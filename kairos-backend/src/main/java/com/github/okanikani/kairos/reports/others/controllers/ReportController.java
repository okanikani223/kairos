package com.github.okanikani.kairos.reports.others.controllers;

import com.github.okanikani.kairos.reports.applications.usecases.FindReportUsecase;
import com.github.okanikani.kairos.reports.applications.usecases.RegisterReportUsecase;
import com.github.okanikani.kairos.reports.applications.usecases.dto.FindReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.RegisterReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.ReportResponse;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.Objects;

/**
 * 勤怠表REST APIコントローラー
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    private final RegisterReportUsecase registerReportUsecase;
    private final FindReportUsecase findReportUsecase;
    
    public ReportController(RegisterReportUsecase registerReportUsecase, FindReportUsecase findReportUsecase) {
        this.registerReportUsecase = Objects.requireNonNull(registerReportUsecase, "registerReportUsecaseは必須です");
        this.findReportUsecase = Objects.requireNonNull(findReportUsecase, "findReportUsecaseは必須です");
    }
    
    /**
     * 勤怠表を登録する
     * @param request 登録リクエスト
     * @param authentication JWT認証情報
     * @return 登録された勤怠表のレスポンス
     */
    @PostMapping
    public ResponseEntity<ReportResponse> registerReport(
            @RequestBody RegisterReportRequest request,
            Authentication authentication) {
        try {
            // JWT認証からユーザーIDを取得して、リクエストのユーザーIDと一致することを確認
            String authenticatedUserId = authentication.getName();
            if (!authenticatedUserId.equals(request.user().userId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            ReportResponse response = registerReportUsecase.execute(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 勤怠表を取得する
     * @param year 年
     * @param month 月
     * @param authentication JWT認証情報
     * @return 勤怠表のレスポンス
     */
    @GetMapping("/{year}/{month}")
    public ResponseEntity<ReportResponse> findReport(
            @PathVariable int year,
            @PathVariable int month,
            Authentication authentication) {
        try {
            // JWT認証からユーザーIDを取得
            String userId = authentication.getName();
            
            YearMonth yearMonth = YearMonth.of(year, month);
            UserDto userDto = new UserDto(userId);
            FindReportRequest request = new FindReportRequest(yearMonth, userDto);
            
            ReportResponse response = findReportUsecase.execute(request);
            
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}