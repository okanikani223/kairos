package com.github.okanikani.kairos.locations.applications.usecases;

import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.applications.usecases.dto.SearchLocationsRequest;
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 位置情報期間検索ユースケース
 */
@Service
public class SearchLocationsUseCase {

    private final LocationRepository locationRepository;

    public SearchLocationsUseCase(LocationRepository locationRepository) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepositoryは必須です");
    }

    /**
     * 指定した期間のユーザーの位置情報を検索する
     * 
     * @param request 検索リクエスト（開始・終了日時）
     * @param userId ユーザーID
     * @return 検索条件に一致する位置情報レスポンスのリスト
     * @throws NullPointerException requestまたはuserIdがnullの場合
     * @throws IllegalArgumentException 検索条件が不正な場合
     */
    public List<LocationResponse> execute(SearchLocationsRequest request, String userId) {
        Objects.requireNonNull(request, "requestは必須です");
        Objects.requireNonNull(userId, "userIdは必須です");
        
        User user = new User(userId);
        List<Location> locations = locationRepository.findByUserAndDateTimeRange(
            user, 
            request.startDateTime(), 
            request.endDateTime()
        );
        
        return locations.stream()
            .map(this::toLocationResponse)
            .collect(Collectors.toList());
    }

    /**
     * LocationエンティティをLocationResponseに変換する
     * 
     * @param location 位置情報エンティティ
     * @return 位置情報レスポンス
     */
    private LocationResponse toLocationResponse(Location location) {
        return new LocationResponse(
            location.id(),
            location.latitude(),
            location.longitude(),
            location.recordedAt()
        );
    }
}