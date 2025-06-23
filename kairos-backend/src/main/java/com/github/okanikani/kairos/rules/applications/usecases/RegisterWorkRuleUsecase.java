package com.github.okanikani.kairos.rules.applications.usecases;

import com.github.okanikani.kairos.rules.applications.usecases.dto.RegisterWorkRuleRequest;
import com.github.okanikani.kairos.rules.applications.usecases.dto.WorkRuleResponse;
import com.github.okanikani.kairos.rules.applications.usecases.mapper.WorkRuleMapper;
import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RegisterWorkRuleUsecase {
    
    private final WorkRuleRepository workRuleRepository;
    
    public RegisterWorkRuleUsecase(WorkRuleRepository workRuleRepository) {
        this.workRuleRepository = Objects.requireNonNull(workRuleRepository, "workRuleRepositoryは必須です");
    }
    
    public WorkRuleResponse execute(RegisterWorkRuleRequest request) {
        Objects.requireNonNull(request, "requestは必須です");
        
        // DTOからエンティティへ変換
        WorkRule workRule = WorkRuleMapper.toWorkRule(request);
        
        // 保存
        workRuleRepository.save(workRule);
        
        // レスポンス作成
        return WorkRuleMapper.toWorkRuleResponse(workRule);
    }
}