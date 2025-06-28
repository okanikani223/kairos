package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.DuplicateResourceException;
import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.mapper.WorkRuleMapper;
import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import com.github.okanikani.kairos.rules.domains.service.WorkRuleDomainService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class RegisterWorkRuleUseCase {
    
    private final WorkRuleRepository workRuleRepository;
    private final WorkRuleDomainService workRuleDomainService;
    
    public RegisterWorkRuleUseCase(WorkRuleRepository workRuleRepository, WorkRuleDomainService workRuleDomainService) {
        this.workRuleRepository = Objects.requireNonNull(workRuleRepository, "workRuleRepositoryは必須です");
        this.workRuleDomainService = Objects.requireNonNull(workRuleDomainService, "workRuleDomainServiceは必須です");
    }
    
    public WorkRuleResponse execute(RegisterWorkRuleRequest request) {
        Objects.requireNonNull(request, "requestは必須です");
        
        User user = new User(request.user().userId());
        
        // 業務ルール: 同一ユーザーの所属期間重複チェック
        List<WorkRule> existingRules = workRuleRepository.findByUser(user);
        if (workRuleDomainService.hasOverlappingPeriod(existingRules, request.membershipStartDate(), request.membershipEndDate())) {
            throw new DuplicateResourceException("指定された所属期間は既存の勤怠ルールと重複しています");
        }
        
        // DTOからエンティティへ変換
        WorkRule workRule = WorkRuleMapper.toWorkRule(request);
        
        // 保存
        WorkRule savedWorkRule = workRuleRepository.save(workRule);
        
        // レスポンス作成
        return WorkRuleMapper.toWorkRuleResponse(savedWorkRule);
    }
}