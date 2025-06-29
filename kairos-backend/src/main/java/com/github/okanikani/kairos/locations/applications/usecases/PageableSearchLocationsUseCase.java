package com.github.okanikani.kairos.locations.applications.usecases;

import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.applications.usecases.dto.PageableSearchLocationsRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.PagedLocationResponse;
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * ページネーション対応位置情報期間検索ユースケース
 */
@Service
public class PageableSearchLocationsUseCase {

    private final LocationRepository locationRepository;

    public PageableSearchLocationsUseCase(LocationRepository locationRepository) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepositoryは必須です");
    }

    /**
     * 指定した期間のユーザーの位置情報をページネーション付きで検索する
     * 
     * @param request 検索リクエスト（開始・終了日時、ページング情報）
     * @param userId ユーザーID
     * @return ページネーション付き位置情報レスポンス
     * @throws NullPointerException requestまたはuserIdがnullの場合
     * @throws IllegalArgumentException 検索条件が不正な場合
     */
    public PagedLocationResponse execute(PageableSearchLocationsRequest request, String userId) {
        Objects.requireNonNull(request, "requestは必須です");
        Objects.requireNonNull(userId, "userIdは必須です");
        
        User user = new User(userId);
        
        // ページング情報を作成（記録日時の昇順でソート）
        Pageable pageable = PageRequest.of(
            request.page(),
            request.size(),
            Sort.by(Sort.Direction.ASC, "recordedAt")
        );
        
        // ページネーション付きで位置情報を取得
        Page<Location> locationPage = locationRepository.findByUserAndDateTimeRange(
            user,
            request.startDateTime(),
            request.endDateTime(),
            pageable
        );
        
        // LocationResponseに変換
        Page<LocationResponse> locationResponsePage = locationPage.map(this::toLocationResponse);
        
        // PagedLocationResponseに変換して返却
        return PagedLocationResponse.from(locationResponsePage);
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