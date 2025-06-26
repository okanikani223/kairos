package com.github.okanikani.kairos.commons.service;

import com.github.okanikani.kairos.commons.service.LocationFilteringService.WorkplaceLocation;
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocationFilteringServiceのテストクラス
 * 位置情報フィルタリング機能のテスト
 */
class LocationFilteringServiceTest {
    
    private LocationFilteringService locationFilteringService;
    private User testUser;
    private LocalDateTime testDateTime;
    
    @BeforeEach
    void setUp() {
        locationFilteringService = new LocationFilteringService();
        testUser = new User("test-user-123");
        testDateTime = LocalDateTime.of(2025, 6, 15, 9, 0);
    }
    
    @Test
    void filterByWorkplaceDistance_空リスト_空リストを返却() {
        WorkplaceLocation workplace = new WorkplaceLocation(35.6812, 139.7671, 100.0);
        
        List<Location> result = locationFilteringService.filterByWorkplaceDistance(
            Collections.emptyList(), workplace, 100.0);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void filterByWorkplaceDistance_範囲内の位置情報_全て含まれる() {
        // 東京駅周辺の位置情報（全て100m以内）
        WorkplaceLocation workplace = new WorkplaceLocation(35.6812, 139.7671, 100.0);
        
        List<Location> locations = Arrays.asList(
            new Location(1L, 35.6812, 139.7671, testDateTime, testUser),      // 作業場所と同一
            new Location(2L, 35.6815, 139.7671, testDateTime.plusMinutes(30), testUser), // 約33m北
            new Location(3L, 35.6812, 139.7675, testDateTime.plusMinutes(60), testUser)  // 約31m東
        );
        
        List<Location> result = locationFilteringService.filterByWorkplaceDistance(
            locations, workplace, 100.0);
        
        assertEquals(3, result.size());
        assertEquals(locations, result);
    }
    
    @Test
    void filterByWorkplaceDistance_範囲外の位置情報_除外される() {
        // 東京駅とその他の遠い場所
        WorkplaceLocation workplace = new WorkplaceLocation(35.6812, 139.7671, 100.0);
        
        List<Location> locations = Arrays.asList(
            new Location(1L, 35.6812, 139.7671, testDateTime, testUser),      // 作業場所と同一（含まれる）
            new Location(2L, 35.6896, 139.7006, testDateTime.plusMinutes(30), testUser), // 新宿駅（除外される）
            new Location(3L, 35.6815, 139.7671, testDateTime.plusMinutes(60), testUser)  // 約33m北（含まれる）
        );
        
        List<Location> result = locationFilteringService.filterByWorkplaceDistance(
            locations, workplace, 100.0);
        
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());  // 東京駅
        assertEquals(3L, result.get(1).id());  // 33m北の地点
    }
    
    @Test
    void filterByWorkplaceDistance_許容距離0_同一地点のみ含まれる() {
        WorkplaceLocation workplace = new WorkplaceLocation(35.6812, 139.7671, 100.0);
        
        List<Location> locations = Arrays.asList(
            new Location(1L, 35.6812, 139.7671, testDateTime, testUser),      // 完全に同一
            new Location(2L, 35.6813, 139.7671, testDateTime.plusMinutes(30), testUser)  // わずかに異なる
        );
        
        List<Location> result = locationFilteringService.filterByWorkplaceDistance(
            locations, workplace, 0.0);
        
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }
    
    @Test
    void filterByWorkplaceDistance_大きな許容距離_全て含まれる() {
        WorkplaceLocation workplace = new WorkplaceLocation(35.6812, 139.7671, 100.0);
        
        List<Location> locations = Arrays.asList(
            new Location(1L, 35.6812, 139.7671, testDateTime, testUser),
            new Location(2L, 35.6896, 139.7006, testDateTime.plusMinutes(30), testUser), // 新宿駅（約7km）
            new Location(3L, 34.7024, 135.4959, testDateTime.plusMinutes(60), testUser)  // 大阪駅（約400km）
        );
        
        // 1000km の許容距離
        List<Location> result = locationFilteringService.filterByWorkplaceDistance(
            locations, workplace, 1000000.0);
        
        assertEquals(3, result.size());
    }
    
    @Test
    void filterByWorkplaceDistance_nullパラメータ_例外が発生() {
        WorkplaceLocation workplace = new WorkplaceLocation(35.6812, 139.7671, 100.0);
        List<Location> locations = Arrays.asList(
            new Location(1L, 35.6812, 139.7671, testDateTime, testUser)
        );
        
        assertThrows(NullPointerException.class, () -> {
            locationFilteringService.filterByWorkplaceDistance(null, workplace, 100.0);
        });
        
        assertThrows(NullPointerException.class, () -> {
            locationFilteringService.filterByWorkplaceDistance(locations, null, 100.0);
        });
    }
    
    @Test
    void filterByWorkplaceDistance_負の許容距離_例外が発生() {
        WorkplaceLocation workplace = new WorkplaceLocation(35.6812, 139.7671, 100.0);
        List<Location> locations = Arrays.asList(
            new Location(1L, 35.6812, 139.7671, testDateTime, testUser)
        );
        
        assertThrows(IllegalArgumentException.class, () -> {
            locationFilteringService.filterByWorkplaceDistance(locations, workplace, -100.0);
        });
    }
    
    @Test
    void WorkplaceLocation_無効な座標_例外が発生() {
        // 無効な緯度
        assertThrows(IllegalArgumentException.class, () -> {
            new WorkplaceLocation(-91.0, 139.7671, 100.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new WorkplaceLocation(91.0, 139.7671, 100.0);
        });
        
        // 無効な経度
        assertThrows(IllegalArgumentException.class, () -> {
            new WorkplaceLocation(35.6812, -181.0, 100.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new WorkplaceLocation(35.6812, 181.0, 100.0);
        });
        
        // 無効な半径
        assertThrows(IllegalArgumentException.class, () -> {
            new WorkplaceLocation(35.6812, 139.7671, -100.0);
        });
    }
    
    @Test
    void WorkplaceLocation_有効な境界値_正常に作成() {
        assertDoesNotThrow(() -> {
            new WorkplaceLocation(-90.0, -180.0, 0.0);
        });
        
        assertDoesNotThrow(() -> {
            new WorkplaceLocation(90.0, 180.0, 10000.0);
        });
        
        assertDoesNotThrow(() -> {
            new WorkplaceLocation(0.0, 0.0, 100.0);
        });
    }
    
    @Test
    void filterByWorkplaceDistance_実際の距離計算検証() {
        // 東京駅を作業場所とし、約50m離れた地点をテスト
        WorkplaceLocation workplace = new WorkplaceLocation(35.6812, 139.7671, 100.0);
        
        // 約50m北の地点（緯度約0.0005度差）
        Location nearLocation = new Location(1L, 35.6817, 139.7671, testDateTime, testUser);
        
        // 約500m北の地点（緯度約0.005度差）
        Location farLocation = new Location(2L, 35.6862, 139.7671, testDateTime.plusMinutes(30), testUser);
        
        List<Location> locations = Arrays.asList(nearLocation, farLocation);
        
        // 100m の許容距離でフィルタリング
        List<Location> result = locationFilteringService.filterByWorkplaceDistance(
            locations, workplace, 100.0);
        
        // 50m地点は含まれ、500m地点は除外される
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }
}