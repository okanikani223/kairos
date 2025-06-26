package com.github.okanikani.kairos.reportcreationrules.others.repositories;

import com.github.okanikani.kairos.reportcreationrules.domains.models.entities.ReportCreationRule;
import com.github.okanikani.kairos.reportcreationrules.domains.models.repositories.ReportCreationRuleRepository;
import com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User;
import com.github.okanikani.kairos.reportcreationrules.others.jpa.entities.ReportCreationRuleJpaEntity;
import com.github.okanikani.kairos.reportcreationrules.others.jpa.repositories.ReportCreationRuleJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 勤怠作成ルールリポジトリのJPA実装
 * 
 * 業務要件: ドメインモデルとJPAエンティティ間の変換とデータ永続化を担当
 */
@Repository
@Primary
public class JpaReportCreationRuleRepository implements ReportCreationRuleRepository {

    private final ReportCreationRuleJpaRepository reportCreationRuleJpaRepository;

    public JpaReportCreationRuleRepository(ReportCreationRuleJpaRepository reportCreationRuleJpaRepository) {
        this.reportCreationRuleJpaRepository = reportCreationRuleJpaRepository;
    }

    @Override
    public void save(ReportCreationRule reportCreationRule) {
        ReportCreationRuleJpaEntity jpaEntity = toJpaEntity(reportCreationRule);
        reportCreationRuleJpaRepository.save(jpaEntity);
    }

    @Override
    public ReportCreationRule findById(Long id) {
        return reportCreationRuleJpaRepository.findById(id)
                .map(this::toDomainModel)
                .orElse(null);
    }

    @Override
    public ReportCreationRule findByUser(User user) {
        return reportCreationRuleJpaRepository.findByUserId(user.userId())
                .map(this::toDomainModel)
                .orElse(null);
    }

    public boolean existsByUser(User user) {
        return reportCreationRuleJpaRepository.existsByUserId(user.userId());
    }

    public boolean existsByUserExcludingId(User user, Long excludeId) {
        return reportCreationRuleJpaRepository.existsByUserIdExcludingId(user.userId(), excludeId);
    }

    @Override
    public void deleteById(Long id) {
        reportCreationRuleJpaRepository.deleteById(id);
    }

    @Override
    public List<ReportCreationRule> findAll() {
        return reportCreationRuleJpaRepository.findAll()
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    public boolean existsById(Long id) {
        return reportCreationRuleJpaRepository.existsById(id);
    }

    /**
     * ドメインモデルをJPAエンティティに変換
     */
    private ReportCreationRuleJpaEntity toJpaEntity(ReportCreationRule reportCreationRule) {
        return new ReportCreationRuleJpaEntity(
                reportCreationRule.user().userId(),
                reportCreationRule.closingDay(),
                reportCreationRule.timeCalculationUnitMinutes()
        );
    }

    /**
     * JPAエンティティをドメインモデルに変換
     */
    private ReportCreationRule toDomainModel(ReportCreationRuleJpaEntity jpaEntity) {
        User user = new User(jpaEntity.getUserId());
        
        return new ReportCreationRule(
                jpaEntity.getId(),
                user,
                jpaEntity.getClosingDay(),
                jpaEntity.getTimeCalculationUnitMinutes()
        );
    }
}