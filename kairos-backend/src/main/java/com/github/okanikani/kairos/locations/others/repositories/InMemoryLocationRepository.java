package com.github.okanikani.kairos.locations.others.repositories;

import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ※これは開発・テスト用の一時的な実装です。
 * 本番環境ではデータベースを使用した実装に置き換える必要があります。
 * TODO: PostgreSQL等を使用した永続化実装への置き換え
 */
@Repository
@Profile("dev")
public class InMemoryLocationRepository implements LocationRepository {
    
    private final Map<Long, Location> locations = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public Location save(Location location) {
        Long id = location.id();
        if (id == null) {
            // 新規作成の場合は自動生成IDを設定
            id = idGenerator.getAndIncrement();
            location = new Location(
                id,
                location.latitude(),
                location.longitude(),
                location.recordedAt(),
                location.user()
            );
        }
        locations.put(id, location);
        return location;
    }
    
    @Override
    public List<Location> findByDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return locations.values().stream()
            .filter(location -> !location.recordedAt().isBefore(startDateTime) && 
                               !location.recordedAt().isAfter(endDateTime))
            .sorted((l1, l2) -> l1.recordedAt().compareTo(l2.recordedAt()))
            .toList();
    }
    
    @Override
    public List<Location> findByDate(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);
        return findByDateTimeRange(startOfDay, endOfDay);
    }
    
    @Override
    public List<Location> findAll() {
        return locations.values().stream()
            .sorted((l1, l2) -> l1.recordedAt().compareTo(l2.recordedAt()))
            .toList();
    }
    
    @Override
    public Location findById(Long id) {
        return locations.get(id);
    }
    
    @Override
    public void deleteById(Long id) {
        locations.remove(id);
    }
    
    @Override
    public List<Location> findByUserAndDateTimeRange(User user, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return locations.values().stream()
            .filter(location -> location.user().equals(user))
            .filter(location -> !location.recordedAt().isBefore(startDateTime) && 
                               !location.recordedAt().isAfter(endDateTime))
            .sorted((l1, l2) -> l1.recordedAt().compareTo(l2.recordedAt()))
            .toList();
    }

    @Override
    public List<Location> findByUser(User user) {
        return locations.values().stream()
            .filter(location -> location.user().equals(user))
            .sorted((l1, l2) -> l1.recordedAt().compareTo(l2.recordedAt()))
            .toList();
    }

    @Override
    public Page<Location> findByUserAndDateTimeRange(User user, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable) {
        // 全体のデータを取得してフィルタリング・ソート
        List<Location> allFilteredLocations = locations.values().stream()
            .filter(location -> location.user().equals(user))
            .filter(location -> !location.recordedAt().isBefore(startDateTime) && 
                               !location.recordedAt().isAfter(endDateTime))
            .sorted((l1, l2) -> l1.recordedAt().compareTo(l2.recordedAt()))
            .toList();
        
        // ページネーション情報を計算
        int totalElements = allFilteredLocations.size();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int startIndex = pageNumber * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalElements);
        
        // 指定ページの範囲でデータを取得
        List<Location> pageContent = startIndex < totalElements 
            ? allFilteredLocations.subList(startIndex, endIndex)
            : List.of();
        
        return new PageImpl<>(pageContent, pageable, totalElements);
    }
}