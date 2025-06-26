package com.github.okanikani.kairos.commons.utils;

/**
 * 地理的座標間の距離計算を行うユーティリティクラス
 * 勤怠管理における作業場所と位置情報の距離判定に使用
 */
public class DistanceCalculator {
    
    // 地球の平均半径（キロメートル）
    // WGS84楕円体の平均半径を使用
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    private DistanceCalculator() {
        // ユーティリティクラスなのでインスタンス化を防ぐ
    }
    
    /**
     * Haversine公式による2地点間の距離計算（メートル単位）
     * 
     * 地球を球体として近似した計算を行う。
     * 勤怠管理用途（数十〜数百メートル程度の範囲）では十分な精度を提供する。
     * 
     * @param lat1 地点1の緯度（度）
     * @param lon1 地点1の経度（度）
     * @param lat2 地点2の緯度（度）
     * @param lon2 地点2の経度（度）
     * @return 2地点間の距離（メートル）
     * @throws IllegalArgumentException 緯度・経度が有効範囲外の場合
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        validateCoordinates(lat1, lon1);
        validateCoordinates(lat2, lon2);
        
        // 緯度・経度をラジアンに変換
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // 緯度・経度の差分を計算
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;
        
        // Haversine公式を適用
        // sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        // c = 2 ⋅ atan2( √a, √(1−a) )
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // 距離 = R ⋅ c（キロメートル単位）
        double distanceKm = EARTH_RADIUS_KM * c;
        
        // メートル単位に変換して返却
        return distanceKm * 1000.0;
    }
    
    /**
     * 座標値の妥当性を検証
     * 
     * @param latitude 緯度（-90.0 〜 90.0）
     * @param longitude 経度（-180.0 〜 180.0）
     * @throws IllegalArgumentException 座標値が範囲外の場合
     */
    private static void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException(
                String.format("緯度は-90.0から90.0の範囲で指定してください。指定値: %f", latitude));
        }
        
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException(
                String.format("経度は-180.0から180.0の範囲で指定してください。指定値: %f", longitude));
        }
    }
}