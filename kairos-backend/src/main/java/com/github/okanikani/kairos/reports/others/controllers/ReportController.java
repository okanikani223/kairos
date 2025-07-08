package com.github.okanikani.kairos.reports.others.controllers;

import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.reports.applications.usecases.DeleteReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.FindReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.GenerateReportFromLocationUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.RegisterReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.UpdateReportUseCase;
import com.github.okanikani.kairos.reports.applications.usecases.dto.DeleteReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.FindReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.GenerateReportFromLocationRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.RegisterReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UpdateReportRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.ReportResponse;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.Objects;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    private final RegisterReportUseCase registerReportUseCase;
    private final FindReportUseCase findReportUseCase;
    private final UpdateReportUseCase updateReportUseCase;
    private final DeleteReportUseCase deleteReportUseCase;
    private final GenerateReportFromLocationUseCase generateReportFromLocationUseCase;
    
    public ReportController(RegisterReportUseCase registerReportUseCase, FindReportUseCase findReportUseCase, UpdateReportUseCase updateReportUseCase, DeleteReportUseCase deleteReportUseCase, GenerateReportFromLocationUseCase generateReportFromLocationUseCase) {
        this.registerReportUseCase = Objects.requireNonNull(registerReportUseCase, "registerReportUseCaseは必須です");
        this.findReportUseCase = Objects.requireNonNull(findReportUseCase, "findReportUseCaseは必須です");
        this.updateReportUseCase = Objects.requireNonNull(updateReportUseCase, "updateReportUseCaseは必須です");
        this.deleteReportUseCase = Objects.requireNonNull(deleteReportUseCase, "deleteReportUseCaseは必須です");
        this.generateReportFromLocationUseCase = Objects.requireNonNull(generateReportFromLocationUseCase, "generateReportFromLocationUseCaseは必須です");
    }
    
    @PostMapping
    public ResponseEntity<ReportResponse> registerReport(
            @RequestBody RegisterReportRequest request,
            Authentication authentication) {
        // セキュリティチェック: JWT認証ユーザーとリクエストユーザーIDの一致確認
        // 理由: 他のユーザーの勤怠表を誤って操作することを防ぐため
        String authenticatedUserId = authentication.getName();
        if (!authenticatedUserId.equals(request.user().userId())) {
            throw new AuthorizationException("認証されたユーザーとリクエストのユーザーが一致しません");
        }
        
        ReportResponse response = registerReportUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{year}/{month}")
    public ResponseEntity<ReportResponse> findReport(
            @PathVariable(name = "year") int year,
            @PathVariable(name = "month") int month,
            Authentication authentication) {
        String userId = authentication.getName();
        
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(year, month);
        } catch (DateTimeException e) {
            throw new ValidationException("無効な年月が指定されました: " + year + "/" + month, e);
        }
        
        UserDto userDto = new UserDto(userId);
        FindReportRequest request = new FindReportRequest(yearMonth, userDto);
        
        ReportResponse response = findReportUseCase.execute(request);
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{year}/{month}")
    public ResponseEntity<ReportResponse> updateReport(
            @PathVariable(name = "year") int year,
            @PathVariable(name = "month") int month,
            @RequestBody UpdateReportRequest request,
            Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        
        // REST API設計原則: パスパラメータとボディの年月一致確認
        // 理由: URLとボディの不整合によるデータ破損を防止するため
        YearMonth pathYearMonth;
        try {
            pathYearMonth = YearMonth.of(year, month);
        } catch (DateTimeException e) {
            throw new ValidationException("無効な年月が指定されました: " + year + "/" + month, e);
        }
        
        if (!pathYearMonth.equals(request.yearMonth())) {
            throw new ValidationException("パスパラメータとリクエストボディの年月が一致しません");
        }
        
        // セキュリティチェック: JWT認証ユーザーとリクエストユーザーIDの一致確認
        if (!authenticatedUserId.equals(request.user().userId())) {
            throw new AuthorizationException("認証されたユーザーとリクエストのユーザーが一致しません");
        }
        
        ReportResponse response = updateReportUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{year}/{month}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable(name = "year") int year,
            @PathVariable(name = "month") int month,
            Authentication authentication) {
        // セキュリティ制約: 認証されたユーザー自身の勤怠表のみ削除可能
        // 理由: 他のユーザーの勤怠データを誤って削除することを防ぐため
        String authenticatedUserId = authentication.getName();
        
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(year, month);
        } catch (DateTimeException e) {
            throw new ValidationException("無効な年月が指定されました: " + year + "/" + month, e);
        }
        
        UserDto userDto = new UserDto(authenticatedUserId);
        DeleteReportRequest request = new DeleteReportRequest(yearMonth, userDto);
        
        deleteReportUseCase.execute(request);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/generate")
    public ResponseEntity<ReportResponse> generateReportFromLocation(
            @RequestBody GenerateReportFromLocationRequest request,
            Authentication authentication) {
        // セキュリティチェック: JWT認証ユーザーとリクエストユーザーIDの一致確認
        // 理由: 他のユーザーの位置情報から勤怠表を誤って生成することを防ぐため
        String authenticatedUserId = authentication.getName();
        if (!authenticatedUserId.equals(request.user().userId())) {
            throw new AuthorizationException("認証されたユーザーとリクエストのユーザーが一致しません");
        }
        
        ReportResponse response = generateReportFromLocationUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}