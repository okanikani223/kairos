package com.github.okanikani.kairos.reportcreationrules.domains.models.entities;

import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.reportcreationrules.domains.models.vos.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReportCreationRuleエンティティのテストクラス
 */
class ReportCreationRuleTest {

    @Test
    void constructor_正常なパラメータ_勤怠作成ルールが作成される() {
        // Arrange
        Long id = 1L;
        User user = new User("testuser");
        int closingDay = 15;
        int timeCalculationUnitMinutes = 15;

        // Act
        ReportCreationRule reportCreationRule = new ReportCreationRule(
            id, user, closingDay, timeCalculationUnitMinutes
        );

        // Assert
        assertEquals(id, reportCreationRule.id());
        assertEquals(user, reportCreationRule.user());
        assertEquals(closingDay, reportCreationRule.closingDay());
        assertEquals(timeCalculationUnitMinutes, reportCreationRule.timeCalculationUnitMinutes());
    }

    @Test
    void constructor_nullId_勤怠作成ルールが作成される() {
        // Arrange & Act
        ReportCreationRule reportCreationRule = new ReportCreationRule(
            null, new User("testuser"), 1, 15
        );

        // Assert
        assertNull(reportCreationRule.id());
        assertEquals("testuser", reportCreationRule.user().userId());
    }

    @Test
    void constructor_nullUser_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new ReportCreationRule(1L, null, 15, 15)
        );
        assertEquals("ユーザーは必須です", exception.getMessage());
    }

    @Test
    void constructor_勤怠締め日が1未満_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new ReportCreationRule(1L, new User("testuser"), 0, 15)
        );
        assertEquals("勤怠締め日は1日から31日までの範囲で指定してください", exception.getMessage());
    }

    @Test
    void constructor_勤怠締め日が31超過_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new ReportCreationRule(1L, new User("testuser"), 32, 15)
        );
        assertEquals("勤怠締め日は1日から31日までの範囲で指定してください", exception.getMessage());
    }

    @Test
    void constructor_勤怠時間計算単位が1未満_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new ReportCreationRule(1L, new User("testuser"), 15, 0)
        );
        assertEquals("勤怠時間計算単位は1分以上である必要があります", exception.getMessage());
    }

    @Test
    void constructor_勤怠時間計算単位が60を超過_例外が発生する() {
        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new ReportCreationRule(1L, new User("testuser"), 15, 61)
        );
        assertEquals("勤怠時間計算単位は60分以下である必要があります", exception.getMessage());
    }

    @Test
    void constructor_境界値1日_勤怠作成ルールが作成される() {
        // Act
        ReportCreationRule reportCreationRule = new ReportCreationRule(
            1L, new User("testuser"), 1, 15
        );

        // Assert
        assertEquals(1, reportCreationRule.closingDay());
    }

    @Test
    void constructor_境界値31日_勤怠作成ルールが作成される() {
        // Act
        ReportCreationRule reportCreationRule = new ReportCreationRule(
            1L, new User("testuser"), 31, 15
        );

        // Assert
        assertEquals(31, reportCreationRule.closingDay());
    }

    @Test
    void constructor_境界値1分_勤怠作成ルールが作成される() {
        // Act
        ReportCreationRule reportCreationRule = new ReportCreationRule(
            1L, new User("testuser"), 15, 1
        );

        // Assert
        assertEquals(1, reportCreationRule.timeCalculationUnitMinutes());
    }

    @Test
    void constructor_境界値60分_勤怠作成ルールが作成される() {
        // Act
        ReportCreationRule reportCreationRule = new ReportCreationRule(
            1L, new User("testuser"), 15, 60
        );

        // Assert
        assertEquals(60, reportCreationRule.timeCalculationUnitMinutes());
    }

    @Test
    void constructor_一般的な値_勤怠作成ルールが作成される() {
        // Arrange
        User user = new User("businessuser");
        int closingDay = 21;  // 21日締め
        int timeCalculationUnitMinutes = 30;  // 30分単位

        // Act
        ReportCreationRule reportCreationRule = new ReportCreationRule(
            2L, user, closingDay, timeCalculationUnitMinutes
        );

        // Assert
        assertEquals(2L, reportCreationRule.id());
        assertEquals("businessuser", reportCreationRule.user().userId());
        assertEquals(21, reportCreationRule.closingDay());
        assertEquals(30, reportCreationRule.timeCalculationUnitMinutes());
    }
}