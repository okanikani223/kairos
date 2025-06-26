package com.github.okanikani.kairos.rules.others.repositories;

import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.WorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import com.github.okanikani.kairos.rules.others.jpa.entities.WorkRuleJpaEntity;
import com.github.okanikani.kairos.rules.others.jpa.repositories.WorkRuleJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 勤怠ルールリポジトリのJPA実装
 * 
 * 業務要件: ドメインモデルとJPAエンティティ間の変換とデータ永続化を担当
 */
@Repository
@Profile("prod")
public class JpaWorkRuleRepository implements WorkRuleRepository {

    private final WorkRuleJpaRepository workRuleJpaRepository;

    public JpaWorkRuleRepository(WorkRuleJpaRepository workRuleJpaRepository) {
        this.workRuleJpaRepository = workRuleJpaRepository;
    }

    @Override
    public void save(WorkRule workRule) {
        WorkRuleJpaEntity jpaEntity = toJpaEntity(workRule);
        workRuleJpaRepository.save(jpaEntity);
    }

    @Override
    public WorkRule findById(Long id) {
        return workRuleJpaRepository.findById(id)
                .map(this::toDomainModel)
                .orElse(null);
    }

    @Override
    public List<WorkRule> findByUser(User user) {
        return workRuleJpaRepository.findByUserIdOrderByMembershipStartDateDesc(user.userId())
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkRule> findActiveByUserAndDate(User user, LocalDate targetDate) {
        return workRuleJpaRepository.findByUserIdAndEffectiveDate(user.userId(), targetDate)
                .map(this::toDomainModel)
                .map(List::of)
                .orElse(List.of());
    }

    @Override
    public void deleteById(Long id) {
        workRuleJpaRepository.deleteById(id);
    }

    public Optional<WorkRule> findByUserAndEffectiveDate(User user, LocalDate effectiveDate) {
        return workRuleJpaRepository.findByUserIdAndEffectiveDate(user.userId(), effectiveDate)
                .map(this::toDomainModel);
    }

    public List<WorkRule> findOverlappingRules(User user, LocalDate startDate, LocalDate endDate) {
        return workRuleJpaRepository.findOverlappingRules(user.userId(), startDate, endDate)
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    public List<WorkRule> findOverlappingRulesExcludingId(User user, LocalDate startDate, LocalDate endDate, Long excludeId) {
        return workRuleJpaRepository.findOverlappingRulesExcludingId(user.userId(), startDate, endDate, excludeId)
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    public boolean existsById(Long id) {
        return workRuleJpaRepository.existsById(id);
    }

    /**
     * ドメインモデルをJPAエンティティに変換
     */
    private WorkRuleJpaEntity toJpaEntity(WorkRule workRule) {
        WorkRuleJpaEntity jpaEntity = new WorkRuleJpaEntity(
                workRule.workPlaceId(),
                workRule.latitude(),
                workRule.longitude(),
                workRule.user().userId(),
                workRule.standardStartTime(),
                workRule.standardEndTime(),
                workRule.breakStartTime(),
                workRule.breakEndTime(),
                workRule.membershipStartDate(),
                workRule.membershipEndDate()
        );
        
        // IDが存在する場合は設定（更新時）
        if (workRule.id() != null) {
            // JPAエンティティのIDを設定するためにリフレクションを使用
            // または専用のコンストラクタを追加する必要がある
        }
        
        return jpaEntity;
    }

    /**
     * JPAエンティティをドメインモデルに変換
     */
    private WorkRule toDomainModel(WorkRuleJpaEntity jpaEntity) {
        User user = new User(jpaEntity.getUserId());
        
        return new WorkRule(
                jpaEntity.getId(),
                jpaEntity.getWorkPlaceId(),
                jpaEntity.getLatitude(),
                jpaEntity.getLongitude(),
                user,
                jpaEntity.getStandardStartTime(),
                jpaEntity.getStandardEndTime(),
                jpaEntity.getBreakStartTime(),
                jpaEntity.getBreakEndTime(),
                jpaEntity.getMembershipStartDate(),
                jpaEntity.getMembershipEndDate()
        );
    }
}