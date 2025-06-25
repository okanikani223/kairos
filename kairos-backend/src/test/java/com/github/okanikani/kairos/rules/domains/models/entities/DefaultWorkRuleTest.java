package com.github.okanikani.kairos.rules.domains.models.entities;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DefaultWorkRuleエンティティのテストクラス
 */
class DefaultWorkRuleTest {

    @Test
    void constructor_正常なパラメータ_デフォルト勤怠ルールが作成される() {
        // Arrange
        Long id = 1L;
        Long workPlaceId = 1001L;
        double latitude = 35.6762;
        double longitude = 139.6503;
        User user = new User("testuser");
        LocalTime standardStartTime = LocalTime.of(9, 0);
        LocalTime standardEndTime = LocalTime.of(17, 30);
        LocalTime breakStartTime = LocalTime.of(12, 0);
        LocalTime breakEndTime = LocalTime.of(13, 0);

        // Act
        DefaultWorkRule defaultWorkRule = new DefaultWorkRule(
            id, workPlaceId, latitude, longitude, user,
            standardStartTime, standardEndTime, breakStartTime, breakEndTime
        );

        // Assert
        assertEquals(id, defaultWorkRule.id());
        assertEquals(workPlaceId, defaultWorkRule.workPlaceId());
        assertEquals(latitude, defaultWorkRule.latitude());
        assertEquals(longitude, defaultWorkRule.longitude());
        assertEquals(user, defaultWorkRule.user());
        assertEquals(standardStartTime, defaultWorkRule.standardStartTime());
        assertEquals(standardEndTime, defaultWorkRule.standardEndTime());
        assertEquals(breakStartTime, defaultWorkRule.breakStartTime());
        assertEquals(breakEndTime, defaultWorkRule.breakEndTime());
    }

    @Test
    void constructor_nullId_デフォルト勤怠ルールが作成される() {
        // Arrange & Act
        DefaultWorkRule defaultWorkRule = new DefaultWorkRule(
            null, 1001L, 35.6762, 139.6503,
            new User("testuser"),
            LocalTime.of(9, 0), LocalTime.of(17, 30),
            LocalTime.of(12, 0), LocalTime.of(13, 0)
        );

        // Assert
        assertNull(defaultWorkRule.id());
        assertEquals(1001L, defaultWorkRule.workPlaceId());
    }

    @Test
    void constructor_nullWorkPlaceId_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new DefaultWorkRule(
                1L, null, 35.6762, 139.6503,
                new User("testuser"),
                LocalTime.of(9, 0), LocalTime.of(17, 30),
                LocalTime.of(12, 0), LocalTime.of(13, 0)
            )
        );
        assertEquals("勤怠先IDは必須です", exception.getMessage());
    }

    @Test
    void constructor_緯度範囲外_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new DefaultWorkRule(
                1L, 1001L, -91.0, 139.6503,
                new User("testuser"),
                LocalTime.of(9, 0), LocalTime.of(17, 30),
                LocalTime.of(12, 0), LocalTime.of(13, 0)
            )
        );
        assertEquals("緯度は-90.0から90.0の範囲で指定してください", exception.getMessage());
    }

    @Test
    void constructor_経度範囲外_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new DefaultWorkRule(
                1L, 1001L, 35.6762, 181.0,
                new User("testuser"),
                LocalTime.of(9, 0), LocalTime.of(17, 30),
                LocalTime.of(12, 0), LocalTime.of(13, 0)
            )
        );
        assertEquals("経度は-180.0から180.0の範囲で指定してください", exception.getMessage());
    }

    @Test
    void constructor_nullUser_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new DefaultWorkRule(
                1L, 1001L, 35.6762, 139.6503, null,
                LocalTime.of(9, 0), LocalTime.of(17, 30),
                LocalTime.of(12, 0), LocalTime.of(13, 0)
            )
        );
        assertEquals("ユーザーは必須です", exception.getMessage());
    }

    @Test
    void constructor_null規定勤怠開始時刻_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new DefaultWorkRule(
                1L, 1001L, 35.6762, 139.6503,
                new User("testuser"),
                null, LocalTime.of(17, 30),
                LocalTime.of(12, 0), LocalTime.of(13, 0)
            )
        );
        assertEquals("規定勤怠開始時刻は必須です", exception.getMessage());
    }

    @Test
    void constructor_null規定勤怠終了時刻_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new DefaultWorkRule(
                1L, 1001L, 35.6762, 139.6503,
                new User("testuser"),
                LocalTime.of(9, 0), null,
                LocalTime.of(12, 0), LocalTime.of(13, 0)
            )
        );
        assertEquals("規定勤怠終了時刻は必須です", exception.getMessage());
    }

    @Test
    void constructor_規定勤怠時刻の順序不正_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new DefaultWorkRule(
                1L, 1001L, 35.6762, 139.6503,
                new User("testuser"),
                LocalTime.of(17, 30), LocalTime.of(9, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0)
            )
        );
        assertEquals("規定勤怠開始時刻は規定勤怠終了時刻より前である必要があります", exception.getMessage());
    }

    @Test
    void constructor_休憩時刻の順序不正_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new DefaultWorkRule(
                1L, 1001L, 35.6762, 139.6503,
                new User("testuser"),
                LocalTime.of(9, 0), LocalTime.of(17, 30),
                LocalTime.of(13, 0), LocalTime.of(12, 0)
            )
        );
        assertEquals("規定休憩開始時刻は規定休憩終了時刻より前である必要があります", exception.getMessage());
    }

    @Test
    void constructor_休憩開始時刻のみnull_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new DefaultWorkRule(
                1L, 1001L, 35.6762, 139.6503,
                new User("testuser"),
                LocalTime.of(9, 0), LocalTime.of(17, 30),
                null, LocalTime.of(13, 0)
            )
        );
        assertEquals("休憩時刻は開始時刻と終了時刻の両方を設定するか、両方ともnullにしてください", exception.getMessage());
    }

    @Test
    void constructor_休憩終了時刻のみnull_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new DefaultWorkRule(
                1L, 1001L, 35.6762, 139.6503,
                new User("testuser"),
                LocalTime.of(9, 0), LocalTime.of(17, 30),
                LocalTime.of(12, 0), null
            )
        );
        assertEquals("休憩時刻は開始時刻と終了時刻の両方を設定するか、両方ともnullにしてください", exception.getMessage());
    }

    @Test
    void constructor_休憩時刻両方null_デフォルト勤怠ルールが作成される() {
        // Act
        DefaultWorkRule defaultWorkRule = new DefaultWorkRule(
            1L, 1001L, 35.6762, 139.6503,
            new User("testuser"),
            LocalTime.of(9, 0), LocalTime.of(17, 30),
            null, null
        );

        // Assert
        assertNull(defaultWorkRule.breakStartTime());
        assertNull(defaultWorkRule.breakEndTime());
    }
}