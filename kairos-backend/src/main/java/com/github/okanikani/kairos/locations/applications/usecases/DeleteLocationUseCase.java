package com.github.okanikani.kairos.locations.applications.usecases;

import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 位置情報削除ユースケース
 */
@Service
public class DeleteLocationUseCase {

    private final LocationRepository locationRepository;

    public DeleteLocationUseCase(LocationRepository locationRepository) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepositoryは必須です");
    }

    /**
     * 指定したIDの位置情報を削除する
     * セキュリティチェック: 位置情報の所有者と要求者が一致する場合のみ削除可能
     * 
     * @param locationId 削除する位置情報ID
     * @param userId 要求ユーザーID
     * @throws NullPointerException locationIdまたはuserIdがnullの場合
     * @throws IllegalArgumentException 位置情報が存在しない場合、または権限がない場合
     */
    public void execute(Long locationId, String userId) {
        Objects.requireNonNull(locationId, "locationIdは必須です");
        Objects.requireNonNull(userId, "userIdは必須です");
        
        Location location = locationRepository.findById(locationId);
        if (location == null) {
            throw new IllegalArgumentException("指定された位置情報が存在しません");
        }
        
        // セキュリティチェック: 位置情報の所有者と要求者の一致確認
        // 理由: 他のユーザーの位置情報を誤って削除することを防止するため
        if (!location.user().userId().equals(userId)) {
            throw new IllegalArgumentException("この位置情報を削除する権限がありません");
        }
        
        locationRepository.deleteById(locationId);
    }
}