package com.github.okanikani.kairos.locations.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 位置情報ID指定取得ユースケース
 */
@Service
public class FindLocationByIdUseCase {

    private final LocationRepository locationRepository;

    public FindLocationByIdUseCase(LocationRepository locationRepository) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepositoryは必須です");
    }

    /**
     * 指定したIDの位置情報を取得する
     * セキュリティチェック: 位置情報の所有者と要求者が一致する場合のみ取得可能
     * 
     * @param locationId 位置情報ID
     * @param userId 要求ユーザーID
     * @return 位置情報レスポンス
     * @throws NullPointerException locationIdまたはuserIdがnullの場合
     * @throws ResourceNotFoundException 位置情報が存在しない場合
     * @throws AuthorizationException 権限がない場合
     */
    public LocationResponse execute(Long locationId, String userId) {
        Objects.requireNonNull(locationId, "locationIdは必須です");
        Objects.requireNonNull(userId, "userIdは必須です");
        
        Location location = locationRepository.findById(locationId);
        if (location == null) {
            throw new ResourceNotFoundException("指定された位置情報が存在しません");
        }
        
        // セキュリティチェック: 位置情報の所有者と要求者の一致確認
        // 理由: 他のユーザーの位置情報への不正アクセスを防止するため
        if (!location.user().userId().equals(userId)) {
            throw new AuthorizationException("この位置情報にアクセスする権限がありません");
        }
        
        return toLocationResponse(location);
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