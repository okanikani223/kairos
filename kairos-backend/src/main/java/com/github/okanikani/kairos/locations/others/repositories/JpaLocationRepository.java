package com.github.okanikani.kairos.locations.others.repositories;

import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import com.github.okanikani.kairos.locations.others.jpa.entities.LocationJpaEntity;
import com.github.okanikani.kairos.locations.others.jpa.repositories.LocationJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 位置情報リポジトリのJPA実装
 * 
 * 業務要件: ドメインモデルとJPAエンティティ間の変換とデータ永続化を担当
 */
@Repository
@Profile("prod")
public class JpaLocationRepository implements LocationRepository {

    private final LocationJpaRepository locationJpaRepository;

    public JpaLocationRepository(LocationJpaRepository locationJpaRepository) {
        this.locationJpaRepository = locationJpaRepository;
    }

    @Override
    public Location save(Location location) {
        LocationJpaEntity jpaEntity = toJpaEntity(location);
        LocationJpaEntity savedEntity = locationJpaRepository.save(jpaEntity);
        return toDomainModel(savedEntity);
    }

    @Override
    public List<Location> findByUser(User user) {
        return locationJpaRepository.findByUserIdOrderByRecordedAtDesc(user.userId())
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Location> findByDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return locationJpaRepository.findByRecordedAtBetween(startDateTime, endDateTime)
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Location> findByUserAndDateTimeRange(User user, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return locationJpaRepository.findByUserIdAndRecordedAtBetween(user.userId(), startDateTime, endDateTime)
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Location> findByDate(LocalDateTime date) {
        // 指定日の0時0分0秒から23時59分59秒までの範囲で検索
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);
        return findByDateTimeRange(startOfDay, endOfDay);
    }

    @Override
    public List<Location> findAll() {
        return locationJpaRepository.findAll()
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Location findById(Long id) {
        return locationJpaRepository.findById(id)
                .map(this::toDomainModel)
                .orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        locationJpaRepository.deleteById(id);
    }

    public List<Location> findByUserAndPeriod(User user, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return findByUserAndDateTimeRange(user, startDateTime, endDateTime);
    }

    public List<LocalDateTime> getLocationRecordTimes(YearMonth yearMonth, User user) {
        // 年月の期間を計算
        LocalDateTime startDateTime = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        
        return locationJpaRepository.findRecordedAtByUserIdAndPeriod(
                user.userId(), startDateTime, endDateTime);
    }

    @Override
    public Page<Location> findByUserAndDateTimeRange(User user, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable) {
        Page<LocationJpaEntity> jpaEntityPage = locationJpaRepository.findByUserIdAndRecordedAtBetween(
            user.userId(), 
            startDateTime, 
            endDateTime, 
            pageable
        );
        
        return jpaEntityPage.map(this::toDomainModel);
    }

    /**
     * ドメインモデルをJPAエンティティに変換
     */
    private LocationJpaEntity toJpaEntity(Location location) {
        return new LocationJpaEntity(
                location.latitude(),
                location.longitude(),
                location.recordedAt(),
                location.user().userId()
        );
    }

    /**
     * JPAエンティティをドメインモデルに変換
     */
    private Location toDomainModel(LocationJpaEntity jpaEntity) {
        User user = new User(jpaEntity.getUserId());
        
        return new Location(
                jpaEntity.getId(),
                jpaEntity.getLatitude(),
                jpaEntity.getLongitude(),
                jpaEntity.getRecordedAt(),
                user
        );
    }
}