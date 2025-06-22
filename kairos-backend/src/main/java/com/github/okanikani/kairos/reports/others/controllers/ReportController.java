package com.github.okanikani.kairos.reports.others.controllers;

import com.github.okanikani.kairos.reports.applications.usecases.DeleteReportUsecase;
import com.github.okanikani.kairos.reports.applications.usecases.FindReportUsecase;
import com.github.okanikani.kairos.reports.applications.usecases.RegisterReportUsecase;
import com.github.okanikani.kairos.reports.applications.usecases.UpdateReportUsecase;
import com.github.okanikani.kairos.reports.applications.usecases.dto.DeleteReportRequest;
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

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    private final RegisterReportUsecase registerReportUsecase;
    private final FindReportUsecase findReportUsecase;
    private final UpdateReportUsecase updateReportUsecase;
    private final DeleteReportUsecase deleteReportUsecase;
    
    public ReportController(RegisterReportUsecase registerReportUsecase, FindReportUsecase findReportUsecase, UpdateReportUsecase updateReportUsecase, DeleteReportUsecase deleteReportUsecase) {
        this.registerReportUsecase = Objects.requireNonNull(registerReportUsecase, "registerReportUsecaseは必須です");
        this.findReportUsecase = Objects.requireNonNull(findReportUsecase, "findReportUsecaseは必須です");
        this.updateReportUsecase = Objects.requireNonNull(updateReportUsecase, "updateReportUsecaseは必須です");
        this.deleteReportUsecase = Objects.requireNonNull(deleteReportUsecase, "deleteReportUsecaseは必須です");
    }
    
    @PostMapping
    public ResponseEntity<ReportResponse> registerReport(
            @RequestBody RegisterReportRequest request,
            Authentication authentication) {
        try {
            // セキュリティチェック: JWT認証ユーザーとリクエストユーザーIDの一致確認
            // 理由: 他のユーザーの勤怠表を誤って操作することを防ぐため
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
    
    @GetMapping("/{year}/{month}")
    public ResponseEntity<ReportResponse> findReport(
            @PathVariable(name = "year") int year,
            @PathVariable(name = "month") int month,
            Authentication authentication) {
        try {
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
            String authenticatedUserId = authentication.getName();
            
            // REST API設計原則: パスパラメータとボディの年月一致確認
            // 理由: URLとボディの不整合によるデータ破損を防止するため
            YearMonth pathYearMonth = YearMonth.of(year, month);
            if (!pathYearMonth.equals(request.yearMonth())) {
                return ResponseEntity.badRequest().build();
            }
            
            // セキュリティチェック: JWT認証ユーザーとリクエストユーザーIDの一致確認
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
    
    @DeleteMapping("/{year}/{month}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable(name = "year") int year,
            @PathVariable(name = "month") int month,
            Authentication authentication) {
        try {
            // セキュリティ制約: 認証されたユーザー自身の勤怠表のみ削除可能
            // 理由: 他のユーザーの勤怠データを誤って削除することを防ぐため
            String authenticatedUserId = authentication.getName();
            
            YearMonth yearMonth = YearMonth.of(year, month);
            UserDto userDto = new UserDto(authenticatedUserId);
            DeleteReportRequest request = new DeleteReportRequest(yearMonth, userDto);
            
            deleteReportUsecase.execute(request);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}