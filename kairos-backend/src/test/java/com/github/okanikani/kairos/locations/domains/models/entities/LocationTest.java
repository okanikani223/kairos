package com.github.okanikani.kairos.locations.domains.models.entities;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.locations.domains.models.vos.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Locationエンティティのテスト
 */
@DisplayName("Locationエンティティのテスト")
class LocationTest {

    @Nested
    @DisplayName("コンストラクタのテスト")
    class ConstructorTest {

        @Test
        @DisplayName("正常系_全ての必須パラメータが設定されている場合_正常にインスタンスが生成される")
        void 正常系_全ての必須パラメータが設定されている場合_正常にインスタンスが生成される() {
            // Arrange
            Long id = 1L;
            double latitude = 35.6895;
            double longitude = 139.6917;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                    new Location(id, latitude, longitude, recordedAt, user));
        }

        @Test
        @DisplayName("正常系_IDがnullの場合_正常にインスタンスが生成される")
        void 正常系_IDがnullの場合_正常にインスタンスが生成される() {
            // Arrange
            double latitude = 35.6895;
            double longitude = 139.6917;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            // Act
            Location location = new Location(null, latitude, longitude, recordedAt, user);

            // Assert
            assertThat(location.id()).isNull();
            assertThat(location.latitude()).isEqualTo(latitude);
            assertThat(location.longitude()).isEqualTo(longitude);
            assertThat(location.recordedAt()).isEqualTo(recordedAt);
            assertThat(location.user()).isEqualTo(user);
        }

        @Test
        @DisplayName("異常系_recordedAtがnullの場合_NullPointerExceptionが発生する")
        void 異常系_recordedAtがnullの場合_NullPointerExceptionが発生する() {
            // Arrange
            Long id = 1L;
            double latitude = 35.6895;
            double longitude = 139.6917;
            User user = new User("testuser");

            // Act & Assert
            assertThatThrownBy(() ->
                    new Location(id, latitude, longitude, null, user))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("記録日時は必須です");
        }

        @Test
        @DisplayName("異常系_userがnullの場合_NullPointerExceptionが発生する")
        void 異常系_userがnullの場合_NullPointerExceptionが発生する() {
            // Arrange
            Long id = 1L;
            double latitude = 35.6895;
            double longitude = 139.6917;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);

            // Act & Assert
            assertThatThrownBy(() ->
                    new Location(id, latitude, longitude, recordedAt, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("ユーザーは必須です");
        }
    }

    @Nested
    @DisplayName("緯度のバリデーションテスト")
    class LatitudeValidationTest {

