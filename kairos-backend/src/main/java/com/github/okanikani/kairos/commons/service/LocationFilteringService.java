package com.github.okanikani.kairos.commons.service;

import com.github.okanikani.kairos.commons.utils.DistanceCalculator;
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 位置情報フィルタリングドメインサービス
 * 
 * 作業場所からの距離に基づいて位置情報をフィルタリングする
 * 勤怠表生成時に正確な勤務時間を計算するために使用
 */
@Service
public class LocationFilteringService {
    
    /**
     * 作業場所からの距離に基づいて位置情報をフィルタリング
     * 
     * 指定された許容距離以内にある位置情報のみを返却する
     * 距離計算にはHaversine公式を使用
     * 
     * @param locations フィルタリング対象の位置情報リスト
     * @param workplace 作業場所の位置情報
     * @param toleranceMeters 許容距離（メートル単位）
     * @return フィルタリング後の位置情報リスト
     * @throws IllegalArgumentException パラメータが無効な場合
     */
    public List<Location> filterByWorkplaceDistance(
            List<Location> locations, 
            WorkplaceLocation workplace, 
            double toleranceMeters) {
        
        Objects.requireNonNull(locations, "位置情報リストは必須です");
        Objects.requireNonNull(workplace, "作業場所情報は必須です");
        
        if (toleranceMeters < 0) {
            throw new IllegalArgumentException(
                "許容距離は0以上の値を指定してください: " + toleranceMeters);
        }
        
        return locations.stream()
            .filter(location -> isWithinWorkplace(location, workplace, toleranceMeters))
            .toList();
    }
    
    /**
     * 位置情報が作業場所の許容範囲内にあるかを判定
     * 
     * @param location 判定対象の位置情報
     * @param workplace 作業場所情報
     * @param toleranceMeters 許容距離（メートル単位）
     * @return 許容範囲内の場合true、範囲外の場合false
     */
    private boolean isWithinWorkplace(Location location, WorkplaceLocation workplace, double toleranceMeters) {
        double distance = DistanceCalculator.calculateDistance(
            location.latitude(), location.longitude(),
            workplace.latitude(), workplace.longitude()
        );
        
        return distance <= toleranceMeters;
    }
    
    /**
     * 作業場所の位置情報を表すレコード
     * 
     * @param latitude 緯度
     * @param longitude 経度
     * @param radiusMeters 作業場所の半径（メートル単位）
     */
    public record WorkplaceLocation(
        double latitude, 
        double longitude, 
        double radiusMeters
    ) {
        
        public WorkplaceLocation {
            // 座標の妥当性検証（Locationエンティティと同じ検証ロジック）
            if (latitude < -90.0 || latitude > 90.0) {
                throw new IllegalArgumentException(
                    String.format("緯度は-90.0～90.0の範囲で指定してください: %f", latitude));
            }
            
            if (longitude < -180.0 || longitude > 180.0) {
                throw new IllegalArgumentException(
                    String.format("経度は-180.0～180.0の範囲で指定してください: %f", longitude));
            }
            
            if (radiusMeters < 0) {
                throw new IllegalArgumentException(
                    String.format("半径は0以上の値を指定してください: %f", radiusMeters));
            }
        }
    }
}