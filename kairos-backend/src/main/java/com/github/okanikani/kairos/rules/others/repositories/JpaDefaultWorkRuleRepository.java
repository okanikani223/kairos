package com.github.okanikani.kairos.rules.others.repositories;

import com.github.okanikani.kairos.rules.domains.models.entities.DefaultWorkRule;
import com.github.okanikani.kairos.rules.domains.models.repositories.DefaultWorkRuleRepository;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import com.github.okanikani.kairos.rules.others.jpa.entities.DefaultWorkRuleJpaEntity;
import com.github.okanikani.kairos.rules.others.jpa.repositories.DefaultWorkRuleJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * デフォルト勤怠ルールリポジトリのJPA実装
 * 
 * 業務要件: ドメインモデルとJPAエンティティ間の変換とデータ永続化を担当
 */
@Repository
@Profile("prod")
public class JpaDefaultWorkRuleRepository implements DefaultWorkRuleRepository {

    private final DefaultWorkRuleJpaRepository defaultWorkRuleJpaRepository;

    public JpaDefaultWorkRuleRepository(DefaultWorkRuleJpaRepository defaultWorkRuleJpaRepository) {
        this.defaultWorkRuleJpaRepository = defaultWorkRuleJpaRepository;
    }

    @Override
    public DefaultWorkRule save(DefaultWorkRule defaultWorkRule) {
        DefaultWorkRuleJpaEntity jpaEntity = toJpaEntity(defaultWorkRule);
        DefaultWorkRuleJpaEntity savedEntity = defaultWorkRuleJpaRepository.save(jpaEntity);
        return toDomainModel(savedEntity);
    }

    @Override
    public DefaultWorkRule findById(Long id) {
        return defaultWorkRuleJpaRepository.findById(id)
                .map(this::toDomainModel)
                .orElse(null);
    }

    @Override
    public List<DefaultWorkRule> findByUser(User user) {
        return defaultWorkRuleJpaRepository.findByUserId(user.userId())
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<DefaultWorkRule> findByWorkPlaceId(Long workPlaceId) {
        return defaultWorkRuleJpaRepository.findByWorkPlaceId(workPlaceId)
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public DefaultWorkRule findByUserAndWorkPlaceId(User user, Long workPlaceId) {
        return defaultWorkRuleJpaRepository.findByUserIdAndWorkPlaceId(user.userId(), workPlaceId)
                .map(this::toDomainModel)
                .orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        defaultWorkRuleJpaRepository.deleteById(id);
    }

    @Override
    public List<DefaultWorkRule> findAll() {
        return defaultWorkRuleJpaRepository.findAll()
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    public Optional<DefaultWorkRule> findByUserAndWorkPlace(User user, Long workPlaceId) {
        return defaultWorkRuleJpaRepository.findByUserIdAndWorkPlaceId(user.userId(), workPlaceId)
                .map(this::toDomainModel);
    }

    public boolean existsByUserAndWorkPlace(User user, Long workPlaceId) {
        return defaultWorkRuleJpaRepository.existsByUserIdAndWorkPlaceId(user.userId(), workPlaceId);
    }

    public boolean existsByUserAndWorkPlaceExcludingId(User user, Long workPlaceId, Long excludeId) {
        return defaultWorkRuleJpaRepository.existsByUserIdAndWorkPlaceIdExcludingId(user.userId(), workPlaceId, excludeId);
    }

    public boolean existsById(Long id) {
        return defaultWorkRuleJpaRepository.existsById(id);
    }

    /**
     * ドメインモデルをJPAエンティティに変換
     */
    private DefaultWorkRuleJpaEntity toJpaEntity(DefaultWorkRule defaultWorkRule) {
        return new DefaultWorkRuleJpaEntity(
                defaultWorkRule.workPlaceId(),
                defaultWorkRule.latitude(),
                defaultWorkRule.longitude(),
                defaultWorkRule.user().userId(),
                defaultWorkRule.standardStartTime(),
                defaultWorkRule.standardEndTime(),
                defaultWorkRule.breakStartTime(),
                defaultWorkRule.breakEndTime()
        );
    }

    /**
     * JPAエンティティをドメインモデルに変換
     */
    private DefaultWorkRule toDomainModel(DefaultWorkRuleJpaEntity jpaEntity) {
        User user = new User(jpaEntity.getUserId());
        
        return new DefaultWorkRule(
                jpaEntity.getId(),
                jpaEntity.getWorkPlaceId(),
                jpaEntity.getLatitude(),
                jpaEntity.getLongitude(),
                user,
                jpaEntity.getStandardStartTime(),
                jpaEntity.getStandardEndTime(),
                jpaEntity.getBreakStartTime(),
                jpaEntity.getBreakEndTime()
        );
    }
}