package com.github.okanikani.kairos.locations.applications.usecases;

import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ユーザーの位置情報一覧取得ユースケース
 */
@Service
public class FindAllLocationsUsecase {

    private final LocationRepository locationRepository;

    public FindAllLocationsUsecase(LocationRepository locationRepository) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepositoryは必須です");
    }

    /**
     * 指定したユーザーの位置情報一覧を取得する
     * 
     * @param userId ユーザーID
     * @return 位置情報レスポンスのリスト
     * @throws NullPointerException userIdがnullの場合
     */
    public List<LocationResponse> execute(String userId) {
        Objects.requireNonNull(userId, "userIdは必須です");
        
        User user = new User(userId);
        List<Location> locations = locationRepository.findByUser(user);
        
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