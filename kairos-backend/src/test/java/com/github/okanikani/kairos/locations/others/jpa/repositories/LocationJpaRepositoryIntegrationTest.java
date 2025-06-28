package com.github.okanikani.kairos.locations.others.jpa.repositories;

import com.github.okanikani.kairos.locations.others.jpa.entities.LocationJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LocationJpaRepositoryの統合テスト
 * GPS座標とタイムスタンプベースのクエリテストを含む
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("LocationJpaRepository統合テスト")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude="
})
class LocationJpaRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("kairos_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    @Autowired
    private LocationJpaRepository locationJpaRepository;

    private LocationJpaEntity location1;
    private LocationJpaEntity location2;
    private LocationJpaEntity location3;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        locationJpaRepository.deleteAll();
        
        baseTime = LocalDateTime.of(2024, 1, 15, 9, 0, 0);
        
        // 同一ユーザーの異なる時刻のロケーション
        location1 = new LocationJpaEntity(
                35.6762, // 東京駅付近
                139.7649,
                baseTime,
                "user1"
        );
        
        location2 = new LocationJpaEntity(
                35.6812, // 少し移動
                139.7671,
                baseTime.plusHours(8), // 8時間後
                "user1"
        );
        
        // 別ユーザーのロケーション
        location3 = new LocationJpaEntity(
                35.6895, // 新宿駅付近
                139.6917,
                baseTime.plusHours(1),
                "user2"
        );
    }

    @Test
    @DisplayName("基本CRUD操作_ロケーション作成")
    void save_正常ケース_ロケーションが作成される() {
        // When
        LocationJpaEntity savedLocation = locationJpaRepository.save(location1);

        // Then
        assertThat(savedLocation.getId()).isNotNull();
        assertThat(savedLocation.getUserId()).isEqualTo("user1");
        assertThat(savedLocation.getLatitude()).isEqualTo(35.6762);
        assertThat(savedLocation.getLongitude()).isEqualTo(139.7649);
        assertThat(savedLocation.getRecordedAt()).isEqualTo(baseTime);
    }

    @Test
    @DisplayName("基本CRUD操作_ID検索")
    void findById_正常ケース_ロケーションが取得される() {
        // Given
        LocationJpaEntity savedLocation = locationJpaRepository.save(location1);

        // When
        Optional<LocationJpaEntity> found = locationJpaRepository.findById(savedLocation.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("user1");
        assertThat(found.get().getLatitude()).isEqualTo(35.6762);
        assertThat(found.get().getLongitude()).isEqualTo(139.7649);
    }

    @Test
    @DisplayName("基本CRUD操作_全件取得")
    void findAll_正常ケース_全ロケーションが取得される() {
        // Given
        locationJpaRepository.save(location1);
        locationJpaRepository.save(location2);
        locationJpaRepository.save(location3);

        // When
        List<LocationJpaEntity> locations = locationJpaRepository.findAll();

        // Then
        assertThat(locations).hasSize(3);
        assertThat(locations)
                .extracting(LocationJpaEntity::getUserId)
                .containsExactlyInAnyOrder("user1", "user1", "user2");
    }

    @Test
    @DisplayName("基本CRUD操作_ロケーション更新はイミュータブル")
    void save_イミュータブル_新しいインスタンスを作成() {
        // Given
        LocationJpaEntity savedLocation = locationJpaRepository.save(location1);
        
        // When - 新しいロケーションを作成（既存のものは変更不可）
        LocationJpaEntity newLocation = new LocationJpaEntity(
                35.6800, // 緯度変更
                139.7700, // 経度変更
                baseTime.plusMinutes(30), // 時刻変更
                "user1"
        );
        LocationJpaEntity savedNewLocation = locationJpaRepository.save(newLocation);

        // Then
        assertThat(savedNewLocation.getId()).isNotEqualTo(savedLocation.getId());
        assertThat(savedNewLocation.getLatitude()).isEqualTo(35.6800);
        assertThat(savedNewLocation.getLongitude()).isEqualTo(139.7700);
        assertThat(savedNewLocation.getRecordedAt()).isEqualTo(baseTime.plusMinutes(30));
        
        // 元のロケーションは変更されていない
        LocationJpaEntity originalLocation = locationJpaRepository.findById(savedLocation.getId()).orElseThrow();
        assertThat(originalLocation.getLatitude()).isEqualTo(35.6762);
        assertThat(originalLocation.getLongitude()).isEqualTo(139.7649);
    }

    @Test
    @DisplayName("基本CRUD操作_ロケーション削除")
    void delete_正常ケース_ロケーションが削除される() {
        // Given
        LocationJpaEntity savedLocation = locationJpaRepository.save(location1);

        // When
        locationJpaRepository.delete(savedLocation);

        // Then
        Optional<LocationJpaEntity> found = locationJpaRepository.findById(savedLocation.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("カスタムクエリ_ユーザー別検索_記録日時降順")
    void findByUserIdOrderByRecordedAtDesc_正常ケース_ユーザーのロケーションが記録日時降順で取得される() {
        // Given
        locationJpaRepository.save(location1); // 9:00
        locationJpaRepository.save(location2); // 17:00 (8時間後)
        locationJpaRepository.save(location3); // 10:00 (user2)

        // When
        List<LocationJpaEntity> user1Locations = locationJpaRepository.findByUserIdOrderByRecordedAtDesc("user1");
        List<LocationJpaEntity> user2Locations = locationJpaRepository.findByUserIdOrderByRecordedAtDesc("user2");

        // Then
        assertThat(user1Locations).hasSize(2);
        assertThat(user1Locations)
                .extracting(LocationJpaEntity::getUserId)
                .containsOnly("user1");
        // 降順なので最新（17:00）が最初に来る
        assertThat(user1Locations.get(0).getRecordedAt()).isEqualTo(baseTime.plusHours(8));
        assertThat(user1Locations.get(1).getRecordedAt()).isEqualTo(baseTime);
        
        assertThat(user2Locations).hasSize(1);
        assertThat(user2Locations.get(0).getUserId()).isEqualTo("user2");
    }

    @Test
    @DisplayName("カスタムクエリ_時刻範囲検索")
    void findByUserIdAndRecordedAtBetween_正常ケース_時刻範囲でフィルタリングされる() {
        // Given
        locationJpaRepository.save(location1); // 9:00
        locationJpaRepository.save(location2); // 17:00 (9:00 + 8時間)
        locationJpaRepository.save(location3); // 10:00 (user2)

        LocalDateTime startTime = baseTime.minusMinutes(30); // 8:30
        LocalDateTime endTime = baseTime.plusHours(4); // 13:00

        // When
        List<LocationJpaEntity> locationsInRange = locationJpaRepository
                .findByUserIdAndRecordedAtBetween("user1", startTime, endTime);

        // Then
        assertThat(locationsInRange).hasSize(1);
        assertThat(locationsInRange.get(0).getRecordedAt()).isEqualTo(baseTime); // 9:00のみ
    }

    @Test
    @DisplayName("カスタムクエリ_最新ロケーション取得")
    void findLatestByUserId_正常ケース_最新ロケーションが取得される() {
        // Given
        locationJpaRepository.save(location1); // 9:00
        locationJpaRepository.save(location2); // 17:00 (最新)

        // When
        LocationJpaEntity latestLocation = locationJpaRepository.findLatestByUserId("user1");

        // Then
        assertThat(latestLocation).isNotNull();
        assertThat(latestLocation.getRecordedAt()).isEqualTo(baseTime.plusHours(8)); // 17:00
        assertThat(latestLocation.getLatitude()).isEqualTo(35.6812);
        assertThat(latestLocation.getLongitude()).isEqualTo(139.7671);
    }

    @Test
    @DisplayName("カスタムクエリ_存在しないユーザーの最新ロケーション")
    void findLatestByUserId_存在しないユーザー_nullが返される() {
        // Given
        locationJpaRepository.save(location1);

        // When
        LocationJpaEntity latestLocation = locationJpaRepository.findLatestByUserId("nonexistent");

        // Then
        assertThat(latestLocation).isNull();
    }

    @Test
    @DisplayName("カスタムクエリ_年月範囲検索")
    void findByUserIdAndRecordedAtBetween_年月範囲_正常に取得される() {
        // Given
        // 1月のロケーション
        LocationJpaEntity january1 = new LocationJpaEntity(
                35.6762,
                139.7649,
                LocalDateTime.of(2024, 1, 5, 9, 0),
                "user1"
        );
        
        LocationJpaEntity january2 = new LocationJpaEntity(
                35.6800,
                139.7700,
                LocalDateTime.of(2024, 1, 25, 17, 0),
                "user1"
        );
        
        // 2月のロケーション
        LocationJpaEntity february = new LocationJpaEntity(
                35.6850,
                139.7750,
                LocalDateTime.of(2024, 2, 10, 12, 0),
                "user1"
        );
        
        locationJpaRepository.save(january1);
        locationJpaRepository.save(january2);
        locationJpaRepository.save(february);

        YearMonth targetMonth = YearMonth.of(2024, 1);
        LocalDateTime startOfMonth = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = targetMonth.atEndOfMonth().atTime(23, 59, 59);

        // When
        List<LocationJpaEntity> januaryLocations = locationJpaRepository
                .findByUserIdAndRecordedAtBetween("user1", startOfMonth, endOfMonth);

        // Then
        assertThat(januaryLocations).hasSize(2);
        assertThat(januaryLocations)
                .extracting(location -> location.getRecordedAt().getMonth().getValue())
                .containsOnly(1); // 1月のみ
    }

    @Test
    @DisplayName("カスタムクエリ_記録時刻範囲での詳細検索")
    void findRecordedAtByUserIdAndPeriod_正常ケース_記録時刻のみが取得される() {
        // Given
        locationJpaRepository.save(location1); // 9:00
        locationJpaRepository.save(location2); // 17:00
        locationJpaRepository.save(location3); // 10:00 (user2)

        LocalDateTime startTime = baseTime.minusMinutes(30); // 8:30
        LocalDateTime endTime = baseTime.plusHours(4); // 13:00

        // When
        List<LocalDateTime> recordedTimes = locationJpaRepository
                .findRecordedAtByUserIdAndPeriod("user1", startTime, endTime);

        // Then
        assertThat(recordedTimes).hasSize(1);
        assertThat(recordedTimes.get(0)).isEqualTo(baseTime); // 9:00のみ
    }

    @Test
    @DisplayName("GPS座標値検証_有効な範囲")
    void save_有効なGPS座標_正常に保存される() {
        // Given
        LocationJpaEntity validLocation = new LocationJpaEntity(
                -90.0, // 最小値
                -180.0, // 最小値
                baseTime,
                "user1"
        );

        // When
        LocationJpaEntity savedLocation = locationJpaRepository.save(validLocation);

        // Then
        assertThat(savedLocation.getLatitude()).isEqualTo(-90.0);
        assertThat(savedLocation.getLongitude()).isEqualTo(-180.0);
    }

    @Test
    @DisplayName("GPS座標値検証_境界値")
    void save_境界値GPS座標_正常に保存される() {
        // Given
        LocationJpaEntity boundaryLocation1 = new LocationJpaEntity(
                90.0, // 最大値
                180.0, // 最大値
                baseTime,
                "user1"
        );
        
        LocationJpaEntity boundaryLocation2 = new LocationJpaEntity(
                0.0, // 中央値
                0.0, // 中央値
                baseTime,
                "user2"
        );

        // When
        LocationJpaEntity saved1 = locationJpaRepository.save(boundaryLocation1);
        LocationJpaEntity saved2 = locationJpaRepository.save(boundaryLocation2);

        // Then
        assertThat(saved1.getLatitude()).isEqualTo(90.0);
        assertThat(saved1.getLongitude()).isEqualTo(180.0);
        assertThat(saved2.getLatitude()).isEqualTo(0.0);
        assertThat(saved2.getLongitude()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("パフォーマンス_大量データでの検索")
    void findByUserIdOrderByRecordedAtDesc_大量データ_パフォーマンスが適切() {
        // Given
        String userId = "performanceUser";
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        
        // 50件のロケーションデータを作成（テスト実行時間短縮のため）
        for (int i = 0; i < 50; i++) {
            LocationJpaEntity location = new LocationJpaEntity(
                    35.6762 + (i * 0.001), // 少しずつ移動
                    139.7649 + (i * 0.001),
                    startTime.plusMinutes(i * 10), // 10分間隔
                    userId
            );
            locationJpaRepository.save(location);
        }

        // When
        long startMillis = System.currentTimeMillis();
        List<LocationJpaEntity> locations = locationJpaRepository.findByUserIdOrderByRecordedAtDesc(userId);
        long endMillis = System.currentTimeMillis();

        // Then
        assertThat(locations).hasSize(50);
        
        // パフォーマンス確認（2秒以内で完了することを期待）
        long executionTime = endMillis - startMillis;
        assertThat(executionTime).isLessThan(2000);
        
        // 降順でソートされていることを確認
        assertThat(locations.get(0).getRecordedAt()).isAfter(locations.get(1).getRecordedAt());
    }
}