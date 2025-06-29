package com.github.okanikani.kairos.rules.domains.models.entities;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.rules.domains.models.vos.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkRuleTest {

    @Test
    void constructor_正常ケース_WorkRuleが作成される() {
        // Arrange
        Long workPlaceId = 1L;
        double latitude = 35.6762;
        double longitude = 139.6503;
        User user = new User("testuser");
        LocalTime standardStartTime = LocalTime.of(9, 0);
        LocalTime standardEndTime = LocalTime.of(17, 30);
        LocalTime breakStartTime = LocalTime.of(12, 0);
        LocalTime breakEndTime = LocalTime.of(13, 0);
        LocalDate membershipStartDate = LocalDate.of(2024, 1, 1);
        LocalDate membershipEndDate = LocalDate.of(2024, 12, 31);

        // Act
        WorkRule workRule = new WorkRule(
            null,
            workPlaceId,
            latitude,
            longitude,
            user,
            standardStartTime,
            standardEndTime,
            breakStartTime,
            breakEndTime,
            membershipStartDate,
            membershipEndDate
        );

        // Assert
        assertNotNull(workRule);
        assertEquals(workPlaceId, workRule.workPlaceId());
        assertEquals(latitude, workRule.latitude());
        assertEquals(longitude, workRule.longitude());
        assertEquals(user, workRule.user());
        assertEquals(standardStartTime, workRule.standardStartTime());
        assertEquals(standardEndTime, workRule.standardEndTime());
        assertEquals(breakStartTime, workRule.breakStartTime());
        assertEquals(breakEndTime, workRule.breakEndTime());
        assertEquals(membershipStartDate, workRule.membershipStartDate());
        assertEquals(membershipEndDate, workRule.membershipEndDate());
    }

    @Test
    void constructor_null勤怠先ID_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new WorkRule(
                null,
                null,
                35.6762,
                139.6503,
                new User("testuser"),
                LocalTime.of(9, 0),
                LocalTime.of(17, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            )
        );
        assertEquals("勤怠先IDは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullユーザー_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRule(
                null,
                1L,
                35.6762,
                139.6503,
                null,
                LocalTime.of(9, 0),
                LocalTime.of(17, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            )
        );
        assertEquals("ユーザーは必須です", exception.getMessage());
    }

    @Test
    void constructor_無効な緯度_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new WorkRule(
                null,
                1L,
                91.0, // 無効な緯度
                139.6503,
                new User("testuser"),
                LocalTime.of(9, 0),
                LocalTime.of(17, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            )
        );
        assertEquals("緯度は-90.0～90.0の範囲で指定してください: 91.000000", exception.getMessage());
    }

    @Test
    void constructor_無効な経度_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new WorkRule(
                null,
                1L,
                35.6762,
                181.0, // 無効な経度
                new User("testuser"),
                LocalTime.of(9, 0),
                LocalTime.of(17, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            )
        );
        assertEquals("経度は-180.0～180.0の範囲で指定してください: 181.000000", exception.getMessage());
    }

    @Test
    void constructor_規定開始時刻がnull_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRule(
                null,
                1L,
                35.6762,
                139.6503,
                new User("testuser"),
                null,
                LocalTime.of(17, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            )
        );
        assertEquals("規定勤怠開始時刻は必須です", exception.getMessage());
    }

    @Test
    void constructor_規定終了時刻がnull_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRule(
                null,
                1L,
                35.6762,
                139.6503,
                new User("testuser"),
                LocalTime.of(9, 0),
                null,
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            )
        );
        assertEquals("規定勤怠終了時刻は必須です", exception.getMessage());
    }

    @Test
    void constructor_所属開始日がnull_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRule(
                null,
                1L,
                35.6762,
                139.6503,
                new User("testuser"),
                LocalTime.of(9, 0),
                LocalTime.of(17, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                null,
                LocalDate.of(2024, 12, 31)
            )
        );
        assertEquals("所属開始日は必須です", exception.getMessage());
    }

    @Test
    void constructor_所属終了日がnull_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new WorkRule(
                null,
                1L,
                35.6762,
                139.6503,
                new User("testuser"),
                LocalTime.of(9, 0),
                LocalTime.of(17, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2024, 1, 1),
                null
            )
        );
        assertEquals("所属終了日は必須です", exception.getMessage());
    }

    @Test
    void constructor_所属期間が無効_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new WorkRule(
                null,
                1L,
                35.6762,
                139.6503,
                new User("testuser"),
                LocalTime.of(9, 0),
                LocalTime.of(17, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2024, 12, 31),
                LocalDate.of(2024, 1, 1) // 開始日 > 終了日
            )
        );
        assertEquals("所属開始日は所属終了日より前である必要があります", exception.getMessage());
    }

    @Test
    void constructor_勤務時間が同じ_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new WorkRule(
                null,
                1L,
                35.6762,
                139.6503,
                new User("testuser"),
                LocalTime.of(9, 0),
                LocalTime.of(9, 0), // 開始時刻 = 終了時刻
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            )
        );
        assertEquals("規定勤怠開始時刻と規定勤怠終了時刻は異なる時刻である必要があります", exception.getMessage());
    }

    @Test
    void constructor_休憩時間が同じ_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new WorkRule(
                null,
                1L,
                35.6762,
                139.6503,
                new User("testuser"),
                LocalTime.of(9, 0),
                LocalTime.of(17, 30),
                LocalTime.of(12, 0),
                LocalTime.of(12, 0), // 休憩開始時刻 = 休憩終了時刻
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            )
        );
        assertEquals("規定休憩開始時刻と規定休憩終了時刻は異なる時刻である必要があります", exception.getMessage());
    }
}