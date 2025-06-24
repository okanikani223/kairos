package com.github.okanikani.kairos.reports.others.adapters;

import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import com.github.okanikani.kairos.reports.domains.service.LocationService;
import com.github.okanikani.kairos.reports.domains.service.ReportPeriodCalculator;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * 位置情報サービス実装クラス
 * 勤怠表ドメインから位置情報ドメインへのアダプター
 * Anti-Corruption Layerパターンを適用し、位置情報ドメインとの境界を管理
 */
@Service
public class LocationServiceImpl implements LocationService {
    
    private final LocationRepository locationRepository;
    
    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepositoryは必須です");
    }
    
    @Override
    public List<LocalDateTime> getLocationRecordTimes(ReportPeriodCalculator.ReportPeriod period, User user) {
        Objects.requireNonNull(period, "periodは必須です");
        Objects.requireNonNull(user, "userは必須です");
        
        // 位置情報ドメインのユーザー情報に変換（Anti-Corruption Layer）
        com.github.okanikani.kairos.locations.domains.models.vos.User locationUser = 
            convertToLocationUser(user);
        
        // ユーザー・期間指定で位置情報を取得
        List<Location> locations = locationRepository.findByUserAndDateTimeRange(
            locationUser,
            period.startDateTime(), 
            period.endDateTime()
        );
        
        // 記録日時を抽出してソート
        return locations.stream()
            .map(Location::recordedAt)
            .sorted()
            .toList();
    }
    
    /**
     * reports.domains.models.vos.User を locations.domains.models.vos.User に変換
     * Anti-Corruption Layerパターンによる境界分離
     */
    private com.github.okanikani.kairos.locations.domains.models.vos.User convertToLocationUser(User user) {
        return new com.github.okanikani.kairos.locations.domains.models.vos.User(user.userId());
    }
}