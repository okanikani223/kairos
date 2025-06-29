package com.github.okanikani.kairos.locations.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.AuthorizationException;
import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.applications.usecases.dto.UpdateLocationRequest;
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 位置情報更新ユースケース
 * 既存の位置情報の緯度、経度、記録日時を更新する
 */
@Service
public class UpdateLocationUseCase {
    
    private final LocationRepository locationRepository;
    
    public UpdateLocationUseCase(LocationRepository locationRepository) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepositoryは必須です");
    }
    
    /**
     * 位置情報を更新する
     * @param id 更新対象の位置情報ID
     * @param request 更新内容
     * @param userId 更新を実行するユーザーID
     * @return 更新された位置情報
     * @throws ValidationException 位置情報が存在しない、または他ユーザーの位置情報の場合
     */
    public LocationResponse execute(Long id, UpdateLocationRequest request, String userId) {
        Objects.requireNonNull(id, "IDは必須です");
        Objects.requireNonNull(request, "更新リクエストは必須です");
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        
        // 既存の位置情報を取得
        Location existingLocation = locationRepository.findById(id);
        if (existingLocation == null) {
            throw new ResourceNotFoundException("指定された位置情報が見つかりません");
        }
        
        // 所有者チェック
        if (!existingLocation.user().userId().equals(userId)) {
            throw new AuthorizationException("他のユーザーの位置情報は更新できません");
        }
        
        // 更新する位置情報を作成（IDとユーザーは変更不可）
        Location updatedLocation = new Location(
            existingLocation.id(),
            request.latitude(),
            request.longitude(),
            request.recordedAt(),
            existingLocation.user()
        );
        
        // 保存
        Location savedLocation = locationRepository.save(updatedLocation);
        
        // レスポンスに変換
        return new LocationResponse(
            savedLocation.id(),
            savedLocation.latitude(),
            savedLocation.longitude(),
            savedLocation.recordedAt()
        );
    }
}