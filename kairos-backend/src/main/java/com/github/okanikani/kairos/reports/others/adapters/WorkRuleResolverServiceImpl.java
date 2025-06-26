package com.github.okanikani.kairos.reports.others.adapters;

import com.github.okanikani.kairos.commons.service.LocationFilteringService.WorkplaceLocation;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import com.github.okanikani.kairos.reports.domains.roundings.MinuteBasedRoundingSetting;
import com.github.okanikani.kairos.reports.domains.roundings.RoundingSetting;
import com.github.okanikani.kairos.reports.domains.service.WorkRuleResolverService;
import com.github.okanikani.kairos.reportcreationrules.domains.models.entities.ReportCreationRule;
import com.github.okanikani.kairos.reportcreationrules.domains.models.repositories.ReportCreationRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;
import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.DefaultWorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * WorkRuleResolverServiceの実装クラス
 * 
 * Anti-Corruption Layerパターンを適用し、
 * 複数のルールドメインから情報を統合してreportsドメインに提供する
 */
@Service
public class WorkRuleResolverServiceImpl implements WorkRuleResolverService {
    
    private final WorkRuleRepository workRuleRepository;
    private final DefaultWorkRuleRepository defaultWorkRuleRepository;
    private final ReportCreationRuleRepository reportCreationRuleRepository;
    
    public WorkRuleResolverServiceImpl(
        WorkRuleRepository workRuleRepository,
        DefaultWorkRuleRepository defaultWorkRuleRepository,
        ReportCreationRuleRepository reportCreationRuleRepository) {
        
        this.workRuleRepository = Objects.requireNonNull(workRuleRepository, "workRuleRepositoryは必須です");
        this.defaultWorkRuleRepository = Objects.requireNonNull(defaultWorkRuleRepository, "defaultWorkRuleRepositoryは必須です");
        this.reportCreationRuleRepository = Objects.requireNonNull(reportCreationRuleRepository, "reportCreationRuleRepositoryは必須です");
    }
    
    @Override
    public int getClosingDay(User user) {
        Objects.requireNonNull(user, "userは必須です");
        
        // ReportCreationRuleから勤怠締め日を取得
        com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User ruleUser = 
            convertToReportCreationRuleUser(user);
        
        ReportCreationRule rule = reportCreationRuleRepository.findByUser(ruleUser);
        
        if (rule != null) {
            return rule.closingDay();
        }
        
        // デフォルト：1日（月の初日から計算）
        return 1;
    }
    
    @Override
    public RoundingSetting createRoundingSetting(User user) {
        Objects.requireNonNull(user, "userは必須です");
        
        // ReportCreationRuleから時間計算単位を取得
        com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User ruleUser = 
            convertToReportCreationRuleUser(user);
        
        ReportCreationRule rule = reportCreationRuleRepository.findByUser(ruleUser);
        
        if (rule != null) {
            return new MinuteBasedRoundingSetting(rule.timeCalculationUnitMinutes());
        }
        
        // デフォルト：15分単位
        return new MinuteBasedRoundingSetting(15);
    }
    
    @Override
    public WorkRuleInfo resolveWorkRule(User user, LocalDate workDate) {
        Objects.requireNonNull(user, "userは必須です");
        Objects.requireNonNull(workDate, "workDateは必須です");
        
        // 1. 有効なWorkRuleを検索（最優先）
        com.github.okanikani.kairos.rules.domains.models.vos.User ruleUser = 
            convertToRuleUser(user);
        
        List<WorkRule> activeRules = workRuleRepository.findActiveByUserAndDate(ruleUser, workDate);
        if (!activeRules.isEmpty()) {
            WorkRule rule = activeRules.get(0);
            return convertFromWorkRule(rule);
        }
        
        // 2. DefaultWorkRuleをフォールバック
        List<DefaultWorkRule> defaultRules = defaultWorkRuleRepository.findByUser(ruleUser);
        if (!defaultRules.isEmpty()) {
            DefaultWorkRule rule = defaultRules.get(0);
            return convertFromDefaultWorkRule(rule);
        }
        
        // 3. システムデフォルト
        return WorkRuleInfo.createDefault();
    }
    
    @Override
    public Optional<WorkplaceLocation> resolveWorkplaceLocation(User user, LocalDate workDate) {
        Objects.requireNonNull(user, "userは必須です");
        Objects.requireNonNull(workDate, "workDateは必須です");
        
        // 1. 有効なWorkRuleを検索（最優先）
        com.github.okanikani.kairos.rules.domains.models.vos.User ruleUser = 
            convertToRuleUser(user);
        
        List<WorkRule> activeRules = workRuleRepository.findActiveByUserAndDate(ruleUser, workDate);
        if (!activeRules.isEmpty()) {
            WorkRule rule = activeRules.get(0);
            return Optional.of(new WorkplaceLocation(
                rule.latitude(),
                rule.longitude(),
                100.0  // デフォルトの許容半径100メートル
            ));
        }
        
        // 2. DefaultWorkRuleをフォールバック
        List<DefaultWorkRule> defaultRules = defaultWorkRuleRepository.findByUser(ruleUser);
        if (!defaultRules.isEmpty()) {
            DefaultWorkRule rule = defaultRules.get(0);
            return Optional.of(new WorkplaceLocation(
                rule.latitude(),
                rule.longitude(),
                100.0  // デフォルトの許容半径100メートル
            ));
        }
        
        // 3. 作業場所が設定されていない場合
        return Optional.empty();
    }
    
    /**
     * WorkRuleからWorkRuleInfoに変換
     */
    private WorkRuleInfo convertFromWorkRule(WorkRule workRule) {
        Duration breakTime = calculateBreakTime(workRule.breakStartTime(), workRule.breakEndTime());
        Duration standardWorkTime = Duration.between(workRule.standardStartTime(), workRule.standardEndTime())
            .minus(breakTime);
        
        return new WorkRuleInfo(
            standardWorkTime,
            workRule.standardStartTime(),
            workRule.standardEndTime(),
            breakTime,
            true
        );
    }
    
    /**
     * DefaultWorkRuleからWorkRuleInfoに変換
     */
    private WorkRuleInfo convertFromDefaultWorkRule(DefaultWorkRule defaultWorkRule) {
        Duration breakTime = calculateBreakTime(defaultWorkRule.breakStartTime(), defaultWorkRule.breakEndTime());
        Duration standardWorkTime = Duration.between(defaultWorkRule.standardStartTime(), defaultWorkRule.standardEndTime())
            .minus(breakTime);
        
        return new WorkRuleInfo(
            standardWorkTime,
            defaultWorkRule.standardStartTime(),
            defaultWorkRule.standardEndTime(),
            breakTime,
            true
        );
    }
    
    /**
     * 休憩時間を計算
     */
    private Duration calculateBreakTime(LocalTime breakStartTime, LocalTime breakEndTime) {
        if (breakStartTime == null || breakEndTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(breakStartTime, breakEndTime);
    }
    
    /**
     * reports.domains.models.vos.User を rules.domains.models.vos.User に変換
     */
    private com.github.okanikani.kairos.rules.domains.models.vos.User convertToRuleUser(User user) {
        return new com.github.okanikani.kairos.rules.domains.models.vos.User(user.userId());
    }
    
    /**
     * reports.domains.models.vos.User を reportcreationrules.domains.models.vos.User に変換
     */
    private com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User convertToReportCreationRuleUser(User user) {
        return new com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User(user.userId());
    }
}