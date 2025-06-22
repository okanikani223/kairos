package com.github.okanikani.kairos.reports.others.controllers;

import com.github.okanikani.kairos.reports.applications.usecases.FindReportUsecase;
import com.github.okanikani.kairos.reports.applications.usecases.RegisterReportUsecase;
import com.github.okanikani.kairos.reports.applications.usecases.UpdateReportUsecase;
import com.github.okanikani.kairos.reports.applications.usecases.dto.FindReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.RegisterReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UpdateReportRequest;
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
    private final UpdateReportUsecase updateReportUsecase;
    
    public ReportController(RegisterReportUsecase registerReportUsecase, FindReportUsecase findReportUsecase, UpdateReportUsecase updateReportUsecase) {
        this.registerReportUsecase = Objects.requireNonNull(registerReportUsecase, "registerReportUsecaseは必須です");
        this.findReportUsecase = Objects.requireNonNull(findReportUsecase, "findReportUsecaseは必須です");
        this.updateReportUsecase = Objects.requireNonNull(updateReportUsecase, "updateReportUsecaseは必須です");
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
            @PathVariable(name = "year") int year,
            @PathVariable(name = "month") int month,
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
    
    @PutMapping("/{year}/{month}")
    public ResponseEntity<ReportResponse> updateReport(
            @PathVariable(name = "year") int year,
            @PathVariable(name = "month") int month,
            @RequestBody UpdateReportRequest request,
            Authentication authentication) {
        try {
            // JWT認証からユーザーIDを取得
            String authenticatedUserId = authentication.getName();
            
            // パスパラメータの年月とリクエストボディの年月が一致することを確認
            YearMonth pathYearMonth = YearMonth.of(year, month);
            if (!pathYearMonth.equals(request.yearMonth())) {
                return ResponseEntity.badRequest().build();
            }
            
            // 認証ユーザーとリクエストのユーザーIDが一致することを確認
            if (!authenticatedUserId.equals(request.user().userId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            ReportResponse response = updateReportUsecase.execute(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}