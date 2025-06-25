package com.github.okanikani.kairos.locations.applications.usecases;

import com.github.okanikani.kairos.locations.applications.usecases.dto.RegisterLocationRequest;
import com.github.okanikani.kairos.locations.applications.usecases.dto.LocationResponse;
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.repositories.LocationRepository;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RegisterLocationUseCase {
    
    private final LocationRepository locationRepository;
    
    public RegisterLocationUseCase(LocationRepository locationRepository) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepositoryは必須です");
    }
    
    public LocationResponse execute(RegisterLocationRequest request, String userId) {
        Objects.requireNonNull(request, "requestは必須です");
        Objects.requireNonNull(userId, "userIdは必須です");
        
        User user = new User(userId);
        
        // 新規位置情報作成（IDはnullでDB採番される）
        Location location = new Location(
            null,
            request.latitude(),
            request.longitude(),
            request.recordedAt(),
            user
        );
        
        // DB保存（IDが採番されたLocationが返却される）
        Location savedLocation = locationRepository.save(location);
        
        // レスポンス作成
        return new LocationResponse(
            savedLocation.id(),
            savedLocation.latitude(),
            savedLocation.longitude(),
            savedLocation.recordedAt()
        );
    }
}