        @ParameterizedTest
        @ValueSource(doubles = {-90.0, -45.0, 0.0, 45.0, 90.0})
        @DisplayName("正常系_有効な緯度の場合_正常にインスタンスが生成される")
        void 正常系_有効な緯度の場合_正常にインスタンスが生成される(double validLatitude) {
            // Arrange
            double longitude = 139.6917;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                    new Location(1L, validLatitude, longitude, recordedAt, user));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-90.1, -91.0, 90.1, 91.0, -180.0, 180.0})
        @DisplayName("異常系_無効な緯度の場合_ValidationExceptionが発生する")
        void 異常系_無効な緯度の場合_ValidationExceptionが発生する(double invalidLatitude) {
            // Arrange
            double longitude = 139.6917;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            // Act & Assert
            assertThatThrownBy(() ->
                    new Location(1L, invalidLatitude, longitude, recordedAt, user))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage(String.format("緯度は-90.0～90.0の範囲で指定してください: %f", invalidLatitude));
        }

        @Test
        @DisplayName("境界値テスト_緯度の最小値マイナス90度_正常にインスタンスが生成される")
        void 境界値テスト_緯度の最小値マイナス90度_正常にインスタンスが生成される() {
            // Arrange
            double minLatitude = -90.0;
            double longitude = 139.6917;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            // Act
            Location location = new Location(1L, minLatitude, longitude, recordedAt, user);

            // Assert
            assertThat(location.latitude()).isEqualTo(minLatitude);
        }

        @Test
        @DisplayName("境界値テスト_緯度の最大値プラス90度_正常にインスタンスが生成される")
        void 境界値テスト_緯度の最大値プラス90度_正常にインスタンスが生成される() {
            // Arrange
            double maxLatitude = 90.0;
            double longitude = 139.6917;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            // Act
            Location location = new Location(1L, maxLatitude, longitude, recordedAt, user);

            // Assert
            assertThat(location.latitude()).isEqualTo(maxLatitude);
        }
    }

    @Nested
    @DisplayName("経度のバリデーションテスト")
    class LongitudeValidationTest {

        @ParameterizedTest
        @ValueSource(doubles = {-180.0, -90.0, 0.0, 90.0, 180.0})
        @DisplayName("正常系_有効な経度の場合_正常にインスタンスが生成される")
        void 正常系_有効な経度の場合_正常にインスタンスが生成される(double validLongitude) {
            // Arrange
            double latitude = 35.6895;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                    new Location(1L, latitude, validLongitude, recordedAt, user));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-180.1, -181.0, 180.1, 181.0, -200.0, 200.0})
        @DisplayName("異常系_無効な経度の場合_ValidationExceptionが発生する")
        void 異常系_無効な経度の場合_ValidationExceptionが発生する(double invalidLongitude) {
            // Arrange
            double latitude = 35.6895;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            // Act & Assert
            assertThatThrownBy(() ->
                    new Location(1L, latitude, invalidLongitude, recordedAt, user))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage(String.format("経度は-180.0～180.0の範囲で指定してください: %f", invalidLongitude));
        }

        @Test
        @DisplayName("境界値テスト_経度の最小値マイナス180度_正常にインスタンスが生成される")
        void 境界値テスト_経度の最小値マイナス180度_正常にインスタンスが生成される() {
            // Arrange
            double latitude = 35.6895;
            double minLongitude = -180.0;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            // Act
            Location location = new Location(1L, latitude, minLongitude, recordedAt, user);

            // Assert
            assertThat(location.longitude()).isEqualTo(minLongitude);
        }

        @Test
        @DisplayName("境界値テスト_経度の最大値プラス180度_正常にインスタンスが生成される")
        void 境界値テスト_経度の最大値プラス180度_正常にインスタンスが生成される() {
            // Arrange
            double latitude = 35.6895;
            double maxLongitude = 180.0;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            // Act
            Location location = new Location(1L, latitude, maxLongitude, recordedAt, user);

            // Assert
            assertThat(location.longitude()).isEqualTo(maxLongitude);
        }
    }

    @Nested
    @DisplayName("フィールドアクセスのテスト")
    class FieldAccessTest {

        @Test
        @DisplayName("id取得_正常に取得できる")
        void id取得_正常に取得できる() {
            // Arrange
            Long expectedId = 1L;
            Location location = createDefaultLocation(expectedId);

            // Act
            Long actualId = location.id();

            // Assert
            assertThat(actualId).isEqualTo(expectedId);
        }

        @Test
        @DisplayName("latitude取得_正常に取得できる")
        void latitude取得_正常に取得できる() {
            // Arrange
            double expectedLatitude = 35.6895;
            Location location = new Location(
                    1L, expectedLatitude, 139.6917,
                    LocalDateTime.of(2025, 1, 15, 9, 0),
                    new User("testuser")
            );

            // Act
            double actualLatitude = location.latitude();

            // Assert
            assertThat(actualLatitude).isEqualTo(expectedLatitude);
        }

        @Test
        @DisplayName("longitude取得_正常に取得できる")
        void longitude取得_正常に取得できる() {
            // Arrange
            double expectedLongitude = 139.6917;
            Location location = new Location(
                    1L, 35.6895, expectedLongitude,
                    LocalDateTime.of(2025, 1, 15, 9, 0),
                    new User("testuser")
            );

            // Act
            double actualLongitude = location.longitude();

            // Assert
            assertThat(actualLongitude).isEqualTo(expectedLongitude);
        }

        @Test
        @DisplayName("recordedAt取得_正常に取得できる")
        void recordedAt取得_正常に取得できる() {
            // Arrange
            LocalDateTime expectedRecordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            Location location = new Location(
                    1L, 35.6895, 139.6917,
                    expectedRecordedAt,
                    new User("testuser")
            );

            // Act
            LocalDateTime actualRecordedAt = location.recordedAt();

            // Assert
            assertThat(actualRecordedAt).isEqualTo(expectedRecordedAt);
        }

        @Test
        @DisplayName("user取得_正常に取得できる")
        void user取得_正常に取得できる() {
            // Arrange
            User expectedUser = new User("testuser");
            Location location = new Location(
                    1L, 35.6895, 139.6917,
                    LocalDateTime.of(2025, 1, 15, 9, 0),
                    expectedUser
            );

            // Act
            User actualUser = location.user();

            // Assert
            assertThat(actualUser).isEqualTo(expectedUser);
        }
    }

    @Nested
    @DisplayName("equalsとhashCodeのテスト")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("equals_同じ値を持つ2つのインスタンス_trueを返す")
        void equals_同じ値を持つ2つのインスタンス_trueを返す() {
            // Arrange
            Long id = 1L;
            double latitude = 35.6895;
            double longitude = 139.6917;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            Location location1 = new Location(id, latitude, longitude, recordedAt, user);
            Location location2 = new Location(id, latitude, longitude, recordedAt, user);

            // Act & Assert
            assertThat(location1).isEqualTo(location2);
            assertThat(location1.hashCode()).isEqualTo(location2.hashCode());
        }

        @Test
        @DisplayName("equals_異なるIDを持つ2つのインスタンス_falseを返す")
        void equals_異なるIDを持つ2つのインスタンス_falseを返す() {
            // Arrange
            double latitude = 35.6895;
            double longitude = 139.6917;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            Location location1 = new Location(1L, latitude, longitude, recordedAt, user);
            Location location2 = new Location(2L, latitude, longitude, recordedAt, user);

            // Act & Assert
            assertThat(location1).isNotEqualTo(location2);
        }

        @Test
        @DisplayName("equals_異なる座標を持つ2つのインスタンス_falseを返す")
        void equals_異なる座標を持つ2つのインスタンス_falseを返す() {
            // Arrange
            Long id = 1L;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            Location location1 = new Location(id, 35.6895, 139.6917, recordedAt, user);
            Location location2 = new Location(id, 35.6896, 139.6918, recordedAt, user);

            // Act & Assert
            assertThat(location1).isNotEqualTo(location2);
        }
    }

    @Nested
    @DisplayName("toStringのテスト")
    class ToStringTest {

        @Test
        @DisplayName("toString_全てのフィールドが含まれる")
        void toString_全てのフィールドが含まれる() {
            // Arrange
            Long id = 1L;
            double latitude = 35.6895;
            double longitude = 139.6917;
            LocalDateTime recordedAt = LocalDateTime.of(2025, 1, 15, 9, 0);
            User user = new User("testuser");

            Location location = new Location(id, latitude, longitude, recordedAt, user);

            // Act
            String result = location.toString();

            // Assert
            assertThat(result)
                    .contains("id=1")
                    .contains("latitude=35.6895")
                    .contains("longitude=139.6917")
                    .contains("recordedAt=2025-01-15T09:00")
                    .contains("user=User[userId=testuser]");
        }
    }

    // ヘルパーメソッド
    private Location createDefaultLocation(Long id) {
        return new Location(
                id,
                35.6895,
                139.6917,
                LocalDateTime.of(2025, 1, 15, 9, 0),
                new User("testuser")
        );
    }
}