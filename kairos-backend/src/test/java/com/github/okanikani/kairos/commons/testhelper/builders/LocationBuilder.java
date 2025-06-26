package com.github.okanikani.kairos.commons.testhelper.builders;

import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.vos.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Locationエンティティのテストデータビルダー
 * テストコードの可読性と保守性向上のためのBuilder パターン実装
 */
public class LocationBuilder {
    
    private Long id = null;
    private double latitude = 35.6895; // 東京都庁の緯度（デフォルト）
    private double longitude = 139.6917; // 東京都庁の経度（デフォルト）
    private LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
    private User user = new User("testuser");
    
    /**
     * LocationBuilderの新しいインスタンスを作成
     * 
     * @return LocationBuilder
     */
    public static LocationBuilder create() {
        return new LocationBuilder();
    }
    
    /**
     * デフォルト値で設定されたLocationBuilderを作成
     * 
     * @return LocationBuilder
     */
    public static LocationBuilder defaultLocation() {
        return new LocationBuilder();
    }
    
    /**
     * IDを設定
     * 
     * @param id ID
     * @return LocationBuilder
     */
    public LocationBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    /**
     * 緯度を設定
     * 
     * @param latitude 緯度
     * @return LocationBuilder
     */
    public LocationBuilder withLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }
    
    /**
     * 経度を設定
     * 
     * @param longitude 経度
     * @return LocationBuilder
     */
    public LocationBuilder withLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }
    
    /**
     * 座標を設定
     * 
     * @param latitude 緯度
     * @param longitude 経度
     * @return LocationBuilder
     */
    public LocationBuilder withCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }
    
    /**
     * 記録日時を設定
     * 
     * @param recordedAt 記録日時
     * @return LocationBuilder
     */
    public LocationBuilder withRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
        return this;
    }
    
    /**
     * 記録日時を設定（年月日時分指定）
     * 
     * @param year 年
     * @param month 月
     * @param day 日
     * @param hour 時
     * @param minute 分
     * @return LocationBuilder
     */
    public LocationBuilder withRecordedAt(int year, int month, int day, int hour, int minute) {
        this.recordedAt = LocalDateTime.of(year, month, day, hour, minute);
        return this;
    }
    
    /**
     * ユーザーを設定
     * 
     * @param user ユーザー
     * @return LocationBuilder
     */
    public LocationBuilder withUser(User user) {
        this.user = user;
        return this;
    }
    
    /**
     * ユーザーを設定（ユーザーID指定）
     * 
     * @param userId ユーザーID
     * @return LocationBuilder
     */
    public LocationBuilder withUser(String userId) {
        this.user = new User(userId);
        return this;
    }
    
    /**
     * 新規作成用（IDなし）のLocationを作成
     * 
     * @return LocationBuilder
     */
    public LocationBuilder asNew() {
        this.id = null;
        return this;
    }
    
    /**
     * 保存済み（IDあり）のLocationを作成
     * 
     * @param id ID
     * @return LocationBuilder
     */
    public LocationBuilder asSaved(Long id) {
        this.id = id;
        return this;
    }
    
    /**
     * 東京都庁の座標を設定
     * 
     * @return LocationBuilder
     */
    public LocationBuilder atTokyoMetropolitanGovernment() {
        return withCoordinates(35.6895, 139.6917);
    }
    
    /**
     * 東京駅の座標を設定
     * 
     * @return LocationBuilder
     */
    public LocationBuilder atTokyoStation() {
        return withCoordinates(35.6812, 139.7671);
    }
    
    /**
     * 新宿駅の座標を設定
     * 
     * @return LocationBuilder
     */
    public LocationBuilder atShinjukuStation() {
        return withCoordinates(35.6896, 139.7006);
    }
    
    /**
     * 無効な緯度を設定（テスト用）
     * 
     * @return LocationBuilder
     */
    public LocationBuilder withInvalidLatitude() {
        this.latitude = 91.0; // 緯度の範囲外
        return this;
    }
    
    /**
     * 無効な経度を設定（テスト用）
     * 
     * @return LocationBuilder
     */
    public LocationBuilder withInvalidLongitude() {
        this.longitude = 181.0; // 経度の範囲外
        return this;
    }
    
    /**
     * 特定のユーザーのLocationを作成
     * 
     * @param userId ユーザーID
     * @return LocationBuilder
     */
    public LocationBuilder forUser(String userId) {
        return withUser(userId);
    }
    
    /**
     * 特定の日時のLocationを作成
     * 
     * @param recordedAt 記録日時
     * @return LocationBuilder
     */
    public LocationBuilder at(LocalDateTime recordedAt) {
        return withRecordedAt(recordedAt);
    }
    
    /**
     * Locationエンティティを構築
     * 
     * @return Location
     */
    public Location build() {
        return new Location(id, latitude, longitude, recordedAt, user);
    }
    
    /**
     * 複数のLocationエンティティを構築
     * 
     * @param count 作成数
     * @return List<Location>
     */
    public List<Location> buildList(int count) {
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Long currentId = (id != null) ? id + i : null;
            locations.add(withId(currentId).build());
        }
        return locations;
    }
    
    /**
     * 時系列のLocationエンティティを構築
     * 
     * @param count 作成数
     * @param intervalMinutes 間隔（分）
     * @return List<Location>
     */
    public List<Location> buildTimeSeriesList(int count, int intervalMinutes) {
        List<Location> locations = new ArrayList<>();
        LocalDateTime currentTime = recordedAt;
        
        for (int i = 0; i < count; i++) {
            Long currentId = (id != null) ? id + i : null;
            locations.add(withId(currentId).withRecordedAt(currentTime).build());
            currentTime = currentTime.plusMinutes(intervalMinutes);
        }
        return locations;
    }
}