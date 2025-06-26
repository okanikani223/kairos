package com.github.okanikani.kairos.commons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DistanceCalculatorのテストクラス
 * Haversine公式による距離計算の正確性と例外処理をテスト
 */
class DistanceCalculatorTest {

    @Test
    void calculateDistance_同一地点の場合_距離は0() {
        // 東京駅の座標
        double latitude = 35.6812;
        double longitude = 139.7671;
        
        double distance = DistanceCalculator.calculateDistance(
            latitude, longitude, latitude, longitude);
        
        assertEquals(0.0, distance, 0.1);
    }
    
    @Test
    void calculateDistance_東京駅から新宿駅_約6キロメートル() {
        // 東京駅: 35.6812, 139.7671
        // 新宿駅: 35.6896, 139.7006
        double distance = DistanceCalculator.calculateDistance(
            35.6812, 139.7671,  // 東京駅
            35.6896, 139.7006   // 新宿駅
        );
        
        // 実際の距離は約6kmなので、許容誤差内であることを確認
        assertTrue(distance >= 5500.0 && distance <= 6500.0,
            "東京駅-新宿駅間の距離が期待範囲外: " + distance + "m");
    }
    
    @Test
    void calculateDistance_東京から大阪_約400キロメートル() {
        // 東京駅: 35.6812, 139.7671
        // 大阪駅: 34.7024, 135.4959
        double distance = DistanceCalculator.calculateDistance(
            35.6812, 139.7671,  // 東京駅
            34.7024, 135.4959   // 大阪駅
        );
        
        // 実際の距離は約400kmなので、許容誤差内であることを確認
        assertTrue(distance >= 390000.0 && distance <= 410000.0,
            "東京-大阪間の距離が期待範囲外: " + distance + "m");
    }
    
    @Test
    void calculateDistance_近距離_100メートル程度の精度確認() {
        // 非常に近い2地点（約100m程度の距離）
        double lat1 = 35.6812;
        double lon1 = 139.7671;
        double lat2 = 35.6822;  // 約0.001度北（約111m）
        double lon2 = 139.7671;
        
        double distance = DistanceCalculator.calculateDistance(lat1, lon1, lat2, lon2);
        
        // 約100-120m程度の距離になることを確認
        assertTrue(distance >= 90.0 && distance <= 130.0,
            "近距離計算の精度が期待範囲外: " + distance + "m");
    }
    
    @Test
    void calculateDistance_緯度が範囲外_例外が発生() {
        assertThrows(IllegalArgumentException.class, () -> {
            DistanceCalculator.calculateDistance(
                -91.0, 139.7671,  // 緯度が-90.0より小さい
                35.6812, 139.7671
            );
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            DistanceCalculator.calculateDistance(
                35.6812, 139.7671,
                91.0, 139.7671     // 緯度が90.0より大きい
            );
        });
    }
    
    @Test
    void calculateDistance_経度が範囲外_例外が発生() {
        assertThrows(IllegalArgumentException.class, () -> {
            DistanceCalculator.calculateDistance(
                35.6812, -181.0,   // 経度が-180.0より小さい
                35.6812, 139.7671
            );
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            DistanceCalculator.calculateDistance(
                35.6812, 139.7671,
                35.6812, 181.0     // 経度が180.0より大きい
            );
        });
    }
    
    @Test
    void calculateDistance_境界値_正常処理() {
        // 緯度・経度の境界値での正常処理確認
        assertDoesNotThrow(() -> {
            DistanceCalculator.calculateDistance(-90.0, -180.0, 90.0, 180.0);
        });
        
        assertDoesNotThrow(() -> {
            DistanceCalculator.calculateDistance(0.0, 0.0, 0.0, 0.0);
        });
    }
    
    @Test
    void calculateDistance_南北半球間_正確な計算() {
        // 北半球と南半球の地点間の距離計算
        double distance = DistanceCalculator.calculateDistance(
            45.0, 0.0,   // 北半球
            -45.0, 0.0   // 南半球
        );
        
        // 北緯45度から南緯45度は90度の差なので、約10,000km程度
        assertTrue(distance >= 9000000.0 && distance <= 11000000.0,
            "南北半球間の距離が期待範囲外: " + distance + "m");
    }
    
    @Test
    void calculateDistance_東西の経度差_正確な計算() {
        // 赤道上での東西の距離計算
        double distance = DistanceCalculator.calculateDistance(
            0.0, -90.0,  // 西経90度
            0.0, 90.0    // 東経90度
        );
        
        // 経度180度差は地球円周の半分、約20,000km
        assertTrue(distance >= 18000000.0 && distance <= 22000000.0,
            "東西経度差の距離が期待範囲外: " + distance + "m");
    }
}