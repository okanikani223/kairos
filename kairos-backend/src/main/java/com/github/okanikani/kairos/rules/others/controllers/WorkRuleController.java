package com.github.okanikani.kairos.rules.others.controllers;

import com.github.okanikani.kairos.rules.applications.usecases.RegisterWorkRuleUsecase;
import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/work-rules")
public class WorkRuleController {
    
    private final RegisterWorkRuleUsecase registerWorkRuleUsecase;
    
    public WorkRuleController(RegisterWorkRuleUsecase registerWorkRuleUsecase) {
        this.registerWorkRuleUsecase = registerWorkRuleUsecase;
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
}